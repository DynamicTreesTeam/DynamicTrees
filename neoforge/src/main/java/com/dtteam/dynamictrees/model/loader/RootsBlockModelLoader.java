package com.dtteam.dynamictrees.model.loader;

import com.dtteam.dynamictrees.model.geometry.BranchBlockModelGeometry;
import com.dtteam.dynamictrees.model.geometry.RootsBlockModelGeometry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

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