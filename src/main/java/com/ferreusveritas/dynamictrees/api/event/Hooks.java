package com.ferreusveritas.dynamictrees.api.event;

import com.ferreusveritas.dynamictrees.api.resource.TreeResourceManager;
import net.minecraftforge.fml.ModLoader;

/**
 * @author Harley O'Connor
 */
public final class Hooks {

    public static void onAddResourceLoaders(TreeResourceManager resourceManager) {
        ModLoader.get().postEvent(new AddResourceLoadersEvent(resourceManager));
    }

}
