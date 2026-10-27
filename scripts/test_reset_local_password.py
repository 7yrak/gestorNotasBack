import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch, MagicMock
import reset_local_password as reset

class ResetPasswordTests(unittest.TestCase):
    def test_invalid_password_is_rejected_before_opening_jar(self):
        for password in ("short", "x" * 73, "a" * 8 + "\nextra", "ñ" * 37):
            with self.subTest(length=len(password)), self.assertRaises(ValueError):
                reset.password_hash(Path("does-not-exist.jar"), password)

    def test_password_mismatch_never_writes_configuration_or_restarts_service(self):
        jar = MagicMock()
        jar.__enter__.return_value.namelist.return_value = [
            "BOOT-INF/classes/com/gestornotas/security/LocalAccount.class"]
        with patch.object(reset.os, "geteuid", return_value=0, create=True), \
             patch.object(reset.os, "umask"), \
             patch.object(reset.zipfile, "ZipFile", return_value=jar), \
             patch.object(reset, "run", return_value=b"1\n") as run, \
             patch.object(reset.getpass, "getpass", side_effect=["password-one", "password-two"]), \
             patch.object(reset, "write_private") as write:
            with self.assertRaisesRegex(ValueError, "no coinciden"):
                reset.main()
            write.assert_not_called()
            self.assertEqual(1, run.call_count)
            self.assertEqual("runuser", run.call_args.args[0][0])

    def test_foreign_account_is_rejected_before_password_prompt(self):
        jar = MagicMock()
        jar.__enter__.return_value.namelist.return_value = [
            "BOOT-INF/classes/com/gestornotas/security/LocalAccount.class"]
        with patch.object(reset.os, "geteuid", return_value=0, create=True), \
             patch.object(reset.os, "umask"), \
             patch.object(reset.zipfile, "ZipFile", return_value=jar), \
             patch.object(reset, "run", return_value=b"0\n"), \
             patch.object(reset.getpass, "getpass") as prompt, \
             patch.object(reset, "write_private") as write:
            with self.assertRaisesRegex(RuntimeError, "No coincide"):
                reset.main()
            prompt.assert_not_called()
            write.assert_not_called()

    def test_unknown_artifact_is_rejected_before_touching_credentials(self):
        jar = MagicMock()
        jar.__enter__.return_value.namelist.return_value = []
        with patch.object(reset.os, "geteuid", return_value=0, create=True), \
             patch.object(reset.os, "umask"), \
             patch.object(reset.zipfile, "ZipFile", return_value=jar), \
             patch.object(reset, "run") as run, \
             patch.object(reset, "write_private") as write:
            with self.assertRaisesRegex(RuntimeError, "Primero despliega"):
                reset.main()
            run.assert_not_called()
            write.assert_not_called()

if __name__ == "__main__":
    unittest.main()
