package com.ferreusveritas.dynamictrees.model.geometry;

import com.ferreusveritas.dynamictrees.model.baked.RootBlockBakedModel;
import com.ferreusveritas.dynamictrees.model.loader.RootBlockModelLoader;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;

import java.util.function.Function;

/**
 * Bakes {@link RootBlockBakedModel} from bark texture location given by {@link RootBlockModelLoader}.
 *
 * @author Harley O'Connor
 */
public class RootBlockModelGeometry extends BranchBlockModelGeometry {

    public RootBlockModelGeometry(final ResourceLocation barkResLoc) {
        super(barkResLoc, null, null, false);
    }

    @Override
    public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
        return new RootBlockBakedModel(modelLocation, this.barkTextureName);
    }

}
