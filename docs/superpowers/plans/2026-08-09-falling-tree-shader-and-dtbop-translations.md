# Falling Tree Shader and DTBOP Translations Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make falling Dynamic Trees entities render with shaders and localize every DTBOP branch item on Minecraft 26.2 Fabric, then ship both rebuilt jars through Ashfall.

**Architecture:** Correct the missing alpha byte at the single point where Dynamic Trees constructs falling-tree quad colors. Correct DTBOP's resource data by mirroring its twelve existing branch-block names under the corresponding item keys. Keep both source changes independently buildable and commit them in their own repositories before Ashfall records their exact revisions and artifacts.

**Tech Stack:** Java 25, Minecraft 26.2, Fabric Loader 0.19.3, Fabric Loom 1.17.17, Gradle, JSON language resources, Python 3 Ashfall validation, Prism Launcher.

## Global Constraints

- Patch the downstream 26.2 ports; do not modify Iris, Sodium, Complementary Reimagined, or their configuration.
- Keep every existing `*_branch` registry ID unchanged.
- Preserve existing tint and diffuse-light calculations.
- Add item translations for all twelve DTBOP branch families, not only Redwood.
- Do not add a new unit-test framework to either upstream-owned mod.
- Preserve unrelated working-tree files, including Dynamic Trees' untracked `.vscode/` directory.
- Deploy only through Ashfall's explicit manifest so the live files receive automatic backups.

---

### Task 1: Make Falling-Tree Quad Colors Opaque

**Files:**
- Modify: `common/src/main/java/com/dtteam/dynamictrees/model/entity/FallingTreeEntityModel.java:161`

**Interfaces:**
- Consumes: `float r`, `float g`, and `float b` values already clamped by the existing 8-bit channel packing.
- Produces: `int newColor` in packed ARGB form, with alpha fixed to `0xFF`, for `QuadInstance.setColor(int)`.

- [ ] **Step 1: Run the focused source-contract check and verify RED**

Run from `/Volumes/Dev/Projects/krona/minecraft-mods/DynamicTrees`:

```bash
python3 -c 'from pathlib import Path; source = Path("common/src/main/java/com/dtteam/dynamictrees/model/entity/FallingTreeEntityModel.java").read_text(); assert "int newColor = 0xFF000000 |" in source, "falling-tree color has no opaque alpha byte"'
```

Expected: exit 1 with `AssertionError: falling-tree color has no opaque alpha byte`.

- [ ] **Step 2: Add the opaque alpha byte without changing RGB calculations**

Replace the current `newColor` assignment with:

```java
int newColor = 0xFF000000 | ((int)(r * 255F) & 255) << 16 | ((int)(g * 255F) & 255) << 8 | ((int)(b * 255F) & 255);
```

Do not alter the tint, diffuse shading, light coordinates, overlay coordinates, render type, or quad submission path.

- [ ] **Step 3: Re-run the focused check and verify GREEN**

Run the command from Step 1 again.

Expected: exit 0 with no output.

- [ ] **Step 4: Build the complete Dynamic Trees Fabric artifact**

Run:

```bash
./gradlew clean :fabric:build
```

Expected: `BUILD SUCCESSFUL` and `fabric/build/libs/dynamictrees-fabric-26.2-1.8.0-BETA03.jar` exists.

- [ ] **Step 5: Inspect the packaged bytecode**

Run:

```bash
javap -classpath fabric/build/libs/dynamictrees-fabric-26.2-1.8.0-BETA03.jar -c -p com.dtteam.dynamictrees.model.entity.FallingTreeEntityModel
```

Expected: `renderToBuffer` contains an integer OR operation combining the calculated RGB channels with the opaque `-16777216` (`0xFF000000`) constant before the call to `QuadInstance.setColor`.

- [ ] **Step 6: Review and commit the renderer patch**

Run:

```bash
git diff --check
git diff -- common/src/main/java/com/dtteam/dynamictrees/model/entity/FallingTreeEntityModel.java
git add common/src/main/java/com/dtteam/dynamictrees/model/entity/FallingTreeEntityModel.java
git commit -m "fix: preserve falling tree opacity with shaders"
```

Expected: one source file in the commit; `.vscode/` remains untracked and unstaged.

---

### Task 2: Localize Every DTBOP Branch Item

**Files:**
- Modify: `/Volumes/Dev/Projects/krona/minecraft-mods/DynamicTrees-BOP/src/main/resources/assets/dtbop/lang/en_us.json:2`

