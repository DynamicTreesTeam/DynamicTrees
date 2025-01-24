package com.dtteam.dynamictrees.config;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.platform.services.IConfigHelper;
import com.mojang.datafixers.util.Pair;

public class DTConfigs {
    public static SimpleConfig CONFIG;
    private static DTConfigProvider configs;

    public static void registerConfigs() {
        configs = new DTConfigProvider();
        createConfigs();

        CONFIG = SimpleConfig.of(DynamicTrees.MOD_ID).provider(configs).request();
    }

    private static void createConfigs() {
        configs.addSection("seeds");
        createConfig(IConfigHelper.LEAVES_SEED_DROP_RATE, "The rate at which seeds drop from leaves.",
                1D, 0D, 64D);
    }

    private static <T> void createConfig(String id, String comment, T def){
        configs.addKeyValuePair(new Pair<>(id, def), comment, null);
    }
    private static <T> void createConfig(String id, String comment, T def, T rangeMin, T rangeMax){
        configs.addKeyValuePair(new Pair<>(id, def), comment, new Pair<>(rangeMin, rangeMax));
    }
    
    public static String getStringConfig (String id){
        Pair<Class<?>, ?> def = configs.getDefaultValues().get(id);
        return CONFIG.getOrDefault(id, (String)def.getSecond());
    }
    public static Integer getIntConfig (String id){
        Pair<Class<?>, ?> def = configs.getDefaultValues().get(id);
        return CONFIG.getOrDefault(id, (Integer) def.getSecond());
    }
    public static Double getDoubleConfig (String id){
        Pair<Class<?>, ?> def = configs.getDefaultValues().get(id);
        return CONFIG.getOrDefault(id, (Double) def.getSecond());
    }
    public static Boolean getBooleanConfig (String id){
        Pair<Class<?>, ?> def = configs.getDefaultValues().get(id);
        return CONFIG.getOrDefault(id, (Boolean) def.getSecond());
    }

}