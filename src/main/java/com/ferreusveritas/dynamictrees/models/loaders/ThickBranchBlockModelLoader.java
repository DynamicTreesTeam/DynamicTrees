package com.ferreusveritas.dynamictrees.models.loaders;

import com.ferreusveritas.dynamictrees.models.geometry.BranchBlockModelGeometry;
import com.ferreusveritas.dynamictrees.models.geometry.CactusBranchBlockModelGeometry;
import com.ferreusveritas.dynamictrees.models.geometry.ThickBranchBlockModelGeometry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author Harley O'Connor
 */
@OnlyIn(Dist.CLIENT)
public class ThickBranchBlockModelLoader extends BranchBlockModelLoader {

    @Override
    public BranchBlockModelGeometry getModelGeometry (final ResourceLocation barkResLoc, final ResourceLocation ringsResLoc, final ResourceLocation strippedResLoc) {
        return new ThickBranchBlockModelGeometry(barkResLoc, ringsResLoc, strippedResLoc);
    }

}
