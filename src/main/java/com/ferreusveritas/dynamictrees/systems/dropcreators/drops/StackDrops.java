package com.ferreusveritas.dynamictrees.systems.dropcreators.drops;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Random;

/**
 * Each item individually attempts to drop with a global rarity. Can drop multiple items, or none.
 *
 * @author Harley O'Connor
 */
public final class StackDrops implements Drops {

    public static final Codec<StackDrops> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    //The item stack codec requires the "Count" key to be capitalized. No clue why.
                    Codec.list(ItemStack.CODEC).fieldOf("items")
                            .forGetter(StackDrops::getItems),
                    Codec.FLOAT.optionalFieldOf("rarity", 1f)
                            .forGetter(StackDrops::getRarity),
                    Codec.INT.optionalFieldOf("chance", 200)
                            .forGetter(StackDrops::getBaseChance)
            ).apply(instance, StackDrops::new)
    );

    /**
     * A {@link List} of {@link ItemStack}s to drop.
     */
    private final List<ItemStack> items;

    /**
     * The rarity of the item. This is what the chance will be divided by.
     */
    private final float rarity;

    /**
     * The base chance of dropping each item. This will be altered depending on the fortune level.
     */
    private final int baseChance;

    public StackDrops(List<ItemStack> items, float rarity, int baseChance) {
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
    public void appendDrops(List<ItemStack> drops, Random random, int fortune) {
        final int chance = Drops.getChance(fortune, this.baseChance);

        if (this.rarity > 0) {
            this.items.forEach(stack -> {
                if (random.nextInt(Math.max((int) (chance / this.rarity), 1)) == 0) {
                    drops.add(stack.copy());
                }
            });
        }
    }

    public static Drops create(float rarity, int baseChance, ItemStack... stacks) {
        return new StackDrops(Lists.newLinkedList(Lists.newArrayList(stacks)), rarity, baseChance);
    }

}
