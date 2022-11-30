package com.ferreusveritas.dynamictrees.model.loader;

import com.ferreusveritas.dynamictrees.model.geometry.BranchBlockModelGeometry;
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
    protected BranchBlockModelGeometry getModelGeometry(ResourceLocation barkTextureName, ResourceLocation ringsTextureName, @Nullable ResourceLocation familyName) {
        return new BranchBlockModelGeometry(barkTextureName, ringsTextureName, familyName, true);
    }

    @Override
    protected String getModelTypeName() {
        return "Thick Branch";
    }

}
