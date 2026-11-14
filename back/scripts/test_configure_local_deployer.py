"""Regression tests for the installed Notes deployment guard."""
import ast
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
import zipfile
import configure_local_deployer as deployer

class LocalDeployerTests(unittest.TestCase):
    def fixture(self):
        return '#!/usr/bin/env bash\nset -euo pipefail\nJAR_FILE="${1:?}"\ntest -f "$JAR_FILE"\n' + deployer.OLD + '\necho stop-service\n'

    def test_only_artifact_guard_is_replaced_and_update_is_idempotent(self):
        before = self.fixture()
        after = deployer.update(before)
        self.assertEqual(before.replace(deployer.OLD, deployer.NEW), after)
        self.assertEqual(after, deployer.update(after))
        self.assertLess(after.index(deployer.NEW_MARKER), after.index("echo stop-service"))
        ast.parse(deployer.NEW_CODE)

    def test_unknown_duplicated_and_modified_guards_are_rejected(self):
        for text in ("unrelated script", self.fixture() * 2,
                     self.fixture().replace("KovaSecurity.class", "SomethingElse.class"),
                     self.fixture().replace('test -f "$JAR_FILE"\n', 'echo unexpected\n')):
            with self.subTest(text=text[:40]), self.assertRaises(ValueError):
                deployer.update(text)

    def test_real_jar_validation_accepts_only_complete_local_auth_artifacts(self):
        with tempfile.TemporaryDirectory() as directory:
            jar = Path(directory) / "app.jar"
            required = [
                "LocalSecurity", "LocalAccount", "LocalSessionController", "NotesIdentity"
            ]
            def check(classes, properties):
                with zipfile.ZipFile(jar, "w") as archive:
                    for name in classes:
                        archive.writestr("BOOT-INF/classes/com/gestornotas/security/" + name + ".class", b"test")
                    archive.writestr("BOOT-INF/classes/application.properties", properties)
                return subprocess.run([sys.executable, "-c", deployer.NEW_CODE, str(jar)], capture_output=True)
            properties = "notes.local.user-id=configured-owner\nserver.servlet.session.cookie.name=NOTES_LOCAL_SESSION\n"
            self.assertEqual(0, check(required, properties).returncode)
            for classes, config in [
                    (["KovaSecurity"], properties), (required[:-1], properties),
                    (required + ["KovaSecurity"], properties), (required, "no-local-config")]:
                with self.subTest(classes=classes):
                    self.assertNotEqual(0, check(classes, config).returncode)

    @unittest.skipUnless(os.name == "posix", "Shell execution runs in Linux CI")
    def test_invalid_artifact_never_reaches_service_stop(self):
        with tempfile.TemporaryDirectory() as directory:
            script = Path(directory) / "deploy.sh"
            script.write_text(deployer.update(self.fixture()))
            jar = Path(directory) / "legacy.jar"
            with zipfile.ZipFile(jar, "w") as archive:
                archive.writestr("legacy.txt", "no authentication")
            result = subprocess.run(["bash", str(script), str(jar)], capture_output=True, text=True)
            self.assertNotEqual(0, result.returncode)
            self.assertNotIn("stop-service", result.stdout)

if __name__ == "__main__":
    unittest.main()