**Interfaces:**
- Consumes: the existing twelve `block.dtbop.*_branch` display values.
- Produces: matching `item.dtbop.*_branch` keys used by Minecraft 26.2 item names.

- [ ] **Step 1: Run the language invariant and verify RED**

Run from `/Volumes/Dev/Projects/krona/minecraft-mods/DynamicTrees-BOP`:

```bash
python3 -c 'import json; from pathlib import Path; data = json.loads(Path("src/main/resources/assets/dtbop/lang/en_us.json").read_text()); branches = {key: value for key, value in data.items() if key.startswith("block.dtbop.") and key.endswith("_branch")}; missing = [key.replace("block.", "item.", 1) for key, value in branches.items() if data.get(key.replace("block.", "item.", 1)) != value]; assert len(branches) == 12 and not missing, f"missing or mismatched branch item translations: {missing}"'
```

Expected: exit 1 listing all twelve missing item keys, including `item.dtbop.redwood_branch`.

- [ ] **Step 2: Add the twelve item translations**

Add these entries after the existing block branch translations:

```json
  "item.dtbop.dead_branch": "Dead Tree",
  "item.dtbop.fir_branch": "Fir Tree",
  "item.dtbop.hellbark_branch": "Hellbark Tree",
  "item.dtbop.jacaranda_branch": "Jacaranda Tree",
  "item.dtbop.magic_branch": "Magic Tree",
  "item.dtbop.mahogany_branch": "Mahogany Tree",
  "item.dtbop.maple_branch": "Maple Tree",
  "item.dtbop.palm_branch": "Palm Tree",
  "item.dtbop.pine_branch": "Pine Tree",
  "item.dtbop.redwood_branch": "Redwood Tree",
  "item.dtbop.umbran_branch": "Umbran Tree",
  "item.dtbop.willow_branch": "Willow Tree",
```

- [ ] **Step 3: Re-run the language invariant and verify GREEN**

Run the command from Step 1 again.

Expected: exit 0 with no output.

- [ ] **Step 4: Build the DTBOP Fabric artifact**

Run:

```bash
./gradlew clean build
```

Expected: `BUILD SUCCESSFUL` and `build/libs/dtbop-3.5.0-fabric-alpha.jar` exists.

- [ ] **Step 5: Verify the packaged language file**

Run:

```bash
unzip -p build/libs/dtbop-3.5.0-fabric-alpha.jar assets/dtbop/lang/en_us.json | python3 -c 'import json, sys; data = json.load(sys.stdin); assert data["item.dtbop.redwood_branch"] == "Redwood Tree"; assert sum(key.startswith("item.dtbop.") and key.endswith("_branch") for key in data) == 12'
```

Expected: exit 0 with no output.

- [ ] **Step 6: Review and commit the localization patch**

Run:

```bash
git diff --check
git diff -- src/main/resources/assets/dtbop/lang/en_us.json
git add src/main/resources/assets/dtbop/lang/en_us.json
git commit -m "fix: localize branch items on 26.2"
```

Expected: only `en_us.json` is committed.

---

### Task 3: Pin Both Rebuilt Ports in Ashfall

**Files:**
- Replace: `/Volumes/Dev/Projects/krona/minecraft-mods/Ashfall/mods/dist/dynamictrees-fabric-26.2-1.8.0-BETA03.jar`
- Create: `/Volumes/Dev/Projects/krona/minecraft-mods/Ashfall/mods/dist/dtbop-3.5.0-fabric-alpha.jar`
- Modify: `/Volumes/Dev/Projects/krona/minecraft-mods/Ashfall/manifest/dynamic-trees.local.toml`
- Create: `/Volumes/Dev/Projects/krona/minecraft-mods/Ashfall/manifest/dynamic-trees-bop.local.toml`
- Modify: `/Volumes/Dev/Projects/krona/minecraft-mods/Ashfall/deploy/ashfall-files.json`
- Modify: `/Volumes/Dev/Projects/krona/minecraft-mods/Ashfall/baseline/mods-sha256.txt`
- Regenerate: `/Volumes/Dev/Projects/krona/minecraft-mods/Ashfall/docs/audits/release-file-inventory.json`

**Interfaces:**
- Consumes: the exact jars and commit revisions produced by Tasks 1 and 2.
- Produces: reproducible Ashfall-owned artifacts and a bounded deployment mapping to the live Prism instance.

- [ ] **Step 1: Run the artifact integration check and verify RED**

