package com.ferreusveritas.dynamictrees.growthlogic;

import com.ferreusveritas.dynamictrees.api.configurations.Configured;

/**
 * @author Harley O'Connor
 */
public final class ConfiguredGrowthLogicKit extends Configured<ConfiguredGrowthLogicKit, GrowthLogicKit> {

    public static final ConfiguredGrowthLogicKit NULL_LOGIC_KIT =
            new ConfiguredGrowthLogicKit(GrowthLogicKit.NULL_LOGIC);

    public ConfiguredGrowthLogicKit(GrowthLogicKit configurable) {
        super(configurable);
    }

    @Override
    public ConfiguredGrowthLogicKit copy() {
        final ConfiguredGrowthLogicKit duplicateLogicKit = new ConfiguredGrowthLogicKit(this.configurable);
        duplicateLogicKit.properties.putAll(this.properties);
        return duplicateLogicKit;
    }

}
