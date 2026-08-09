# Fabric Creaking Heart and Config Sync Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Prevent Fabric creaking-heart block entities from invoking vanilla's incompatible axis lookup, then ship the corrected jar and canonical Ashfall configs to the live Prism instance.

**Architecture:** Add Fabric's missing copy of the already-proven NeoForge mixin and register it in the Fabric mixin configuration. The mixin intercepts only Dynamic Trees creaking-heart branch states and delegates to their branch-aware log check. Ashfall remains the artifact and config source of truth, and its existing bounded deployment path replaces the live jar and both comment/format-drifted configs with automatic backups.

**Tech Stack:** Java 25, Minecraft 26.2, Fabric Loader 0.19.3, Fabric Loom 1.17.17, Sponge Mixin, Gradle, Python 3 Ashfall tooling, Prism Launcher.

## Global Constraints

- Do not add `AXIS` to Dynamic Trees branch states.
- Do not change creaking-heart activation, resin, loot, or connectivity behavior.
- Keep the NeoForge mixin unchanged.
- Do not change any Dynamic Trees config key or value.
- Ashfall's committed `dynamictrees-common.toml` and `dynamictrees-server.toml` are canonical.
- Do not edit DTBOP or any save data.
- Preserve Dynamic Trees' untracked `.vscode/` directory.
- Deploy only through Ashfall's bounded deployment script so every replaced live file is backed up.

---

### Task 1: Restore the Creaking-Heart Redirect on Fabric

**Files:**
- Create: `fabric/src/main/java/com/dtteam/dynamictrees/mixin/MixinCreakingHeartBlock.java`
- Modify: `fabric/src/main/resources/dynamictrees.fabric.mixins.json:10`

**Interfaces:**
- Consumes: vanilla static `CreakingHeartBlock.hasRequiredLogs(BlockState, LevelReader, BlockPos)` and Dynamic Trees static `CreakingHeartBranchBlock.hasRequiredLogs(BlockState, LevelReader, BlockPos)`.
- Produces: a cancellable head injection that returns the branch-aware result only for `CreakingHeartBranchBlock` states.

- [ ] **Step 1: Run the Fabric mixin artifact contract and verify RED**

Run from `/Volumes/Dev/Projects/krona/minecraft-mods/DynamicTrees`:

```bash
python3 -c 'import json; from pathlib import Path; source = Path("fabric/src/main/java/com/dtteam/dynamictrees/mixin/MixinCreakingHeartBlock.java"); config = json.loads(Path("fabric/src/main/resources/dynamictrees.fabric.mixins.json").read_text()); missing = []; missing += [] if source.is_file() else ["Fabric mixin source"]; missing += [] if "MixinCreakingHeartBlock" in config["mixins"] else ["Fabric mixin registration"]; assert not missing, f"missing creaking-heart compatibility: {missing}"'
```

Expected: exit 1 listing both `Fabric mixin source` and `Fabric mixin registration`.

- [ ] **Step 2: Create the focused Fabric mixin**

Create `MixinCreakingHeartBlock.java` with exactly this implementation, matching the working NeoForge source:

```java
package com.dtteam.dynamictrees.mixin;

import com.dtteam.dynamictrees.block.branch.CreakingHeartBranchBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreakingHeartBlock.class)
public class MixinCreakingHeartBlock {

    @Inject(method = "hasRequiredLogs", at = @At("HEAD"), cancellable = true)
    private static void hasRequiredLogs(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof CreakingHeartBranchBlock) {
            cir.setReturnValue(CreakingHeartBranchBlock.hasRequiredLogs(state, level, pos));
        }
    }

}
```

- [ ] **Step 3: Register the mixin in Fabric resources**

Add `"MixinCreakingHeartBlock",` to the `mixins` array in `dynamictrees.fabric.mixins.json`, immediately before `"MixinMinecraftServer"`.

- [ ] **Step 4: Re-run the source contract and verify GREEN**

Run the command from Step 1 again.

Expected: exit 0 with no output.

- [ ] **Step 5: Clean-build the Fabric jar**

