package com.ferreusveritas.dynamictrees.api.resource.loading;

import com.ferreusveritas.dynamictrees.api.resource.loading.ResourceLoader;
import com.ferreusveritas.dynamictrees.api.treepacks.ApplierRegistryEvent;
import net.minecraftforge.fml.ModLoader;

/**
 * @author Harley O'Connor
 */
public interface ApplierResourceLoader<P> extends ResourceLoader<P> {

    void registerAppliers();

    static void postApplierEvent(ApplierRegistryEvent<?, ?> event) {
        ModLoader.get().postEvent(event);
    }

}
