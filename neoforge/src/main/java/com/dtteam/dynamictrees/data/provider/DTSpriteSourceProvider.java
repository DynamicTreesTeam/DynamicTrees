package com.dtteam.dynamictrees.data.provider;

import com.dtteam.dynamictrees.api.registry.Registry;
import com.dtteam.dynamictrees.client.ThickBranchRingsSource;
import com.dtteam.dynamictrees.tree.family.Family;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.data.SpriteSourceProvider;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DTSpriteSourceProvider extends SpriteSourceProvider {

    private final String modId;
    private final List<Registry<Family>> registries;

    public DTSpriteSourceProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId,
                                  Registry<?>... registries) {
        super(output, lookupProvider, modId);
        this.modId = modId;
        this.registries = ImmutableList.copyOf(castToFamilyRegistries(registries));
    }

    @SuppressWarnings("unchecked")
    private static List<Registry<Family>> castToFamilyRegistries(Registry<?>... registries) {
        return Arrays.stream(registries)
                .filter(registry -> Family.class.isAssignableFrom(registry.getType()))
                .map(registry -> (Registry<Family>) registry)
                .toList();
    }

    @Override
    protected void gather() {
        SourceList blockSourceList = atlas(Identifier.withDefaultNamespace("blocks"));
        this.registries.forEach(registry ->
                registry.dataGenerationStream(this.modId).forEach(family ->
                        family.topBranchTextureLocations().forEach(location ->
                                blockSourceList.addSource(new ThickBranchRingsSource(location)))
                )
        );
    }
}
