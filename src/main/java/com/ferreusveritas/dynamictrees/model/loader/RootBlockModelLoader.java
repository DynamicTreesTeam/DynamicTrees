package com.ferreusveritas.dynamictrees.model.loader;

import com.ferreusveritas.dynamictrees.model.geometry.BranchBlockModelGeometry;
import com.ferreusveritas.dynamictrees.model.geometry.RootBlockModelGeometry;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author Harley O'Connor
 */
@OnlyIn(Dist.CLIENT)
public class RootBlockModelLoader extends BranchBlockModelLoader {

    @Override
    public BranchBlockModelGeometry read(JsonDeserializationContext context, JsonObject json) {
        final JsonObject textures = this.getTexturesJson(json);
        return new RootBlockModelGeometry(this.getBarkResLoc(textures));
    }

    @Override
    protected String getModelTypeName() {
        return "Surface Root";
    }

}
