package com.ferreusveritas.dynamictrees.util;

import com.google.common.collect.Maps;
import net.minecraft.state.IntegerProperty;
import net.minecraft.util.Util;

import java.util.Map;

import static net.minecraft.state.properties.BlockStateProperties.AGE_1;
import static net.minecraft.state.properties.BlockStateProperties.AGE_15;
import static net.minecraft.state.properties.BlockStateProperties.AGE_2;
import static net.minecraft.state.properties.BlockStateProperties.AGE_25;
import static net.minecraft.state.properties.BlockStateProperties.AGE_3;
import static net.minecraft.state.properties.BlockStateProperties.AGE_5;
import static net.minecraft.state.properties.BlockStateProperties.AGE_7;

/**
 * @author Harley O'Connor
 */
public final class AgeProperties {

    private AgeProperties() {
    }

    /**
     * A map of maximum ages to their respective age {@link IntegerProperty property}.
     */
    private static final Map<Integer, IntegerProperty> AGE_PROPERTIES = Util.make(Maps.newHashMap(), map -> {
        map.put(1, AGE_1);
        map.put(2, AGE_2);
        map.put(3, AGE_3);
        map.put(5, AGE_5);
        map.put(7, AGE_7);
        map.put(15, AGE_15);
        map.put(25, AGE_25);
    });

    public static IntegerProperty getOrCreate(int maxAge) {
        return AGE_PROPERTIES.computeIfAbsent(maxAge, k -> IntegerProperty.create("age", 0, maxAge));
    }

}
