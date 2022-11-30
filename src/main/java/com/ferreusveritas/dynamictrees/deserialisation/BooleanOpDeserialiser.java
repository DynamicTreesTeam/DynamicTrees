package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import net.minecraft.Util;
import net.minecraft.world.phys.shapes.BooleanOp;

import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class BooleanOpDeserialiser implements JsonDeserialiser<BooleanOp> {

    private static final Map<String, BooleanOp> VALUES = Util.make(Maps.newHashMap(), values -> {
        values.put("false", BooleanOp.FALSE);
        values.put("not_or", BooleanOp.NOT_OR);
        values.put("only_second", BooleanOp.ONLY_SECOND);
        values.put("not_first", BooleanOp.NOT_FIRST);
        values.put("only_first", BooleanOp.ONLY_FIRST);
        values.put("not_second", BooleanOp.NOT_SECOND);
        values.put("not_same", BooleanOp.NOT_SAME);
        values.put("not_and", BooleanOp.NOT_AND);
        values.put("and", BooleanOp.AND);
        values.put("same", BooleanOp.SAME);
        values.put("second", BooleanOp.SECOND);
        values.put("causes", BooleanOp.CAUSES);
        values.put("first", BooleanOp.FIRST);
        values.put("caused_by", BooleanOp.CAUSED_BY);
        values.put("or", BooleanOp.OR);
        values.put("true", BooleanOp.TRUE);
    });

    @Override
    public Result<BooleanOp, JsonElement> deserialise(JsonElement input) {
        return JsonDeserialisers.STRING.deserialise(input)
                .map(VALUES::get);
    }

}
