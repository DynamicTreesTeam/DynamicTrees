package com.dtteam.dynamictrees.model.loader;

import com.dtteam.dynamictrees.model.geometry.BranchBlockModelGeometry;
import com.dtteam.dynamictrees.utility.IdentifierUtils;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.IdentifierException;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

/**
 * Loads a branch block model from a Json file, with useful warnings when things aren't found.
 *
 * <p>Can also be used by sub-classes to load other models, like for roots in
 * {@link SurfaceRootBlockModelLoader}.</p>
 *
 * @author Harley O'Connor
 */
public class BranchBlockModelLoader implements IGeometryLoader<BranchBlockModelGeometry> {

    public static final Logger LOGGER = LogManager.getLogger();

    private static final String TEXTURES = "textures";
    private static final String BARK = "bark";
    private static final String RINGS = "rings";

    @Override
    public BranchBlockModelGeometry read(JsonObject modelObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
        final JsonObject textures = this.getTexturesObject(modelObject);
        final Identifier familyName = this.getLocation(modelObject, "family");

        return this.getModelGeometry(this.getBarkTextureLocation(textures), this.getRingsTextureLocation(textures),
                familyName == null ? null : IdentifierUtils.parseDTLocation(familyName));
    }

    protected JsonObject getTexturesObject(final JsonObject modelContents) {
        if (!modelContents.has(TEXTURES) || !modelContents.get(TEXTURES).isJsonObject()) {
            this.throwRequiresElement(TEXTURES, "Json Object");
        }

        return modelContents.getAsJsonObject(TEXTURES);
    }

    protected Identifier getBarkTextureLocation(final JsonObject textureObject) {
        return this.getTextureLocation(textureObject, BARK);
    }

    protected Identifier getRingsTextureLocation(final JsonObject textureObject) {
        return this.getTextureLocation(textureObject, RINGS);
    }

    @Nullable
    protected Identifier getLocation(final JsonObject object, String identifier) {
        try {
            return this.getLocationOrThrow(this.getOrThrow(object, identifier));
        } catch (final RuntimeException e) {
            return null;
        }
    }

    protected Identifier getTextureLocation(final JsonObject textureObject, final String textureElement) {
        try {
            return this.getLocationOrThrow(this.getOrThrow(textureObject, textureElement));
        } catch (final RuntimeException e) {
            LOGGER.error("{} missing or did not have valid \"{}\" texture location element, using missing " +
                    "texture.", this.getModelTypeName(), textureElement);
            return MissingTextureAtlasSprite.getLocation();
        }
    }

    protected String getOrThrow(final JsonObject jsonObject, final String identifier) {
        if (jsonObject.get(identifier) == null || !jsonObject.get(identifier).isJsonPrimitive() ||
                !jsonObject.get(identifier).getAsJsonPrimitive().isString()) {
            this.throwRequiresElement(identifier, "String");
        }

        return jsonObject.get(identifier).getAsString();
    }

    protected void throwRequiresElement(final String element, final String expectedType) {
        throw new RuntimeException(this.getModelTypeName() + " requires a valid \"" + element + "\" element of " +
                "type " + expectedType + ".");
    }

    protected Identifier getLocationOrThrow(final String location) {
        try {
            return Identifier.parse(location);
        } catch (IdentifierException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return The type of model the class is loading. Useful for warnings when using sub-classes.
     */
    protected String getModelTypeName() {
        return "Branch";
    }

    /**
     * Gets the {@link BranchBlockModelGeometry} object from the given bark and rings texture locations.
     * Can be overridden by subclasses to provide their custom {@link BranchBlockModelGeometry}.
     *
     * @param barkTextureLocation The {@link Identifier} object for the bark.
     * @param ringsTextureLocation The {@link Identifier} object for the rings.
     * @return The {@link BranchBlockModelGeometry} object.
     */
    protected BranchBlockModelGeometry getModelGeometry(final Identifier barkTextureLocation,
                                                        final Identifier ringsTextureLocation,
                                                        @Nullable final Identifier familyName) {
        return new BranchBlockModelGeometry(barkTextureLocation, ringsTextureLocation, familyName, false);
    }

}