package com.ferreusveritas.dynamictrees.worldgen.json;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.util.json.JsonHelper;
import com.ferreusveritas.dynamictrees.util.json.JsonDeserialisers;
import com.ferreusveritas.dynamictrees.util.json.DeserialisationResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Gets an {@link BiomePropertySelectors.IDensitySelector} object from a {@link JsonElement}.
 *
 * @author Harley O'Connor
 */
public final class DensitySelectorDeserialiser implements JsonBiomeDatabaseDeserialiser<BiomePropertySelectors.IDensitySelector> {

    @Override
    public DeserialisationResult<BiomePropertySelectors.IDensitySelector> deserialise(JsonElement jsonElement) {
        final DeserialisationResult<BiomePropertySelectors.IDensitySelector> densitySelector = new DeserialisationResult<>();

        JsonHelper.JsonElementReader.of(jsonElement).ifOfType(JsonObject.class, jsonObject -> densitySelector.copyFrom(this.readDensitySelector(jsonObject)))
                .elseIfOfType(JsonArray.class, jsonArray -> densitySelector.setValue(this.createScaleDensitySelector(jsonArray)))
                .elseIfOfType(Float.class, density -> densitySelector.setValue((rnd, n) -> density))
                .elseUnsupportedError(densitySelector::setErrorMessage);

        return densitySelector;
    }

    private BiomePropertySelectors.IDensitySelector createScaleDensitySelector(final JsonArray jsonArray) {
        final List<Float> parameters = new ArrayList<>();

        for (final JsonElement element : jsonArray) {
            JsonDeserialisers.FLOAT.deserialise(element).ifSuccessful(parameters::add)
                    .otherwiseWarn("Error whilst applying density selector: ");
        }

        switch (parameters.size()) {
            case 0: return (rnd, n) -> n;
            case 1: return (rnd, n) -> n * parameters.get(0);
            case 2: return (rnd, n) -> (n * parameters.get(0)) + parameters.get(1);
            case 3: return (rnd, n) -> ((n * parameters.get(0)) + parameters.get(1)) * parameters.get(2);
            default: return (rnd, n) -> 0.0f;
        }
    }

    private DeserialisationResult<BiomePropertySelectors.IDensitySelector> readDensitySelector(final JsonObject jsonObject) {
        final DeserialisationResult<BiomePropertySelectors.IDensitySelector> densitySelector = new DeserialisationResult<>();

        JsonHelper.JsonObjectReader.of(jsonObject)
                .ifContains(SCALE, jsonElement -> JsonHelper.JsonElementReader.of(jsonElement)
                        .ifOfType(JsonArray.class, jsonArray -> densitySelector.setValue(this.createScaleDensitySelector(jsonArray)))
                        .ifFailed(densitySelector::addWarning))
                .elseIfContains(STATIC, jsonElement -> JsonHelper.JsonElementReader.of(jsonElement)
                        .ifOfType(Float.class, density -> densitySelector.setValue((rnd, n) -> density))
                        .ifFailed(densitySelector::addWarning))
                .elseIfContains(MATH, jsonElement -> {
                    final JsonMath jsonMath = new JsonMath(jsonElement);
                    densitySelector.setValue((rnd, n) -> jsonMath.apply(rnd, (float) n));
                });

        return densitySelector;
    }

}
