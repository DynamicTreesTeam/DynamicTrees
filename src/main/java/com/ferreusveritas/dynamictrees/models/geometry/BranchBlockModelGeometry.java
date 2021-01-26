package com.ferreusveritas.dynamictrees.models.geometry;

import com.ferreusveritas.dynamictrees.models.bakedmodels.BasicBranchBlockBakedModel;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.*;
import java.util.function.Function;

/**
 * @author Harley O'Connor
 */
@OnlyIn(Dist.CLIENT)
public class BranchBlockModelGeometry implements IModelGeometry<BranchBlockModelGeometry> {

    protected final ResourceLocation barkResLoc;
    protected final ResourceLocation ringsResLoc;

    public BranchBlockModelGeometry(ResourceLocation barkResLoc, ResourceLocation ringsResLoc) {
        this.barkResLoc = barkResLoc;
        this.ringsResLoc = ringsResLoc;
    }

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
        return new BasicBranchBlockBakedModel(modelLocation, this.barkResLoc, this.ringsResLoc);
    }

    @Override
    public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        ResourceLocation[] textures = {this.barkResLoc, this.ringsResLoc};
        List<RenderMaterial> renderMaterials = new ArrayList<>();

        for (ResourceLocation textureLoc : textures) {
            if (textureLoc != null) // Sub-classes can make unneeded textures null.
                renderMaterials.add(new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, textureLoc));
        }

        return renderMaterials;
    }

}
