package com.ferreusveritas.dynamictrees.util.json;

import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesPaging;
import com.google.gson.*;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

public class JsonHelper {

	private static final Logger LOGGER = LogManager.getLogger();

	public enum ResourceFolder {
		ASSETS("assets/"),
		DATA("data/"),
		TREES("trees/");

		private final String folderName;

		ResourceFolder(String folderName) {
			this.folderName = folderName;
		}
	}

	@Nullable
	public static JsonElement load(ResourceLocation jsonLocation) {
		return load(jsonLocation, ResourceFolder.DATA);
	}

	@Nullable
	public static JsonElement load(ResourceLocation jsonLocation, ResourceFolder resourceFolder) {
		String filename = resourceFolder.folderName + jsonLocation.getNamespace() + "/" + (resourceFolder == ResourceFolder.DATA ? "trees/" : "") + jsonLocation.getPath();
		InputStream in = LeavesPaging.class.getClassLoader().getResourceAsStream(filename);
		if(in == null) {
			LOGGER.fatal("Could not open resource " + filename);
			return null;
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		return new Gson().fromJson(reader, JsonElement.class);
	}

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
	 * Determines if a {@link JsonElement} is a comment (comments start with an underscore).
	 *
	 * @param jsonElement The {@link JsonElement} object.
	 * @return True if {@link JsonElement} is a comment.
	 */
	public static boolean isComment(final JsonElement jsonElement) {
		final ObjectFetchResult<String> fetchResult = JsonObjectGetters.STRING_GETTER.get(jsonElement);
		return fetchResult.wasSuccessful() && isComment(fetchResult.getValue());
	}

	/**
	 * Determines if the key of a {@link JsonElement} is a comment (comments start with
	 * an underscore).
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
	@SuppressWarnings("unchecked")
	public static <T> T getFromPrimitive (JsonPrimitive jsonPrimitive, Class<T> type) {
		for (JsonPrimitives primitive : JsonPrimitives.values()) {
			if (primitive.get(jsonPrimitive) != null) {
				return (T) primitive.get(jsonPrimitive);
			}
		}
		return null;
	}

	public enum JsonPrimitives {
		STRING(String.class), BOOLEAN(Boolean.class), INTEGER(Integer.class), LONG(Long.class), DOUBLE(Double.class), FLOAT(Float.class);

		private final Class<?> typeClass;

		JsonPrimitives(Class<?> typeClass) {
			this.typeClass = typeClass;
		}

		public boolean isOfType (JsonPrimitive jsonPrimitive) {
			boolean isOfType = true;
			switch (this) {
				case STRING: isOfType = jsonPrimitive.isString(); break;
				case BOOLEAN: isOfType = jsonPrimitive.isBoolean(); break;
				case INTEGER:
					try { jsonPrimitive.getAsInt(); } catch (NumberFormatException e) { isOfType = false; } break;
				case LONG:
					try { jsonPrimitive.getAsLong(); } catch (NumberFormatException e) { isOfType = false; } break;
				case DOUBLE:
					try { jsonPrimitive.getAsDouble(); } catch (NumberFormatException e) { isOfType = false; } break;
				case FLOAT:
					try { jsonPrimitive.getAsFloat(); } catch (NumberFormatException e) { isOfType = false; } break;
				default:
					isOfType = false; break;
			}
			return isOfType;
		}

		@Nullable
		public Object get (JsonPrimitive jsonPrimitive) {
			if (!this.isOfType(jsonPrimitive))
				return null;

			// Return the value, or null. Cast any primitive values to their object form.
			switch (this) {
				case STRING: return jsonPrimitive.getAsString();
				case BOOLEAN: return this.typeClass.cast(jsonPrimitive.getAsBoolean());
				case INTEGER: return this.typeClass.cast(jsonPrimitive.getAsInt());
				case LONG: return this.typeClass.cast(jsonPrimitive.getAsLong());
				case FLOAT: return this.typeClass.cast(jsonPrimitive.getAsFloat());
				default: return null;
			}
		}
	}

}
