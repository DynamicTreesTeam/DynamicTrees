# Falling Tree Shader and DTBOP Translation Fix Design

## Scope

Apply two minimal compatibility fixes to the Minecraft 26.2 Fabric ports:

- Make Dynamic Trees falling-tree geometry visible when shaders honor vertex alpha.
- Give every Dynamic Trees for Biomes O' Plenty branch item a translated display name.

Build both mods, update the Ashfall modpack and active Prism Launcher instance, verify the result, and commit and push each repository separately.

## Dynamic Trees Rendering Fix

`FallingTreeEntityModel` reconstructs branch and leaf quads for the falling-tree entity. It currently sends colors as `0xRRGGBB` to `QuadInstance.setColor`, whose 26.2 color contract is packed ARGB. The omitted high byte becomes alpha zero. Shader pipelines that consume vertex alpha therefore make both trunks and leaves invisible even though the entity still casts a shadow.

Change the reconstructed color to `0xFFRRGGBB`. Keep the existing tint and diffuse-light calculations unchanged. This correction belongs in the common renderer because it is valid for both vanilla and shader rendering and affects every falling tree using that path.

## DTBOP Translation Fix

Dynamic Trees registers branch blocks and corresponding branch items under IDs such as `dtbop:redwood_branch`. The `_branch` suffix is an intentional internal representation for variable-radius trunk segments and must not be renamed.

Minecraft 26.2 derives the branch item's display key from the item registry ID, producing `item.dtbop.redwood_branch`. DTBOP currently supplies only matching `block.dtbop.*_branch` entries. Add item translations for all twelve DTBOP branch families, mirroring the existing localized block display names. This avoids a Redwood-only special case and fixes every item registered through the same path.

## Verification and Delivery

For each repository:

1. Run the relevant clean Fabric build and tests.
2. Inspect the output jar to confirm the changed class or language resource is packaged.
3. Update the Ashfall modpack artifact and metadata without disturbing unrelated working-tree changes.
4. Back up and replace the corresponding jar in the active Prism instance.
5. Run the modpack's available validation checks.
6. Commit only files belonging to this work and push the current port branch.

Runtime acceptance criteria are that a falling Dark Oak or other dynamic tree renders its trunk and leaves with Complementary Reimagined enabled, and the DTBOP Redwood trunk item displays its localized tree name instead of `item.dtbop.redwood_branch`.

## Non-goals

- Do not modify Complementary Reimagined, Iris, or Sodium configuration.
- Do not rename branch registry IDs or alter saved-world data.
- Do not refactor the falling-tree rendering pipeline beyond the missing alpha correction.
- Do not modify unrelated files already present in either repository or modpack.