Run:

```bash
./gradlew clean :fabric:build
```

Expected: `BUILD SUCCESSFUL` and `fabric/build/libs/dynamictrees-fabric-26.2-1.8.0-BETA03.jar` exists.

- [ ] **Step 6: Verify the packaged class and mixin registration**

Run:

```bash
jar tf fabric/build/libs/dynamictrees-fabric-26.2-1.8.0-BETA03.jar | rg '^com/dtteam/dynamictrees/mixin/MixinCreakingHeartBlock.class$'
unzip -p fabric/build/libs/dynamictrees-fabric-26.2-1.8.0-BETA03.jar dynamictrees.fabric.mixins.json | python3 -c 'import json, sys; config = json.load(sys.stdin); assert "MixinCreakingHeartBlock" in config["mixins"]'
```

Expected: the first command prints the class path and the second exits 0.

- [ ] **Step 7: Review and commit the Fabric fix**

Run:

```bash
git diff --check
git diff -- fabric/src/main/java/com/dtteam/dynamictrees/mixin/MixinCreakingHeartBlock.java fabric/src/main/resources/dynamictrees.fabric.mixins.json
git add fabric/src/main/java/com/dtteam/dynamictrees/mixin/MixinCreakingHeartBlock.java fabric/src/main/resources/dynamictrees.fabric.mixins.json
git commit -m "fix: redirect creaking heart log checks on Fabric"
```

Expected: exactly the new mixin and Fabric mixin JSON are committed; `.vscode/` remains unstaged.

---

### Task 2: Pin and Deploy the Fixed Jar and Canonical Configs

**Files:**
- Replace: `/Volumes/Dev/Projects/krona/minecraft-mods/Ashfall/mods/dist/dynamictrees-fabric-26.2-1.8.0-BETA03.jar`
- Modify: `/Volumes/Dev/Projects/krona/minecraft-mods/Ashfall/manifest/dynamic-trees.local.toml`
- Modify: `/Volumes/Dev/Projects/krona/minecraft-mods/Ashfall/baseline/mods-sha256.txt`
- Regenerate: `/Volumes/Dev/Projects/krona/minecraft-mods/Ashfall/docs/audits/release-file-inventory.json`
- Deploy unchanged canonical sources: `/Volumes/Dev/Projects/krona/minecraft-mods/Ashfall/defaultconfigs/dynamictrees-common.toml`
- Deploy unchanged canonical sources: `/Volumes/Dev/Projects/krona/minecraft-mods/Ashfall/defaultconfigs/dynamictrees-server.toml`

**Interfaces:**
- Consumes: the Task 1 Fabric jar and source commit.
- Produces: an Ashfall-pinned artifact, three backed-up live replacements, byte-identical live configs, and a refreshed release audit.

- [ ] **Step 1: Run the Ashfall/live integration assertions and verify RED**

Run from `/Volumes/Dev/Projects/krona/minecraft-mods/Ashfall`:

```bash
python3 -c 'import hashlib; from pathlib import Path; built = Path("../DynamicTrees/fabric/build/libs/dynamictrees-fabric-26.2-1.8.0-BETA03.jar"); pinned = Path("mods/dist/dynamictrees-fabric-26.2-1.8.0-BETA03.jar"); live_root = Path("/Users/krona/Library/Application Support/PrismLauncher/instances/26.2/minecraft"); failures = []; failures += [] if hashlib.sha256(built.read_bytes()).digest() == hashlib.sha256(pinned.read_bytes()).digest() else ["Ashfall jar"]; failures += [] if (Path("defaultconfigs/dynamictrees-common.toml").read_bytes() == (live_root / "config/dynamictrees-common.toml").read_bytes()) else ["common config"]; failures += [] if (Path("defaultconfigs/dynamictrees-server.toml").read_bytes() == (live_root / "config/dynamictrees-server.toml").read_bytes()) else ["server config"]; assert not failures, f"out-of-sync Dynamic Trees assets: {failures}"'
```

