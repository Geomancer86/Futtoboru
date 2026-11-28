"""T-01 (D27): tests of tools/test_level.py, tools/test_tiers.py and tools/suite_lock.py (python -m pytest tools)."""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import suite_lock  # noqa: E402
import test_level  # noqa: E402
import test_tiers  # noqa: E402


def test_levels_of_files():
    assert test_level.level_of("docs/HANDOFF.md") == "docs"
    assert test_level.level_of("README.md") == "docs"
    assert test_level.level_of("Jenkinsfile") == "docs"
    assert test_level.level_of("tools/test_level.py") == "docs"
    assert test_level.level_of("tools/data/validate.py") == "data"
    assert test_level.level_of("futtoboru-core/src/main/resources/gamedata/core/pack.json") == "data"
    assert test_level.level_of("futtoboru-core/src/main/java/com/rndmodgames/futtoboru/screens/MenuScreen.java") \
        == "presentation"
    assert test_level.level_of("futtoboru-core/src/main/resources/mods/language/BaseBundle.properties") == "presentation"
    assert test_level.level_of("futtoboru-core/src/test/java/com/rndmodgames/futtoboru/BasicTests.java") == "presentation"
    assert test_level.level_of("futtoboru-desktop/src/main/java/com/rndmodgames/desktop/DesktopLauncher.java") \
        == "presentation"
    assert test_level.level_of("futtoboru-core/src/main/java/com/rndmodgames/futtoboru/engine/X.java") == "simulation"
    assert test_level.level_of("pom.xml") == "simulation"
    assert test_level.level_of("mvnw") == "simulation"


def test_slow_tags_from_the_source():
    source = '\n'.join([
        "@Tag(\"slow\")",
        "class A {",
        "}",
    ])
    assert test_tiers.slow_parts(source) == (True, set())
    source = '\n'.join([
        "/** doc */",
        "class B {",
        "    @Test",
        "    @Tag(\"slow\")",
        "    void heavy() {",
        "    }",
        "    @Test",
        "    void light() {",
        "    }",
        "}",
    ])
    assert test_tiers.slow_parts(source) == (False, {"heavy"})


def test_the_lock_is_exclusive_and_stale_locks_are_taken_over(tmp_path, monkeypatch):
    monkeypatch.setattr(suite_lock, "LOCK_DIR", str(tmp_path))
    owner = suite_lock.acquire("suite", "first", wait_min=0, expire_min=20)
    assert owner is not None
    assert suite_lock.acquire("suite", "second", wait_min=0, expire_min=20) is None  # held, no waiting
    # a holder whose process is gone loses the lock
    path = suite_lock.lock_path("suite")
    held = suite_lock.read_owner(path)
    held["pid"] = 999999999
    suite_lock.write_owner(path, held)
    assert suite_lock.acquire("suite", "third", wait_min=0, expire_min=20) is not None


def test_run_locked_returns_the_exit_code_and_logs_history(tmp_path, monkeypatch):
    monkeypatch.setattr(suite_lock, "LOCK_DIR", str(tmp_path))
    code = suite_lock.run_locked([sys.executable, "-c", "import sys; sys.exit(3)"], cwd=str(tmp_path))
    assert code == 3
    assert not os.path.exists(suite_lock.lock_path("suite")), "released"
    with open(os.path.join(str(tmp_path), suite_lock.HISTORY), encoding="utf-8") as handle:
        assert json.loads(handle.readline())["exit"] == 3


def test_an_extensionless_script_runs_its_windows_twin(tmp_path):
    script = tmp_path / "mvnw"
    script.write_text("#!/bin/sh\n")
    (tmp_path / "mvnw.cmd").write_text("@echo off\n")
    assert suite_lock.windows_twin(str(script), is_windows=True) == str(script) + ".cmd"
    assert suite_lock.windows_twin(str(script), is_windows=False) == str(script)
    assert suite_lock.windows_twin(str(tmp_path / "mvnw.cmd"), is_windows=True) == str(tmp_path / "mvnw.cmd")
    lone = tmp_path / "tool"
    lone.write_text("x")
    assert suite_lock.windows_twin(str(lone), is_windows=True) == str(lone)
