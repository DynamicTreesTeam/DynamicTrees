package com.ferreusveritas.dynamictrees.api.resource.loading.preparation;

import com.ferreusveritas.dynamictrees.api.resource.ResourceAccessor;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * @author Harley O'Connor
 */
public interface ResourcePreparer<R> {

    ResourceAccessor<R> prepare(ResourceManager resourceManager);

}
