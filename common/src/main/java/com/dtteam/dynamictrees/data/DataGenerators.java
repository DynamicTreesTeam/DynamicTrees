package com.dtteam.dynamictrees.data;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.function.Supplier;

public class DataGenerators {

    private static final HashMap<ResourceLocation, Supplier<Generator<?, ?>>> generators = new HashMap<>();

    public static <P extends DTDataProvider,R> Supplier<Generator<P, R>> getGenerator (ResourceLocation location, Class<R> registryEntry, Class<P> provider){
        Supplier<Generator<P, R>> gen = generators.get(location);
        
    }

    public static void addGenerator (ResourceLocation location, Supplier<Generator<?, ?>> generatorSupplier){
        generators.put(location, generatorSupplier);
    }

    private class DataGenerator<P, R> {
        Supplier<Generator<P, R>>
    }

}
