package com.dtteam.dynamictrees.platform.services;

import com.dtteam.dynamictrees.api.registry.AbstractRegistry;
import com.dtteam.dynamictrees.api.registry.RegistryEntry;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.api.resource.TreeResourceManager;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IEventHelper {

    <V extends RegistryEntry<V>> void postRegistryEvent(AbstractRegistry<V> registry);
    <V extends RegistryEntry<V>> void postTypedRegistryEvent(TypedRegistry<V> registry);
    void postAddResourceLoadersEvent(TreeResourceManager resourceManager);
    //returns true when cancelled
    boolean onTransitionSaplingToTree(Species species, Level level, BlockPos pos);
    void postJsonDeserializerRegistryEvent();
}
