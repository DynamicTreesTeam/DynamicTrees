package com.ferreusveritas.dynamictrees.worldgen.structure;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;

/**
 * @author Harley O'Connor
 */
public class RegularPatternModifier implements PatternModifier {

    private final JigsawPattern pattern;

    private RegularPatternModifier(JigsawPattern pattern) {
        this.pattern = pattern;
    }

    public PatternModifier replacePiece(int index, Pair<JigsawPiece, Integer> rawPiece) {
        pattern.rawTemplates.remove(index);
        pattern.rawTemplates.add(index, rawPiece);
        pattern.templates.remove(index);
        pattern.templates.add(index, rawPiece.getFirst());
        return this;
    }

    @Override
    public PatternModifier removePiece(int index) {
        pattern.rawTemplates.remove(index);
        pattern.templates.remove(index);
        return this;
    }

    @Override
    public void removeAllPieces() {
        pattern.rawTemplates.clear();
        pattern.templates.clear();
    }

    public static PatternModifier village(String type, String patternGroup) {
        ResourceLocation patternName = new ResourceLocation("village/" + type + "/" + patternGroup);
        JigsawPattern pattern = WorldGenRegistries.TEMPLATE_POOL.get(patternName);
        if (pattern == null) {
            VillageTreeReplacement.LOGGER.error("Could not find JigsawPattern with name {}.", patternName);
            return PatternModifier.NULL;
        }
        return new RegularPatternModifier(pattern);
    }

}
