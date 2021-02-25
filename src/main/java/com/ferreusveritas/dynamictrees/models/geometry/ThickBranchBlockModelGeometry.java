package com.ferreusveritas.dynamictrees.models.geometry;

import com.ferreusveritas.dynamictrees.client.thickrings.ThickRingAtlasTexture;
import com.ferreusveritas.dynamictrees.client.thickrings.ThickRingTextureManager;
import com.ferreusveritas.dynamictrees.models.bakedmodels.ThickBranchBlockBakedModel;
import com.ferreusveritas.dynamictrees.models.loaders.ThickBranchBlockModelLoader;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.IModelConfiguration;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

/**
 * Bakes {@link ThickBranchBlockBakedModel} from bark and rings texture locations given by
 * {@link ThickBranchBlockModelLoader}.
 *
 * @author Harley O'Connor
 */
@OnlyIn(Dist.CLIENT)
public class ThickBranchBlockModelGeometry extends BranchBlockModelGeometry {

    protected final ResourceLocation thickRingsResLoc;

    public ThickBranchBlockModelGeometry(ResourceLocation barkResLoc, ResourceLocation ringsResLoc) {
        super(barkResLoc, ringsResLoc);

        this.thickRingsResLoc = ThickRingTextureManager.addRingTextureLocation(ringsResLoc);
    }

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
        return new ThickBranchBlockBakedModel(modelLocation, this.barkResLoc, this.ringsResLoc, this.thickRingsResLoc);
    }

    @Override
    public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        Collection<RenderMaterial> renderMaterials = super.getTextures(owner, modelGetter, missingTextureErrors);

        renderMaterials.add(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, this.thickRingsResLoc));

        return renderMaterials;
    }

}
