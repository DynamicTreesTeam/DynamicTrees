package com.dtteam.dynamictrees.worldgen.structure;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.tree.species.Species;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.PlainVillagePools;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.world.level.levelgen.structure.pools.ListPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool.Projection.RIGID;
import static net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool.Projection.TERRAIN_MATCHING;

/**
 * @author Harley O'Connor
 */
public final class VillageTreeReplacement {

    public static final Logger LOGGER = LogManager.getLogger();
    private static final String REPLACEMENT_TOWN_CENTER_ID = DynamicTrees.location("village/plains/town_centers/plains_meeting_point_3").toString();

    public static void replaceTreesFromVanillaVillages(BootstrapContext<StructureTemplatePool> context) {
        // Replace Oak tree in Plains village town center.
        var processorLists = context.lookup(Registries.PROCESSOR_LIST);
        var templatePools = context.lookup(Registries.TEMPLATE_POOL);
        final TreePoolElement townCenterTreePattern = new TreePoolElement(Species.REGISTRY.get(DynamicTrees.OAK), new BlockPos(5, 1, 5) /*new BlockPos(0, 1, 0)*/, RIGID);
        RegularTemplatePoolModifier.create(templatePools, PlainVillagePools.START)
                .replaceTemplate(3,
                        new ListPoolElement(ImmutableList.of(
                                StructurePoolElement.legacy(REPLACEMENT_TOWN_CENTER_ID, processorLists.getOrThrow(ProcessorLists.MOSSIFY_70_PERCENT)).apply(RIGID),
                                townCenterTreePattern
                        ), RIGID)
                ).replaceTemplate(7,
                        new ListPoolElement(ImmutableList.of(
                                StructurePoolElement.legacy(REPLACEMENT_TOWN_CENTER_ID, processorLists.getOrThrow(ProcessorLists.ZOMBIE_PLAINS)).apply(RIGID),
                                townCenterTreePattern
                        ), RIGID)
                ).registerPool(context);

        // Replace Oak trees from Plains village.
        final TreePoolElement oakTreePattern = new TreePoolElement(Species.REGISTRY.get(DynamicTrees.OAK), TERRAIN_MATCHING);
        RegularTemplatePoolModifier.village(templatePools, "plains", "trees").replaceTemplate(0, oakTreePattern).registerPool(context);
        RegularTemplatePoolModifier.village(templatePools, "plains", "decor").replaceTemplate(1, oakTreePattern).registerPool(context);
        RegularTemplatePoolModifier.village(templatePools, "plains", "zombie/decor").replaceTemplate(1, oakTreePattern).registerPool(context);

        // Replace Acacia trees from Savanna village.
        final TreePoolElement acaciaTreePattern = new TreePoolElement(Species.REGISTRY.get(DynamicTrees.ACACIA), TERRAIN_MATCHING);
        RegularTemplatePoolModifier.village(templatePools, "savanna", "trees").replaceTemplate(0, acaciaTreePattern).registerPool(context);
        RegularTemplatePoolModifier.village(templatePools, "savanna", "decor").replaceTemplate(1, acaciaTreePattern).registerPool(context);
        RegularTemplatePoolModifier.village(templatePools, "savanna", "zombie/decor").replaceTemplate(1, acaciaTreePattern).registerPool(context);

        // Replace Spruce trees from Snowy village.
        final TreePoolElement spruceTreePattern = new TreePoolElement(Species.REGISTRY.get(DynamicTrees.SPRUCE), TERRAIN_MATCHING);
        RegularTemplatePoolModifier.village(templatePools, "snowy", "trees").replaceTemplate(0, spruceTreePattern).registerPool(context);
        RegularTemplatePoolModifier.village(templatePools, "snowy", "decor").replaceTemplate(3, spruceTreePattern).registerPool(context);
        RegularTemplatePoolModifier.village(templatePools, "snowy", "zombie/decor").replaceTemplate(3, spruceTreePattern).registerPool(context);

        // Replace Spruce and Pine trees from Taiga village.
        RegularTemplatePoolModifier.village(templatePools, "taiga", "decor")
                .replaceTemplate(7, spruceTreePattern)
                .replaceTemplate(8, spruceTreePattern)
                .registerPool(context);
        RegularTemplatePoolModifier.village(templatePools, "taiga", "zombie/decor")
                .replaceTemplate(4, spruceTreePattern)
                .replaceTemplate(5, spruceTreePattern)
                .registerPool(context);
    }

}
