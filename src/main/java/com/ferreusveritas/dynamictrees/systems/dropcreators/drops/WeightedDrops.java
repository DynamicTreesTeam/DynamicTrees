package com.ferreusveritas.dynamictrees.systems.dropcreators.drops;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Harley O'Connor
 */
public final class WeightedDrops implements Drops {

    public static final Codec<WeightedDrops> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(ItemStack.CODEC, Codec.INT).fieldOf("items")
                            .forGetter(WeightedDrops::getItems),
                    Codec.FLOAT.fieldOf("rarity").forGetter(WeightedDrops::getRarity),
                    Codec.INT.fieldOf("base_chance").forGetter(WeightedDrops::getBaseChance)
            ).apply(instance, WeightedDrops::new)
    );

    /** A {@link Map} of {@link ItemStack}s to drop and their weight. */
    private final Map<ItemStack, Integer> items;

    /** The rarity of the item. This is what the chance will be divided by. */
    private final float rarity;

    /** The base chance of dropping each item. This will be altered depending on the fortune level. */
    private final int baseChance;

    public WeightedDrops(Map<ItemStack, Integer> items, float rarity, int baseChance) {
        this.items = items;
        this.rarity = rarity;
        this.baseChance = baseChance;
    }

    public Map<ItemStack, Integer> getItems() {
        return items;
    }

    public float getRarity() {
        return rarity;
    }

    public int getBaseChance() {
        return baseChance;
    }

    public void addItem(final ItemStack stack, final int weight) {
        this.items.put(stack, weight);
    }

    public int getTotalWeight() {
        return this.items.values().stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    public List<ItemStack> appendDrops(List<ItemStack> drops, Random random, int fortune) {
        final int chance = this.getChance(fortune, this.baseChance) * this.getTotalWeight();

        this.items.forEach((stack, weight) -> {
                if (random.nextInt((int) ((chance / weight) / this.rarity)) == 0) {
					drops.add(stack.copy());
				}
        });
        return drops;
    }

}
