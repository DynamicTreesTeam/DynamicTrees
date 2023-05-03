package com.ferreusveritas.dynamictrees.util;

import com.google.common.collect.Maps;
import net.minecraft.Util;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import oshi.util.tuples.Pair;

import java.util.Map;

public final class OffsetProperties {

    private OffsetProperties() {
    }

    /**
     * A map of maximum ages to their respective age {@link IntegerProperty property}.
     */
    private static final Map<Pair<Integer,Integer>, IntegerProperty> AGE_PROPERTIES = Util.make(Maps.newHashMap(), map ->
            map.put(new Pair<>(4,8), IntegerProperty.create("radius_offset", 4,8)));

    public static IntegerProperty getOrCreate(int min, int max) {
        return getOrCreate(new Pair<>(min,max));
    }

    public static IntegerProperty getOrCreate(Pair<Integer,Integer> minMax) {
        return AGE_PROPERTIES.computeIfAbsent(minMax, k -> IntegerProperty.create("radius_offset", minMax.getA(), minMax.getB()));
    }

}
