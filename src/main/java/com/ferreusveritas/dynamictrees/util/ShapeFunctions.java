package com.ferreusveritas.dynamictrees.util;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;
import java.util.Optional;

/**
 * @author Harley O'Connor
 */
public final class ShapeFunctions {

    private static final Map<String, ShapeFunction<?>> SHAPE_FUNCTIONS = Util.make(Maps.newHashMap(), shapeFunctions -> {
        shapeFunctions.put("fruit", new FruitShapeFunction());
        shapeFunctions.put("pod", new PodShapeFunction());
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
        DataResult<Pair<P, JsonElement>> parametersResult = shapeFunction.getParameters(parametersJson);
        DataResult<VoxelShape> shapeResult = parametersResult.map(pair -> shapeFunction.calculateShape(pair.getFirst()));
        return shapeResult;
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
            return Shapes.create(createFruitShape(fraction, radius, topHeight, bottomHeight));
        }

        /**
         * @author Max Hyper
         */
        public static AABB createFruitShape(float fraction, float radius, float topHeight, float bottomHeight) {
            return new AABB(
                    ((fraction / 2) - radius) / fraction, topHeight / fraction, ((fraction / 2) - radius) / fraction,
                    ((fraction / 2) + radius) / fraction, bottomHeight / fraction, ((fraction / 2) + radius) / fraction
            );
        }

        public record Parameters(float radius, float height, float stemLength, float fraction) {

            public static final Codec<Parameters> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.FLOAT.fieldOf("radius").forGetter(Parameters::radius),
                    Codec.FLOAT.fieldOf("height").forGetter(Parameters::height),
                    Codec.FLOAT.fieldOf("stem_length").forGetter(Parameters::stemLength),
                    Codec.FLOAT.optionalFieldOf("fraction", 20.0F).forGetter(Parameters::fraction)
            ).apply(instance, Parameters::new));

        }

    }

    public static class PodShapeFunction implements ShapeFunction<PodShapeFunction.Parameters> {

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
            final float sideOffset = parameters.sideOffset / fraction;
            final Direction side = parameters.side;
            return Shapes.create(
                    offsetBoundingBox(
                            FruitShapeFunction.createFruitShape(fraction, radius, topHeight, bottomHeight),
                            side,
                            sideOffset
                    )
            );

        }

        /**
         * @author Max Hyper
         */
        public static AABB offsetBoundingBox(AABB box, Direction dir, float offset) {
            return box.move(dir.getStepX() * offset, dir.getStepY() * offset, dir.getStepZ() * offset);
        }

        public record Parameters(float radius, float height, float stemLength, float fraction, float sideOffset,
                                 Direction side) {

            public static final Codec<Direction> SIDE_CODEC = Codec.STRING.comapFlatMap(side -> DataResult.success(Optional.ofNullable(Direction.byName(side)).orElse(Direction.NORTH)), Direction::name);

            public static final Codec<Parameters> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.FLOAT.fieldOf("radius").forGetter(Parameters::radius),
                    Codec.FLOAT.fieldOf("height").forGetter(Parameters::height),
                    Codec.FLOAT.fieldOf("stem_length").forGetter(Parameters::stemLength),
                    Codec.FLOAT.optionalFieldOf("fraction", 16.0F).forGetter(Parameters::fraction),
                    Codec.FLOAT.fieldOf("side_offset").forGetter(Parameters::sideOffset),
                    SIDE_CODEC.fieldOf("side").forGetter(Parameters::side)
            ).apply(instance, Parameters::new));

        }

    }

}
