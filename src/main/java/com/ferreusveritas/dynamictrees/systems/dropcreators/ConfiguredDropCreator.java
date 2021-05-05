package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.api.configurations.Configured;

/**
 * @author Harley O'Connor
 */
public final class ConfiguredDropCreator<DC extends DropCreator> extends Configured<ConfiguredDropCreator<DC>, DC> {

    public ConfiguredDropCreator(DC dropCreator) {
        super(dropCreator);
    }

}
