package com.ferreusveritas.dynamictrees.data;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;

import java.util.function.Consumer;

/**
 * @author Harley O'Connor
 */
public final class DTLootParameterSets {

    public static final LootParameterSet HARVEST = register("harvest", builder ->
            builder.required(LootParameters.BLOCK_STATE).required(DTLootParameters.SPECIES).required(DTLootParameters.FERTILITY).required(DTLootParameters.FORTUNE));

    public static final LootParameterSet VOLUNTARY = register("voluntary", builder ->
            builder.required(LootParameters.BLOCK_STATE).required(DTLootParameters.SPECIES).required(DTLootParameters.FERTILITY));

    public static final LootParameterSet LEAVES = register("leaves", builder ->
            builder.required(LootParameters.BLOCK_STATE).required(DTLootParameters.SPECIES).required(DTLootParameters.FORTUNE));

    public static final LootParameterSet LOGS = register("logs", builder ->
            builder.required(LootParameters.BLOCK_STATE).required(DTLootParameters.SPECIES).required(DTLootParameters.LOGS_AND_STICKS));

    private static LootParameterSet register(String path, Consumer<LootParameterSet.Builder> builderConsumer) {
        final LootParameterSet.Builder builder = new LootParameterSet.Builder();
        builderConsumer.accept(builder);

        final LootParameterSet paramSet = builder.build();
        LootParameterSets.REGISTRY.put(DynamicTrees.resLoc(path), paramSet);

        return paramSet;
    }

}
