package com.ferreusveritas.dynamictrees.model.geometry;

import com.ferreusveritas.dynamictrees.client.thickrings.ThickRingTextureManager;
import com.ferreusveritas.dynamictrees.model.baked.BasicBranchBlockBakedModel;
import com.ferreusveritas.dynamictrees.model.baked.ThickBranchBlockBakedModel;
import com.ferreusveritas.dynamictrees.model.loader.BranchBlockModelLoader;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Bakes {@link BasicBranchBlockBakedModel} from bark and rings texture locations given by {@link
 * BranchBlockModelLoader}.
 *
 * <p>Can also be used by sub-classes to bake other models, like for roots in
 * {@link RootBlockModelGeometry}.</p>
 *
 * @author Harley O'Connor
 */
@OnlyIn(Dist.CLIENT)
public class BranchBlockModelGeometry implements IModelGeometry<BranchBlockModelGeometry> {

    protected final Set<ResourceLocation> textures = new HashSet<>();
    protected final ResourceLocation barkTextureName;
    protected final ResourceLocation ringsTextureName;
    protected final boolean forceThickness;

    protected ResourceLocation familyName;
    protected Family family;

    protected ResourceLocation thickRingsResLoc;

    public BranchBlockModelGeometry(@Nullable final ResourceLocation barkTextureName, @Nullable final ResourceLocation ringsTextureName, @Nullable final ResourceLocation familyName, final boolean forceThickness) {
        this.barkTextureName = barkTextureName;
        this.ringsTextureName = ringsTextureName;
        this.familyName = familyName;
        this.forceThickness = forceThickness;

        this.addTextures(barkTextureName, ringsTextureName);
    }

    /**
     * Adds the given texture {@link ResourceLocation} objects to the list. Checks they're not null before adding them
     * so {@link Nullable} objects can be fed safely.
     *
     * @param textureResourceLocations Texture {@link ResourceLocation} objects.
     */
    protected void addTextures(final ResourceLocation... textureResourceLocations) {
        for (ResourceLocation resourceLocation : textureResourceLocations) {
            if (resourceLocation != null) {
                this.textures.add(resourceLocation);
            }
        }
    }

    @Override
    public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
        if (!this.useThickModel(this.setFamily(modelLocation))) {
            return new BasicBranchBlockBakedModel(modelLocation, this.barkTextureName, this.ringsTextureName);
        } else {
            return new ThickBranchBlockBakedModel(modelLocation, this.barkTextureName, this.ringsTextureName, this.thickRingsResLoc);
        }
    }

    private ResourceLocation setFamilyName(final ResourceLocation modelResLoc) {
        if (this.familyName == null) {
            this.familyName = new ResourceLocation(modelResLoc.getNamespace(), modelResLoc.getPath().replace("block/", "").replace("_branch", "").replace("stripped_", ""));
        }
        return this.familyName;
    }

    private Family setFamily(final ResourceLocation modelResLoc) {
        if (this.family == null) {
            this.family = Family.REGISTRY.get(this.setFamilyName(modelResLoc));
        }
        return this.family;
    }

    private boolean useThickModel(final Family family) {
        return this.forceThickness || family.isThick();
    }

    @SuppressWarnings("deprecation")
    @Override
    public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        if (this.thickRingsResLoc == null && this.useThickModel(this.setFamily(new ResourceLocation(owner.getModelName())))) {
            this.thickRingsResLoc = ThickRingTextureManager.addRingTextureLocation(this.ringsTextureName);
            this.addTextures(this.thickRingsResLoc);
        }

        return this.textures.stream()
                .map(resourceLocation -> new Material(TextureAtlas.LOCATION_BLOCKS, resourceLocation))
                .collect(Collectors.toList());
    }

}
