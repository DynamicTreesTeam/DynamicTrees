package com.ferreusveritas.dynamictrees.worldgen.structure;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;

/**
 * @author Harley O'Connor
 */
public interface PatternModifier {

    PatternModifier NULL = new PatternModifier() {
        @Override public PatternModifier replacePiece(int index, Pair<JigsawPiece, Integer> rawPiece) { return this; }
        @Override public PatternModifier removePiece(int index) {
            return this;
        }
        @Override public void removeAllPieces() {}
    };

    PatternModifier replacePiece(int index, Pair<JigsawPiece, Integer> rawPiece);

    PatternModifier removePiece(int index);

    void removeAllPieces();

}
