package com.ferreusveritas.dynamictrees.worldgen.deserialisation;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.ferreusveritas.dynamictrees.deserialisation.JsonHelper;
import com.ferreusveritas.dynamictrees.deserialisation.result.JsonResult;
import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
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
                                                                       Consumer<String> warningConsumer) {
        final AtomicReference<BiomePropertySelectors.DensitySelector> densitySelector =
                new AtomicReference<>();

        JsonHelper.JsonObjectReader.of(jsonObject)
                .ifContains(SCALE, jsonElement ->
                        JsonDeserialisers.JSON_ARRAY.deserialise(jsonElement)
                                .map(this::createScaleDensitySelector)
                                .ifSuccessOrElse(densitySelector::set, warningConsumer, warningConsumer)
                ).elseIfContains(STATIC, jsonElement ->
                        JsonDeserialisers.FLOAT.deserialise(jsonElement)
                                .map(this::createStaticDensitySelector)
                                .ifSuccessOrElse(densitySelector::set, warningConsumer, warningConsumer)
                ).elseIfContains(MATH, jsonElement -> {
                    final JsonMath jsonMath = new JsonMath(jsonElement);
                    densitySelector.set((rnd, n) -> jsonMath.apply(rnd, (float) n));
                }).elseRun(() ->
                        warningConsumer.accept("Could not get Json object chance selector as it did not contain " +
                                "key '" + SCALE + "', '" + STATIC + "' or '" + MATH + "'.")
                );

        return densitySelector.get();
    }

}
