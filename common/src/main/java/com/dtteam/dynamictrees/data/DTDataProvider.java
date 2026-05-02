package com.dtteam.dynamictrees.data;

import net.minecraft.data.DataProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
public interface DTDataProvider extends DataProvider {

    interface Language extends DTDataProvider {
        void addBlock(Supplier<? extends Block> key, String name);

        void addItem(Supplier<? extends Item> key, String name);

        void add(String key, String value);
    }
}
