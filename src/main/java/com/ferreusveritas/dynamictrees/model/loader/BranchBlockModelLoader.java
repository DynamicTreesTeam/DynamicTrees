package com.ferreusveritas.dynamictrees.model.loader;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.model.geometry.BranchBlockModelGeometry;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;
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
@OnlyIn(Dist.CLIENT)
public class BranchBlockModelLoader implements IModelLoader<BranchBlockModelGeometry> {

    public static final Logger LOGGER = LogManager.getLogger();

    private static final String TEXTURES = "textures";
    private static final String BARK = "bark";
    private static final String RINGS = "rings";

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
    }

    @Override
    public BranchBlockModelGeometry read(JsonDeserializationContext context, JsonObject json) {
        final JsonObject texturesJson = this.getTexturesJson(json);
        final ResourceLocation familyName = this.getFamilyName(json);

        return this.getModelGeometry(this.getBarkResLoc(texturesJson), this.getRingsResLoc(texturesJson),
                familyName == null ? null : TreeRegistry.processResLoc(familyName));
    }

    protected JsonObject getTexturesJson(final JsonObject json) {
        if (!json.has(TEXTURES) || !json.get(TEXTURES).isJsonObject()) {
            this.throwRequiresElement(TEXTURES, "Json Object");
        }

        return json.getAsJsonObject(TEXTURES);
    }

    protected ResourceLocation getBarkResLoc(final JsonObject json) {
        return this.getTextureLocation(json, BARK);
    }

    protected ResourceLocation getRingsResLoc(final JsonObject json) {
        return this.getTextureLocation(json, RINGS);
    }

    @Nullable
    protected ResourceLocation getFamilyName(final JsonObject json) {
        try {
            return this.getNameOrThrow(this.getOrThrow(json, "family"));
        } catch (final RuntimeException e) {
            return null;
        }
    }

    protected ResourceLocation getTextureLocation(final JsonObject texturesJson, final String textureElement) {
        try {
            return this.getNameOrThrow(this.getOrThrow(texturesJson, textureElement));
        } catch (final RuntimeException e) {
            LOGGER.error("{} missing or did not have valid \"{}\" texture location element, using missing " +
                    "texture.", this.getModelTypeName(), textureElement);
            return MissingTextureAtlasSprite.getLocation();
        }
    }

    protected String getOrThrow(final JsonObject json, final String key) {
        if (json.get(key) == null || !json.get(key).isJsonPrimitive() ||
                !json.get(key).getAsJsonPrimitive().isString()) {
            this.throwRequiresElement(key, "String");
        }

        return json.get(key).getAsString();
    }

    protected void throwRequiresElement(final String element, final String expectedType) {
        throw new RuntimeException(this.getModelTypeName() + " requires a valid \"" + element + "\" element of " +
                "type " + expectedType + ".");
    }

    protected ResourceLocation getNameOrThrow(final String location) {
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
     * Gets the {@link IModelGeometry} object from the given bark and rings texture locations. Can be overridden by
     * sub-classes to provide their custom {@link IModelGeometry}.
     *
     * @param barkTextureName  The {@link ResourceLocation} object for the bark.
     * @param ringsTextureName The {@link ResourceLocation} object for the rings.
     * @return The {@link IModelGeometry} object.
     */
    protected BranchBlockModelGeometry getModelGeometry(final ResourceLocation barkTextureName,
                                                        final ResourceLocation ringsTextureName,
                                                        @Nullable final ResourceLocation familyName) {
        return new BranchBlockModelGeometry(barkTextureName, ringsTextureName, familyName, false);
    }

}
