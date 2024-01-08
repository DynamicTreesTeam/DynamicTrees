package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.util.IgnoreThrowable;
import com.ferreusveritas.dynamictrees.util.JsonMapWrapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class JsonHelper {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Gson GSON = (new GsonBuilder())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public static Gson getGson() {
        return GSON;
    }

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

    public static <T> Optional<T> getAsOptional(JsonObject object, String key, JsonDeserialiser<T> deserialiser) throws DeserialisationException {
        final JsonElement element = object.get(key);
        return element == null ? Optional.empty() :
                deserialiser.deserialise(element)
                        .map(Optional::ofNullable)
                        .orElseThrow();
    }

    public static BlockBehaviour.Properties getBlockProperties(final JsonObject jsonObject, final MapColor defaultMapColor, final Function<MapColor, BlockBehaviour.Properties> defaultPropertiesGetter, final Consumer<String> errorConsumer, final Consumer<String> warningConsumer) {
        final BlockBehaviour.Properties properties = defaultPropertiesGetter.apply(JsonHelper.getOrDefault(jsonObject, "map_color", MapColor.class, defaultMapColor));

        JsonPropertyApplierLists.PROPERTIES.applyAll(new JsonMapWrapper(jsonObject), properties)
                .forEachErrorWarning(errorConsumer, warningConsumer);
        return properties;
    }

    public static <T extends Throwable> void throwIfNotJsonObject(JsonElement json, Supplier<T> throwableSupplier)
            throws T {
        if (!json.isJsonObject()) {
            throw throwableSupplier.get();
        }
    }

    public static void throwIfShouldNotLoad(JsonObject json) throws IgnoreThrowable {
        final String key = "only_if_loaded";
        if (json.has(key)) {
            final boolean continueLoading = JsonDeserialisers.STRING.deserialise(json.get(key))
                    .map(modId -> ModList.get().isLoaded(modId))
                    .orElse(true);
            if (!continueLoading) {
                throw IgnoreThrowable.INSTANCE;
            }
        }
    }

}
