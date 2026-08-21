"""Emit Minecraft 26.2 item definitions from existing models/item JSON."""
from __future__ import annotations

import json
import shutil
import sys
from pathlib import Path

SKIP_MODELS = {
    "branch.json",
    "root_branch.json",
    "standard_seed.json",
}

SPECIAL_TINTS = {
    "dendro_potion": [
        {"type": "dynamictrees:dendro_potion"},
        {"type": "minecraft:constant", "value": -1},
    ],
    "staff": [
        {"type": "dynamictrees:staff", "index": 0},
        {"type": "dynamictrees:staff", "index": 1},
        {"type": "minecraft:constant", "value": -1},
    ],
}


def collect_item_models(resource_roots: list[Path]) -> dict[str, Path]:
    found: dict[str, Path] = {}
    for root in resource_roots:
        models_dir = root / "assets" / "dynamictrees" / "models" / "item"
        if not models_dir.is_dir():
            continue
        for path in models_dir.glob("*.json"):
            if path.name in SKIP_MODELS:
                continue
            found[path.stem] = path
    return found


def item_definition(item_id: str) -> dict:
    model: dict = {
        "type": "minecraft:model",
        "model": f"dynamictrees:item/{item_id}",
    }
    tints = SPECIAL_TINTS.get(item_id)
    if tints:
        model["tints"] = tints
    return {"model": model}


def main(argv: list[str]) -> int:
    if len(argv) < 3:
        print("usage: generate_item_defs_26.py <resource-root> [<resource-root> ...] <dest>")
        return 2
    dest = Path(argv[-1])
    roots = [Path(p) for p in argv[1:-1]]
    items = collect_item_models(roots)

    if dest.exists():
        shutil.rmtree(dest)
    out_dir = dest / "assets" / "dynamictrees" / "items"
    out_dir.mkdir(parents=True, exist_ok=True)

    for item_id in sorted(items):
        (out_dir / f"{item_id}.json").write_text(
            json.dumps(item_definition(item_id), indent=2) + "\n",
            encoding="utf-8",
        )
    print(f"wrote {len(items)} item definitions -> {out_dir}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
