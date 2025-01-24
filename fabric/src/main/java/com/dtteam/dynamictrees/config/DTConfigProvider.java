package com.dtteam.dynamictrees.config;

import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class DTConfigProvider implements SimpleConfig.DefaultConfig {

    private String configContents = "";

    //id -> (default, cached)
    public HashMap<String,Pair<Class<?>, ?>> getDefaultValues() {
        return defaultValues;
    }

    private final HashMap<String,Pair<Class<?>, ?>> defaultValues = new HashMap<>();

    public void addKeyValuePair(Pair<String, ?> keyValuePair, String comment, @Nullable Pair<?, ?> range) {
        defaultValues.put(keyValuePair.getFirst(), new Pair<>(keyValuePair.getSecond().getClass(), keyValuePair.getSecond()));
        configContents +=
                "   # " + comment + "\n" +
                "   # Default: " + keyValuePair.getSecond() + "\n" +
                (range == null ? "" : "   # Range: " + range.getFirst() + " ~ " + range.getSecond() + "\n") +
                "   " + keyValuePair.getFirst() + "=" + keyValuePair.getSecond() + "\n";
    }

    public void addSection(String name){
        configContents += "\n[" + name + "]\n";
    }

    @Override
    public String get(String namespace) {
        return configContents;
    }
}