Run from `/Volumes/Dev/Projects/krona/minecraft-mods/Ashfall`:

```bash
python3 -c 'import hashlib, json; from pathlib import Path; root = Path.cwd(); built = {"dynamictrees-fabric-26.2-1.8.0-BETA03.jar": root.parent / "DynamicTrees/fabric/build/libs/dynamictrees-fabric-26.2-1.8.0-BETA03.jar", "dtbop-3.5.0-fabric-alpha.jar": root.parent / "DynamicTrees-BOP/build/libs/dtbop-3.5.0-fabric-alpha.jar"}; copies = {entry["target"]: entry["source"] for entry in json.loads((root / "deploy/ashfall-files.json").read_text())["copy"]}; failures = [name for name, source in built.items() if not (root / "mods/dist" / name).is_file() or hashlib.sha256(source.read_bytes()).digest() != hashlib.sha256((root / "mods/dist" / name).read_bytes()).digest() or copies.get("mods/" + name) != "mods/dist/" + name]; assert not failures, f"unintegrated port artifacts: {failures}"'
```

Expected: exit 1 because both rebuilt artifacts are not yet pinned, and DTBOP has no deployment entry.

- [ ] **Step 2: Copy the exact built jars into Ashfall**

Run:

```bash
cp ../DynamicTrees/fabric/build/libs/dynamictrees-fabric-26.2-1.8.0-BETA03.jar mods/dist/dynamictrees-fabric-26.2-1.8.0-BETA03.jar
cp ../DynamicTrees-BOP/build/libs/dtbop-3.5.0-fabric-alpha.jar mods/dist/dtbop-3.5.0-fabric-alpha.jar
```

Expected: each Ashfall jar has the same SHA-256 as its source build artifact.

- [ ] **Step 3: Update the local source manifests**

Read the exact source revisions with `git -C ../DynamicTrees rev-parse HEAD` and `git -C ../DynamicTrees-BOP rev-parse HEAD`, and read each artifact hash with `shasum -a 256`.

Update `manifest/dynamic-trees.local.toml` so `sha256` matches the rebuilt Dynamic Trees jar and `source-revision` is the Task 1 commit.

Create `manifest/dynamic-trees-bop.local.toml` with these fixed fields:

```toml
filename = 'dtbop-3.5.0-fabric-alpha.jar'
name = "Dynamic Trees for Biomes O' Plenty"
side = 'both'
source-repository = 'https://github.com/Arilas/DynamicTrees-BOP'
x-prismlauncher-loaders = [ 'fabric' ]
x-prismlauncher-mc-versions = [ '26.2' ]
x-prismlauncher-version-number = '3.5.0-fabric-alpha'

[[x-prismlauncher-dependencies]]
addonId = 'P7dR8mSH'
type = 'REQUIRED'
```

Using `apply_patch`, add `sha256` after `side` with the literal 64-character value printed by `shasum`, and add `source-revision` after `source-repository` with the literal 40-character value printed by `git rev-parse`. Validate the result with `python3 -c 'import tomllib; tomllib.load(open("manifest/dynamic-trees-bop.local.toml", "rb"))'`.

- [ ] **Step 4: Add the DTBOP artifact to the bounded deployment manifest**

Add this object immediately after the existing Dynamic Trees jar mapping in `deploy/ashfall-files.json`:

```json
{
  "source": "mods/dist/dtbop-3.5.0-fabric-alpha.jar",
  "root": "minecraft",
  "target": "mods/dtbop-3.5.0-fabric-alpha.jar"
}
```

Preserve valid JSON and do not add either local source manifest to the Prism `.index` directory; the manifests document Ashfall provenance and are consumed by release inventory generation.

- [ ] **Step 5: Update the expected mod hashes**

Replace the Dynamic Trees line in `baseline/mods-sha256.txt` with its new exact SHA-256. Replace the DTBOP line with its new exact SHA-256, preserving the file's existing lexical path order.

- [ ] **Step 6: Re-run the artifact integration check and verify GREEN**

Run the command from Step 1 again.

Expected: exit 0 with no output.

- [ ] **Step 7: Run repository tests and inspect a dry-run deployment**

Run:

```bash
python3 -m unittest discover -s tests -v
python3 scripts/deploy.py --manifest deploy/ashfall-files.json --dry-run
```

Expected: all tracked tests pass. The dry run lists both changed mod jars and no unrelated save, account, screenshot, or server-list targets. It must not report an active matching Java process before applying.

