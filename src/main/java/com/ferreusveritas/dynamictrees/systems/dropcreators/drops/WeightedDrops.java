package com.ferreusveritas.dynamictrees.systems.dropcreators.drops;

import com.ferreusveritas.dynamictrees.util.MathHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.ai.behavior.WeightedList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Can drop only one item picked randomly from the selection of all items with weighted odds.
 *
 * @author Max Hyper
 */
public final class WeightedDrops implements Drops {

    public static final Codec<WeightedDrops> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(ITEM_CODEC, Codec.INT).fieldOf("items")
                            .forGetter(WeightedDrops::getItems),
                    Codec.FLOAT.optionalFieldOf("rarity", 1f)
                            .forGetter(WeightedDrops::getRarity),
                    Codec.INT.optionalFieldOf("chance", 200)
                            .forGetter(WeightedDrops::getBaseChance),
                    Codec.INT.optionalFieldOf("min_count", 1)
                            .forGetter(WeightedDrops::getMinAttempts),
                    Codec.INT.optionalFieldOf("max_count", 1)
                            .forGetter(WeightedDrops::getMaxAttempts)
            ).apply(instance, WeightedDrops::new)
    );

    /**
     * A {@link Map} of {@link Item}s to drop and their weight.
     */
    private final Map<Item, Integer> items;

    /**
     * The rarity of the item. This is what the chance will be divided by.
     */
    private final float rarity;

    /**
     * The base chance of dropping an item. This will be altered depending on the fortune level.
     */
    private final int baseChance;

    /**
     * The minimum times an item is attempted to be added to the drops.
     */
    private final int minAttempts;

    /**
     * The maximum times an item is attempted to be added to the drops.
     */
    private final int maxAttempts;

    public WeightedDrops(Map<Item, Integer> items, float rarity, int baseChance, int minAttempts, int maxAttempts) {
        this.items = items;
        this.rarity = rarity;
        this.baseChance = baseChance;
        this.minAttempts = minAttempts;
        this.maxAttempts = maxAttempts;
    }

    public Map<Item, Integer> getItems() {
        return items;
    }

    public float getRarity() {
        return rarity;
    }

    public int getBaseChance() {
        return baseChance;
    }

    public int getMinAttempts() {
        return minAttempts;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void addItem(final Item item, final int weight) {
        this.items.put(item, weight);
    }

    @Override
    public void appendDrops(List<ItemStack> drops, Random random, int fortune) {
        final int chance = Drops.getChance(fortune, this.baseChance);
        final int attempts = MathHelper.randomBetween(random, this.minAttempts, this.maxAttempts);

        WeightedList<Item> list = new WeightedList<>();
        this.items.forEach(list::add);
        for (int i = 0; i < attempts; i++) {
            if (random.nextInt(Math.max((int) (chance / this.rarity), 1)) == 0) {
                drops.add(new ItemStack(list.getOne(random)));
            }
        }

    }

}
