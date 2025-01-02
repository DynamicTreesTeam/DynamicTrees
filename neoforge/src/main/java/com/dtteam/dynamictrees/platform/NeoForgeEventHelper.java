package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.event.*;
import com.dtteam.dynamictrees.api.registry.*;
import com.dtteam.dynamictrees.api.resource.TreeResourceManager;
import com.dtteam.dynamictrees.event.handler.EventHandlers;
import com.dtteam.dynamictrees.platform.services.IEventHelper;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.common.NeoForge;

public class NeoForgeEventHelper implements IEventHelper {

    @Override
    public <V extends RegistryEntry<V>> void postRegistryEvent(AbstractRegistry<V> registry) {
        ModLoader.postEvent(new RegistryEvent<V>(registry));
    }

    @Override
    public <V extends RegistryEntry<V>> void postTypedRegistryEvent(TypedRegistry<V> registry) {
        ModLoader.postEvent(new TypeRegistryEvent<V>(registry));
    }

    @Override
    public void postAddResourceLoadersEvent(TreeResourceManager resourceManager) {
        ModLoader.postEvent(new AddResourceLoadersEvent(resourceManager));
    }

    public boolean onTransitionSaplingToTree(Species species, Level level, BlockPos pos) {
        return NeoForge.EVENT_BUS.post(new TransitionSaplingToTreeEvent(species, level, pos)).isCanceled();
    }

    @Override
    public void postJsonDeserializerRegistryEvent() {
        ModLoader.postEvent(new JsonDeserializerRegistryEvent());
    }

}