- [ ] **Step 8: Apply the bounded deployment and validate live equality**

Run only after confirming Minecraft is closed:

```bash
python3 scripts/deploy.py --manifest deploy/ashfall-files.json --apply
python3 scripts/validate-pack.py --repo "$PWD" --instance "/Users/krona/Library/Application Support/PrismLauncher/instances/26.2"
```

Expected: deployment creates a timestamped backup under `/Users/krona/Library/Application Support/PrismLauncher/instances/26.2/ashfall-backups/`; validation passes with both live jars hash-identical to Ashfall's copies.

- [ ] **Step 9: Commit the Ashfall artifact and manifest update**

Review `git status --short`, then stage exactly:

```bash
git add mods/dist/dynamictrees-fabric-26.2-1.8.0-BETA03.jar mods/dist/dtbop-3.5.0-fabric-alpha.jar manifest/dynamic-trees.local.toml manifest/dynamic-trees-bop.local.toml deploy/ashfall-files.json baseline/mods-sha256.txt
git commit -m "fix: update Dynamic Trees port artifacts"
```

Expected: one local Ashfall commit containing only both accepted port artifacts and their reproducibility metadata.

- [ ] **Step 10: Regenerate and commit the release inventory**

Run:

```bash
ASHFALL_ARTIFACT_COMMIT="$(git rev-parse HEAD)"
python3 scripts/release_inventory.py --repo "$PWD" --manifest deploy/ashfall-files.json --commit "$ASHFALL_ARTIFACT_COMMIT" --output docs/audits/release-file-inventory.json
git add docs/audits/release-file-inventory.json
git commit -m "docs: refresh Ashfall release inventory"
```

Expected: the inventory describes both local jars, records live hash matches, and points to the artifact integration commit from Step 9.

---

### Task 4: Final Verification and Delivery

**Files:**
- Verify only; no new files.

**Interfaces:**
- Consumes: all three repository commits and the deployed Prism artifacts.
- Produces: pushed Dynamic Trees and DTBOP port branches plus a clean, locally committed Ashfall handoff.

- [ ] **Step 1: Re-run source and package checks**

Run the focused GREEN checks from Tasks 1 and 2, then:

```bash
git -C /Volumes/Dev/Projects/krona/minecraft-mods/DynamicTrees diff --check
git -C /Volumes/Dev/Projects/krona/minecraft-mods/DynamicTrees-BOP diff --check
python3 -m unittest discover -s /Volumes/Dev/Projects/krona/minecraft-mods/Ashfall/tests -v
python3 /Volumes/Dev/Projects/krona/minecraft-mods/Ashfall/scripts/validate-pack.py --repo /Volumes/Dev/Projects/krona/minecraft-mods/Ashfall --instance "/Users/krona/Library/Application Support/PrismLauncher/instances/26.2"
```

Expected: every command passes. Dynamic Trees may show only the preserved untracked `.vscode/` directory; the other scoped source changes are committed.

- [ ] **Step 2: Confirm built, pinned, and live hashes are identical**

Run `shasum -a 256` on each of these triples:

- Dynamic Trees build, `Ashfall/mods/dist` copy, and live Prism jar.
- DTBOP build, `Ashfall/mods/dist` copy, and live Prism jar.

Expected: all three Dynamic Trees hashes match each other, and all three DTBOP hashes match each other.

- [ ] **Step 3: Push both port branches**

Run:

```bash
git -C /Volumes/Dev/Projects/krona/minecraft-mods/DynamicTrees push fork port/26.2
git -C /Volumes/Dev/Projects/krona/minecraft-mods/DynamicTrees-BOP push fork port/26.2-fabric
```

Expected: both `fork` branches advance to the new commits. `Ashfall` remains locally committed because `git -C /Volumes/Dev/Projects/krona/minecraft-mods/Ashfall remote -v` returns no configured remote.

- [ ] **Step 4: Request the runtime acceptance check**

Ask the user to fully restart the Prism Launcher `26.2` instance with Complementary Reimagined enabled and verify both behaviors:

1. Fell a Dynamic Dark Oak tree and confirm the animated trunk and leaves are visible, not only its shadow.
2. Inspect the DTBOP Redwood trunk item and confirm its display name is `Redwood Tree` rather than `item.dtbop.redwood_branch`.

After the game exits, run `Ashfall/scripts/check-log.sh`. Expected: no new Dynamic Trees, DTBOP, Iris, or renderer errors beyond the recorded baseline.
