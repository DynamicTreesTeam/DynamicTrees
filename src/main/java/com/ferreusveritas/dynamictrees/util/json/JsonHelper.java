package com.ferreusveritas.dynamictrees.util.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;

public class JsonHelper {

	private static final Logger LOGGER = LogManager.getLogger();

	@Nullable
	public static JsonElement load(@Nullable final File file) {

		if (file != null && file.exists() && file.isFile() && file.canRead()) {
			String fileName = file.getAbsolutePath();

			try {
				JsonParser parser = new JsonParser();
				return parser.parse(new FileReader(file));
			}
			catch (Exception e) {
				LOGGER.fatal("Can't open " + fileName + ": " + e.getMessage());
			}
		}

		return null;
	}

	/**
	 * Determines if the key of a {@link JsonElement} is a comment (comments start with
	 * an underscore).
	 *
	 * @param jsonElement The {@link JsonElement} object.
	 * @return True if {@link JsonElement} is a comment.
	 */
	public static boolean isComment(final JsonElement jsonElement) {
		final ObjectFetchResult<String> fetchResult = JsonObjectGetters.STRING_GETTER.get(jsonElement);
		return fetchResult.wasSuccessful() && isComment(fetchResult.getValue());
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
	 * Gets the boolean value from the element name of the {@link JsonObject} given, or
	 * returns the default value given if the element was not found or wasn't a boolean.
	 *
	 * @param jsonObject The {@link JsonObject}.
	 * @param elementName The name of the element to get.
	 * @param defaultValue The default value if it couldn't be obtained.
	 * @return The boolean value.
	 */
	@SuppressWarnings("boxing")
	public static boolean getOrDefault (final JsonObject jsonObject, final String elementName, final boolean defaultValue) {
		final JsonElement element = jsonObject.get(elementName);

		if (element == null)
			return defaultValue;

		final ObjectFetchResult<Boolean> fetchResult = JsonObjectGetters.BOOLEAN_GETTER.get(element);
		return fetchResult.wasSuccessful() && fetchResult.getValue();
	}

	@Nullable
	public static <T> T getFromObjectOrWarn(final JsonObject jsonObject, final String key, final Class<T> classToGet, final String errorPrefix, final boolean required) {
		if (!jsonObject.has(key)) {
			if (required)
				LOGGER.warn("{} {}", errorPrefix, "Element didn't exist.");
			return null;
		}

		final ObjectFetchResult<T> fetchResult = JsonObjectGetters.getObjectGetter(classToGet).get(jsonObject.get(key));

		if (!fetchResult.wasSuccessful()) {
			LOGGER.warn("{} {}", errorPrefix, fetchResult.getErrorMessage());
			return null;
		}

		return fetchResult.getValue();
	}

}
