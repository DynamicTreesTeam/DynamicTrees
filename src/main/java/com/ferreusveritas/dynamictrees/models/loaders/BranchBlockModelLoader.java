package com.ferreusveritas.dynamictrees.models.loaders;

import com.ferreusveritas.dynamictrees.models.geometry.BranchBlockModelGeometry;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
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
    public void onResourceManagerReload(IResourceManager resourceManager) { }

    @Nullable
    @Override
    public BranchBlockModelGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        final JsonObject textures = this.getTexturesObject(modelContents);

        if (textures == null)
            return null;

        final ResourceLocation barkResLoc = this.getBarkResLoc(textures);
        final ResourceLocation ringsResLoc = this.getRingsResLoc(textures);

        if (barkResLoc == null || ringsResLoc == null)
            return null;

        return this.getModelGeometry(barkResLoc, ringsResLoc);
    }

    @Nullable
    protected JsonObject getTexturesObject (final JsonObject modelContents) {
        if (!modelContents.get(TEXTURES).isJsonObject()) {
            LOGGER.warn("Skipped loading {} block model as it did not have a Json object with identifier '{}'.", this.getModelTypeName(), TEXTURES);
            return null;
        }

        return modelContents.getAsJsonObject(TEXTURES);
    }

    @Nullable
    protected ResourceLocation getBarkResLoc (final JsonObject texturesContents) {
        final String resLocStr = this.getOrWarn(texturesContents, BARK, TEXTURES);
        return resLocStr != null ? this.getResLoc(resLocStr) : null;
    }

    @Nullable
    protected ResourceLocation getRingsResLoc (final JsonObject texturesContents) {
        final String resLocStr = this.getOrWarn(texturesContents, RINGS, TEXTURES);
        return resLocStr != null ? this.getResLoc(resLocStr) : null;
    }

    @Nullable
    private String getOrWarn(final JsonObject jsonObject, final String identifier, final String jsonObjectName) {
        if (jsonObject.get(identifier) == null) {
            LOGGER.warn("Skipping loading {} block model as identifier '{}' not found in Json object '{}'.", this.getModelTypeName(), identifier, jsonObjectName);
            return null;
        }
        if (!jsonObject.get(identifier).isJsonPrimitive() || !jsonObject.get(identifier).getAsJsonPrimitive().isString()) {
            LOGGER.warn("Skipping loading {} block model as identifier '{}' was not a string in Json object '{}'.", this.getModelTypeName(), identifier, jsonObjectName);
            return null;
        }

        return jsonObject.get(identifier).getAsString();
    }

    /**
     * @return The type of model the class is loading. Useful for warnings when using sub-classes.
     */
    protected String getModelTypeName () {
        return "branch";
    }

    @Nullable
    private ResourceLocation getResLoc(final String resLocStr) {
        try {
            return new ResourceLocation(resLocStr);
        } catch (ResourceLocationException e) {
            LOGGER.warn("Skipped loading {} block model as resource location could not be created from string: {}", this.getModelTypeName(), e.getMessage());
        }
        return null;
    }

    /**
     * Gets the {@link IModelGeometry} object from the given bark and rings texture locations.
     * Can be overridden by sub-classes to provide their custom {@link IModelGeometry}.
     *
     * @param barkResLoc The {@link ResourceLocation} object for the bark.
     * @param ringsResLoc The {@link ResourceLocation} object for the rings.
     * @return The {@link IModelGeometry} object.
     */
    protected BranchBlockModelGeometry getModelGeometry (final ResourceLocation barkResLoc, final ResourceLocation ringsResLoc) {
        return new BranchBlockModelGeometry(barkResLoc, ringsResLoc);
    }

}
