package com.ferreusveritas.dynamictrees.models.loaders;

import com.ferreusveritas.dynamictrees.models.geometry.BranchBlockModelGeometry;
import com.ferreusveritas.dynamictrees.models.geometry.CactusBranchBlockModelGeometry;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.IModelLoader;

/**
 * @author Harley O'Connor
 */
@OnlyIn(Dist.CLIENT)
public class BranchBlockModelLoader implements IModelLoader<BranchBlockModelGeometry> {

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }

    @Override
    public BranchBlockModelGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        // TODO: Document json errors here.
        final JsonObject textures = this.getTexturesObject(modelContents);

        return this.getModelGeometry(this.getBarkResLoc(textures), this.getRingsResLoc(textures));
    }

    protected JsonObject getTexturesObject (JsonObject modelContents) {
        return modelContents.getAsJsonObject("textures");
    }

    protected ResourceLocation getBarkResLoc (JsonObject texturesContents) {
        return this.convertStrToResLoc(texturesContents.get("bark").getAsString());
    }

    protected ResourceLocation getRingsResLoc (JsonObject texturesContents) {
        return this.convertStrToResLoc(texturesContents.get("rings").getAsString());
    }

    protected BranchBlockModelGeometry getModelGeometry (final ResourceLocation barkResLoc, final ResourceLocation ringsResLoc) {
        return new BranchBlockModelGeometry(barkResLoc, ringsResLoc);
    }

    private ResourceLocation convertStrToResLoc (final String resLocStr) {
        if (!resLocStr.contains(":")) return new ResourceLocation("minecraft", resLocStr);
        return new ResourceLocation(resLocStr.substring(0, resLocStr.indexOf(':')), resLocStr.substring(resLocStr.indexOf(':') + 1));
    }

}
