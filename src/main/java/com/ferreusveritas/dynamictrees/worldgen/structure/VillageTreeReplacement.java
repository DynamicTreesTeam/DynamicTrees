package com.ferreusveritas.dynamictrees.worldgen.structure;

import com.mojang.datafixers.util.Pair;
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
        RegularPatternModifier.village("plains", "town_centers").replacePiece(3,
                Pair.of(JigsawPiece.legacy("dynamictrees:village/plains/town_centers/plains_meeting_point_3",
                        ProcessorLists.MOSSIFY_70_PERCENT).apply(JigsawPattern.PlacementBehaviour.RIGID), 50)
        );
        RegularPatternModifier.village("plains", "zombie/town_centers").replacePiece(3,
                Pair.of(JigsawPiece.legacy("dynamictrees:village/plains/town_centers/plains_meeting_point_3",
                        ProcessorLists.ZOMBIE_PLAINS).apply(JigsawPattern.PlacementBehaviour.RIGID), 1)
        );

        removeTreesFromVanillaVillages();
    }

    private static void removeTreesFromVanillaVillages() {
        // Remove Oak trees from Plains village.
        RegularPatternModifier.village("plains", "trees").removeAllPieces();
        RegularPatternModifier.village("plains", "decor").removePiece(1);
        RegularPatternModifier.village("plains", "zombie/decor").removePiece(1);

        // Remove Acacia trees from Savanna village.
        RegularPatternModifier.village("savanna", "trees").removeAllPieces();
        RegularPatternModifier.village("savanna", "decor").removePiece(1);
        RegularPatternModifier.village("savanna", "zombie/decor").removePiece(1);

        // Remove Spruce trees from Snowy village.
        RegularPatternModifier.village("snowy", "trees").removeAllPieces();
        RegularPatternModifier.village("snowy", "decor").removePiece(3);
        RegularPatternModifier.village("snowy", "zombie/decor").removePiece(3);

        // Remove Spruce and Pine trees from Taiga village.
        RegularPatternModifier.village("taiga", "decor")
                .removePiece(7)
                .removePiece(8);
        RegularPatternModifier.village("taiga", "zombie/decor")
                .removePiece(4)
                .removePiece(5);
    }

}
