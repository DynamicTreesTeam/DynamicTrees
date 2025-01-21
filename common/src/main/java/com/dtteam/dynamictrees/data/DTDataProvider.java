package com.dtteam.dynamictrees.data;

import net.minecraft.resources.ResourceLocation;

import static com.dtteam.dynamictrees.utility.helper.ResourceLocationUtils.prefix;

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
