package com.ferreusveritas.dynamictrees.data;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.function.Consumer;

/**
 * @author Harley O'Connor
 */
public final class DTLootParameterSets {

    public static final LootContextParamSet HARVEST = register("harvest", builder ->
            builder.required(LootContextParams.BLOCK_STATE)
                    .required(DTLootParameters.SPECIES)
                    .required(DTLootParameters.FERTILITY)
                    .required(DTLootParameters.FORTUNE)
    );

    public static final LootContextParamSet VOLUNTARY = register("voluntary", builder ->
            builder.required(LootContextParams.BLOCK_STATE)
                    .required(DTLootParameters.SPECIES)
                    .required(DTLootParameters.FERTILITY)
    );

    public static final LootContextParamSet LEAVES = register("leaves", builder ->
            builder.required(LootContextParams.BLOCK_STATE)
                    .required(LootContextParams.TOOL)
                    .required(DTLootParameters.SPECIES)
                    .required(DTLootParameters.FORTUNE)
    );

    public static final LootContextParamSet LOGS = register("logs", builder ->
            builder.required(LootContextParams.BLOCK_STATE)
                    .required(LootContextParams.TOOL)
                    .required(DTLootParameters.SPECIES)
                    .required(DTLootParameters.LOGS_AND_STICKS)
    );

    private static LootContextParamSet register(String path, Consumer<LootContextParamSet.Builder> builderConsumer) {
        final LootContextParamSet.Builder builder = new LootContextParamSet.Builder();
        builderConsumer.accept(builder);

        final LootContextParamSet paramSet = builder.build();
        LootContextParamSets.REGISTRY.put(DynamicTrees.resLoc(path), paramSet);

        return paramSet;
    }

}
