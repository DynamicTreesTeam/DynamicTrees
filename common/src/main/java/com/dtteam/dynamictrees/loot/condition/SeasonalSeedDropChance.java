package com.dtteam.dynamictrees.loot.condition;

import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.loot.DTLootContextParams;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import com.mojang.serialization.MapCodec;

/**
 * @author Harley O'Connor
 */
public final class SeasonalSeedDropChance implements LootItemCondition {

    public static final MapCodec<SeasonalSeedDropChance> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance
                    .group(Codec.EMPTY.forGetter(a-> Unit.INSTANCE))
                    .apply(instance, SeasonalSeedDropChance::new));

    private SeasonalSeedDropChance(Unit unit) {
    }

    public MapCodec<? extends LootItemCondition> codec() {
        return DTRegistries.SEASONAL_SEED_DROP_CHANCE.get();
    }

    public boolean test(LootContext context) {
        Float seasonalSeedDropFactor = context.getOptionalParameter(DTLootContextParams.SEASONAL_SEED_DROP_FACTOR);
        assert seasonalSeedDropFactor != null;
        double minimumDropRate = DTConfigs.SERVER.minSeasonalLeavesSeedDropRate.get();
        double adjustedSeasonalSeedDropFactor = Math.min(seasonalSeedDropFactor + minimumDropRate, 1.0F);
        return DTConfigs.SERVER.leavesSeedDropRate.get() * adjustedSeasonalSeedDropFactor > context.getRandom().nextFloat();
    }

    public static LootItemCondition.Builder seasonalSeedDropChance() {
        return () -> new SeasonalSeedDropChance(Unit.INSTANCE);
    }

}
