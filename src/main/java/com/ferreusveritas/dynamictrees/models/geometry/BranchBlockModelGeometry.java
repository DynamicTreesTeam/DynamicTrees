package com.ferreusveritas.dynamictrees.models.geometry;

import com.ferreusveritas.dynamictrees.models.baked.BasicBranchBlockBakedModel;
import com.ferreusveritas.dynamictrees.models.baked.ThickBranchBlockBakedModel;
import com.ferreusveritas.dynamictrees.models.loader.BranchBlockModelLoader;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
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
 * {@link RootBlockModelGeometry}.</p>
 *
 * @author Harley O'Connor
 */
@OnlyIn(Dist.CLIENT)
public class BranchBlockModelGeometry implements IUnbakedGeometry<BranchBlockModelGeometry> {
    protected final Set<ResourceLocation> textures = new HashSet<>();
    protected final ResourceLocation barkResLoc;
    protected final ResourceLocation ringsResLoc;
    protected final boolean forceThickness;

    protected ResourceLocation familyResLoc;
    protected Family family;

    protected ResourceLocation thickRingsResLoc;

    public BranchBlockModelGeometry(@Nullable final ResourceLocation barkResLoc, @Nullable final ResourceLocation ringsResLoc, @Nullable final ResourceLocation familyResLoc,
            final boolean forceThickness) {
        this.barkResLoc = barkResLoc;
        this.ringsResLoc = ringsResLoc;
        this.familyResLoc = familyResLoc;
        this.forceThickness = forceThickness;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides,
            ResourceLocation modelLocation) {
        if (!this.useThickModel(this.setFamily(modelLocation))) {
            return new BasicBranchBlockBakedModel(modelLocation, this.barkResLoc, this.ringsResLoc);
        } else {
            return new ThickBranchBlockBakedModel(modelLocation, this.barkResLoc, this.ringsResLoc, this.thickRingsResLoc);
        }
    }

    private ResourceLocation setFamilyResLoc(final ResourceLocation modelResLoc) {
        if (this.familyResLoc == null) {
            this.familyResLoc = new ResourceLocation(modelResLoc.getNamespace(), modelResLoc.getPath().replace("block/", "").replace("_branch", "").replace("stripped_", ""));
        }
        return this.familyResLoc;
    }

    private Family setFamily(final ResourceLocation modelResLoc) {
        if (this.family == null) {
            this.family = Family.REGISTRY.get(this.setFamilyResLoc(modelResLoc));
        }
        return this.family;
    }

    private boolean useThickModel(final Family family) {
        return this.forceThickness || family.isThick();
    }
}