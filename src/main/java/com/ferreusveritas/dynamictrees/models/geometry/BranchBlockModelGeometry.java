package com.ferreusveritas.dynamictrees.models.geometry;

import com.ferreusveritas.dynamictrees.models.baked.BasicBranchBlockBakedModel;
import com.ferreusveritas.dynamictrees.models.baked.ThickBranchBlockBakedModel;
import com.ferreusveritas.dynamictrees.models.loader.BranchBlockModelLoader;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Bakes {@link BasicBranchBlockBakedModel} from bark and rings texture locations given by {@link
 * BranchBlockModelLoader}.
 *
 * <p>Can also be used by sub-classes to bake other models, like for roots in
 * {@link SurfaceRootBlockModelGeometry}.</p>
 *
 * @author Harley O'Connor
 */
@OnlyIn(Dist.CLIENT)
public class BranchBlockModelGeometry implements IUnbakedGeometry<BranchBlockModelGeometry> {
    protected final Set<ResourceLocation> textures = new HashSet<>();
    protected final ResourceLocation barkTextureLocation;
    protected final ResourceLocation ringsTextureLocation;
    protected final boolean forceThickness;

    protected ResourceLocation familyName;
    protected Family family;

    protected ResourceLocation thickRingsTextureLocation;

    public BranchBlockModelGeometry(@Nullable final ResourceLocation barkTextureLocation, @Nullable final ResourceLocation ringsTextureLocation, @Nullable final ResourceLocation familyName, final boolean forceThickness) {
        this.barkTextureLocation = barkTextureLocation;
        this.ringsTextureLocation = ringsTextureLocation;
        this.familyName = familyName;
        this.forceThickness = forceThickness;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides,
            ResourceLocation modelLocation) {
        boolean useThickModel = this.useThickModel(this.setFamily(modelLocation));
        if (!useThickModel) {
            return new BasicBranchBlockBakedModel(context, modelLocation, this.barkTextureLocation, this.ringsTextureLocation, spriteGetter);
        } else {
            if (this.thickRingsTextureLocation == null)
                this.thickRingsTextureLocation = this.ringsTextureLocation.withSuffix("_thick");
            return new ThickBranchBlockBakedModel(context, modelLocation, this.barkTextureLocation, this.ringsTextureLocation, this.thickRingsTextureLocation, spriteGetter);
        }
    }

    private ResourceLocation setFamilyName(final ResourceLocation modelLocation) {
        if (this.familyName == null) {
            this.familyName = new ResourceLocation(modelLocation.getNamespace(), modelLocation.getPath().replace("block/", "").replace("_branch", "").replace("stripped_", ""));
        }
        return this.familyName;
    }

    private Family setFamily(final ResourceLocation modelResLoc) {
        if (this.family == null) {
            this.family = Family.REGISTRY.get(this.setFamilyName(modelResLoc));
        }
        return this.family;
    }

    protected boolean useThickModel(final Family family) {
        return this.forceThickness || family.isThick();
    }
}