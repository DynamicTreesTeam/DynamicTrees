package com.ferreusveritas.dynamictrees.models.geometry;

import com.ferreusveritas.dynamictrees.models.baked.SurfaceRootBlockBakedModel;
import com.ferreusveritas.dynamictrees.models.loader.SurfaceRootBlockModelLoader;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;

import java.util.function.Function;

/**
 * Bakes {@link SurfaceRootBlockBakedModel} from bark texture location given by {@link SurfaceRootBlockModelLoader}.
 *
 * @author Harley O'Connor
 */
public class SurfaceRootBlockModelGeometry extends BranchBlockModelGeometry {

    public SurfaceRootBlockModelGeometry(final ResourceLocation barkResLoc) {
        super(barkResLoc, null, null, false);
    }

    @Override
    public BakedModel bake(IGeometryBakingContext owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
        return new SurfaceRootBlockBakedModel(modelLocation, this.barkTextureLocation, spriteGetter);
    }

}