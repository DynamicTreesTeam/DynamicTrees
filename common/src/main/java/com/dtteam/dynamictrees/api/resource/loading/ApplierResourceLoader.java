package com.dtteam.dynamictrees.api.resource.loading;

/**
 * @author Harley O'Connor
 */
public interface ApplierResourceLoader<P> extends ResourceLoader<P> {

    void registerAppliers();

//    static void postApplierEvent(ApplierRegistryEvent<?, ?> event) {
//        ModLoader.get().postEvent(event);
//    }

}
