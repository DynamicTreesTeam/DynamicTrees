package com.dtteam.dynamictrees.deserialization.deserializer.worldgen;

import com.dtteam.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.dtteam.dynamictrees.deserialization.DeserializationException;
import com.dtteam.dynamictrees.deserialization.result.JsonResult;
import com.dtteam.dynamictrees.deserialization.result.Result;
import com.dtteam.dynamictrees.util.JsonMath;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Gets an {@link BiomePropertySelectors.ChanceSelector} object from a {@link JsonElement}.
 *
 * @author Harley O'Connor
 */
public final class ChanceSelectorDeserializer implements JsonBiomeDatabaseDeserializer<BiomePropertySelectors.ChanceSelector> {

    @Override
    public Result<BiomePropertySelectors.ChanceSelector, JsonElement> deserialize(JsonElement input) {
        return JsonResult.forInput(input)
                .mapIfType(JsonObject.class, this::readChanceSelector)
                .elseMapIfType(Float.class, this::createSimpleChanceSelector)
                .elseMapIfType(String.class, name -> {
                    if (name.equalsIgnoreCase("standard")) {
                        return (rnd, spc, rad) -> rnd.nextFloat() < (rad > 3 ? 2.0f / rad : 1.0f) ?
                                BiomePropertySelectors.Chance.OK : BiomePropertySelectors.Chance.CANCEL;
                    }
                    throw new DeserializationException("Unrecognised named chance selector \"" + name + "\".");
                }).elseTypeError();
    }

    private BiomePropertySelectors.ChanceSelector createSimpleChanceSelector(float value) {
        if (value <= 0) {
            return (rnd, spc, rad) -> BiomePropertySelectors.Chance.CANCEL;
        } else if (value >= 1) {
            return (rnd, spc, rad) -> BiomePropertySelectors.Chance.OK;
        }
        return (rnd, spc, rad) -> rnd.nextFloat() < value ?
                BiomePropertySelectors.Chance.OK : BiomePropertySelectors.Chance.CANCEL;
    }

    @Nullable
    private BiomePropertySelectors.ChanceSelector readChanceSelector(JsonObject jsonObject,
                                                                     Consumer<String> warningConsumer)
            throws DeserializationException {
        return JsonResult.forInput(jsonObject)
                .mapIfContains(STATIC, JsonElement.class, input ->
                        JsonResult.forInput(input)
                                .mapIfType(Float.class, this::createSimpleChanceSelector)
                                .elseMapIfType(String.class, name -> {
                                    if (this.isDefault(name)) {
                                        return (rnd, spc, rad) -> BiomePropertySelectors.Chance.UNHANDLED;
                                    }
                                    throw new DeserializationException("Unrecognised named chance selector \"" + name + "\".");
                                }).elseTypeError()
                ).elseMapIfContains(MATH, JsonElement.class, input ->
                        JsonResult.forInput(input)
                                .map(element -> {
                                    final JsonMath jsonMath = new JsonMath(input);
                                    return (rnd, spc, rad) -> rnd.nextFloat() < jsonMath.apply(rnd, spc, rad) ?
                                            BiomePropertySelectors.Chance.OK : BiomePropertySelectors.Chance.CANCEL;
                                })
                ).map(result -> {
                    result.getWarnings().forEach(warningConsumer);
                    return result.orElseThrow();
                }).orElseThrow();
    }

}
