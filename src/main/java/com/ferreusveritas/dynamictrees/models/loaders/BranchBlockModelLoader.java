package com.ferreusveritas.dynamictrees.models.loaders;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.models.geometry.BranchBlockModelGeometry;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
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

    @Override
    public BranchBlockModelGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelObject) {
        final JsonObject textures = this.getTexturesObject(modelObject);
        final ResourceLocation familyResLoc = this.getResLoc(modelObject);

        return this.getModelGeometry(this.getBarkResLoc(textures), this.getRingsResLoc(textures),
                familyResLoc == null ? null : TreeRegistry.processResLoc(familyResLoc));
    }

    protected JsonObject getTexturesObject (final JsonObject modelContents) {
        if (!modelContents.has(TEXTURES) || !modelContents.get(TEXTURES).isJsonObject())
            this.throwRequiresElement(TEXTURES, "Json Object");

        return modelContents.getAsJsonObject(TEXTURES);
    }

    protected ResourceLocation getBarkResLoc (final JsonObject textureObject) {
        return this.getTextureLocation(textureObject, BARK);
    }

    protected ResourceLocation getRingsResLoc (final JsonObject textureObject) {
        return this.getTextureLocation(textureObject, RINGS);
    }

    @Nullable
    protected ResourceLocation getResLoc(final JsonObject object) {
        try {
            return this.getResLocOrThrow(this.getOrThrow(object, "family"));
        } catch (final RuntimeException e) {
            return null;
        }
    }

    protected ResourceLocation getTextureLocation (final JsonObject textureObject, final String textureElement) {
        try {
            return this.getResLocOrThrow(this.getOrThrow(textureObject, textureElement));
        } catch (final RuntimeException e) {
            LOGGER.error("{} missing or did not have valid \"{}\" texture location element, using missing " +
                    "texture.", this.getModelTypeName(), textureElement);
            return MissingTextureSprite.getLocation();
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

    protected ResourceLocation getResLocOrThrow(final String resLocStr) {
        try {
            return new ResourceLocation(resLocStr);
        } catch (ResourceLocationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return The type of model the class is loading. Useful for warnings when using sub-classes.
     */
    protected String getModelTypeName () {
        return "Branch";
    }

    /**
     * Gets the {@link IModelGeometry} object from the given bark and rings texture locations.
     * Can be overridden by sub-classes to provide their custom {@link IModelGeometry}.
     *
     * @param barkResLoc The {@link ResourceLocation} object for the bark.
     * @param ringsResLoc The {@link ResourceLocation} object for the rings.
     * @return The {@link IModelGeometry} object.
     */
    protected BranchBlockModelGeometry getModelGeometry (final ResourceLocation barkResLoc,
                                                         final ResourceLocation ringsResLoc,
                                                         @Nullable final ResourceLocation familyResLoc) {
        return new BranchBlockModelGeometry(barkResLoc, ringsResLoc, familyResLoc, false);
    }

}
