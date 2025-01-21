package com.dtteam.dynamictrees.api.resource.loading;

/**
 * @author Harley O'Connor
 */
public interface ApplierResourceLoader<P> extends ResourceLoader<P> {

    void registerAppliers();

}
