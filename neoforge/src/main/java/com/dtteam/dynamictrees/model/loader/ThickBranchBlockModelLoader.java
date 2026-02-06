package com.dtteam.dynamictrees.model.loader;

import com.dtteam.dynamictrees.model.geometry.BranchBlockModelGeometry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * @author Harley O'Connor
 */

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
