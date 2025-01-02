package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.api.registry.AbstractRegistry;
import com.dtteam.dynamictrees.api.registry.RegistryEntry;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.api.resource.TreeResourceManager;
import com.dtteam.dynamictrees.api.resource.loading.StagedApplierResourceLoader;
import com.dtteam.dynamictrees.deserialization.PropertyAppliers;
import com.dtteam.dynamictrees.item.Seed;
import com.dtteam.dynamictrees.platform.services.IEventHelper;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;

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
    public void postSpeciesPostGenerationEvent(PostGenerationContext context) {

    }

    @Override
    public boolean postTransitionSaplingToTreeEvent(Species species, Level level, BlockPos pos) {
        return false;
    }

    @Override
    public Seed.VoluntaryPlantEventResult postSeedVoluntaryPlantEvent(ItemEntity entityItem, Species species, BlockPos pos, boolean willPlant) {
        return new Seed.VoluntaryPlantEventResult(false, false);
    }

}
