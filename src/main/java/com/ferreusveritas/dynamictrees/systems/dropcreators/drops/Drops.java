package com.ferreusveritas.dynamictrees.systems.dropcreators.drops;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Random;

/**
 * @author Harley O'Connor
 */
@FunctionalInterface
public interface Drops {

    Codec<Item> ITEM_CODEC = ResourceLocation.CODEC.comapFlatMap(registryName -> {
        final Item item = ForgeRegistries.ITEMS.getValue(registryName);
        return item == null ? DataResult.error("Could not find item for registry name \"" + registryName + "\".") :
                DataResult.success(item);
    }, Item::getRegistryName);

    Drops NONE = (drops, random, fortune) -> {};

    void appendDrops(List<ItemStack> drops, Random random, int fortune);

    static int getChance(int fortune, int baseChance) {
        if (baseChance <= 1) return baseChance;
        int chance = baseChance;
        if (fortune > 0) {
            chance -= 10 << fortune;
            if (chance < 40) {
                chance = 40;
            }
        }
        return chance;
    }

}
