package com.dtteam.dynamictrees.model;

import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class BranchBlockUnbakedModel implements UnbakedModel {
    protected final Identifier barkTextureLocation;
    protected final Identifier ringsTextureLocation;
    protected final Identifier familyName;
    protected final boolean forceThickness;

    public BranchBlockUnbakedModel(Identifier barkTextureLocation, Identifier ringsTextureLocation, @Nullable Identifier familyName, boolean forceThickness) {
        this.barkTextureLocation = barkTextureLocation;
        this.ringsTextureLocation = ringsTextureLocation;
        this.familyName = familyName;
        this.forceThickness = forceThickness;
    }
}
