package com.ferreusveritas.dynamictrees.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A {@link JsonObject} wrapper implementing {@link Map}.
 *
 * @author Harley O'Connor
 */
public final class JsonMapWrapper implements Map<String, JsonElement> {

    private final JsonObject jsonObject;

    public JsonMapWrapper(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    @Override
    public int size() {
        return this.jsonObject.size();
    }

    @Override
    public boolean isEmpty() {
        return this.jsonObject.size() < 1;
    }

    @Override
    public boolean containsKey(Object key) {
        if (!(key instanceof String)) {
            return false;
        }
        return this.jsonObject.has(((String) key));
    }

    @Override
    public boolean containsValue(Object value) {
        return this.jsonObject.entrySet()
                .stream()
                .map(Entry::getValue)
                .anyMatch(jsonElement -> jsonElement.equals(value));
    }

    @Nullable
    @Override
    public JsonElement get(Object key) {
        if (!(key instanceof String)) {
            return null;
        }
        return this.jsonObject.get(((String) key));
    }

    @Override
    public JsonElement put(String key, JsonElement value) {
        final JsonElement previousElement = this.jsonObject.get(key);
        this.jsonObject.add(key, value);
        return previousElement;
    }

    @Nullable
    @Override
    public JsonElement remove(Object key) {
        if (!(key instanceof String)) {
            return null;
        }
        return this.jsonObject.remove(((String) key));
    }

    @Override
    public void putAll(Map<? extends String, ? extends JsonElement> m) {
        m.forEach(this.jsonObject::add);
    }

    @Override
    public void clear() {
        this.jsonObject.entrySet().stream()
                .map(Entry::getKey)
                .forEach(this.jsonObject::remove);
    }

    @Override
    public Set<String> keySet() {
        return this.jsonObject.entrySet().stream()
                .map(Entry::getKey)
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<JsonElement> values() {
        return this.jsonObject.entrySet().stream()
                .map(Entry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public Set<Entry<String, JsonElement>> entrySet() {
        return this.jsonObject.entrySet();
    }

}
