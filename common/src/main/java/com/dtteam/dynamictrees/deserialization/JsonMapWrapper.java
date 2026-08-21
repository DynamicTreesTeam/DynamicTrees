package com.dtteam.dynamictrees.deserialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

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

    public int size() {
        return this.jsonObject.size();
    }

    public boolean isEmpty() {
        return this.jsonObject.isEmpty();
    }

    public boolean containsKey(Object key) {
        if (!(key instanceof String)) {
            return false;
        }
        return this.jsonObject.has(((String) key));
    }

    public boolean containsValue(Object value) {
        return this.jsonObject.entrySet()
                .stream()
                .map(Entry::getValue)
                .anyMatch(jsonElement -> jsonElement.equals(value));
    }

    @Nullable
    public JsonElement get(Object key) {
        if (!(key instanceof String)) {
            return null;
        }
        return this.jsonObject.get(((String) key));
    }

    public JsonElement put(String key, JsonElement value) {
        final JsonElement previousElement = this.jsonObject.get(key);
        this.jsonObject.add(key, value);
        return previousElement;
    }

    @Nullable
    public JsonElement remove(Object key) {
        if (!(key instanceof String)) {
            return null;
        }
        return this.jsonObject.remove(((String) key));
    }

    public void putAll(Map<? extends String, ? extends JsonElement> m) {
        m.forEach(this.jsonObject::add);
    }

    public void clear() {
        this.jsonObject.entrySet().stream()
                .map(Entry::getKey)
                .forEach(this.jsonObject::remove);
    }

    public Set<String> keySet() {
        return this.jsonObject.entrySet().stream()
                .map(Entry::getKey)
                .collect(Collectors.toSet());
    }

    public Collection<JsonElement> values() {
        return this.jsonObject.entrySet().stream()
                .map(Entry::getValue)
                .collect(Collectors.toList());
    }

    public Set<Entry<String, JsonElement>> entrySet() {
        return this.jsonObject.entrySet();
    }

}
