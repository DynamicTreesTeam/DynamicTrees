package com.dtteam.dynamictrees.platform.services;

import java.util.List;

public interface IConfigHelper {

    <T> T getConfig (String config, Class<T> tClass);
    <T extends Enum<T>> T getEnumConfig (String config, Class<T> tClass);

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
    @SuppressWarnings("unchecked")
    default List<String> getStringListConfig(String config){
        return getConfig(config, List.class);
    }

    boolean isServerConfigLoaded ();
    boolean isCommonConfigLoaded ();
    boolean isClientConfigLoaded ();

}
