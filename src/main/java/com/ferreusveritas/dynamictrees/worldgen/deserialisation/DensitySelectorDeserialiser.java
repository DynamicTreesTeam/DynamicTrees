package com.ferreusveritas.dynamictrees.worldgen.deserialisation;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.deserialisation.DeserialisationException;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.ferreusveritas.dynamictrees.deserialisation.result.JsonResult;
import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Gets an {@link BiomePropertySelectors.DensitySelector} object from a {@link JsonElement}.
 *
 * @author Harley O'Connor
 */
public final class DensitySelectorDeserialiser implements JsonBiomeDatabaseDeserialiser<BiomePropertySelectors.DensitySelector> {

    @Override
    public Result<BiomePropertySelectors.DensitySelector, JsonElement> deserialise(JsonElement input) {
        return JsonResult.forInput(input)
                .mapIfType(JsonObject.class, this::readDensitySelector)
                .elseMapIfType(JsonArray.class, this::createScaleDensitySelector)
                .elseMapIfType(Float.class, this::createStaticDensitySelector)
                .elseTypeError();
    }

    private BiomePropertySelectors.DensitySelector createStaticDensitySelector(float density) {
        return (rnd, n) -> density;
    }

    private BiomePropertySelectors.DensitySelector createScaleDensitySelector(JsonArray jsonArray,
                                                                              Consumer<String> warningConsumer) {
        final List<Float> parameters = new ArrayList<>();

        for (final JsonElement element : jsonArray) {
            JsonDeserialisers.FLOAT.deserialise(element).ifSuccessOrElse(
                    parameters::add,
                    warningConsumer,
                    warningConsumer
            );
        }

        switch (parameters.size()) {
            case 0:
                return (rnd, n) -> n;
            case 1:
                return (rnd, n) -> n * parameters.get(0);
            case 2:
                return (rnd, n) -> (n * parameters.get(0)) + parameters.get(1);
            case 3:
                return (rnd, n) -> ((n * parameters.get(0)) + parameters.get(1)) * parameters.get(2);
            default:
                return (rnd, n) -> 0.0f;
        }
    }

    @Nullable
    private BiomePropertySelectors.DensitySelector readDensitySelector(JsonObject jsonObject,
                                                                       Consumer<String> warningConsumer)
            throws DeserialisationException {

        return JsonResult.forInput(jsonObject)
                .mapIfContains(SCALE, JsonArray.class, this::createScaleDensitySelector)
                .elseMapIfContains(STATIC, Float.class, this::createStaticDensitySelector)
                .elseMapIfContains(MATH, JsonElement.class, input -> {
                    final JsonMath jsonMath = new JsonMath(input);
                    return (rnd, n) -> jsonMath.apply(rnd, (float) n);
                }).elseTypeError()
                .forEachWarning(warningConsumer)
                .orElseThrow();
    }

}
