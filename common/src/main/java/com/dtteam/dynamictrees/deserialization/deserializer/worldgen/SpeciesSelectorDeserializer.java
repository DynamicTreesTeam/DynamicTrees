package com.dtteam.dynamictrees.deserialization.deserializer.worldgen;

import com.dtteam.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.dtteam.dynamictrees.deserialization.DeserializationException;
import com.dtteam.dynamictrees.deserialization.JsonDeserializers;
import com.dtteam.dynamictrees.deserialization.result.JsonResult;
import com.dtteam.dynamictrees.deserialization.result.Result;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.utility.helper.TreeRegistryHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Gets an {@link BiomePropertySelectors.SpeciesSelector} object from a {@link JsonElement}.
 *
 * @author Harley O'Connor
 */
public final class SpeciesSelectorDeserializer implements JsonBiomeDatabaseDeserializer<BiomePropertySelectors.SpeciesSelector> {

    @Override
    public Result<BiomePropertySelectors.SpeciesSelector, JsonElement> deserialize(final JsonElement input) {
        return JsonResult.forInput(input)
                .mapIfType(Species.class, this::readStatic)
                .elseMapIfType(String.class, this::readStatic)
                .elseMapIfType(JsonObject.class, this::readSelector)
                .elseTypeError();
    }

    private BiomePropertySelectors.SpeciesSelector readStatic(Species species) {
        return new BiomePropertySelectors.StaticSpeciesSelector(
                new BiomePropertySelectors.SpeciesSelection(species)
        );
    }

    private BiomePropertySelectors.SpeciesSelector readStatic(String string) throws DeserializationException {
        if (this.isDefault(string)) {
            return new BiomePropertySelectors.StaticSpeciesSelector();
        }
        throw new DeserializationException("\"" + string + "\" is not a supported parameter for a " +
                "static species selector.");
    }

    private BiomePropertySelectors.SpeciesSelector readSelector(JsonObject object, Consumer<String> warningConsumer)
            throws DeserializationException {

        return JsonResult.forInput(object)
                .mapIfContains(STATIC, JsonElement.class, input ->
                        JsonResult.forInput(input)
                                .mapIfType(Species.class, this::readStatic)
                                .elseMapIfType(String.class, this::readStatic)
                                .elseTypeError()
                                .forEachWarning(warningConsumer)
                                .orElseThrow()
                ).elseMapIfContains(RANDOM, JsonElement.class, input ->
                        this.getRandomSpeciesSelector(input, warningConsumer)
                ).forEachWarning(warningConsumer)
                .orElseThrow();
    }

    @Nullable
    private BiomePropertySelectors.SpeciesSelector getRandomSpeciesSelector(JsonElement input,
                                                                            Consumer<String> warningConsumer)
            throws DeserializationException {

        return JsonDeserializers.JSON_OBJECT.deserialize(input).map(object -> {
            final BiomePropertySelectors.RandomSpeciesSelector randomSelector = new BiomePropertySelectors.RandomSpeciesSelector();

            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                final String speciesName = entry.getKey();

                JsonDeserializers.INTEGER.deserialize(entry.getValue()).ifSuccessOrElseThrow(weight -> {
                    if (weight > 0) {
                        if (this.isDefault(speciesName)) {
                            randomSelector.add(weight);
                        } else {
                            TreeRegistryHelper.findSpeciesSloppy(speciesName).ifValid(species ->
                                    randomSelector.add(species, weight)
                            );
                        }
                    }
                }, warningConsumer);
            }

            if (randomSelector.getSize() < 1) {
                throw new DeserializationException("No species were selected in random selector '" + input + "'.");
            }
            return randomSelector;
        })
                .forEachWarning(warningConsumer)
                .orElseThrow();
    }

}
