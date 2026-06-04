package com.dtteam.dynamictrees.deserialization.deserializer.worldgen;

import com.dtteam.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.dtteam.dynamictrees.deserialization.DeserializationException;
import com.dtteam.dynamictrees.deserialization.math.MathOperator;
import com.dtteam.dynamictrees.deserialization.math.ExpressionParser;
import com.dtteam.dynamictrees.deserialization.JsonMath;
import com.dtteam.dynamictrees.deserialization.result.JsonResult;
import com.dtteam.dynamictrees.deserialization.result.Result;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Gets an {@link BiomePropertySelectors.ChanceSelector} object from a {@link JsonElement}.
 *
 * @author Harley O'Connor
 */
public final class ChanceSelectorDeserializer implements JsonBiomeDatabaseDeserializer<BiomePropertySelectors.ChanceSelector> {

    @Override
    public Result<BiomePropertySelectors.ChanceSelector, JsonElement> deserialize(JsonElement input) {
        return JsonResult.forInput(input)
                .mapIfType(JsonObject.class, this::readJsonChanceSelector) // Deprecated
                .elseMapIfType(Double.class, this::createSimpleChanceSelector)
                .elseMapIfType(String.class, this::readExpressionSelector)
                .elseTypeError();
    }

    private BiomePropertySelectors.ChanceSelector createSimpleChanceSelector(double value) {
        if (value <= 0) {
            return mc -> BiomePropertySelectors.Chance.CANCEL;
        } 
        if (value >= 1) {
            return mc -> BiomePropertySelectors.Chance.OK;
        }
        return mc -> mc.rand().nextDouble() < value ?
                BiomePropertySelectors.Chance.OK : BiomePropertySelectors.Chance.CANCEL;
    }

    @Nullable
    @Deprecated
    private BiomePropertySelectors.ChanceSelector readJsonChanceSelector(JsonObject jsonObject, Consumer<String> warningConsumer)
            throws DeserializationException {
        return JsonResult.forInput(jsonObject)
                .mapIfContains(STATIC, JsonElement.class, this::createSimpleChanceSelectorFromJson)
                .elseMapIfContains(MATH, JsonElement.class, this::createMathSelector)
                .forEachWarning(warningConsumer)
                .orElseThrow();
    }

    @Deprecated
    private BiomePropertySelectors.ChanceSelector createSimpleChanceSelectorFromJson(JsonElement element, Consumer<String> warningConsumer)
            throws DeserializationException {
        if (element.getAsJsonPrimitive().isNumber()) {
            return createSimpleChanceSelector(element.getAsFloat());
        }
        if (element.getAsJsonPrimitive().isString() && isDefault(element.getAsString())) {
            return mc -> BiomePropertySelectors.Chance.UNHANDLED;
        }
        throw new DeserializationException("Unrecognised named chance selector \"" + element.getAsString() + "\".");
    }

    @Deprecated
    private BiomePropertySelectors.ChanceSelector createMathSelector(JsonElement element, Consumer<String> warningConsumer) {
        final JsonMath jsonMath = new JsonMath(element);
        return mc -> mc.rand().nextDouble() < jsonMath.apply(mc)
            ? BiomePropertySelectors.Chance.OK
            : BiomePropertySelectors.Chance.CANCEL;
    }
    
    private BiomePropertySelectors.ChanceSelector readExpressionSelector(
        String string,
        Consumer<String> warningConsumer
    ) {
        
        if ("standard".equalsIgnoreCase(string)) {
            return mc -> mc.rand().nextFloat() < (mc.radius() > 3 ? 2.0f / mc.radius() : 1.0f) ?
                BiomePropertySelectors.Chance.OK : BiomePropertySelectors.Chance.CANCEL;
        }
        
        try {
            MathOperator expression =  ExpressionParser.parse(string);
            return mc -> mc.rand().nextDouble() < expression.apply(mc) ? BiomePropertySelectors.Chance.OK : BiomePropertySelectors.Chance.CANCEL;
        } catch (Exception e) {
            warningConsumer.accept("Failed to parse expression: \"" + string + "\", error: " + e.getMessage());
            return mc -> BiomePropertySelectors.Chance.CANCEL;
        }
    }
    
}
