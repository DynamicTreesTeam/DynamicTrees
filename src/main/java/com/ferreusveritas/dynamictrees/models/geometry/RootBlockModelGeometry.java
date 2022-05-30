package com.ferreusveritas.dynamictrees.models.geometry;

import com.ferreusveritas.dynamictrees.models.bakedmodels.RootBlockBakedModel;
import com.ferreusveritas.dynamictrees.models.loaders.RootBlockModelLoader;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
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
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
        return new RootBlockBakedModel(modelLocation, this.barkResLoc);
    }

}
