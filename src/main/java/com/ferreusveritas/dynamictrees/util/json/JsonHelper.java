package com.ferreusveritas.dynamictrees.util.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
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
		final ObjectFetchResult<String> fetchResult = JsonObjectGetters.STRING.get(jsonElement);
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
	public static <T> T getOrDefault (final JsonObject jsonObject, final String elementName, final Class<T> type, final T defaultValue) {
		final JsonElement element = jsonObject.get(elementName);

		if (element == null)
			return defaultValue;

		final T result = JsonObjectGetters.getObjectGetter(type).get(element).getValue();
		return result == null ? defaultValue : result;
	}

	/**
	 * Gets the value of type {@link T} from the {@link JsonObject} using the given key,
	 * or null if it was not found. It also prints a warning with the given <tt>errorPrefix</tt>
	 * if it was unsuccessful in fetching the type {@link T}, or if the element was not
	 * found for the key if the <tt>required</tt> parameter is true.
	 *
	 * @param jsonObject The {@link JsonObject} to get from.
	 * @param key The key of the value to get.
	 * @param classToGet The {@link Class} object of type {@link T}.
	 * @param errorPrefix The message to display as a prefix to the warning.
	 * @param required True if the user should be warned if the element doesn't exist.
	 * @param <T> The type of the {@link Object} to fetch.
	 * @return The {@link Object} of type {@link T}, or null if it was not found or unsuccessful.
	 */
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

	public static JsonObjectReader ifContains (final JsonObject jsonObject, final String key, final Consumer<JsonElement> elementConsumer) {
		return new JsonObjectReader(jsonObject).ifContains(key, elementConsumer);
	}

	public static final class JsonElementReader {
		private final JsonElement jsonElement;
		private boolean read = false;
		private String lastError;
		private final List<Class<?>> attemptedClasses = new ArrayList<>();

		private JsonElementReader(JsonElement jsonElement) {
			this.jsonElement = jsonElement;
		}

		public <T> JsonElementReader ifOfType (final Class<T> typeClass, final Consumer<T> consumer) {
			JsonObjectGetters.getObjectGetter(typeClass).get(jsonElement).ifSuccessful(value -> {
				consumer.accept(value);
				this.read = true;
			}).otherwise(errorMessage -> this.lastError = errorMessage).otherwise(() -> this.attemptedClasses.add(typeClass));
			return this;
		}

		public <T> JsonElementReader ifArrayForEach (final Class<T> typeClass, final Consumer<T> consumer) {
			JsonObjectGetters.JSON_ARRAY.get(jsonElement).ifSuccessful(jsonArray -> {
				jsonArray.forEach(arrayElement -> JsonObjectGetters.getObjectGetter(typeClass).get(arrayElement)
						.ifSuccessful(consumer).otherwise(errorMessage -> this.lastError = errorMessage));
				this.read = true;
			}).otherwise(() -> this.attemptedClasses.add(typeClass));
			return this;
		}

		public <T> JsonElementReader elseIfOfType (final Class<T> typeClass, final Consumer<T> consumer) {
			if (!this.read)
				this.ifOfType(typeClass, consumer);
			return this;
		}

		public JsonElementReader ifFailed(final Consumer<String> errorConsumer) {
			if (!this.read && this.lastError != null)
				errorConsumer.accept(this.lastError);
			return this;
		}

		public JsonElementReader elseWarn (final String warningMessage) {
			if (!this.read)
				LogManager.getLogger().warn(warningMessage);
			return this;
		}

		public JsonElementReader elseUnsupportedError(final Consumer<String> errorConsumer) {
			if (!this.read) {
				final StringBuilder stringBuilder = new StringBuilder("Element was not one of the following types: ");
				for (int i = 0; i < this.attemptedClasses.size(); i++) {
					stringBuilder.append(this.attemptedClasses.get(i)).append(i != this.attemptedClasses.size() - 1 ? ", " : "");
				}
				errorConsumer.accept(stringBuilder.toString());
			}
			return this;
		}

		public String getLastError() {
			return lastError;
		}

		public static JsonElementReader of (final JsonElement jsonElement) {
			return new JsonElementReader(jsonElement);
		}

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
				this.lastError = JsonElementReader.of(this.jsonObject.get(key)).ifOfType(type, typeConsumer).getLastError();
				this.read = true;
			}
			return this;
		}

		public JsonObjectReader elseIfContains(final String key, final Consumer<JsonElement> elementConsumer) {
			if (!this.read)
				this.ifContains(key, elementConsumer);
			return this;
		}

		public <T> JsonObjectReader elseIfContains(final String key, final Class<T> type, final Consumer<T> typeConsumer) {
			if (!this.read)
				this.ifContains(key, type, typeConsumer);
			return this;
		}

		public JsonObjectReader elseWarn(final String errorPrefix) {
			if (this.lastError != null) {
				LogManager.getLogger().warn(errorPrefix + this.lastError);
			}
			return this;
		}

		public JsonObjectReader elseRun(final Runnable runnable) {
			if (!this.read)
				runnable.run();
			return this;
		}

		public JsonObjectReader reset () {
			this.read = false;
			this.lastError = null;
			return this;
		}

		public static JsonObjectReader of (final JsonObject jsonObject) {
			return new JsonObjectReader(jsonObject);
		}

	}

}
