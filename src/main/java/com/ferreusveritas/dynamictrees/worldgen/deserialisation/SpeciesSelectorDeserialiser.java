package com.ferreusveritas.dynamictrees.worldgen.deserialisation;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.deserialisation.DeserialisationException;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.ferreusveritas.dynamictrees.deserialisation.JsonHelper;
import com.ferreusveritas.dynamictrees.deserialisation.result.JsonResult;
import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Gets an {@link BiomePropertySelectors.SpeciesSelector} object from a {@link JsonElement}.
 *
 * @author Harley O'Connor
 */
public final class SpeciesSelectorDeserialiser implements JsonBiomeDatabaseDeserialiser<BiomePropertySelectors.SpeciesSelector> {

    @Override
    public Result<BiomePropertySelectors.SpeciesSelector, JsonElement> deserialise(final JsonElement input) {
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

    private BiomePropertySelectors.SpeciesSelector readStatic(String string) throws DeserialisationException {
        if (this.isDefault(string)) {
            return new BiomePropertySelectors.StaticSpeciesSelector();
        }
        throw new DeserialisationException("\"" + string + "\" is not a supported parameter for a " +
                "static species selector.");
    }

    private BiomePropertySelectors.SpeciesSelector readSelector(JsonObject object, Consumer<String> warningConsumer) {
        final AtomicReference<BiomePropertySelectors.SpeciesSelector> selection = new AtomicReference<>();

        JsonHelper.JsonObjectReader.of(object)
                .ifContains(STATIC, staticElement ->
                        JsonResult.forInput(staticElement)
                                .mapIfType(Species.class, this::readStatic)
                                .elseMapIfType(String.class, this::readStatic)
                                .elseTypeError()
                                .ifSuccessOrElse(selection::set, warningConsumer, warningConsumer)
                )
                .elseIfContains(RANDOM, randomElement ->
                        selection.set(this.getRandomSpeciesSelector(randomElement, warningConsumer))
                ).elseRun(() -> warningConsumer.accept("Species selector did not have one of either elements '" +
                        STATIC + "' or '" + RANDOM + "'."));

        return selection.get();
    }

    @Nullable
    private BiomePropertySelectors.SpeciesSelector getRandomSpeciesSelector(JsonElement input,
                                                                            Consumer<String> warningConsumer) {
        final AtomicReference<BiomePropertySelectors.SpeciesSelector> selectorResult = new AtomicReference<>();

        JsonDeserialisers.JSON_OBJECT.deserialise(input).ifSuccessOrElse(
                object -> {
                    final BiomePropertySelectors.RandomSpeciesSelector randomSelector = new BiomePropertySelectors.RandomSpeciesSelector();
                    object.entrySet().forEach(entry -> {
                        final String speciesName = entry.getKey();

                        JsonDeserialisers.INTEGER.deserialise(entry.getValue()).ifSuccessOrElse(weight -> {
                            if (weight > 0) {
                                if (this.isDefault(speciesName)) {
                                    randomSelector.add(weight);
                                } else {
                                    TreeRegistry.findSpeciesSloppy(speciesName).ifValid((species) -> randomSelector.add(species, weight));
                                }
                            }
                        }, warningConsumer, warningConsumer);
                    });

                    if (randomSelector.getSize() < 1) {
                        // TODO: Object reader that allows for errors and cleans up this mess.
                        warningConsumer.accept("No species were selected in random selector '" + input + "'.");
                    } else {
                        selectorResult.set(randomSelector);
                    }
                },
                warningConsumer,
                warningConsumer
        );

        return selectorResult.get();
    }

}
