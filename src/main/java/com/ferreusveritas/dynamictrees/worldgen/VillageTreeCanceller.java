package com.ferreusveritas.dynamictrees.worldgen;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Harley O'Connor
 */
public final class VillageTreeCanceller {

    public static final Logger LOGGER = LogManager.getLogger();

    public interface PatternExtractor {

        PatternExtractor removePiece(int index);

        void removeAllPieces();

    }

    public static class NullPatternExtractor implements PatternExtractor {

        public static final PatternExtractor INSTANCE = new NullPatternExtractor();

        private NullPatternExtractor() { }
        @Override public PatternExtractor removePiece(int index) { return this; }
        @Override public void removeAllPieces() { }
    }

    public static class RegularPatternExtractor implements PatternExtractor {

        private final JigsawPattern pattern;

        private RegularPatternExtractor(JigsawPattern pattern) {
            this.pattern = pattern;
        }

        @Override
        public PatternExtractor removePiece(int index) {
            pattern.rawTemplates.remove(index);
            pattern.templates.remove(index);
            return this;
        }

        @Override
        public void removeAllPieces() {
            pattern.rawTemplates.clear();
            pattern.templates.clear();
        }

        public static PatternExtractor village(String villageType, String patternGroup) {
            ResourceLocation patternName = new ResourceLocation("village/" + villageType + "/" + patternGroup);
            JigsawPattern pattern = WorldGenRegistries.TEMPLATE_POOL.get(patternName);
            if (pattern == null) {
                LOGGER.error("Could not find JigsawPattern with name \"{}\".", patternName);
                return NullPatternExtractor.INSTANCE;
            }
            return new RegularPatternExtractor(pattern);
        }

    }

    public static void removeTreesFromVanillaVillages() {
        // Remove Oak trees from Plains village.
        RegularPatternExtractor.village("plains", "trees").removeAllPieces();
        RegularPatternExtractor.village("plains", "decor").removePiece(1);
        RegularPatternExtractor.village("plains", "zombie/decor").removePiece(1);

        // Remove Acacia trees from Savanna village.
        RegularPatternExtractor.village("savanna", "trees").removeAllPieces();
        RegularPatternExtractor.village("savanna", "decor").removePiece(1);
        RegularPatternExtractor.village("savanna", "zombie/decor").removePiece(1);

        // Remove Spruce trees from Snowy village.
        RegularPatternExtractor.village("snowy", "trees").removeAllPieces();
        RegularPatternExtractor.village("snowy", "decor").removePiece(3);
        RegularPatternExtractor.village("snowy", "zombie/decor").removePiece(3);

        // Remove Spruce and Pine trees from Taiga village.
        RegularPatternExtractor.village("taiga", "decor")
                .removePiece(7)
                .removePiece(8);
        RegularPatternExtractor.village("taiga", "zombie/decor")
                .removePiece(4)
                .removePiece(5);
    }

}
