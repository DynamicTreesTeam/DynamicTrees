package com.ferreusveritas.dynamictrees.data.provider;

import net.minecraft.util.ResourceLocation;

import static com.ferreusveritas.dynamictrees.util.ResourceLocationUtils.prefix;

/**
 * @author Harley O'Connor
 */
public interface DTDataProvider {

    default ResourceLocation block(ResourceLocation blockLocation) {
        return prefix(blockLocation, "block/");
    }

    default ResourceLocation item(ResourceLocation resourceLocation) {
        return prefix(resourceLocation, "item/");
    }

}
