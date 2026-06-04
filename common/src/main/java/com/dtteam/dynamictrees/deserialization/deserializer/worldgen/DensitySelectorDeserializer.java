package com.dtteam.dynamictrees.deserialization.deserializer.worldgen;

import com.dtteam.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.dtteam.dynamictrees.deserialization.*;
import com.dtteam.dynamictrees.deserialization.math.ExpressionParser;
import com.dtteam.dynamictrees.deserialization.math.operator.MathOperator;
import com.dtteam.dynamictrees.deserialization.math.operator.Scale;
import com.dtteam.dynamictrees.deserialization.result.JsonResult;
import com.dtteam.dynamictrees.deserialization.result.Result;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

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
                .mapIfType(JsonObject.class, this::readDensitySelector) // Deprecated
                .elseMapIfType(JsonArray.class, this::createScaleDensitySelector) // Deprecated
                .elseMapIfType(Double.class, this::createStaticDensitySelector)
                .elseMapIfType(String.class, this::readExpressionSelector)
                .elseTypeError();
    }

    private BiomePropertySelectors.DensitySelector createStaticDensitySelector(double density) {
        return mc -> density;
    }

    @Deprecated
    private BiomePropertySelectors.DensitySelector createScaleDensitySelector(
        JsonArray jsonArray,
        Consumer<String> warningConsumer
    ) {
        final List<Double> parameters = new ArrayList<>();

        for (final JsonElement element : jsonArray) {
            JsonDeserializers.DOUBLE.deserialize(element).ifSuccessOrElse(
                    parameters::add,
                    warningConsumer,
                    warningConsumer
            );
        }
        MathOperator mathOperator = new Scale(parameters);
        return mathOperator::apply;
    }

    @Nullable
    @Deprecated
    private BiomePropertySelectors.DensitySelector readDensitySelector(
        JsonObject jsonObject,
        Consumer<String> warningConsumer
    ) throws DeserializationException {
        return JsonResult.forInput(jsonObject)
                .mapIfContains(SCALE, JsonArray.class, this::createScaleDensitySelector)
                .elseMapIfContains(STATIC, Double.class, this::createStaticDensitySelector)
                .elseMapIfContains(MATH, JsonElement.class, input -> {
                    final JsonMath jsonMath = new JsonMath(input);
                    return jsonMath::apply;
                }).elseTypeError()
                .forEachWarning(warningConsumer)
                .orElseThrow();
    }
    
    private BiomePropertySelectors.DensitySelector readExpressionSelector(
        String string,
        Consumer<String> warningConsumer
    ) {
        try {
            MathOperator expression = ExpressionParser.parse(string);
            return expression::apply;
        } catch (Exception e) {
            warningConsumer.accept("Failed to parse expression: \"" + string + "\", error: " + e.getMessage());
            return mc -> 0.0f;
        }
    }
    
}
