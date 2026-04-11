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
                    .required(DTContextKeys.SPECIES)
                    .required(DTContextKeys.SEASONAL_SEED_DROP_FACTOR)
                    .required(LootContextParams.TOOL)
                    .optional(LootContextParams.EXPLOSION_RADIUS)
    );

    public static final ContextKeySet VOLUNTARY = register("voluntary", builder ->
            builder.required(LootContextParams.BLOCK_STATE)
                    .required(DTContextKeys.SEASONAL_SEED_DROP_FACTOR)
                    .required(DTContextKeys.FERTILITY)
    );

    public static final ContextKeySet BRANCHES = register("branches", builder ->
            builder.required(LootContextParams.TOOL)
                    .required(DTContextKeys.SPECIES)
                    .required(DTContextKeys.VOLUME)
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
