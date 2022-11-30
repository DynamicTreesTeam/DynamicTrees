package com.ferreusveritas.dynamictrees.worldgen.structure;

import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.world.level.levelgen.structure.pools.ListPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool.Projection.RIGID;
import static net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool.Projection.TERRAIN_MATCHING;

/**
 * @author Harley O'Connor
 */
public final class VillageTreeReplacement {

    public static final Logger LOGGER = LogManager.getLogger();
    private static final String REPLACEMENT_TOWN_CENTER_ID =
            "dynamictrees:village/plains/town_centers/plains_meeting_point_3";

    public static void replaceTreesFromVanillaVillages() {
        // Replace Oak tree in Plains village town center.
        final TreePoolElement townCenterTreePattern = new TreePoolElement(Species.REGISTRY.get(DTTrees.OAK), new BlockPos(5, 1, 5) /*new BlockPos(0, 1, 0)*/, RIGID);
        RegularTemplatePoolModifier.village("plains", "town_centers")
                .replaceTemplate(3,
                        new ListPoolElement(ImmutableList.of(
                                StructurePoolElement.legacy(REPLACEMENT_TOWN_CENTER_ID, ProcessorLists.MOSSIFY_70_PERCENT).apply(RIGID),
                                townCenterTreePattern
                        ), RIGID)
                ).replaceTemplate(7,
                        new ListPoolElement(ImmutableList.of(
                                StructurePoolElement.legacy(REPLACEMENT_TOWN_CENTER_ID, ProcessorLists.ZOMBIE_PLAINS).apply(RIGID),
                                townCenterTreePattern
                        ), RIGID)
                );

        // Replace Oak trees from Plains village.
        final TreePoolElement oakTreePattern = new TreePoolElement(Species.REGISTRY.get(DTTrees.OAK), TERRAIN_MATCHING);
        RegularTemplatePoolModifier.village("plains", "trees").replaceTemplate(0, oakTreePattern);
        RegularTemplatePoolModifier.village("plains", "decor").replaceTemplate(1, oakTreePattern);
        RegularTemplatePoolModifier.village("plains", "zombie/decor").replaceTemplate(1, oakTreePattern);

        // Replace Acacia trees from Savanna village.
        final TreePoolElement acaciaTreePattern = new TreePoolElement(Species.REGISTRY.get(DTTrees.ACACIA), TERRAIN_MATCHING);
        RegularTemplatePoolModifier.village("savanna", "trees").replaceTemplate(0, acaciaTreePattern);
        RegularTemplatePoolModifier.village("savanna", "decor").replaceTemplate(1, acaciaTreePattern);
        RegularTemplatePoolModifier.village("savanna", "zombie/decor").replaceTemplate(1, acaciaTreePattern);

        // Replace Spruce trees from Snowy village.
        final TreePoolElement spruceTreePattern = new TreePoolElement(Species.REGISTRY.get(DTTrees.SPRUCE), TERRAIN_MATCHING);
        RegularTemplatePoolModifier.village("snowy", "trees").replaceTemplate(0, spruceTreePattern);
        RegularTemplatePoolModifier.village("snowy", "decor").replaceTemplate(3, spruceTreePattern);
        RegularTemplatePoolModifier.village("snowy", "zombie/decor").replaceTemplate(3, spruceTreePattern);

        // Replace Spruce and Pine trees from Taiga village.
        RegularTemplatePoolModifier.village("taiga", "decor")
                .replaceTemplate(7, spruceTreePattern)
                .replaceTemplate(8, spruceTreePattern);
        RegularTemplatePoolModifier.village("taiga", "zombie/decor")
                .replaceTemplate(4, spruceTreePattern)
                .replaceTemplate(5, spruceTreePattern);
    }

}
