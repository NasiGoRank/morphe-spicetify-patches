#!/usr/bin/env python3
"""Compare Spotify Connect device type snapshots without exposing device IDs.

Input: two saved JSON responses from the official
GET https://api.spotify.com/v1/me/player/devices endpoint (scope:
user-read-playback-state). OAuth token is NOT stored or read by this script.

Usage:
  python3 scripts/compare-connect-devices.py --desktop desktop.json --android android.json
"""
from __future__ import annotations

import argparse
import json
from pathlib import Path


def summarize(path: Path) -> list[dict[str, object]]:
    payload = json.loads(path.read_text(encoding="utf-8"))
    devices = payload.get("devices")
    if not isinstance(devices, list):
        raise ValueError(f"{path}: expected an object with a devices array")
    sanitized = []
    for d in devices:
        if not isinstance(d, dict):
            continue
        # No IDs, names, access tokens, local IPs, or user account info.
        sanitized.append({
            "type": str(d.get("type", "unknown")).lower(),
            "is_active": bool(d.get("is_active", False)),
            "is_restricted": bool(d.get("is_restricted", False)),
            "supports_volume": bool(d.get("supports_volume", False)),
        })
    return sanitized


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--desktop", required=True, type=Path)
    parser.add_argument("--android", required=True, type=Path)
    opts = parser.parse_args()

    for label, path in [("Desktop snapshot", opts.desktop), ("Android snapshot", opts.android)]:
        print(f"\n{label}")
        results = summarize(path)
        if not results:
            print("  No devices returned. Start Spotify and retry the authorized API request.")
        for i, d in enumerate(results, 1):
            print(f"  Device {i}: type={d['type']}; active={d['is_active']}; "
                  f"restricted={d['is_restricted']}; supports_volume={d['supports_volume']}")

    print("\nNote: the Connect type is a reported device category, not a Premium entitlement.")
    print("The official Spotify Web API shuffle control requires Premium for the account.")


if __name__ == "__main__":
    main()
