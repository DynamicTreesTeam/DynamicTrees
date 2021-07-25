package com.ferreusveritas.dynamictrees.worldgen.json;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.json.JsonHelper;
import com.ferreusveritas.dynamictrees.util.json.JsonGetters;
import com.ferreusveritas.dynamictrees.util.json.FetchResult;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

/**
 * Gets an {@link BiomePropertySelectors.ISpeciesSelector} object from a {@link JsonElement}.
 *
 * @author Harley O'Connor
 */
public final class SpeciesSelectorGetter implements JsonBiomeDatabaseGetter<BiomePropertySelectors.ISpeciesSelector> {

    @Override
    public FetchResult<BiomePropertySelectors.ISpeciesSelector> get(final JsonElement jsonElement) {
        final FetchResult<BiomePropertySelectors.ISpeciesSelector> speciesSelectorFetchResult = this.getStaticSelector(jsonElement);

        if (speciesSelectorFetchResult.wasSuccessful())
            return speciesSelectorFetchResult;

        JsonHelper.JsonElementReader.of(jsonElement).ifOfType(JsonObject.class, jsonObject -> {
            JsonHelper.JsonObjectReader.of(jsonObject).ifContains(STATIC, staticElement -> speciesSelectorFetchResult.copyFrom(this.getStaticSelector(staticElement)))
                    .elseIfContains(RANDOM, randomElement -> speciesSelectorFetchResult.copyFrom(this.getRandomSpeciesSelector(randomElement)))
                    .elseRun(() -> speciesSelectorFetchResult.setErrorMessage("Species selector did not have one of either elements '" + STATIC + "' or '" + RANDOM + "'."));
        });

        return speciesSelectorFetchResult;
    }

    private FetchResult<BiomePropertySelectors.ISpeciesSelector> getStaticSelector (final JsonElement jsonElement) {
        final FetchResult<BiomePropertySelectors.ISpeciesSelector> speciesSelectorFetchResult = new FetchResult<>();

        JsonHelper.JsonElementReader.of(jsonElement).ifOfType(Species.class, species ->
                speciesSelectorFetchResult.setValue(new BiomePropertySelectors.StaticSpeciesSelector(new BiomePropertySelectors.SpeciesSelection(species))))
                .elseIfOfType(String.class, str -> {
                    if (this.isDefault(str))
                        speciesSelectorFetchResult.setValue(new BiomePropertySelectors.StaticSpeciesSelector());
                    else speciesSelectorFetchResult.setErrorMessage("'" + str + " is not a supported parameter for a static species selector.");
                }).elseUnsupportedError(speciesSelectorFetchResult::setErrorMessage);

        return speciesSelectorFetchResult;
    }

    private FetchResult<BiomePropertySelectors.ISpeciesSelector> getRandomSpeciesSelector(final JsonElement jsonElement) {
        final FetchResult<BiomePropertySelectors.ISpeciesSelector> selectorFetchResult = new FetchResult<>();
        final FetchResult<JsonObject> fetchResult = JsonGetters.JSON_OBJECT.get(jsonElement);

        if (!fetchResult.wasSuccessful())
            return FetchResult.failureFromOther(fetchResult);

        final BiomePropertySelectors.RandomSpeciesSelector randomSelector = new BiomePropertySelectors.RandomSpeciesSelector();
        selectorFetchResult.setValue(randomSelector);

        for (final Map.Entry<String, JsonElement> entry : fetchResult.getValue().entrySet()) {
            final String speciesName = entry.getKey();

            JsonHelper.JsonElementReader.of(entry.getValue()).ifOfType(Integer.class, weight -> {
                if (weight > 0) {
                    if (this.isDefault(speciesName)) {
                        randomSelector.add(weight);
                    } else {
                        TreeRegistry.findSpeciesSloppy(speciesName).ifValid((species) -> randomSelector.add(species, weight));
                    }
                }
            }).ifFailed(selectorFetchResult::addWarning);
        }

        if (randomSelector.getSize() > 0)
            return selectorFetchResult;
        else return selectorFetchResult.setErrorMessage("No species were selected in random selector '" + jsonElement + "'.");
    }

}
