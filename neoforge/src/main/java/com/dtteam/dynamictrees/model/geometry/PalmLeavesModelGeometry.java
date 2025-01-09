package com.dtteam.dynamictrees.model.geometry;

import com.dtteam.dynamictrees.model.baked.LargePalmLeavesBakedModel;
import com.dtteam.dynamictrees.model.baked.MediumPalmLeavesBakedModel;
import com.dtteam.dynamictrees.model.baked.SmallPalmLeavesBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Function;

public class PalmLeavesModelGeometry implements IUnbakedGeometry<PalmLeavesModelGeometry> {

    protected final ResourceLocation frondsResLoc;

    private final int frondType;

    public PalmLeavesModelGeometry(final ResourceLocation frondsResLoc, int type){
        this.frondsResLoc = frondsResLoc;
        this.frondType = type;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext iGeometryBakingContext, ModelBaker modelBaker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides itemOverrides) {
        return switch (frondType) {
            default -> new LargePalmLeavesBakedModel(frondsResLoc, spriteGetter);
            case 1 -> new MediumPalmLeavesBakedModel(frondsResLoc, spriteGetter);
            case 2 -> new SmallPalmLeavesBakedModel(frondsResLoc, spriteGetter);
        };
    }

}