Expected: exit 1 listing `Ashfall jar`, `common config`, and `server config`.

- [ ] **Step 2: Copy the exact built jar into Ashfall**

Run:

```bash
cp ../DynamicTrees/fabric/build/libs/dynamictrees-fabric-26.2-1.8.0-BETA03.jar mods/dist/dynamictrees-fabric-26.2-1.8.0-BETA03.jar
shasum -a 256 ../DynamicTrees/fabric/build/libs/dynamictrees-fabric-26.2-1.8.0-BETA03.jar mods/dist/dynamictrees-fabric-26.2-1.8.0-BETA03.jar
```

Expected: both printed SHA-256 values are identical.

- [ ] **Step 3: Update artifact provenance and the expected live hash**

Run `git -C ../DynamicTrees rev-parse HEAD` and `shasum -a 256 mods/dist/dynamictrees-fabric-26.2-1.8.0-BETA03.jar`.

Using `apply_patch`, set `manifest/dynamic-trees.local.toml`'s `source-revision` to the literal 40-character Task 1 commit and `sha256` to the literal 64-character artifact hash. Replace the Dynamic Trees line in `baseline/mods-sha256.txt` with that same hash. Do not alter DTBOP's manifest or hash.

- [ ] **Step 4: Run Ashfall tests and inspect the full deployment dry run**

Run:

```bash
python3 -m unittest discover -s tests -v
python3 scripts/deploy.py --manifest deploy/ashfall-files.json --dry-run
```

Expected: all 63 tracked tests pass. The dry run identifies the Dynamic Trees jar plus `dynamictrees-common.toml` and `dynamictrees-server.toml` as changes. If it identifies any other changed non-Dynamic-Trees target, inspect that difference before applying.

- [ ] **Step 5: Apply the bounded deployment**

After confirming Minecraft is closed, run:

```bash
python3 scripts/deploy.py --manifest deploy/ashfall-files.json --apply
```

Expected: Ashfall prints a timestamped backup path containing the previous live jar and both previous live config files.

- [ ] **Step 6: Re-run the integration assertions and verify GREEN**

Run the command from Step 1 again.

Expected: exit 0 with no output.

- [ ] **Step 7: Verify live artifact equality and pack validity**

Run:

```bash
python3 scripts/validate-pack.py --repo "$PWD" --instance "/Users/krona/Library/Application Support/PrismLauncher/instances/26.2"
shasum -a 256 ../DynamicTrees/fabric/build/libs/dynamictrees-fabric-26.2-1.8.0-BETA03.jar mods/dist/dynamictrees-fabric-26.2-1.8.0-BETA03.jar "/Users/krona/Library/Application Support/PrismLauncher/instances/26.2/minecraft/mods/dynamictrees-fabric-26.2-1.8.0-BETA03.jar"
cmp -s defaultconfigs/dynamictrees-common.toml "/Users/krona/Library/Application Support/PrismLauncher/instances/26.2/minecraft/config/dynamictrees-common.toml"
cmp -s defaultconfigs/dynamictrees-server.toml "/Users/krona/Library/Application Support/PrismLauncher/instances/26.2/minecraft/config/dynamictrees-server.toml"
```

Expected: validation passes, all three jar hashes match, and both `cmp` commands exit 0.

- [ ] **Step 8: Commit Ashfall's rebuilt artifact metadata**

Run:

```bash
git diff --check
git status --short
git add mods/dist/dynamictrees-fabric-26.2-1.8.0-BETA03.jar manifest/dynamic-trees.local.toml baseline/mods-sha256.txt
git commit -m "fix: update Dynamic Trees creaking heart port"
```

Expected: the commit contains the jar, its local manifest, and the baseline hash only. The canonical config files remain unchanged in Git because they were already the source of truth.

- [ ] **Step 9: Regenerate and commit the now-verifiable release inventory**

Run:

