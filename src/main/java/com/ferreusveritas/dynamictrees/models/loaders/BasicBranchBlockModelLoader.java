package com.ferreusveritas.dynamictrees.models.loaders;

import com.ferreusveritas.dynamictrees.models.geomtry.BasicBranchBlockGeometry;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelLoader;

/**
 * @author Harley O'Connor
 */
public final class BasicBranchBlockModelLoader implements IModelLoader<BasicBranchBlockGeometry> {

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }

    @Override
    public BasicBranchBlockGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        // TODO: Document json errors here.

        final JsonObject textures = modelContents.getAsJsonObject("textures");

        final String barkResLocStr = textures.get("bark").getAsString();
        final String ringsResLocStr = textures.get("rings").getAsString();

        return new BasicBranchBlockGeometry(this.convertStrToResLoc(barkResLocStr), this.convertStrToResLoc(ringsResLocStr));
    }

    private ResourceLocation convertStrToResLoc (final String resLocStr) {
        if (!resLocStr.contains(":")) return new ResourceLocation("minecraft", resLocStr);
        return new ResourceLocation(resLocStr.substring(0, resLocStr.indexOf(':')), resLocStr.substring(resLocStr.indexOf(':') + 1));
    }

}
