package com.dtteam.dynamictrees.client;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;

import java.util.LinkedList;
import java.util.List;

public interface GeneratesBlockTintSources {

    default List<BlockTintSource> tintSources (BlockColors blockColors){
        List<BlockTintSource> list = new LinkedList<>();
        int maxIndex = maxTintIndex();
        for (int i=0; i<=maxIndex; i++){
            list.add(generateTintSource(blockColors, i));
        }
        return list;
    }

    default int maxTintIndex() {
        return 0;
    }

    BlockTintSource generateTintSource (BlockColors blockColors, int tintIndex);

}
