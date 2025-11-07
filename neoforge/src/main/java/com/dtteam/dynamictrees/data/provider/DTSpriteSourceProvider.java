package com.dtteam.dynamictrees.data.provider;

import com.dtteam.dynamictrees.api.registry.Registry;
import com.dtteam.dynamictrees.client.ThickBranchRingsSource;
import com.dtteam.dynamictrees.tree.family.Family;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SpriteSourceProvider;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DTSpriteSourceProvider extends SpriteSourceProvider {

    private final String modId;
    private final List<Registry<Family>> registries;

    public DTSpriteSourceProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, ExistingFileHelper existingFileHelper,
                                  Registry<?>... registries) {
        super(output, lookupProvider, modId, existingFileHelper);
        this.modId = modId;
        this.registries = ImmutableList.copyOf(castToFamilyRegistries(registries));
    }

    @SuppressWarnings("unchecked")
    private List<Registry<Family>> castToFamilyRegistries(Registry<?>... registries){
        return Arrays.stream(registries)
                .filter(registry->Family.class.isAssignableFrom(registry.getType()))
                .map(registry->(Registry<Family>)registry).toList();
    }

    @Override
    protected void gather() {
        SourceList blockSourceList = atlas(BLOCKS_ATLAS);
        this.registries.forEach(registry ->
                registry.dataGenerationStream(this.modId).forEach(
                        family -> gatherForFamily(family, blockSourceList)
                )
        );
    }

    private void gatherForFamily(Family family, SourceList atlasList){
        family.topBranchTextureLocations().forEach(location ->
                atlasList.addSource(new ThickBranchRingsSource(location)));
    }
}
