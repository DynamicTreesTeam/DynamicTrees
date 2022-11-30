package com.ferreusveritas.dynamictrees.util;

import com.google.common.collect.Maps;
import net.minecraft.Util;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.Map;

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
        map.put(1, BlockStateProperties.AGE_1);
        map.put(2, BlockStateProperties.AGE_2);
        map.put(3, BlockStateProperties.AGE_3);
        map.put(5, BlockStateProperties.AGE_5);
        map.put(7, BlockStateProperties.AGE_7);
        map.put(15, BlockStateProperties.AGE_15);
        map.put(25, BlockStateProperties.AGE_25);
    });

    public static IntegerProperty getOrCreate(int maxAge) {
        return AGE_PROPERTIES.computeIfAbsent(maxAge, k -> IntegerProperty.create("age", 0, maxAge));
    }

}
