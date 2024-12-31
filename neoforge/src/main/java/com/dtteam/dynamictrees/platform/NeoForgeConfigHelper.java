package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.init.DTConfigs;
import com.dtteam.dynamictrees.platform.services.IConfigHelper;

public class NeoForgeConfigHelper implements IConfigHelper {

    @Override
    public <T> T getConfig(String config, Class<T> tClass) {
        if (!DTConfigs.CONFIGS.containsKey(config)){
            DynamicTrees.LOG.error("Failed to get configuration \"{}\" of {} as it does not exist.", config, tClass);
            return null;
        }
        Object retVal = DTConfigs.CONFIGS.get(config).get();
        if (!tClass.isInstance(retVal)) {
            DynamicTrees.LOG.error("Failed to get configuration \"{}\" of {} as it is of {} instead.", config, tClass, retVal.getClass());
            return null;
        }
        return tClass.cast(DTConfigs.CONFIGS.get(config).get());
    }

    @Override
    public boolean isServerConfigLoaded() {
        return DTConfigs.SERVER_CONFIG.isLoaded();
    }

    @Override
    public boolean isCommonConfigLoaded() {
        return DTConfigs.COMMON_CONFIG.isLoaded();
    }

    @Override
    public boolean isClientConfigLoaded() {
        return DTConfigs.CLIENT_CONFIG.isLoaded();
    }

}