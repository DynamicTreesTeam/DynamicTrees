package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.DynamicTreesAddonEntrypoint;
import com.dtteam.dynamictrees.api.registry.AbstractRegistry;
import com.dtteam.dynamictrees.api.registry.RegistryEntry;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.api.resource.TreeResourceManager;
import com.dtteam.dynamictrees.api.resource.loading.StagedApplierResourceLoader;
import com.dtteam.dynamictrees.api.worldgen.PoissonDiscProvider;
import com.dtteam.dynamictrees.deserialization.JsonPropertyAppliers;
import com.dtteam.dynamictrees.deserialization.PropertyAppliers;
import com.dtteam.dynamictrees.item.Seed;
import com.dtteam.dynamictrees.platform.services.IEventHelper;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.dtteam.dynamictrees.tree.species.Species;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

public class FabricEventHelper implements IEventHelper {

    @Override
    public <V extends RegistryEntry<V>> void postRegistryEvent(AbstractRegistry<V> registry) {
    }

    @Override
    public <V extends RegistryEntry<V>> void postTypedRegistryEvent(TypedRegistry<V> registry) {
    }

    @Override
    public void postAddResourceLoadersEventPre(TreeResourceManager resourceManager) {
        for (EntrypointContainer<DynamicTreesAddonEntrypoint> container : FabricLoader.getInstance().getEntrypointContainers("dynamictrees", DynamicTreesAddonEntrypoint.class)) {
            try {
                container.getEntrypoint().onAddResourceLoaders(resourceManager);
            } catch (Throwable e) {
                DynamicTrees.LOG.error("Failed to invoke Dynamic Trees addon resource loader for mod: {}", container.getProvider().getMetadata().getId(), e);
            }
        }
    }

    @Override
    public void postAddResourceLoadersEventPost(TreeResourceManager resourceManager) {
    }

    @Override
    public void postJsonDeserializerRegistryEvent() {
    }

    @Override
    public <O, I> void postApplierEvent(StagedApplierResourceLoader.ApplierStage stage, PropertyAppliers<O, I> appliers, String identifier) {
    }

    @Override
    public <O> void postBiomeEntryApplierEvent(JsonPropertyAppliers<O> appliers, String identifier) {
    }

    @Override
    public <O> void postCancellationApplierEvent(JsonPropertyAppliers<O> appliers, String identifier) {
    }

    @Override
    public void postSpeciesPostGenerationEvent(PostGenerationContext context) {
    }

    @Override
    public boolean postTransitionSaplingToTreeEvent(Species species, Level level, BlockPos pos) {
        return false;
    }

    @Override
    public boolean canCropGrow(Level level, BlockPos pos, BlockState state, boolean doGrow) {
        return doGrow;
    }

    @Override
    public void cropGrowPost(Level level, BlockPos pos, BlockState state) {
    }

    @Override
    public Species.BiomeSuitabilityEventResult postBiomeSuitabilityEvent(Level level, Biome biome, Species species, BlockPos pos) {
        return new Species.BiomeSuitabilityEventResult(false, 0.0f);
    }

    @Override
    public Seed.VoluntaryPlantEventResult postSeedVoluntaryPlantEvent(ItemEntity entityItem, Species species, BlockPos pos, boolean willPlant) {
        return new Seed.VoluntaryPlantEventResult(false, willPlant);
    }

    @Override
    public PoissonDiscProvider postPoissonDiscProviderCreateEvent(LevelAccessor level, PoissonDiscProvider poissonDiscProvider) {
        return poissonDiscProvider;
    }

}
