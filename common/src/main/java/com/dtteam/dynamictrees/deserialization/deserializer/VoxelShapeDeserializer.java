package com.dtteam.dynamictrees.deserialization.deserializer;

import com.dtteam.dynamictrees.deserialization.DeserializationException;
import com.dtteam.dynamictrees.deserialization.JsonDeserializers;
import com.dtteam.dynamictrees.deserialization.JsonHelper;
import com.dtteam.dynamictrees.deserialization.result.JsonResult;
import com.dtteam.dynamictrees.deserialization.result.Result;
import com.dtteam.dynamictrees.util.CommonVoxelShapes;
import com.dtteam.dynamictrees.util.ShapeFunctions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Locale;

/**
 * @author Harley O'Connor
 */
public final class VoxelShapeDeserializer implements JsonDeserializer<VoxelShape> {

    @Override
    public Result<VoxelShape, JsonElement> deserialize(JsonElement input) {
        return JsonResult.forInput(input)
                .mapIfType(String.class, name ->
                        CommonVoxelShapes.SHAPES.getOrDefault(name.toLowerCase(Locale.ENGLISH), Shapes.block())
                ).elseMapIfType(AABB.class, Shapes::create)
                .elseMapIfType(JsonArray.class, this::deserializeArray)
                .elseMapIfType(JsonObject.class, this::deserializeObject)
                .elseTypeError();
    }

    private VoxelShape deserializeArray(JsonArray array) throws DeserializationException {
        VoxelShape shape = Shapes.empty();
        for (JsonElement element : array) {
            shape = Shapes.or(
                    JsonDeserializers.AABB.deserialize(element)
                            .map(Shapes::create)
                            .orElseThrow(),
                    shape);
        }
        return shape;
    }

    private VoxelShape deserializeObject(JsonObject json) throws DeserializationException {
        return JsonResult.forInput(json)
                .mapIfContains("function", String.class, functionId -> ShapeFunctions.calculateShape(
                        functionId,
                        getParametersJson(json)
                ).getOrThrow())
                .elseMapIfContains("shapes", JsonArray.class, shapes -> {
                    final BooleanOp operator = JsonHelper.getOrDefault(json, "operator", BooleanOp.class, BooleanOp.OR);
                    return deserializeShapes(operator, shapes);
                })
                .elseTypeError().orElseThrow();
    }

    private JsonObject getParametersJson(JsonObject json) {
        return JsonHelper.getOrDefault(json, "parameters", JsonObject.class, new JsonObject());
    }

    private VoxelShape deserializeShapes(BooleanOp operator, JsonArray shapes) throws DeserializationException {
        if (shapes.isEmpty()) {
            return Shapes.empty();
        }
        VoxelShape shape = this.deserialize(shapes.get(0)).orElseThrow();
        for (int i = 1; i < shapes.size(); i++) {
            shape = Shapes.join(shape, this.deserialize(shapes.get(i)).orElseThrow(), operator);
        }
        return shape;
    }

}
