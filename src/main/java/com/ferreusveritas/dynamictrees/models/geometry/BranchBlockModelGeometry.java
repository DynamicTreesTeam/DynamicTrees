package com.ferreusveritas.dynamictrees.models.geometry;

import com.ferreusveritas.dynamictrees.models.bakedmodels.BasicBranchBlockBakedModel;
import com.ferreusveritas.dynamictrees.models.loaders.BranchBlockModelLoader;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Bakes {@link BasicBranchBlockBakedModel} from bark and rings texture locations
 * given by {@link BranchBlockModelLoader}.
 *
 * <p>Can also be used by sub-classes to bake other models, like for roots in
 * {@link RootBlockModelGeometry}.</p>
 *
 * @author Harley O'Connor
 */
@OnlyIn(Dist.CLIENT)
public class BranchBlockModelGeometry implements IModelGeometry<BranchBlockModelGeometry> {

    protected final List<ResourceLocation> textures = new ArrayList<>();
    protected final ResourceLocation barkResLoc;
    protected final ResourceLocation ringsResLoc;

    public BranchBlockModelGeometry(@Nullable final ResourceLocation barkResLoc, @Nullable final ResourceLocation ringsResLoc) {
        this.barkResLoc = barkResLoc;
        this.ringsResLoc = ringsResLoc;

        this.addTextures(barkResLoc, ringsResLoc);
    }

    /**
     * Adds the given texture {@link ResourceLocation} objects to the list. Checks they're not null
     * before adding them so {@link Nullable} objects can be fed safely.
     *
     * @param textureResourceLocations Texture {@link ResourceLocation} objects.
     */
    protected void addTextures (final ResourceLocation... textureResourceLocations) {
        for (ResourceLocation resourceLocation : textureResourceLocations) {
            if (resourceLocation != null) {
                this.textures.add(resourceLocation);
            }
        }
    }

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
        return new BasicBranchBlockBakedModel(modelLocation, this.barkResLoc, this.ringsResLoc);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return this.textures.stream().map(resourceLocation -> new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, resourceLocation))
                .collect(Collectors.toList());
    }

}
