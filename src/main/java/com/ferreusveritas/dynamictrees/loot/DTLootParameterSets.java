package com.ferreusveritas.dynamictrees.loot;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.function.Consumer;

/**
 * @author Harley O'Connor
 */
public final class DTLootParameterSets {

    public static final LootContextParamSet LEAVES = register("leaves", builder ->
            builder.required(LootContextParams.BLOCK_STATE)
                    .required(DTLootContextParams.SPECIES)
                    .required(DTLootContextParams.SEASONAL_SEED_DROP_FACTOR)
                    .required(LootContextParams.TOOL)
                    .optional(LootContextParams.EXPLOSION_RADIUS)
    );

    public static final LootContextParamSet VOLUNTARY = register("voluntary", builder ->
            builder.required(LootContextParams.BLOCK_STATE)
                    .required(DTLootContextParams.SEASONAL_SEED_DROP_FACTOR)
                    .required(DTLootContextParams.FERTILITY)
    );

    public static final LootContextParamSet BRANCHES = register("branches", builder ->
            builder.required(LootContextParams.TOOL)
                    .required(DTLootContextParams.SPECIES)
                    .required(DTLootContextParams.VOLUME)
                    .optional(LootContextParams.EXPLOSION_RADIUS)
    );

    private static LootContextParamSet register(String path, Consumer<LootContextParamSet.Builder> builderConsumer) {
        final LootContextParamSet.Builder builder = new LootContextParamSet.Builder();
        builderConsumer.accept(builder);

        final LootContextParamSet paramSet = builder.build();
        LootContextParamSets.REGISTRY.put(DynamicTrees.location(path), paramSet);

        return paramSet;
    }

    /** Invoked to initialise static fields. */
    public static void load() {}

}
