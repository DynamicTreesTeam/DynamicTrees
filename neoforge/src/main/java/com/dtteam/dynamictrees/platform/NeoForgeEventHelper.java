package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.api.registry.AbstractRegistry;
import com.dtteam.dynamictrees.api.registry.RegistryEntry;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.api.resource.TreeResourceManager;
import com.dtteam.dynamictrees.api.resource.loading.StagedApplierResourceLoader;
import com.dtteam.dynamictrees.deserialization.PropertyAppliers;
import com.dtteam.dynamictrees.event.*;
import com.dtteam.dynamictrees.item.Seed;
import com.dtteam.dynamictrees.platform.services.IEventHelper;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.common.NeoForge;

public class NeoForgeEventHelper implements IEventHelper {

    @Override
    public <V extends RegistryEntry<V>> void postRegistryEvent(AbstractRegistry<V> registry) {
        ModLoader.postEvent(new RegistryEvent<>(registry));
    }

    @Override
    public <V extends RegistryEntry<V>> void postTypedRegistryEvent(TypedRegistry<V> registry) {
        ModLoader.postEvent(new TypeRegistryEvent<V>(registry));
    }

    @Override
    public void postAddResourceLoadersEvent(TreeResourceManager resourceManager) {
        ModLoader.postEvent(new AddResourceLoadersEvent(resourceManager));
    }

    @Override
    public void postJsonDeserializerRegistryEvent() {
        ModLoader.postEvent(new JsonDeserializerRegistryEvent());
    }

    @Override
    public <O, I> void postApplierEvent(StagedApplierResourceLoader.ApplierStage stage, PropertyAppliers<O, I> appliers, String identifier) {
        switch (stage){
            case LOAD -> ModLoader.postEvent(new ApplierRegistryEvent.Load<>(appliers, identifier));
            case GATHER_DATA -> ModLoader.postEvent(new ApplierRegistryEvent.GatherData<>(appliers, identifier));
            case SETUP -> ModLoader.postEvent(new ApplierRegistryEvent.Setup<>(appliers, identifier));
            case RELOAD -> ModLoader.postEvent(new ApplierRegistryEvent.Reload<>(appliers, identifier));
            case COMMON -> ModLoader.postEvent(new ApplierRegistryEvent.Common<>(appliers, identifier));
        }
    }

    @Override
    public void postSpeciesPostGenerationEvent(PostGenerationContext context) {
        NeoForge.EVENT_BUS.post(new SpeciesPostGenerationEvent(context.level(), context.species(), context.pos(), context.endPoints(), context.initialDirtState()));
    }

    ///////////////////////////////////////////
    //CANCELLABLE EVENTS
    ///////////////////////////////////////////

    public boolean postTransitionSaplingToTreeEvent(Species species, Level level, BlockPos pos) {
        return NeoForge.EVENT_BUS.post(new TransitionSaplingToTreeEvent(species, level, pos)).isCanceled();
    }

    public Seed.VoluntaryPlantEventResult postSeedVoluntaryPlantEvent (ItemEntity entityItem, Species species, BlockPos pos, boolean willPlant){
        SeedVoluntaryPlantEvent event = NeoForge.EVENT_BUS.post(new SeedVoluntaryPlantEvent(entityItem, species, pos, willPlant));
        return new Seed.VoluntaryPlantEventResult(event.isCanceled(), event.getWillPlant());
    }

}