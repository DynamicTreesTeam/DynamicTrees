package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.deserialisation.result.JsonResult;
import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

/**
 * A {@link Codec} wrapper for a {@link JsonDeserialiser}.
 *
 * @author Harley O'Connor
 */
public final class CodecDeserialiserWrapper<O> implements JsonDeserialiser<O> {

    private final Codec<O> codec;

    public CodecDeserialiserWrapper(Codec<O> codec) {
        this.codec = codec;
    }

    @Override
    public Result<O, JsonElement> deserialise(JsonElement input) {
        final DataResult<Pair<O, JsonElement>> dataResult = codec.decode(JsonOps.INSTANCE, input);
        if (!dataResult.result().isPresent()) {
            return JsonResult.failure(
                    input,
                    dataResult.error()
                            .map(DataResult.PartialResult::message)
                            .orElse("Unknown de-serialisation error.")
            );
        }
        return JsonResult.success(input, dataResult.result().get().getFirst());
    }

}
