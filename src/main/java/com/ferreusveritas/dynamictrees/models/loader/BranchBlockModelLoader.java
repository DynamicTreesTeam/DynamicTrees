package com.ferreusveritas.dynamictrees.models.loader;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.models.geometry.BranchBlockModelGeometry;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

/**
 * Loads a branch block model from a Json file, with useful warnings when things aren't found.
 *
 * <p>Can also be used by sub-classes to load other models, like for roots in
 * {@link RootBlockModelLoader}.</p>
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
        final ResourceLocation familyName = this.getLocation(modelObject);

        return this.getModelGeometry(this.getBarkTextureLocation(textures), this.getRingsTextureLocation(textures),
                familyName == null ? null : TreeRegistry.processResLoc(familyName));
    }

    protected JsonObject getTexturesObject(final JsonObject modelContents) {
        if (!modelContents.has(TEXTURES) || !modelContents.get(TEXTURES).isJsonObject()) {
            this.throwRequiresElement(TEXTURES, "Json Object");
        }

        return modelContents.getAsJsonObject(TEXTURES);
    }

    protected ResourceLocation getBarkTextureLocation(final JsonObject textureObject) {
        return this.getTextureLocation(textureObject, BARK);
    }

    protected ResourceLocation getRingsTextureLocation(final JsonObject textureObject) {
        return this.getTextureLocation(textureObject, RINGS);
    }

    @Nullable
    protected ResourceLocation getLocation(final JsonObject object) {
        try {
            return this.getLocationOrThrow(this.getOrThrow(object, "family"));
        } catch (final RuntimeException e) {
            return null;
        }
    }

    protected ResourceLocation getTextureLocation(final JsonObject textureObject, final String textureElement) {
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

    protected ResourceLocation getLocationOrThrow(final String location) {
        try {
            return new ResourceLocation(location);
        } catch (ResourceLocationException e) {
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
     * @param barkTextureLocation The {@link ResourceLocation} object for the bark.
     * @param ringsTextureLocation The {@link ResourceLocation} object for the rings.
     * @return The {@link BranchBlockModelGeometry} object.
     */
    protected BranchBlockModelGeometry getModelGeometry(final ResourceLocation barkTextureLocation,
                                                        final ResourceLocation ringsTextureLocation,
                                                        @Nullable final ResourceLocation familyName) {
        return new BranchBlockModelGeometry(barkTextureLocation, ringsTextureLocation, familyName, false);
    }

}