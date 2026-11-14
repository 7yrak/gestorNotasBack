#!/usr/bin/env python3
"""Update only the installed Notes artifact guard from KOVA to local authentication."""
import argparse
import ast
from datetime import datetime, timezone
import os
from pathlib import Path
import stat
import subprocess
import tempfile

TARGET = Path("/usr/local/sbin/deploy-gestor-notas-from-jenkins.sh")
BACKUPS = Path("/var/backups/gestor-notas-deployer")
OLD_MARKER = "# KOVA-only artifact and private-health guard"
NEW_MARKER = "# Local-only artifact and private-health guard"

OLD_CODE = """import sys,zipfile
with zipfile.ZipFile(sys.argv[1]) as jar:
    required=['BOOT-INF/classes/com/gestornotas/security/KovaSecurity.class', 'BOOT-INF/classes/db/migration/V2__kova_identity_binding.sql']
    if not set(required).issubset(jar.namelist()):
        raise SystemExit('KOVA-only artifact required; existing service was not stopped')
"""
NEW_CODE = """import sys,zipfile
with zipfile.ZipFile(sys.argv[1]) as jar:
    names = set(jar.namelist())
    required = {
        'BOOT-INF/classes/com/gestornotas/security/LocalSecurity.class',
        'BOOT-INF/classes/com/gestornotas/security/LocalAccount.class',
        'BOOT-INF/classes/com/gestornotas/security/LocalSessionController.class',
        'BOOT-INF/classes/com/gestornotas/security/NotesIdentity.class',
    }
    if not required.issubset(names):
        raise SystemExit('Local-login artifact required; existing service was not stopped')
    if 'BOOT-INF/classes/com/gestornotas/security/KovaSecurity.class' in names:
        raise SystemExit('Mixed authentication artifact rejected; existing service was not stopped')
    properties = jar.read('BOOT-INF/classes/application.properties').decode('utf-8')
    if 'notes.local.user-id=' not in properties or 'NOTES_LOCAL_SESSION' not in properties:
        raise SystemExit('Local account configuration required; existing service was not stopped')
"""

def block(marker, delimiter, code):
    return marker + '\npython3 - "$JAR_FILE" <<\'' + delimiter + "'\n" + code + delimiter

OLD = block(OLD_MARKER, "KOVA_ARTIFACT", OLD_CODE)
NEW = block(NEW_MARKER, "LOCAL_ARTIFACT", NEW_CODE)

def update(text):
    """Recognize the exact known guard and preserve every other byte of shell logic."""
    ast.parse(NEW_CODE)
    if OLD_MARKER in text:
        if text.count(OLD) != 1 or text.count(OLD_MARKER) != 1 or NEW_MARKER in text:
            raise ValueError("Unexpected installed guard. No changes were made.")
        result = text.replace(OLD, NEW, 1)
        # The guard must remain immediately after JAR validation, before any stop.
        if 'test -f "$JAR_FILE"\n' + NEW not in result:
            raise ValueError("Unexpected guard location. No changes were made.")
        return result
    if text.count(NEW) == 1 and text.count(NEW_MARKER) == 1:
        return text
    raise ValueError("Unknown deployer version. No changes were made.")

def install(check_only=False):
    import fcntl
    if os.geteuid() != 0:
        raise RuntimeError("Run with sudo on the application server.")
    with Path("/run/lock/gestor-notas-local-deployer.lock").open("a") as lock:
        fcntl.flock(lock, fcntl.LOCK_EX)
        info = TARGET.lstat()
        if not stat.S_ISREG(info.st_mode) or info.st_uid != 0 or info.st_mode & 0o022:
            raise RuntimeError("Unexpected deployer ownership or permissions. No changes were made.")
        before = TARGET.read_bytes()
        after = update(before.decode("utf-8")).encode("utf-8")
        if before == after:
            print("Local-login deployer already configured. No service was restarted.")
            return
        if check_only:
            print("Local-login guard update verified. No files or services changed.")
            return
        if BACKUPS.is_symlink():
            raise RuntimeError("Unexpected backup symlink. No changes were made.")
        BACKUPS.mkdir(mode=0o700, parents=True, exist_ok=True)
        backup_info = BACKUPS.stat()
        if backup_info.st_uid != 0 or backup_info.st_mode & 0o077:
            raise RuntimeError("Unexpected backup permissions. No changes were made.")
        backup = BACKUPS / ("deploy-" + datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%S%fZ") + ".sh")
        descriptor = os.open(backup, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
        with os.fdopen(descriptor, "wb") as stream:
            stream.write(before)
        temporary = None
        try:
            with tempfile.NamedTemporaryFile(dir=TARGET.parent, prefix=".notes-local-", delete=False) as stream:
                temporary = Path(stream.name)
                stream.write(after)
                stream.flush()
                os.fsync(stream.fileno())
            os.chmod(temporary, stat.S_IMODE(info.st_mode))
            os.chown(temporary, info.st_uid, info.st_gid)
            subprocess.run(["bash", "-n", str(temporary)], check=True, capture_output=True)
            if TARGET.is_symlink() or TARGET.read_bytes() != before:
                raise RuntimeError("Deployer changed during update. No replacement was made.")
            os.replace(temporary, TARGET)
            temporary = None
        finally:
            if temporary is not None:
                temporary.unlink(missing_ok=True)
        print("Local-login artifact guard installed. Backup: " + str(backup))
        print("Services and databases were not changed. Run gestor-notas-build in Jenkins again.")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check-only", action="store_true")
    args = parser.parse_args()
    try:
        install(args.check_only)
    except (OSError, ValueError, RuntimeError, subprocess.CalledProcessError) as error:
        parser.exit(1, "Update failed: " + str(error) + "\n")
