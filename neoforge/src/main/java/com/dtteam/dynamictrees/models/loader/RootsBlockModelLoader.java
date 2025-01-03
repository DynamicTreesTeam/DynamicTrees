package com.dtteam.dynamictrees.models.loader;

import com.dtteam.dynamictrees.models.geometry.BranchBlockModelGeometry;
import com.dtteam.dynamictrees.models.geometry.RootsBlockModelGeometry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

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