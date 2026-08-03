package com.dtteam.dynamictrees.loot;

import com.dtteam.dynamictrees.DynamicTrees;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.function.Consumer;

/**
 * @author Harley O'Connor
 */
public final class DTLootParameterSets {

    public static final ContextKeySet LEAVES = register("leaves", builder ->
            builder.required(LootContextParams.BLOCK_STATE)
                    .required(DTLootContextParams.SPECIES)
                    .required(DTLootContextParams.SEASONAL_SEED_DROP_FACTOR)
                    .required(LootContextParams.TOOL)
                    .optional(LootContextParams.EXPLOSION_RADIUS)
    );

    public static final ContextKeySet LEAVES_BLOCK = register("leaves_block", builder ->
            builder.required(LootContextParams.BLOCK_STATE)
                    .required(LootContextParams.ORIGIN)
                    .required(LootContextParams.TOOL)
                    .optional(LootContextParams.THIS_ENTITY)
                    .optional(LootContextParams.BLOCK_ENTITY)
                    .optional(LootContextParams.EXPLOSION_RADIUS)
                    .required(DTLootContextParams.SPECIES)
                    .required(DTLootContextParams.SEASONAL_SEED_DROP_FACTOR)
    );

    public static final ContextKeySet VOLUNTARY = register("voluntary", builder ->
            builder.required(LootContextParams.BLOCK_STATE)
                    .required(DTLootContextParams.SEASONAL_SEED_DROP_FACTOR)
                    .required(DTLootContextParams.FERTILITY)
    );

    public static final ContextKeySet BRANCHES = register("branches", builder ->
            builder.required(LootContextParams.TOOL)
                    .required(DTLootContextParams.SPECIES)
                    .required(DTLootContextParams.VOLUME)
                    .optional(LootContextParams.EXPLOSION_RADIUS)
    );

    private static ContextKeySet register(String path, Consumer<ContextKeySet.Builder> builderConsumer) {
        final ContextKeySet.Builder builder = new ContextKeySet.Builder();
        builderConsumer.accept(builder);

        final ContextKeySet paramSet = builder.build();
        LootContextParamSets.REGISTRY.put(DynamicTrees.location(path), paramSet);

        return paramSet;
    }

    /** Invoked to initialise static fields. */
    public static void load() {}

}
