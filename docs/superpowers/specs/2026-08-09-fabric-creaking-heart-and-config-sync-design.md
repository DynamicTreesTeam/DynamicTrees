# Fabric Creaking Heart and Ashfall Config Sync Design

## Scope

Fix the Minecraft 26.2 Fabric server crash caused when a Dynamic Trees pale-oak creaking-heart branch is ticked. Rebuild and ship the corrected Dynamic Trees jar through Ashfall, then restore byte-for-byte equality between Ashfall's canonical Dynamic Trees configs and the live Prism instance.

## Root Cause

Vanilla `CreakingHeartBlock.hasRequiredLogs` reads `RotatedPillarBlock.AXIS` from the heart state. Dynamic Trees creaking-heart branches represent variable-radius tree networks and intentionally do not have an axis property.

Dynamic Trees already intercepts the vanilla method on NeoForge and delegates dynamic heart states to `CreakingHeartBranchBlock.hasRequiredLogs`, which checks neighboring dynamic branches. The Fabric port omitted both the mixin class and its mixin-configuration entry. Consequently, the inherited vanilla creaking-heart block-entity tick reaches the incompatible axis lookup and crashes the integrated server.

## Fabric Fix

Add the same focused mixin used by NeoForge to the Fabric source set and register it in `dynamictrees.fabric.mixins.json`. At the head of vanilla `hasRequiredLogs`, the mixin returns Dynamic Trees' branch-aware result only when the state belongs to `CreakingHeartBranchBlock`. Vanilla heart states continue through the unmodified vanilla implementation.

Keep the platform copies separate for this downstream port. Moving the mixin into common sources would require broader loader/resource restructuring, while adding an axis property to dynamic branches would impose incorrect single-axis semantics on a multi-directional branch network.

## Ashfall Config Synchronization

Ashfall is the source of truth for `dynamictrees-common.toml` and `dynamictrees-server.toml`. Semantic parsing confirms that the repository and live instance currently contain identical gameplay keys and values; their differences are comments and formatting only.

Use Ashfall's bounded deployment manifest to replace the live configs with the committed repository copies while deploying the rebuilt jar. Do not copy generated live formatting back into Ashfall. The deployment must back up the current live jar and both live configs before replacement.

## Verification and Delivery

Verification must prove:

- The pre-fix Fabric artifact lacks the creaking-heart mixin class/configuration entry.
- The rebuilt Fabric artifact contains the mixin class and lists it in `dynamictrees.fabric.mixins.json`.
- The Dynamic Trees Fabric build succeeds.
- Ashfall's tracked tests and pack validation pass.
- The build, Ashfall, and live Dynamic Trees jars have identical SHA-256 hashes.
- Both live Dynamic Trees config files are byte-identical to Ashfall after deployment.
- Loading the affected world no longer produces the `axis`-property exception at the existing heart block.

Commit the Dynamic Trees source fix and push `port/26.2`. Commit the updated jar, hash, provenance, baseline, and refreshed release inventory in Ashfall. Ashfall remains local because it has no configured remote.

## Non-goals

- Do not add `AXIS` to Dynamic Trees branch states.
- Do not change creaking-heart activation, resin, loot, or branch-connectivity behavior.
- Do not alter any Dynamic Trees config value.
- Do not edit DTBOP for this crash.
- Do not delete or modify the affected world block or save data.
