#!/usr/bin/env python3
"""Set the sole Notes owner's password interactively on the application server."""
import getpass
import os
from pathlib import Path
import re
import subprocess
import tempfile
import time
import urllib.error
import urllib.request
import json
import zipfile

OWNER = "f1ce8d47-f208-4ab9-932b-8979840887a2"
USERNAME = "tyrak"
SERVICE = "gestor-notas-backend.service"
JAR = Path("/opt/gestor-notas-backend/gestor-notas.jar")
ENV_FILE = Path("/etc/gestor-notas-local-login.env")
DROPIN = Path("/etc/systemd/system/gestor-notas-backend.service.d/99-local-login.conf")
JAVA_SOURCE = """import java.io.*;
import java.nio.charset.StandardCharsets;
import org.springframework.security.crypto.bcrypt.BCrypt;
class NotesPasswordHash {
    public static void main(String[] args) throws Exception {
        var reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        String password = reader.readLine();
        System.out.print(BCrypt.hashpw(password, BCrypt.gensalt(12)));
    }
}
"""

def run(command, **kwargs):
    result = subprocess.run(command, capture_output=True, **kwargs)
    if result.returncode:
        raise RuntimeError("Falló una herramienta del servidor. No se muestran datos privados.")
    return result.stdout

def password_hash(jar_path, password, java="java"):
    if len(password) < 8 or len(password.encode("utf-8")) > 72 or "\n" in password or "\r" in password:
        raise ValueError("Usa al menos 8 caracteres y como máximo 72 bytes.")
    with tempfile.TemporaryDirectory(prefix="notes-password-") as directory:
        directory = Path(directory)
        with zipfile.ZipFile(jar_path) as jar:
            dependencies = []
            for prefix in ("spring-security-crypto-", "spring-jcl-"):
                names = [name for name in jar.namelist()
                         if name.startswith("BOOT-INF/lib/" + prefix) and name.endswith(".jar")]
                if len(names) != 1:
                    raise RuntimeError("El JAR no contiene las librerías necesarias para la contraseña.")
                destination = directory / Path(names[0]).name
                destination.write_bytes(jar.read(names[0]))
                dependencies.append(str(destination))
        source = directory / "NotesPasswordHash.java"
        source.write_text(JAVA_SOURCE, encoding="utf-8")
        result = run([java, "-cp", os.pathsep.join(dependencies), str(source)],
                     input=password.encode("utf-8") + b"\n").decode("ascii").strip()
        if not re.fullmatch(r"\$2[aby]\$12\$[./A-Za-z0-9]{53}", result):
            raise RuntimeError("No se pudo generar una contraseña BCrypt válida.")
        return result

def write_private(path, content, mode):
    if path.is_symlink():
        raise RuntimeError("Ruta de configuración inesperada. No se reemplazó.")
    path.parent.mkdir(parents=True, exist_ok=True)
    temporary = None
    try:
        with tempfile.NamedTemporaryFile(dir=path.parent, prefix=".notes-password-", delete=False) as stream:
            temporary = Path(stream.name)
            stream.write(content.encode("utf-8"))
            stream.flush()
            os.fsync(stream.fileno())
        os.chmod(temporary, mode)
        os.replace(temporary, path)
        temporary = None
    finally:
        if temporary is not None:
            temporary.unlink(missing_ok=True)

def main():
    if os.geteuid() != 0:
        raise RuntimeError("Ejecuta este comando con sudo en el servidor.")
    os.umask(0o077)
    with zipfile.ZipFile(JAR) as jar:
        if "BOOT-INF/classes/com/gestornotas/security/LocalAccount.class" not in jar.namelist():
            raise RuntimeError("Primero despliega el login local con gestor-notas-build en Jenkins.")
    query = "SELECT count(*) FROM users WHERE user_id='" + OWNER + "' AND username='tyrak';"
    owner = run(["runuser", "-u", "postgres", "--", "psql", "-X", "-qAt", "-v",
                 "ON_ERROR_STOP=1", "-d", "gestor_notas", "-c", query]).strip()
    if owner != b"1":
        raise RuntimeError("No coincide la cuenta propietaria tyrak. No se hicieron cambios.")
    print("Nueva contraseña local para tyrak. Las notas se conservarán.")
    password = getpass.getpass("Nueva contraseña (mínimo 8 caracteres): ")
    if password != getpass.getpass("Repite la contraseña: "):
        raise ValueError("Las contraseñas no coinciden. No se hicieron cambios.")
    hashed = password_hash(JAR, password)
    del password
    write_private(ENV_FILE, "NOTES_LOCAL_USER_ID=" + OWNER +
                  "\nNOTES_LOCAL_PASSWORD_HASH=" + hashed + "\n", 0o600)
    write_private(DROPIN, "[Service]\nEnvironmentFile=" + str(ENV_FILE) + "\n", 0o644)
    run(["systemctl", "daemon-reload"])
    run(["systemctl", "restart", SERVICE])
    for _ in range(45):
        try:
            with urllib.request.urlopen("http://127.0.0.1:8082/auth/info", timeout=2) as response:
                if json.loads(response.read()).get("provider") != "LOCAL":
                    raise RuntimeError("El servicio no está usando el login local.")
            try:
                urllib.request.urlopen("http://127.0.0.1:8082/auth/session", timeout=2).close()
            except urllib.error.HTTPError as error:
                if error.code == 401:
                    print("Contraseña configurada. Ingresa con el usuario tyrak y tu nueva clave.")
                    print("Sesiones anteriores cerradas. No se modificaron las notas ni la base de datos.")
                    return
        except (OSError, urllib.error.URLError):
            pass
        time.sleep(1)
    raise RuntimeError("La clave quedó configurada, pero el servicio no confirmó su arranque. Revisar systemctl status.")

if __name__ == "__main__":
    try:
        main()
    except (OSError, ValueError, RuntimeError, zipfile.BadZipFile, KeyboardInterrupt) as error:
        raise SystemExit("No se completó el cambio: " + str(error))
