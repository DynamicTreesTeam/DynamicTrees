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

    public PatternModifier replaceTemplate(int index, JigsawPiece newTemplate) {
        Pair<JigsawPiece, Integer> removedRawTemplate = pattern.rawTemplates.remove(index);
        pattern.rawTemplates.add(index, Pair.of(newTemplate, removedRawTemplate.getSecond()));
        pattern.templates.replaceAll(template -> {
            if (template == removedRawTemplate.getFirst()) {
                return newTemplate;
            }
            return template;
        });
        return this;
    }

    @Override
    public PatternModifier removeTemplate(int index) {
        Pair<JigsawPiece, Integer> removedRawTemplate = pattern.rawTemplates.remove(index);
        pattern.templates.removeIf(template -> template == removedRawTemplate.getFirst());
        return this;
    }

    @Override
    public void removeAllTemplates() {
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
