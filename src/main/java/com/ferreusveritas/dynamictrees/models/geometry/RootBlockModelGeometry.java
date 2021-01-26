package com.ferreusveritas.dynamictrees.models.geometry;

import com.ferreusveritas.dynamictrees.models.bakedmodels.RootBlockBakedModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

/**
 * @author Harley O'Connor
 */
public class RootBlockModelGeometry extends BranchBlockModelGeometry {

    public RootBlockModelGeometry (ResourceLocation barkResLoc) {
        super(barkResLoc, null, null);
    }

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
        return new RootBlockBakedModel(modelLocation, this.barkResLoc);
    }

    @Override
    public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return Arrays.asList(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, this.barkResLoc));
    }
}
