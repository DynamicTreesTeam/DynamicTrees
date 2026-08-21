package com.dtteam.dynamictrees.data;

import net.minecraft.data.DataProvider;
import net.minecraft.resources.Identifier;

import static com.dtteam.dynamictrees.utility.ResourceLocationUtils.prefix;

/**
 * @author Harley O'Connor
 */
public interface DTDataProvider extends DataProvider {

    default Identifier block(Identifier blockLocation) {
        return prefix(blockLocation, "block/");
    }

    default Identifier item(Identifier resourceLocation) {
        return prefix(resourceLocation, "item/");
    }

    interface BlockState extends DTDataProvider { }
    interface ItemModel extends DTDataProvider { }
    interface Language extends DTDataProvider { }

}
