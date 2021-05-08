package com.ferreusveritas.dynamictrees.systems.dropcreators.drops;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Random;

/**
 * @author Harley O'Connor
 */
public final class NormalDrops implements Drops {

    public static final Codec<NormalDrops> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                Codec.list(ItemStack.CODEC).fieldOf("items")
                        .forGetter(NormalDrops::getItems),
                Codec.FLOAT.optionalFieldOf("rarity", 1f)
                        .forGetter(NormalDrops::getRarity),
                Codec.INT.optionalFieldOf("chance", 200)
                        .forGetter(NormalDrops::getBaseChance)
        ).apply(instance, NormalDrops::new)
    );

    /** A {@link List} of {@link ItemStack}s to drop. */
    private final List<ItemStack> items;

    /** The rarity of the item. This is what the chance will be divided by. */
    private final float rarity;

    /** The base chance of dropping each item. This will be altered depending on the fortune level. */
    private final int baseChance;

    public NormalDrops(List<ItemStack> items, float rarity, int baseChance) {
        this.items = items;
        this.rarity = rarity;
        this.baseChance = baseChance;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public float getRarity() {
        return rarity;
    }

    public int getBaseChance() {
        return baseChance;
    }

    @Override
    public List<ItemStack> appendDrops(List<ItemStack> drops, Random random, int fortune) {
        final int chance = this.getChance(fortune, this.baseChance);

        this.items.forEach(stack -> {
            if (random.nextInt((int) (chance / this.rarity)) == 0) {
                drops.add(stack.copy());
            }
        });
        return drops;
    }

}
