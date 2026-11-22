#!/usr/bin/env python3
"""Prevent accidentally publishing a Morphe source that points to the unfixed upstream bundle."""

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
REPO = "NasiGoRank/morphe-spicetify-patches"
README = (ROOT / "README.md").read_text(encoding="utf-8")
BUNDLE = json.loads((ROOT / "patches-bundle.json").read_text(encoding="utf-8"))
ABI = (ROOT / "patches/src/main/java/app/spicetify/patches/spotify/settings/NativeSettingsAbi.java").read_text(encoding="utf-8")
SETTINGS = (ROOT / "patches/src/main/kotlin/app/spicetify/patches/spotify/settings/SettingsPatch.kt").read_text(encoding="utf-8")

assert f"https://morphe.software/add-source?github={REPO}/tree/main" in README
assert f"https://raw.githubusercontent.com/{REPO}/refs/heads/main/patches-bundle.json" in README
assert "add-source?github=spicetify/morphe-patches" not in README
assert "raw.githubusercontent.com/spicetify/morphe-patches/refs/heads/main/patches-bundle.json" not in README
assert BUNDLE["download_url"].startswith(f"https://github.com/{REPO}/releases/download/")
assert "describe(definition).getBytes(StandardCharsets.UTF_8)" in ABI
assert "DexPool" not in ABI
assert "NativeSettingsAbi.digest(definition)" in SETTINGS
print("Fork source links, release bundle provenance and semantic ABI digest: OK")
