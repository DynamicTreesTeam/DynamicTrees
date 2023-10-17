package com.ferreusveritas.dynamictrees.worldgen.structure;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.PlainVillagePools;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.world.level.levelgen.structure.pools.ListPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
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

    public static void replaceTreesFromVanillaVillages(HolderLookup.Provider vanillaProvider, BootstapContext<StructureTemplatePool> context) {
        // Replace Oak tree in Plains village town center.
        HolderLookup.RegistryLookup<StructureProcessorList> processorLists = vanillaProvider.lookupOrThrow(Registries.PROCESSOR_LIST);
        final TreePoolElement townCenterTreePattern = new TreePoolElement(Species.REGISTRY.get(DTTrees.OAK), new BlockPos(5, 1, 5) /*new BlockPos(0, 1, 0)*/, RIGID);
        RegularTemplatePoolModifier.create(vanillaProvider, PlainVillagePools.START)
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
        final TreePoolElement oakTreePattern = new TreePoolElement(Species.REGISTRY.get(DTTrees.OAK), TERRAIN_MATCHING);
        RegularTemplatePoolModifier.village(vanillaProvider, "plains", "trees").replaceTemplate(0, oakTreePattern).registerPool(context);
        RegularTemplatePoolModifier.village(vanillaProvider, "plains", "decor").replaceTemplate(1, oakTreePattern).registerPool(context);
        RegularTemplatePoolModifier.village(vanillaProvider, "plains", "zombie/decor").replaceTemplate(1, oakTreePattern).registerPool(context);

        // Replace Acacia trees from Savanna village.
        final TreePoolElement acaciaTreePattern = new TreePoolElement(Species.REGISTRY.get(DTTrees.ACACIA), TERRAIN_MATCHING);
        RegularTemplatePoolModifier.village(vanillaProvider, "savanna", "trees").replaceTemplate(0, acaciaTreePattern).registerPool(context);
        RegularTemplatePoolModifier.village(vanillaProvider, "savanna", "decor").replaceTemplate(1, acaciaTreePattern).registerPool(context);
        RegularTemplatePoolModifier.village(vanillaProvider, "savanna", "zombie/decor").replaceTemplate(1, acaciaTreePattern).registerPool(context);

        // Replace Spruce trees from Snowy village.
        final TreePoolElement spruceTreePattern = new TreePoolElement(Species.REGISTRY.get(DTTrees.SPRUCE), TERRAIN_MATCHING);
        RegularTemplatePoolModifier.village(vanillaProvider, "snowy", "trees").replaceTemplate(0, spruceTreePattern).registerPool(context);
        RegularTemplatePoolModifier.village(vanillaProvider, "snowy", "decor").replaceTemplate(3, spruceTreePattern).registerPool(context);
        RegularTemplatePoolModifier.village(vanillaProvider, "snowy", "zombie/decor").replaceTemplate(3, spruceTreePattern).registerPool(context);

        // Replace Spruce and Pine trees from Taiga village.
        RegularTemplatePoolModifier.village(vanillaProvider, "taiga", "decor")
                .replaceTemplate(7, spruceTreePattern)
                .replaceTemplate(8, spruceTreePattern)
                .registerPool(context);
        RegularTemplatePoolModifier.village(vanillaProvider, "taiga", "zombie/decor")
                .replaceTemplate(4, spruceTreePattern)
                .replaceTemplate(5, spruceTreePattern)
                .registerPool(context);
    }

}
