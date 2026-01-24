package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.platform.services.IConfigHelper;
import com.mojang.datafixers.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FabricConfigHelper implements IConfigHelper {

    private <T> T getConfig(String config, Class<T> tClass){
        if (!DTConfigs.CONFIG.containsConfig(config)){
            DynamicTrees.LOG.error("Failed to get configuration \"{}\" of {} as it does not exist.", config, tClass);
        }
        Pair<Class<?>, ?> def = DTConfigs.getDefaultValue(config);
        if (!tClass.equals(def.getFirst())) {
            DynamicTrees.LOG.error("Failed to get configuration \"{}\" of {} as it is of {} instead.", config, tClass, def.getFirst());
            return null;
        }
        return tClass.cast(def.getSecond());
    }

    @Override
    public Boolean getBoolConfig(String config){
        return DTConfigs.CONFIG.getOrDefault(config, getConfig(config, Boolean.class));
    }
    @Override
    public Integer getIntConfig(String config){
        return DTConfigs.CONFIG.getOrDefault(config, getConfig(config, Integer.class));
    }
    @Override
    public Double getDoubleConfig(String config){
        if (!DTConfigs.CONFIG.containsConfig(config)){
            DynamicTrees.LOG.error("Failed to get configuration \"{}\" of {} as it does not exist.", config, Double.class);
            return null;
        }
        Pair<Class<?>, ?> def = DTConfigs.getDefaultValue(config);
        Double defaultVal;
        if (def.getFirst().equals(Float.class)) {
            defaultVal = ((Float) def.getSecond()).doubleValue();
        } else if (def.getFirst().equals(Double.class)) {
            defaultVal = (Double) def.getSecond();
        } else {
            DynamicTrees.LOG.error("Failed to get configuration \"{}\" of {} as it is of {} instead.", config, Double.class, def.getFirst());
            return null;
        }
        return DTConfigs.CONFIG.getOrDefault(config, defaultVal);
    }
    @Override
    public String getStringConfig(String config){
        return DTConfigs.CONFIG.getOrDefault(config, getConfig(config, String.class));
    }

    @Override
    public <T extends Enum<T>> T getEnumConfig(String config, Class<T> tClass) {
        String value = getStringConfig(config);
        return Enum.valueOf(tClass, value.toUpperCase(Locale.ENGLISH));
    }

    @Override
    public List<String> getStringListConfig(String config){
        String array = getStringConfig(config);
        if (array == null) return new ArrayList<>();
        array = array.trim().substring(1, array.length()-1); //remove [ and ]
        String[] values = array.split(",");
        List<String> stringList = new ArrayList<>();
        for (String val : values) { //remove " and "
            if (val.isEmpty()) continue;
            val = val.trim();
            if (val.charAt(0) == '\"') val = val.substring(1, val.length()-1).trim();
            stringList.add(val);
        }
        return stringList;
    }

    @Override
    public boolean isServerConfigLoaded() {
        return DTConfigs.isLoaded;
    }

    @Override
    public boolean isCommonConfigLoaded() {
        return DTConfigs.isLoaded;
    }

    @Override
    public boolean isClientConfigLoaded() {
        return DTConfigs.isLoaded;
    }
}