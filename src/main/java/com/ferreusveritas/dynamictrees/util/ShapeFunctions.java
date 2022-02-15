package com.ferreusveritas.dynamictrees.util;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class ShapeFunctions {

    private static final Map<String, ShapeFunction<?>> SHAPE_FUNCTIONS = Util.make(Maps.newHashMap(), shapeFunctions -> {
        shapeFunctions.put("fruit", new FruitShapeFunction());
    });

    /**
     * Registers a new shape function with the given {@code functionId}, if it's not already taken.
     *
     * @return {@code true} if registration was successful (there was not already a shape function registered)
     */
    public static boolean registerShapeFunction(String functionId, ShapeFunction<?> shapeFunction) {
        return SHAPE_FUNCTIONS.putIfAbsent(functionId, shapeFunction) == null;
    }

    public static DataResult<VoxelShape> calculateShape(String functionId, JsonElement parametersJson) {
        final ShapeFunction<?> shapeFunction = SHAPE_FUNCTIONS.get(functionId);
        return shapeFunction != null ? calculateShape(shapeFunction, parametersJson) :
                DataResult.error("No shape function with ID \"" + functionId + "\".");
    }

    private static <P> DataResult<VoxelShape> calculateShape(ShapeFunction<P> shapeFunction, JsonElement parametersJson) {
        return shapeFunction.getParameters(parametersJson).map(pair -> shapeFunction.calculateShape(pair.getFirst()));
    }

    public interface ShapeFunction<P> {

        DataResult<Pair<P, JsonElement>> getParameters(JsonElement json);

        VoxelShape calculateShape(P parameters);

    }

    public static class FruitShapeFunction implements ShapeFunction<FruitShapeFunction.Parameters> {

        @Override
        public DataResult<Pair<Parameters, JsonElement>> getParameters(JsonElement json) {
            return Parameters.CODEC.decode(JsonOps.INSTANCE, json);
        }

        /**
         * @author Max Hyper
         */
        @Override
        public VoxelShape calculateShape(Parameters parameters) {
            final float fraction = parameters.fraction;
            final float radius = parameters.radius;
            final float topHeight = fraction - parameters.stemLength;
            final float bottomHeight = topHeight - parameters.height;
            return VoxelShapes.create(new AxisAlignedBB(
                    ((fraction / 2) - radius) / fraction, topHeight / fraction, ((fraction / 2) - radius) / fraction,
                    ((fraction / 2) + radius) / fraction, bottomHeight / fraction, ((fraction / 2) + radius) / fraction
            ));
        }

        public static class Parameters {

            public static final Codec<Parameters> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.FLOAT.fieldOf("radius").forGetter(Parameters::getRadius),
                    Codec.FLOAT.fieldOf("height").forGetter(Parameters::getHeight),
                    Codec.FLOAT.fieldOf("stem_length").forGetter(Parameters::getStemLength),
                    Codec.FLOAT.optionalFieldOf("fraction", 20.0F).forGetter(Parameters::getHeight)
            ).apply(instance, Parameters::new));

            private final float radius;
            private final float height;
            private final float stemLength;
            private final float fraction;

            public Parameters(float radius, float height, float stemLength, float fraction) {
                this.radius = radius;
                this.height = height;
                this.stemLength = stemLength;
                this.fraction = fraction;
            }

            public float getRadius() {
                return radius;
            }

            public float getHeight() {
                return height;
            }

            public float getStemLength() {
                return stemLength;
            }

            public float getFraction() {
                return fraction;
            }

        }

    }

}
