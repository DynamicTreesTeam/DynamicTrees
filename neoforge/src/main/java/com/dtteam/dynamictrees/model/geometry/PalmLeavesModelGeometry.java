package com.dtteam.dynamictrees.model.geometry;

import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;

public class PalmLeavesModelGeometry implements UnbakedModel {

    protected final Identifier frondsResLoc;

    private final int frondType;

    public PalmLeavesModelGeometry(final Identifier frondsResLoc, int type){
        this.frondsResLoc = frondsResLoc;
        this.frondType = type;
    }

//    @Override
//    public BakedModel bake(IGeometryBakingContext iGeometryBakingContext, ModelBaker modelBaker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides itemOverrides) {
//        return switch (frondType) {
//            default -> new LargePalmLeavesBakedModel(frondsResLoc, spriteGetter);
//            case 1 -> new MediumPalmLeavesBakedModel(frondsResLoc, spriteGetter);
//            case 2 -> new SmallPalmLeavesBakedModel(frondsResLoc, spriteGetter);
//        };
//    }

}