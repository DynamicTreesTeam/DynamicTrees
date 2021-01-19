package com.ferreusveritas.dynamictrees.models.loaders;

import com.ferreusveritas.dynamictrees.models.geometry.BranchBlockModelGeometry;
import com.ferreusveritas.dynamictrees.models.geometry.RootBlockModelGeometry;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author Harley O'Connor
 */
@OnlyIn(Dist.CLIENT)
public class RootBlockModelLoader extends BranchBlockModelLoader {

    @Override
    public BranchBlockModelGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        final JsonObject textures = this.getTexturesObject(modelContents);

        return this.getModelGeometry(this.getBarkResLoc(textures));
    }

    private RootBlockModelGeometry getModelGeometry (final ResourceLocation barkResLoc) {
        return new RootBlockModelGeometry(barkResLoc);
    }

}
