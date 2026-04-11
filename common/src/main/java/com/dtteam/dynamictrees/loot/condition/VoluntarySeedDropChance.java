package com.dtteam.dynamictrees.loot.condition;

import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.loot.DTLootContextParams;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * @author Harley O'Connor
 */
public final class VoluntarySeedDropChance implements LootItemCondition {

    public static final MapCodec<VoluntarySeedDropChance> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance
                    .group(Codec.FLOAT.fieldOf("rarity").forGetter((c)->c.rarity))
                    .apply(instance, VoluntarySeedDropChance::new));

    private final float rarity;

    public VoluntarySeedDropChance(float rarity) {
        this.rarity = rarity;
    }

    @Override
    public MapCodec<? extends LootItemCondition> codec() {
        return CODEC;
    }

    //    @Override
//    public LootItemConditionType getType() {
//        return DTRegistries.VOLUNTARY_SEED_DROP_CHANCE.get();
//    }

    @Override
    public boolean test(LootContext context) {
        final Float seasonalSeedDropFactor = context.getOptionalParameter(DTLootContextParams.SEASONAL_SEED_DROP_FACTOR);
        assert seasonalSeedDropFactor != null;
        double minimumDropRate = DTConfigs.SERVER.minSeasonalVoluntarySeedDropRate.get();
        double adjustedSeasonalSeedDropFactor = Math.min(seasonalSeedDropFactor + minimumDropRate, 1.0F);
        return rarity * DTConfigs.SERVER.voluntarySeedDropRate.get() * adjustedSeasonalSeedDropFactor > context.getRandom().nextFloat();
    }

    public static LootItemCondition.Builder voluntarySeedDropChance() {
        return () -> new VoluntarySeedDropChance(1.0F);
    }

    public static LootItemCondition.Builder voluntarySeedDropChance(float rarity) {
        return () -> new VoluntarySeedDropChance(rarity);
    }

}
