package com.dtteam.dynamictrees.data.provider;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * @author Harley O'Connor
 */
public class DTLootTableProvider extends LootTableProvider {

    public DTLootTableProvider(PackOutput output, String modId, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Set.of(),
                List.of(new SubProviderEntry(a->new DTBlockLootSubProvider(a, modId), LootContextParamSets.BLOCK)),
                registries);
    }


}
