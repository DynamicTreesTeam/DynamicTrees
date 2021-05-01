package com.ferreusveritas.dynamictrees.worldgen.json;

import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.util.json.JsonHelper;
import com.ferreusveritas.dynamictrees.util.json.ObjectFetchResult;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Gets an {@link BiomePropertySelectors.IChanceSelector} object from a {@link JsonElement}.
 *
 * @author Harley O'Connor
 */
public final class ChanceSelectorGetter implements IJsonBiomeDatabaseObjectGetter<BiomePropertySelectors.IChanceSelector> {

    @Override
    public ObjectFetchResult<BiomePropertySelectors.IChanceSelector> get(JsonElement jsonElement) {
        final ObjectFetchResult<BiomePropertySelectors.IChanceSelector> chanceSelector = new ObjectFetchResult<>();

        JsonHelper.JsonElementReader.of(jsonElement).ifOfType(JsonObject.class, jsonObject -> chanceSelector.copyFrom(this.readChanceSelector(jsonObject)))
                .elseIfOfType(Float.class, chance -> chanceSelector.setValue(createSimpleChanceSelector(chance)))
                .elseIfOfType(String.class, str -> {
                    if (this.isDefault(str))
                        chanceSelector.setValue((rnd, spc, rad) -> rnd.nextFloat() < (rad > 3 ? 2.0f / rad : 1.0f) ?
                                BiomePropertySelectors.Chance.OK : BiomePropertySelectors.Chance.CANCEL);
                }).elseUnsupportedError(chanceSelector::setErrorMessage);

        return chanceSelector;
    }

    private static BiomePropertySelectors.IChanceSelector createSimpleChanceSelector(float value) {
        if (value <= 0) {
            return (rnd, spc, rad) -> BiomePropertySelectors.Chance.CANCEL;
        } else if (value >= 1) {
            return (rnd, spc, rad) -> BiomePropertySelectors.Chance.OK;
        }
        return (rnd, spc, rad) -> rnd.nextFloat() < value ? BiomePropertySelectors.Chance.OK : BiomePropertySelectors.Chance.CANCEL;
    }

    private ObjectFetchResult<BiomePropertySelectors.IChanceSelector> readChanceSelector(final JsonObject jsonObject) {
        final ObjectFetchResult<BiomePropertySelectors.IChanceSelector> chanceSelector = new ObjectFetchResult<>();

        JsonHelper.JsonObjectReader.of(jsonObject)
                .ifContains(STATIC, jsonElement ->
                    JsonHelper.JsonElementReader.of(jsonElement)
                            .ifOfType(Float.class, chance -> chanceSelector.setValue(createSimpleChanceSelector(chance))).ifFailed(chanceSelector::addWarning)
                            .elseIfOfType(String.class, str -> {
                                if (this.isDefault(str))
                                    chanceSelector.setValue((rnd, spc, rad) -> BiomePropertySelectors.Chance.UNHANDLED);
                            }).ifFailed(chanceSelector::setErrorMessage))
                .elseIfContains(MATH, jsonElement -> {
                    final JsonMath jsonMath = new JsonMath(jsonElement);
                    chanceSelector.setValue((rnd, spc, rad) -> rnd.nextFloat() < jsonMath.apply(rnd, spc, rad) ?
                            BiomePropertySelectors.Chance.OK : BiomePropertySelectors.Chance.CANCEL);
                }).elseRun(() -> chanceSelector.setErrorMessage("Could not get Json object chance selector as it did not contain key '" + STATIC + "' or '" + MATH + "'."));

        return chanceSelector;
    }

}
