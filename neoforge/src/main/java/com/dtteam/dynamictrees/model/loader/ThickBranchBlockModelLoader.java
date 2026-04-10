package com.dtteam.dynamictrees.model.loader;

import com.dtteam.dynamictrees.model.geometry.BranchBlockModelGeometry;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * @author Harley O'Connor
 */

public class ThickBranchBlockModelLoader extends BranchBlockModelLoader {

    @Override
    protected BranchBlockModelGeometry getModelGeometry(Identifier barkTextureLocation, Identifier ringsTextureLocation, @Nullable Identifier familyName) {
        return new BranchBlockModelGeometry(barkTextureLocation, ringsTextureLocation, familyName, true);
    }

    @Override
    protected String getModelTypeName() {
        return "Thick Branch";
    }

}
