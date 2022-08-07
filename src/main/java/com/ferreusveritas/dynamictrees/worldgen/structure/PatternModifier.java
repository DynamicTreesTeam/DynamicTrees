package com.ferreusveritas.dynamictrees.worldgen.structure;

import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;

/**
 * @author Harley O'Connor
 */
public interface PatternModifier {

    PatternModifier NULL = new PatternModifier() {
        @Override public PatternModifier replaceTemplate(int index, JigsawPiece template) { return this; }
        @Override public PatternModifier removeTemplate(int index) { return this; }
        @Override public void removeAllTemplates() {}
    };

    PatternModifier replaceTemplate(int index, JigsawPiece template);

    PatternModifier removeTemplate(int index);

    void removeAllTemplates();

}
