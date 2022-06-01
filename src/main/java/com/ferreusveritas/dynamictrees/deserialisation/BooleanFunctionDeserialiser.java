package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import net.minecraft.util.Util;
import net.minecraft.util.math.shapes.IBooleanFunction;

import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class BooleanFunctionDeserialiser implements JsonDeserialiser<IBooleanFunction> {

    private static final Map<String, IBooleanFunction> VALUES = Util.make(Maps.newHashMap(), values -> {
        values.put("false", IBooleanFunction.FALSE);
        values.put("not_or", IBooleanFunction.NOT_OR);
        values.put("only_second", IBooleanFunction.ONLY_SECOND);
        values.put("not_first", IBooleanFunction.NOT_FIRST);
        values.put("only_first", IBooleanFunction.ONLY_FIRST);
        values.put("not_second", IBooleanFunction.NOT_SECOND);
        values.put("not_same", IBooleanFunction.NOT_SAME);
        values.put("not_and", IBooleanFunction.NOT_AND);
        values.put("and", IBooleanFunction.AND);
        values.put("same", IBooleanFunction.SAME);
        values.put("second", IBooleanFunction.SECOND);
        values.put("causes", IBooleanFunction.CAUSES);
        values.put("first", IBooleanFunction.FIRST);
        values.put("caused_by", IBooleanFunction.CAUSED_BY);
        values.put("or", IBooleanFunction.OR);
        values.put("true", IBooleanFunction.TRUE);
    });

    @Override
    public Result<IBooleanFunction, JsonElement> deserialise(JsonElement input) {
        return JsonDeserialisers.STRING.deserialise(input)
                .map(VALUES::get);
    }

}
