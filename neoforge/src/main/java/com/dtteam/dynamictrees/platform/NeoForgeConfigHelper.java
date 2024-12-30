package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.DynamicTreesCommon;
import com.dtteam.dynamictrees.api.registry.*;
import com.dtteam.dynamictrees.init.DTConfigs;
import com.dtteam.dynamictrees.platform.services.IConfigHelper;
import com.dtteam.dynamictrees.platform.services.IRegistryHelper;
import net.neoforged.fml.ModLoader;

public class NeoForgeConfigHelper implements IConfigHelper {

    @Override
    public <T> T getConfig(String config, Class<T> tClass) {
        if (!DTConfigs.CONFIGS.containsKey(config)){
            DynamicTreesCommon.LOG.error("Failed to get configuration \"{}\" of {} as it does not exist.", config, tClass);
            return null;
        }
        Object retVal = DTConfigs.CONFIGS.get(config).get();
        if (!tClass.isInstance(retVal)) {
            DynamicTreesCommon.LOG.error("Failed to get configuration \"{}\" of {} as it is of {} instead.", config, tClass, retVal.getClass());
            return null;
        }
        return tClass.cast(DTConfigs.CONFIGS.get(config).get());
    }

}