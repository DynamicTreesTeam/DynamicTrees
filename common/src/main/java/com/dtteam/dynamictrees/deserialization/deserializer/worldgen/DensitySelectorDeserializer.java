package com.dtteam.dynamictrees.deserialization.deserializer.worldgen;

import com.dtteam.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.dtteam.dynamictrees.deserialization.DeserializationException;
import com.dtteam.dynamictrees.deserialization.JsonDeserializers;
import com.dtteam.dynamictrees.deserialization.result.JsonResult;
import com.dtteam.dynamictrees.deserialization.result.Result;
import com.dtteam.dynamictrees.util.JsonMath;
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
public final class DensitySelectorDeserializer implements JsonBiomeDatabaseDeserializer<BiomePropertySelectors.DensitySelector> {

    @Override
    public Result<BiomePropertySelectors.DensitySelector, JsonElement> deserialize(JsonElement input) {
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
            JsonDeserializers.FLOAT.deserialize(element).ifSuccessOrElse(
                    parameters::add,
                    warningConsumer,
                    warningConsumer
            );
        }

        return switch (parameters.size()) {
            case 0 -> (rnd, n) -> n;
            case 1 -> (rnd, n) -> n * parameters.getFirst();
            case 2 -> (rnd, n) -> (n * parameters.getFirst()) + parameters.get(1);
            case 3 -> (rnd, n) -> ((n * parameters.getFirst()) + parameters.get(1)) * parameters.get(2);
            default -> (rnd, n) -> 0.0f;
        };
    }

    @Nullable
    private BiomePropertySelectors.DensitySelector readDensitySelector(JsonObject jsonObject,
                                                                       Consumer<String> warningConsumer)
            throws DeserializationException {

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
