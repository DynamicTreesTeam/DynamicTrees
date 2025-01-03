package com.dtteam.dynamictrees.models.loader;

import com.dtteam.dynamictrees.models.geometry.BranchBlockModelGeometry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * @author Harley O'Connor
 */
@OnlyIn(Dist.CLIENT)
public class ThickBranchBlockModelLoader extends BranchBlockModelLoader {

    @Override
    protected BranchBlockModelGeometry getModelGeometry(ResourceLocation barkTextureLocation, ResourceLocation ringsTextureLocation, @Nullable ResourceLocation familyName) {
        return new BranchBlockModelGeometry(barkTextureLocation, ringsTextureLocation, familyName, true);
    }

    @Override
    protected String getModelTypeName() {
        return "Thick Branch";
    }

}
