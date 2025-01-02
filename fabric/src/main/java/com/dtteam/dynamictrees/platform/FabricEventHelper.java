package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.api.registry.AbstractRegistry;
import com.dtteam.dynamictrees.api.registry.RegistryEntry;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.api.resource.TreeResourceManager;
import com.dtteam.dynamictrees.platform.services.IEventHelper;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
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
    public boolean onTransitionSaplingToTree(Species species, Level level, BlockPos pos) {
        return false;
    }


}
