package com.ferreusveritas.dynamictrees.worldgen.structure;

import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.template.ProcessorLists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Harley O'Connor
 */
public final class VillageTreeReplacement {

    public static final Logger LOGGER = LogManager.getLogger();

    public static void replaceTreesFromVanillaVillages() {
        // Replace Oak tree in Plains village town center.
        RegularPatternModifier.village("plains", "town_centers").replaceTemplate(3,
                JigsawPiece.legacy("dynamictrees:village/plains/town_centers/plains_meeting_point_3",
                        ProcessorLists.MOSSIFY_70_PERCENT).apply(JigsawPattern.PlacementBehaviour.RIGID)
        ).replaceTemplate(7,
                JigsawPiece.legacy("dynamictrees:village/plains/town_centers/plains_meeting_point_3",
                        ProcessorLists.ZOMBIE_PLAINS).apply(JigsawPattern.PlacementBehaviour.RIGID)
        );

        // Replace Oak trees from Plains village.
        final TreeJigsawPiece oakTreePattern = new TreeJigsawPiece(Species.REGISTRY.get(DTTrees.OAK),
                JigsawPattern.PlacementBehaviour.TERRAIN_MATCHING);
        RegularPatternModifier.village("plains", "trees").replaceTemplate(0, oakTreePattern);
        RegularPatternModifier.village("plains", "decor").replaceTemplate(1, oakTreePattern);
        RegularPatternModifier.village("plains", "zombie/decor").replaceTemplate(1, oakTreePattern);

        // Replace Acacia trees from Savanna village.
        final TreeJigsawPiece acaciaTreePattern = new TreeJigsawPiece(Species.REGISTRY.get(DTTrees.ACACIA),
                JigsawPattern.PlacementBehaviour.TERRAIN_MATCHING);
        RegularPatternModifier.village("savanna", "trees").replaceTemplate(0, acaciaTreePattern);
        RegularPatternModifier.village("savanna", "decor").replaceTemplate(1, acaciaTreePattern);
        RegularPatternModifier.village("savanna", "zombie/decor").replaceTemplate(1, acaciaTreePattern);

        // Replace Spruce trees from Snowy village.
        final TreeJigsawPiece spruceTreePattern = new TreeJigsawPiece(Species.REGISTRY.get(DTTrees.SPRUCE),
                JigsawPattern.PlacementBehaviour.TERRAIN_MATCHING);
        RegularPatternModifier.village("snowy", "trees").replaceTemplate(0, spruceTreePattern);
        RegularPatternModifier.village("snowy", "decor").replaceTemplate(3, spruceTreePattern);
        RegularPatternModifier.village("snowy", "zombie/decor").replaceTemplate(3, spruceTreePattern);

        // Replace Spruce and Pine trees from Taiga village.
        RegularPatternModifier.village("taiga", "decor")
                .replaceTemplate(7, spruceTreePattern)
                .replaceTemplate(8, spruceTreePattern);
        RegularPatternModifier.village("taiga", "zombie/decor")
                .replaceTemplate(4, spruceTreePattern)
                .replaceTemplate(5, spruceTreePattern);
    }

}
