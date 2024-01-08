package com.ferreusveritas.dynamictrees.models.loader;

import com.ferreusveritas.dynamictrees.models.geometry.BranchBlockModelGeometry;
import com.ferreusveritas.dynamictrees.models.geometry.RootsBlockModelGeometry;
import com.ferreusveritas.dynamictrees.models.geometry.SurfaceRootBlockModelGeometry;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * @author Harley O'Connor
 */
@OnlyIn(Dist.CLIENT)
public class RootsBlockModelLoader extends BranchBlockModelLoader {

    protected BranchBlockModelGeometry getModelGeometry(final ResourceLocation barkTextureLocation,
                                                        final ResourceLocation ringsTextureLocation,
                                                        @Nullable final ResourceLocation familyName) {
        return new RootsBlockModelGeometry(barkTextureLocation, ringsTextureLocation, familyName);
    }

    @Override
    protected String getModelTypeName() {
        return "Roots";
    }

}