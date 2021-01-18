package com.ferreusveritas.dynamictrees.models.geometry;

import com.ferreusveritas.dynamictrees.models.bakedmodels.CactusBranchBlockBakedModel;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.IModelConfiguration;

import java.util.function.Function;

/**
 * @author Harley O'Connor
 */
@OnlyIn(Dist.CLIENT)
public final class CactusBranchBlockModelGeometry extends BranchBlockModelGeometry {

    public CactusBranchBlockModelGeometry(ResourceLocation barkResLoc, ResourceLocation ringsResLoc) {
        super(barkResLoc, ringsResLoc);
    }

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
        return new CactusBranchBlockBakedModel(modelLocation, this.barkResLoc, this.ringsResLoc);
    }

}
