package com.dtteam.dynamictrees.platform.services;

import com.dtteam.dynamictrees.api.registry.AbstractRegistry;
import com.dtteam.dynamictrees.api.registry.RegistryEntry;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.api.resource.TreeResourceManager;
import com.dtteam.dynamictrees.api.resource.loading.StagedApplierResourceLoader;
import com.dtteam.dynamictrees.api.worldgen.PoissonDiscProvider;
import com.dtteam.dynamictrees.deserialization.JsonPropertyAppliers;
import com.dtteam.dynamictrees.deserialization.PropertyAppliers;
import com.dtteam.dynamictrees.item.Seed;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

public interface IEventHelper {

    <V extends RegistryEntry<V>> void postRegistryEvent(AbstractRegistry<V> registry);
    <V extends RegistryEntry<V>> void postTypedRegistryEvent(TypedRegistry<V> registry);
    void postAddResourceLoadersEventPre(TreeResourceManager resourceManager);
    void postAddResourceLoadersEventPost(TreeResourceManager resourceManager);
    void postJsonDeserializerRegistryEvent();
    <O, I> void postApplierEvent(StagedApplierResourceLoader.ApplierStage stage, PropertyAppliers<O, I> appliers, String identifier);
    <O> void postBiomeEntryApplierEvent(JsonPropertyAppliers<O> appliers, String identifier);
    <O> void postCancellationApplierEvent(JsonPropertyAppliers<O> appliers, String identifier);
    void postSpeciesPostGenerationEvent(PostGenerationContext context);

    boolean postTransitionSaplingToTreeEvent(Species species, Level level, BlockPos pos);
    boolean canCropGrow(Level level, BlockPos pos, BlockState state, boolean doGrow);
    void cropGrowPost(Level level, BlockPos pos, BlockState state);

    Species.BiomeSuitabilityEventResult postBiomeSuitabilityEvent (Level level, Biome biome, Species species, BlockPos pos);
    Seed.VoluntaryPlantEventResult postSeedVoluntaryPlantEvent (ItemEntity entityItem, Species species, BlockPos pos, boolean willPlant);
    PoissonDiscProvider postPoissonDiscProviderCreateEvent (LevelAccessor level, PoissonDiscProvider poissonDiscProvider);
}
