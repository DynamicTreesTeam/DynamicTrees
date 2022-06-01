package com.ferreusveritas.dynamictrees.api.resource.loading.preparation;

import com.ferreusveritas.dynamictrees.api.resource.ResourceAccessor;
import net.minecraft.resources.IResourceManager;

/**
 * @author Harley O'Connor
 */
public interface ResourcePreparer<R> {

    ResourceAccessor<R> prepare(IResourceManager resourceManager);

}
