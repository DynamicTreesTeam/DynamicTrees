package com.dtteam.dynamictrees.model.loader;

import com.dtteam.dynamictrees.model.geometry.PalmLeavesModelGeometry;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.IdentifierException;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class PalmLeavesModelLoader implements IGeometryLoader<PalmLeavesModelGeometry> {

    public static final Logger LOGGER = LogManager.getLogger();

    private static final String FROND = "frond";
    private static final String TEXTURES = "textures";

    private final int frondType;

    public PalmLeavesModelLoader(int type){
        frondType = type;
    }

    @Override
    public PalmLeavesModelGeometry read(JsonObject modelObject, JsonDeserializationContext deserializationContext) {
        final JsonObject textures = this.getTexturesObject(modelObject);
        return new PalmLeavesModelGeometry(getTextureLocation(textures, FROND), frondType);
    }

    protected Identifier getTextureLocation (final JsonObject textureObject, final String textureElement) {
        try {
            return this.getResLocOrThrow(this.getOrThrow(textureObject, textureElement));
        } catch (final RuntimeException e) {
            LOGGER.error("{} missing or did not have valid \"{}\" texture location element, using missing " +
                    "texture.", this.getModelTypeName(), textureElement);
            return MissingTextureAtlasSprite.getLocation();
        }
    }

    protected JsonObject getTexturesObject (final JsonObject modelContents) {
        if (!modelContents.has(TEXTURES) || !modelContents.get(TEXTURES).isJsonObject())
            this.throwRequiresElement(TEXTURES, "Json Object");

        return modelContents.getAsJsonObject(TEXTURES);
    }

    protected Identifier getResLocOrThrow(final String resLocStr) {
        try {
            return Identifier.parse(resLocStr);
        } catch (IdentifierException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getOrThrow(final JsonObject jsonObject, final String identifier) {
        if (jsonObject.get(identifier) == null || !jsonObject.get(identifier).isJsonPrimitive() ||
                !jsonObject.get(identifier).getAsJsonPrimitive().isString())
            this.throwRequiresElement(identifier, "String");

        return jsonObject.get(identifier).getAsString();
    }

    protected void throwRequiresElement (final String element, final String expectedType) {
        throw new RuntimeException(this.getModelTypeName() + " requires a valid \"" + element + "\" element of " +
                "type " + expectedType + ".");
    }

    /**
     * @return The type of model the class is loading. Useful for warnings when using sub-classes.
     */
    protected String getModelTypeName () {
        return "Palm Fronds";
    }

}