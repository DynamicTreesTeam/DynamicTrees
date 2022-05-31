package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.deserialisation.result.JsonResult;
import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.ferreusveritas.dynamictrees.util.CommonVoxelShapes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * @author Harley O'Connor
 */
public final class VoxelShapeDeserialiser implements JsonDeserialiser<VoxelShape> {

    @Override
    public Result<VoxelShape, JsonElement> deserialise(JsonElement input) {
        return JsonResult.forInput(input)
                .mapIfType(String.class, name ->
                        CommonVoxelShapes.SHAPES.getOrDefault(name.toLowerCase(), Shapes.block())
                ).elseMapIfType(AABB.class, Shapes::create)
                .elseMapIfType(JsonArray.class, array -> {
                    VoxelShape shape = Shapes.empty();
                    for (JsonElement element : array) {
                        shape = Shapes.or(
                                JsonDeserialisers.AXIS_ALIGNED_BB.deserialise(element)
                                        .map(Shapes::create)
                                        .orElseThrow()
                        );
                    }
                    return shape;
                }).elseTypeError();
    }

}
