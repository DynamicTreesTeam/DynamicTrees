package com.dtteam.dynamictrees.platform.services;

public interface IConfigHelper {

    <T> T getConfig (String config, Class<T> tClass);

    default Boolean getBoolConfig(String config){
        return getConfig(config, Boolean.class);
    }
    default Integer getIntConfig(String config){
        return getConfig(config, Integer.class);
    }
    default Double getDoubleConfig(String config){
        return getConfig(config, Double.class);
    }
    default String getStringConfig(String config){
        return getConfig(config, String.class);
    }

}
