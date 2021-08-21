package com.ferreusveritas.dynamictrees.worldgen.deserialisation;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.deserialisation.DeserialisationException;
import com.ferreusveritas.dynamictrees.deserialisation.JsonHelper;
import com.ferreusveritas.dynamictrees.deserialisation.result.JsonResult;
import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Gets an {@link BiomePropertySelectors.ChanceSelector} object from a {@link JsonElement}.
 *
 * @author Harley O'Connor
 */
public final class ChanceSelectorDeserialiser implements JsonBiomeDatabaseDeserialiser<BiomePropertySelectors.ChanceSelector> {

    @Override
    public Result<BiomePropertySelectors.ChanceSelector, JsonElement> deserialise(JsonElement input) {
        return JsonResult.forInput(input)
                .mapIfType(JsonObject.class, this::readChanceSelector)
                .elseMapIfType(Float.class, ChanceSelectorDeserialiser::createSimpleChanceSelector)
                .elseMapIfType(String.class, name -> {
                    if (name.equalsIgnoreCase("standard")) {
                        return (rnd, spc, rad) -> rnd.nextFloat() < (rad > 3 ? 2.0f / rad : 1.0f) ?
                                BiomePropertySelectors.Chance.OK : BiomePropertySelectors.Chance.CANCEL;
                    }
                    throw new DeserialisationException("Unrecognised named chance selector \"" + name + "\".");
                }).elseTypeError();
    }

    private static BiomePropertySelectors.ChanceSelector createSimpleChanceSelector(float value) {
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
                                                                     Consumer<String> warningConsumer) {
        final AtomicReference<BiomePropertySelectors.ChanceSelector> chanceSelector = new AtomicReference<>();

        JsonHelper.JsonObjectReader.of(jsonObject)
                .ifContains(STATIC, jsonElement ->
                        JsonResult.forInput(jsonElement)
                                .mapIfType(Float.class, ChanceSelectorDeserialiser::createSimpleChanceSelector)
                                .elseMapIfType(String.class, name -> {
                                    if (this.isDefault(name)) {
                                        return (rnd, spc, rad) -> BiomePropertySelectors.Chance.UNHANDLED;
                                    }
                                    throw new DeserialisationException("Unrecognised named chance selector \"" + name + "\".");
                                }).elseTypeError()
                                .ifSuccessOrElse(chanceSelector::set, warningConsumer, warningConsumer)
                ).elseIfContains(MATH, jsonElement -> {
                    final JsonMath jsonMath = new JsonMath(jsonElement);
                    chanceSelector.set((rnd, spc, rad) -> rnd.nextFloat() < jsonMath.apply(rnd, spc, rad) ?
                            BiomePropertySelectors.Chance.OK : BiomePropertySelectors.Chance.CANCEL);
                }).elseRun(() ->
                        warningConsumer.accept("Could not get Json object chance selector as it did not contain " +
                                "key '" + STATIC + "' or '" + MATH + "'.")
                );

        return chanceSelector.get();
    }

}
