package com.ferreusveritas.dynamictrees.loot;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;

import java.util.function.Consumer;

/**
 * @author Harley O'Connor
 */
public final class DTLootParameterSets {

    public static final LootParameterSet LEAVES = register("leaves", builder ->
            builder.required(LootParameters.BLOCK_STATE)
                    .required(DTLootParameters.SEASONAL_SEED_DROP_FACTOR)
                    .required(LootParameters.TOOL)
                    .optional(LootParameters.EXPLOSION_RADIUS)
    );

    public static final LootParameterSet VOLUNTARY = register("voluntary", builder ->
            builder.required(LootParameters.BLOCK_STATE)
                    .required(DTLootParameters.SEASONAL_SEED_DROP_FACTOR)
                    .required(DTLootParameters.FERTILITY)
    );

    public static final LootParameterSet BRANCHES = register("branches", builder ->
            builder.required(LootParameters.TOOL)
                    .required(DTLootParameters.VOLUME)
                    .optional(LootParameters.EXPLOSION_RADIUS)
    );

    private static LootParameterSet register(String path, Consumer<LootParameterSet.Builder> builderConsumer) {
        final LootParameterSet.Builder builder = new LootParameterSet.Builder();
        builderConsumer.accept(builder);

        final LootParameterSet paramSet = builder.build();
        LootParameterSets.REGISTRY.put(DynamicTrees.resLoc(path), paramSet);

        return paramSet;
    }

    /** Invoked to initialise static fields. */
    public static void load() {}

}
