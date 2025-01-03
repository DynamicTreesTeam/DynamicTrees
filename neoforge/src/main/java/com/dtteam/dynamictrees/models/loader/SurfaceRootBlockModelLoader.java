package com.dtteam.dynamictrees.models.loader;

import com.dtteam.dynamictrees.models.geometry.BranchBlockModelGeometry;
import com.dtteam.dynamictrees.models.geometry.SurfaceRootBlockModelGeometry;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * @author Harley O'Connor
 */
@OnlyIn(Dist.CLIENT)
public class SurfaceRootBlockModelLoader extends BranchBlockModelLoader {

    @Override
    public BranchBlockModelGeometry read(JsonObject modelObject, JsonDeserializationContext deserializationContext) {
        final JsonObject textures = this.getTexturesObject(modelObject);
        return new SurfaceRootBlockModelGeometry(this.getBarkTextureLocation(textures));
    }

    @Override
    protected String getModelTypeName() {
        return "Surface Root";
    }

}