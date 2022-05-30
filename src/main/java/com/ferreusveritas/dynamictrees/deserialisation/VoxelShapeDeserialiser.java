package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.deserialisation.result.JsonResult;
import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.ferreusveritas.dynamictrees.util.CommonVoxelShapes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.util.math.AxisAlignedBB;
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
                .elseMapIfType(JsonArray.class, array -> {
                    VoxelShape shape = VoxelShapes.empty();
                    for (JsonElement element : array) {
                        shape = VoxelShapes.or(
                                JsonDeserialisers.AXIS_ALIGNED_BB.deserialise(element)
                                        .map(VoxelShapes::create)
                                        .orElseThrow()
                        );
                    }
                    return shape;
                }).elseTypeError();
    }

}
