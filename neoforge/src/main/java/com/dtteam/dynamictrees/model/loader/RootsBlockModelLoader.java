package com.dtteam.dynamictrees.model.loader;

import com.dtteam.dynamictrees.model.geometry.BranchBlockModelGeometry;
import com.dtteam.dynamictrees.model.geometry.RootsBlockModelGeometry;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * @author Harley O'Connor
 */

public class RootsBlockModelLoader extends BranchBlockModelLoader {

    protected BranchBlockModelGeometry getModelGeometry(final Identifier barkTextureLocation,
                                                        final Identifier ringsTextureLocation,
                                                        @Nullable final Identifier familyName) {
        return new RootsBlockModelGeometry(barkTextureLocation, ringsTextureLocation, familyName);
    }

    @Override
    protected String getModelTypeName() {
        return "Roots";
    }

}