package com.dtteam.dynamictrees.data.provider;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * @author Harley O'Connor
 */
public class DTLootTableProvider extends LootTableProvider {

    public DTLootTableProvider(PackOutput output, String modId, ExistingFileHelper fileHelper, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Set.of(),
                List.of(new SubProviderEntry(a->new DTBlockLootSubProvider(a, modId, fileHelper), LootContextParamSets.BLOCK)),
                registries);
    }


}