```bash
ASHFALL_PORT_COMMIT="$(git rev-parse HEAD)"
python3 scripts/release_inventory.py --repo "$PWD" --manifest deploy/ashfall-files.json --commit "$ASHFALL_PORT_COMMIT" --output docs/audits/release-file-inventory.json
git add docs/audits/release-file-inventory.json
git commit -m "docs: refresh Ashfall release inventory"
```

Expected: inventory generation succeeds now that the canonical configs and every full-manifest live target match. The inventory includes the Dynamic Trees and DTBOP local artifacts.

---

### Task 3: Final Verification, Push, and Crash-World Acceptance

**Files:**
- Verify only; no new source files.

**Interfaces:**
- Consumes: Task 1's pushed candidate and Task 2's live deployment.
- Produces: a pushed `port/26.2` branch, clean Ashfall commits, and runtime confirmation that the existing heart block no longer crashes the server.

- [ ] **Step 1: Run fresh source, package, build, and pack checks**

Run:

```bash
cd /Volumes/Dev/Projects/krona/minecraft-mods/DynamicTrees
python3 -c 'import json; from pathlib import Path; source = Path("fabric/src/main/java/com/dtteam/dynamictrees/mixin/MixinCreakingHeartBlock.java"); config = json.loads(Path("fabric/src/main/resources/dynamictrees.fabric.mixins.json").read_text()); assert source.is_file() and "MixinCreakingHeartBlock" in config["mixins"]'
./gradlew :fabric:build
jar tf fabric/build/libs/dynamictrees-fabric-26.2-1.8.0-BETA03.jar | rg '^com/dtteam/dynamictrees/mixin/MixinCreakingHeartBlock.class$'
unzip -p fabric/build/libs/dynamictrees-fabric-26.2-1.8.0-BETA03.jar dynamictrees.fabric.mixins.json | python3 -c 'import json, sys; assert "MixinCreakingHeartBlock" in json.load(sys.stdin)["mixins"]'
git diff --check
git status --short --branch

cd /Volumes/Dev/Projects/krona/minecraft-mods/Ashfall
python3 -m unittest discover -s tests -v
python3 -c 'import hashlib; from pathlib import Path; built = Path("../DynamicTrees/fabric/build/libs/dynamictrees-fabric-26.2-1.8.0-BETA03.jar"); pinned = Path("mods/dist/dynamictrees-fabric-26.2-1.8.0-BETA03.jar"); live_root = Path("/Users/krona/Library/Application Support/PrismLauncher/instances/26.2/minecraft"); assert hashlib.sha256(built.read_bytes()).digest() == hashlib.sha256(pinned.read_bytes()).digest(); assert Path("defaultconfigs/dynamictrees-common.toml").read_bytes() == (live_root / "config/dynamictrees-common.toml").read_bytes(); assert Path("defaultconfigs/dynamictrees-server.toml").read_bytes() == (live_root / "config/dynamictrees-server.toml").read_bytes()'
python3 scripts/validate-pack.py --repo "$PWD" --instance "/Users/krona/Library/Application Support/PrismLauncher/instances/26.2"
git diff --check
git status --short --branch
```

Expected: every command exits 0; Dynamic Trees shows only `.vscode/` as untracked and Ashfall is clean.

- [ ] **Step 2: Push the Dynamic Trees port branch**

Run:

```bash
git -C /Volumes/Dev/Projects/krona/minecraft-mods/DynamicTrees push fork port/26.2
```

Expected: `fork/port/26.2` advances through the design, plan, and creaking-heart source-fix commits. Ashfall is not pushed because it has no configured remote.

- [ ] **Step 3: Request the runtime crash-world check**

Ask the user to restart the Prism `26.2` instance and load the same affected world. The existing block entity at `(-252,79,-182)` must tick without `Cannot get property ... axis ... pale_oak_creaking_heart_branch`, and no save editing or block removal should be needed.

After the game exits, run:

```bash
rg -n "Cannot get property.*axis|pale_oak_creaking_heart_branch|Ticking block entity" "/Users/krona/Library/Application Support/PrismLauncher/instances/26.2/minecraft/logs/latest.log"
```

Expected: no new match from the post-fix session. Historical crash reports remain untouched.
