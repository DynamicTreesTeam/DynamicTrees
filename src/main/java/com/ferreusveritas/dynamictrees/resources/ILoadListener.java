package com.ferreusveritas.dynamictrees.resources;

import net.minecraft.resources.IResourceManager;

/**
 * An alternate version of {@link net.minecraft.resources.IFutureReloadListener} that's only
 * called on the initial load of the game (for {@link TreesResourceManager}).
 *
 * @author Harley O'Connor
 */
public interface ILoadListener {

    void load (final IResourceManager resourceManager);

}
