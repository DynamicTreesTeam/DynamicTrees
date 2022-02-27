package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.deserialisation.result.JsonResult;
import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.ferreusveritas.dynamictrees.util.CommonVoxelShapes;
import com.ferreusveritas.dynamictrees.util.ShapeFunctions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

/**
 * @author Harley O'Connor
 */
public final class VoxelShapeDeserialiser implements JsonDeserialiser<VoxelShape> {

    @Override
    public Result<VoxelShape, JsonElement> deserialise(JsonElement input) {
        return JsonResult.forInput(input)
                .mapIfType(String.class, name ->
                        CommonVoxelShapes.SHAPES.getOrDefault(name.toLowerCase(), VoxelShapes.block())
                ).elseMapIfType(AxisAlignedBB.class, VoxelShapes::create)
                .elseMapIfType(JsonArray.class, this::deserialiseArray)
                .elseMapIfType(JsonObject.class, this::deserialiseObject)
                .elseTypeError();
    }

    private VoxelShape deserialiseArray(JsonArray array) throws DeserialisationException {
        VoxelShape shape = VoxelShapes.empty();
        for (JsonElement element : array) {
            shape = VoxelShapes.or(
                    JsonDeserialisers.AXIS_ALIGNED_BB.deserialise(element)
                            .map(VoxelShapes::create)
                            .orElseThrow()
            );
        }
        return shape;
    }

    private VoxelShape deserialiseObject(JsonObject json) throws DeserialisationException {
        return JsonResult.forInput(json)
                .mapIfContains("function", String.class, functionId -> ShapeFunctions.calculateShape(
                        functionId,
                        getParametersJson(json)
                ).getOrThrow(true, s -> {}))
                .elseMapIfContains("shapes", JsonArray.class, shapes -> {
                    final IBooleanFunction operator = JsonHelper.getOrDefault(json, "operator",
                            IBooleanFunction.class, IBooleanFunction.OR);
                    return deserialiseShapes(operator, shapes);
                })
                .elseTypeError().orElseThrow();
    }

    private JsonObject getParametersJson(JsonObject json) {
        return JsonHelper.getOrDefault(json, "parameters", JsonObject.class, new JsonObject());
    }

    private VoxelShape deserialiseShapes(IBooleanFunction operator, JsonArray shapes) throws DeserialisationException {
        if (shapes.size() < 1) {
            return VoxelShapes.empty();
        }
        VoxelShape shape = this.deserialise(shapes.get(0)).orElseThrow();
        for (int i = 1; i < shapes.size(); i++) {
            shape = VoxelShapes.join(shape, this.deserialise(shapes.get(i)).orElseThrow(), operator);
        }
        return shape;
    }

}
