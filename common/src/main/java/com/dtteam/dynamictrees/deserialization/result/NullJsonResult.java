package com.dtteam.dynamictrees.deserialization.result;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * An implementation of {@link JsonResult} that is
 */
public class NullJsonResult<T> extends JsonResult<T> {

    public NullJsonResult(JsonElement input, @Nullable T value, @Nullable String error) {
        super(input, value, error);
    }

    public NullJsonResult(JsonElement input, @Nullable T value, @Nullable String error, List<String> warnings) {
        super(input, value, error, warnings);
    }

    @Override
    public boolean success() {
        return true;
    }
}
