package com.ferreusveritas.dynamictrees.util.json;

import com.ferreusveritas.dynamictrees.systems.dropcreators.drops.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class DropsGetter implements IJsonObjectGetter<Drops> {

    public static final Map<String, Codec<Drops>> DROPS_TYPES = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <D extends Drops> void registerCodec(String id, Codec<D> dropsCodec) {
        DROPS_TYPES.put(id, ((Codec<Drops>) dropsCodec));
    }

    static {
        registerCodec("stack", StackDrops.CODEC);
        registerCodec("weighted", WeightedDrops.CODEC);
    }

    @Override
    public ObjectFetchResult<Drops> get(JsonElement jsonElement) {
        return JsonObjectGetters.JSON_OBJECT.get(jsonElement).map(object -> {
            final String id = JsonHelper.getOrDefault(object, "id", String.class, null);
            if (id == null) {
                return null;
            }

            final Codec<Drops> codec = DROPS_TYPES.get(id);
            if (codec == null) {
                return null;
            }

            final JsonObject properties = JsonHelper.getOrDefault(object, "properties", JsonObject.class, new JsonObject());
            return codec.decode(JsonOps.INSTANCE, properties)
                    .result().map(Pair::getFirst).orElse(null);
        }, "Error de-serialising drops from element \"" + jsonElement + "\".");
    }

}
