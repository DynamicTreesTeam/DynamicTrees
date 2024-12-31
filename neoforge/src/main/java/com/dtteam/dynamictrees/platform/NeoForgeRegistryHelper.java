package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.api.registry.*;
import com.dtteam.dynamictrees.init.NeoForgeRegistryLoader;
import com.dtteam.dynamictrees.init.RegistryLoader;
import com.dtteam.dynamictrees.platform.services.IRegistryHelper;
import net.neoforged.fml.ModLoader;

public class NeoForgeRegistryHelper implements IRegistryHelper {

    private static RegistryLoader registriesInstance;
    @Override
    public RegistryLoader getRegistryLoader() {
        if (registriesInstance == null){
            registriesInstance = new NeoForgeRegistryLoader();
        }
        return registriesInstance;
    }

    @Override
    public <V extends RegistryEntry<V>> void postRegistryEvent(AbstractRegistry<V> registry) {
        ModLoader.postEvent(new RegistryEvent<V>(registry));
    }

    @Override
    public <V extends RegistryEntry<V>> void postTypedRegistryEvent(TypedRegistry<V> registry) {
        ModLoader.postEvent(new TypeRegistryEvent<V>(registry));
    }

    @Override
    public RegistryHandler newRegistryHandler() {
        return new NeoForgeRegistryHandler();
    }
    @Override
    public RegistryHandler newRegistryHandler(String modId) {
        return new NeoForgeRegistryHandler(modId);
    }

}