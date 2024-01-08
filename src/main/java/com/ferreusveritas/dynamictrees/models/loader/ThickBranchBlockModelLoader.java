package com.ferreusveritas.dynamictrees.models.loader;

import com.ferreusveritas.dynamictrees.models.geometry.BranchBlockModelGeometry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
