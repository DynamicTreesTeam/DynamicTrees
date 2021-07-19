package com.ferreusveritas.dynamictrees.systems.dropcreators.drops;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Harley O'Connor
 */
public final class WeightedDrops implements Drops {

    private static final Codec<Item> ITEM_CODEC = ResourceLocation.CODEC.comapFlatMap(registryName -> {
        final Item item = ForgeRegistries.ITEMS.getValue(registryName);
        return item == null ? DataResult.error("Could not find item for registry name \"" + registryName + "\".") :
                DataResult.success(item);
    }, Item::getRegistryName);

    public static final Codec<WeightedDrops> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(ITEM_CODEC, Codec.INT).fieldOf("items")
                            .forGetter(WeightedDrops::getItems),
                    Codec.FLOAT.fieldOf("rarity").forGetter(WeightedDrops::getRarity),
                    Codec.INT.fieldOf("base_chance").forGetter(WeightedDrops::getBaseChance)
            ).apply(instance, WeightedDrops::new)
    );

    /** A {@link Map} of {@link Item}s to drop and their weight. */
    private final Map<Item, Integer> items;

    /** The rarity of the item. This is what the chance will be divided by. */
    private final float rarity;

    /** The base chance of dropping each item. This will be altered depending on the fortune level. */
    private final int baseChance;

    public WeightedDrops(Map<Item, Integer> items, float rarity, int baseChance) {
        this.items = items;
        this.rarity = rarity;
        this.baseChance = baseChance;
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

    public void addItem(final Item item, final int weight) {
        this.items.put(item, weight);
    }

    public int getTotalWeight() {
        return this.items.values().stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    public void appendDrops(List<ItemStack> drops, Random random, int fortune) {
        final int chance = this.getChance(fortune, this.baseChance) * this.getTotalWeight();

        this.items.forEach((item, weight) -> {
                if (random.nextInt((int) ((chance / weight) / this.rarity)) == 0) {
					drops.add(new ItemStack(item));
				}
        });
    }

}
