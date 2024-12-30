package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.api.registry.*;
import com.dtteam.dynamictrees.platform.services.IRegistryHelper;
import net.neoforged.fml.ModLoader;

public class NeoForgeRegistryHelper implements IRegistryHelper {

    @Override
    public <V extends RegistryEntry<V>> void postRegistryEvent(AbstractRegistry<V> registry) {
        ModLoader.postEvent(new RegistryEvent<V>(registry));
    }

    @Override
    public <V extends RegistryEntry<V>> void postTypedRegistryEvent(TypedRegistry<V> registry) {
        ModLoader.postEvent(new TypeRegistryEvent<V>(registry));
    }

    @Override
    public RegistryHandler newRegistryHandler(String modid) {
        return new NeoForgeRegistryHandler(modid);
    }

}