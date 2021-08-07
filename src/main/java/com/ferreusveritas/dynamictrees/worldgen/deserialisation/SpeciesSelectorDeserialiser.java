package com.ferreusveritas.dynamictrees.worldgen.deserialisation;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.deserialisation.JsonHelper;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.ferreusveritas.dynamictrees.deserialisation.DeserialisationResult;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

/**
 * Gets an {@link BiomePropertySelectors.ISpeciesSelector} object from a {@link JsonElement}.
 *
 * @author Harley O'Connor
 */
public final class SpeciesSelectorDeserialiser implements JsonBiomeDatabaseDeserialiser<BiomePropertySelectors.ISpeciesSelector> {

    @Override
    public DeserialisationResult<BiomePropertySelectors.ISpeciesSelector> deserialise(final JsonElement jsonElement) {
        final DeserialisationResult<BiomePropertySelectors.ISpeciesSelector> speciesSelectorResult = this.getStaticSelector(jsonElement);

        if (speciesSelectorResult.wasSuccessful())
            return speciesSelectorResult;

        JsonHelper.JsonElementReader.of(jsonElement).ifOfType(JsonObject.class, jsonObject -> {
            JsonHelper.JsonObjectReader.of(jsonObject).ifContains(STATIC, staticElement -> speciesSelectorResult.copyFrom(this.getStaticSelector(staticElement)))
                    .elseIfContains(RANDOM, randomElement -> speciesSelectorResult.copyFrom(this.getRandomSpeciesSelector(randomElement)))
                    .elseRun(() -> speciesSelectorResult.setErrorMessage("Species selector did not have one of either elements '" + STATIC + "' or '" + RANDOM + "'."));
        });

        return speciesSelectorResult;
    }

    private DeserialisationResult<BiomePropertySelectors.ISpeciesSelector> getStaticSelector (final JsonElement jsonElement) {
        final DeserialisationResult<BiomePropertySelectors.ISpeciesSelector> speciesSelectorResult = new DeserialisationResult<>();

        JsonHelper.JsonElementReader.of(jsonElement).ifOfType(Species.class, species ->
                speciesSelectorResult.setValue(new BiomePropertySelectors.StaticSpeciesSelector(new BiomePropertySelectors.SpeciesSelection(species))))
                .elseIfOfType(String.class, str -> {
                    if (this.isDefault(str))
                        speciesSelectorResult.setValue(new BiomePropertySelectors.StaticSpeciesSelector());
                    else speciesSelectorResult.setErrorMessage("'" + str + " is not a supported parameter for a static species selector.");
                }).elseUnsupportedError(speciesSelectorResult::setErrorMessage);

        return speciesSelectorResult;
    }

    private DeserialisationResult<BiomePropertySelectors.ISpeciesSelector> getRandomSpeciesSelector(final JsonElement jsonElement) {
        final DeserialisationResult<BiomePropertySelectors.ISpeciesSelector> selectorResult = new DeserialisationResult<>();
        final DeserialisationResult<JsonObject> result = JsonDeserialisers.JSON_OBJECT.deserialise(jsonElement);

        if (!result.wasSuccessful())
            return DeserialisationResult.failureFromOther(result);

        final BiomePropertySelectors.RandomSpeciesSelector randomSelector = new BiomePropertySelectors.RandomSpeciesSelector();
        selectorResult.setValue(randomSelector);

        for (final Map.Entry<String, JsonElement> entry : result.getValue().entrySet()) {
            final String speciesName = entry.getKey();

            JsonHelper.JsonElementReader.of(entry.getValue()).ifOfType(Integer.class, weight -> {
                if (weight > 0) {
                    if (this.isDefault(speciesName)) {
                        randomSelector.add(weight);
                    } else {
                        TreeRegistry.findSpeciesSloppy(speciesName).ifValid((species) -> randomSelector.add(species, weight));
                    }
                }
            }).ifFailed(selectorResult::addWarning);
        }

        if (randomSelector.getSize() > 0)
            return selectorResult;
        else return selectorResult.setErrorMessage("No species were selected in random selector '" + jsonElement + "'.");
    }

}
