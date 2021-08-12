package com.ferreusveritas.dynamictrees.deserialisation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class JsonHelper {

    private static final Logger LOGGER = LogManager.getLogger();

    @Nullable
    public static JsonElement load(@Nullable final File file) {
        if (file != null && file.exists() && file.isFile() && file.canRead()) {
            String fileName = file.getAbsolutePath();

            try {
                JsonParser parser = new JsonParser();
                return parser.parse(new FileReader(file));
            } catch (Exception e) {
                LOGGER.fatal("Can't open " + fileName + ": " + e.getMessage());
            }
        }

        return null;
    }

    /**
     * Determines if the key of a {@link JsonElement} is a comment (comments start with an underscore).
     *
     * @param jsonElement The {@link JsonElement} object.
     * @return True if {@link JsonElement} is a comment.
     */
    @SuppressWarnings("Convert2MethodRef") // Can't use method ref, "ambiguous call".
    public static boolean isComment(final JsonElement jsonElement) {
        return JsonDeserialisers.STRING.deserialise(jsonElement)
                .map(string -> isComment(string))
                .orElse(false);
    }

    /**
     * Determines if a key is a comment (comments start with an underscore).
     *
     * @param key The key of the {@link JsonElement}.
     * @return True if key is a comment.
     */
    public static boolean isComment(final String key) {
        return key.startsWith("_");
    }

    /**
     * Gets the boolean value from the element name of the {@link JsonObject} given, or returns the default value given
     * if the element was not found or wasn't a boolean.
     *
     * @param jsonObject   The {@link JsonObject}.
     * @param elementName  The name of the element to get.
     * @param defaultValue The default value if it couldn't be obtained.
     * @return The boolean value.
     */
    public static <T> T getOrDefault(final JsonObject jsonObject, final String elementName, final Class<T> type, final T defaultValue) {
        final JsonElement element = jsonObject.get(elementName);

        if (element == null) {
            return defaultValue;
        }

        return JsonDeserialisers.get(type).deserialise(element).orElse(defaultValue);
    }

    /**
     * Gets the boolean value from the element name of the {@link JsonObject} given, or returns the default value given
     * if the element was not found or wasn't a boolean.
     *
     * @param jsonObject      The {@link JsonObject}.
     * @param elementName     The name of the element to get.
     * @param defaultValue    The default value if it couldn't be obtained.
     * @param errorConsumer   The {@link Consumer<String>} to accept if there is an error.
     * @param warningConsumer The {@link Consumer<String>} to accept if there is a warning.
     * @return The boolean value.
     */
    public static <T> T getOrDefault(final JsonObject jsonObject, final String elementName, final Class<T> type, final T defaultValue, final Consumer<String> errorConsumer, final Consumer<String> warningConsumer) {
        final JsonElement element = jsonObject.get(elementName);

        if (element == null) {
            return defaultValue;
        }

        return JsonDeserialisers.get(type).deserialise(element)
                .orElse(
                        defaultValue,
                        errorConsumer,
                        warningConsumer
                );
    }

    public static JsonObjectReader ifContains(final JsonObject jsonObject, final String key, final Consumer<JsonElement> elementConsumer) {
        return new JsonObjectReader(jsonObject).ifContains(key, elementConsumer);
    }

    public static AbstractBlock.Properties getBlockProperties(final JsonObject jsonObject, final Material defaultMaterial, final MaterialColor defaultMaterialColor, final BiFunction<Material, MaterialColor, AbstractBlock.Properties> defaultPropertiesGetter, final Consumer<String> errorConsumer, final Consumer<String> warningConsumer) {
        final Material material = JsonHelper.getOrDefault(jsonObject, "material", Material.class, defaultMaterial);
        final AbstractBlock.Properties properties = defaultPropertiesGetter.apply(material,
                JsonHelper.getOrDefault(jsonObject, "material_color", MaterialColor.class, defaultMaterialColor));

        JsonPropertyApplierLists.PROPERTIES.applyAll(jsonObject, properties).forEachErrorWarning(errorConsumer, warningConsumer);
        return properties;
    }

    public static final class JsonObjectReader {
        private final JsonObject jsonObject;
        private boolean read = false;
        private String lastError;

        private JsonObjectReader(JsonObject jsonObject) {
            this.jsonObject = jsonObject;
        }

        public JsonObjectReader ifContains(final String key, final Consumer<JsonElement> elementConsumer) {
            if (this.jsonObject.has(key)) {
                elementConsumer.accept(this.jsonObject.get(key));
                this.read = true;
            }
            return this;
        }

        public <T> JsonObjectReader ifContains(final String key, final Class<T> type, final Consumer<T> typeConsumer) {
            if (this.jsonObject.has(key)) {
                this.lastError = JsonDeserialisers.getOrThrow(type)
                        .deserialise(this.jsonObject.get(key)).ifSuccess(typeConsumer)
                        .getError();
                this.read = true;
            }
            return this;
        }

        public JsonObjectReader elseIfContains(final String key, final Consumer<JsonElement> elementConsumer) {
            if (!this.read) {
                this.ifContains(key, elementConsumer);
            }
            return this;
        }

        public <T> JsonObjectReader elseIfContains(final String key, final Class<T> type, final Consumer<T> typeConsumer) {
            if (!this.read) {
                this.ifContains(key, type, typeConsumer);
            }
            return this;
        }

        public JsonObjectReader elseWarn(final String errorPrefix) {
            if (this.lastError != null) {
                LogManager.getLogger().warn(errorPrefix + this.lastError);
            }
            return this;
        }

        public JsonObjectReader elseRun(final Runnable runnable) {
            if (!this.read) {
                runnable.run();
            }
            return this;
        }

        public JsonObjectReader reset() {
            this.read = false;
            this.lastError = null;
            return this;
        }

        public static JsonObjectReader of(final JsonObject jsonObject) {
            return new JsonObjectReader(jsonObject);
        }

    }

}
