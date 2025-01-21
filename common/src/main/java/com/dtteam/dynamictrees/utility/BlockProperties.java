package com.dtteam.dynamictrees.utility;

import com.google.common.collect.Maps;
import net.minecraft.Util;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import oshi.util.tuples.Pair;

import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class BlockProperties {

    private BlockProperties() {
    }

    public static final int[] defaultAges = {1,2,3,5,7,15,25};

    /**
     * A map of maximum ages to their respective age {@link IntegerProperty property}.
     */
    private static final Map<Integer, IntegerProperty> AGE_PROPERTIES = Util.make(Maps.newHashMap(), map -> {
        map.put(1, BlockStateProperties.AGE_1);
        map.put(2, BlockStateProperties.AGE_2);
        map.put(3, BlockStateProperties.AGE_3);
        map.put(5, BlockStateProperties.AGE_5);
        map.put(7, BlockStateProperties.AGE_7);
        map.put(15, BlockStateProperties.AGE_15);
        map.put(25, BlockStateProperties.AGE_25);
    });

    public static IntegerProperty getOrCreateAge(int maxAge) {
        return AGE_PROPERTIES.computeIfAbsent(maxAge, k -> IntegerProperty.create("age", 0, maxAge));
    }

    /**
     * A map of maximum ages to their respective age {@link IntegerProperty property}.
     */
    private static final Map<Pair<Integer,Integer>, IntegerProperty> OFFSET_PROPERTIES = Util.make(Maps.newHashMap(), map ->
            map.put(new Pair<>(4,8), IntegerProperty.create("radius_offset", 4,8)));

    public static IntegerProperty getOrCreateOffset(int min, int max) {
        return getOrCreateOffset(new Pair<>(min,max));
    }

    public static IntegerProperty getOrCreateOffset(Pair<Integer,Integer> minMax) {
        return OFFSET_PROPERTIES.computeIfAbsent(minMax, k -> IntegerProperty.create("radius_offset", minMax.getA(), minMax.getB()));
    }

}
