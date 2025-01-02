package com.dtteam.dynamictrees.platform.services;

import com.dtteam.dynamictrees.api.registry.AbstractRegistry;
import com.dtteam.dynamictrees.api.registry.RegistryEntry;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.api.resource.TreeResourceManager;
import com.dtteam.dynamictrees.api.resource.loading.StagedApplierResourceLoader;
import com.dtteam.dynamictrees.deserialization.PropertyAppliers;
import com.dtteam.dynamictrees.item.Seed;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;

public interface IEventHelper {

    <V extends RegistryEntry<V>> void postRegistryEvent(AbstractRegistry<V> registry);
    <V extends RegistryEntry<V>> void postTypedRegistryEvent(TypedRegistry<V> registry);
    void postAddResourceLoadersEvent(TreeResourceManager resourceManager);
    void postJsonDeserializerRegistryEvent();
    <O, I> void postApplierEvent(StagedApplierResourceLoader.ApplierStage stage, PropertyAppliers<O, I> appliers, String identifier);
    void postSpeciesPostGenerationEvent(PostGenerationContext context);
    boolean postTransitionSaplingToTreeEvent(Species species, Level level, BlockPos pos);
    Seed.VoluntaryPlantEventResult postSeedVoluntaryPlantEvent (ItemEntity entityItem, Species species, BlockPos pos, boolean willPlant);
}
