package com.ferreusveritas.dynamictrees.util.json;

import com.ferreusveritas.dynamictrees.systems.dropcreators.drops.Drops;
import com.ferreusveritas.dynamictrees.systems.dropcreators.drops.NormalDrops;
import com.ferreusveritas.dynamictrees.systems.dropcreators.drops.WeightedDrops;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Harley O'Connor
 */
public final class DropsGetter implements IJsonObjectGetter<Drops> {

    public static final Map<String, DropsEntry<?>> DROPS_TYPES = new HashMap<>();

    public static final class DropsEntry<D extends Drops> {
        private final Class<D> dropsClass;
        private final Codec<D> codec;

        public DropsEntry(Class<D> dropsClass, Codec<D> codec) {
            this.dropsClass = dropsClass;
            this.codec = codec;
        }
    }

    static {
        DROPS_TYPES.put("normal", new DropsEntry<>(NormalDrops.class, NormalDrops.CODEC));
        DROPS_TYPES.put("weighted", new DropsEntry<>(WeightedDrops.class, WeightedDrops.CODEC));
    }

    @Override
    @SuppressWarnings("unchecked")
    public ObjectFetchResult<Drops> get(JsonElement jsonElement) {
        return JsonObjectGetters.JSON_OBJECT.get(jsonElement).map(object -> {
            final String id = JsonHelper.getOrDefault(object, "id", String.class, null);
            if (id == null) {
                return null;
            }

            final DropsEntry<Drops> drops = (DropsEntry<Drops>) DROPS_TYPES.get(id);
            if (drops == null) {
                return null;
            }

            final JsonObject properties = JsonHelper.getOrDefault(object, "properties", JsonObject.class, new JsonObject());
            return drops.codec.decode(JsonOps.INSTANCE, properties)
                    .result().map(Pair::getFirst).orElse(null);
        }, "Error de-serialising drops.");
    }

}
