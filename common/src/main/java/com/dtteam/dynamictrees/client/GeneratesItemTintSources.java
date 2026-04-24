package com.dtteam.dynamictrees.client;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemTintSource;

import java.util.LinkedList;
import java.util.List;

public interface GeneratesItemTintSources {

    default List<ItemTintSource> tintSources (BlockColors blockColors){
        List<ItemTintSource> list = new LinkedList<>();
        int maxIndex = maxTintIndex();
        for (int i=0; i<=maxIndex; i++){
            list.add(generateTintSource(blockColors, i));
        }
        return list;
    }

    default int maxTintIndex() {
        return 0;
    }

    ItemTintSource generateTintSource (BlockColors blockColors, int tintIndex);

}
