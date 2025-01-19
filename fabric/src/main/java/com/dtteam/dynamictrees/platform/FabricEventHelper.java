package com.dtteam.dynamictrees.platform;

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
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

public class FabricEventHelper implements IEventHelper {

    @Override
    public <V extends RegistryEntry<V>> void postRegistryEvent(AbstractRegistry<V> registry) {
        //RegistryEvent.EVENT.invoker().
    }

    @Override
    public <V extends RegistryEntry<V>> void postTypedRegistryEvent(TypedRegistry<V> registry) {

    }

    @Override
    public void postAddResourceLoadersEvent(TreeResourceManager resourceManager) {

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
        return false;
    }

    @Override
    public void cropGrowPost(Level level, BlockPos pos, BlockState state) {

    }

    @Override
    public Species.BiomeSuitabilityEventResult postBiomeSuitabilityEvent(Level level, Biome biome, Species species, BlockPos pos) {
        return null;
    }

    @Override
    public Seed.VoluntaryPlantEventResult postSeedVoluntaryPlantEvent(ItemEntity entityItem, Species species, BlockPos pos, boolean willPlant) {
        return new Seed.VoluntaryPlantEventResult(false, false);
    }

    @Override
    public PoissonDiscProvider postPoissonDiscProviderCreateEvent(LevelAccessor level, PoissonDiscProvider poissonDiscProvider) {
        return null;
    }

}
