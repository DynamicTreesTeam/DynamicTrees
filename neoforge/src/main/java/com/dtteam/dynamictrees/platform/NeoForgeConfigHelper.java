package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.platform.services.IConfigHelper;

import java.util.List;

public class NeoForgeConfigHelper implements IConfigHelper {

    private <T> T getConfig(String config, Class<T> tClass) {
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
    public Boolean getBoolConfig(String config){
        return getConfig(config, Boolean.class);
    }
    @Override
    public Integer getIntConfig(String config){
        return getConfig(config, Integer.class);
    }
    @Override
    public Double getDoubleConfig(String config){
        return getConfig(config, Double.class);
    }
    @Override
    public String getStringConfig(String config){
        return getConfig(config, String.class);
    }
    @Override
    public <T extends Enum<T>> T getEnumConfig(String config, Class<T> tClass) {
        return getConfig(config, tClass);
    }
    @SuppressWarnings("unchecked")
    @Override
    public List<String> getStringListConfig(String config){
        return getConfig(config, List.class);
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