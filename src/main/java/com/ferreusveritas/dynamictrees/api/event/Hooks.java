package com.ferreusveritas.dynamictrees.api.event;

import com.ferreusveritas.dynamictrees.api.resource.ResourceManager;
import net.minecraftforge.fml.ModLoader;

/**
 * @author Harley O'Connor
 */
public final class Hooks {

    public static void onAddResourceLoaders(ResourceManager resourceManager) {
        ModLoader.get().postEvent(new AddResourceLoadersEvent(resourceManager));
    }

}
