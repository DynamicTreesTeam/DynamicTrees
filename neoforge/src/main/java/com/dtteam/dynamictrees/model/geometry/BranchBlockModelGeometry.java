package com.dtteam.dynamictrees.model.geometry;

import com.dtteam.dynamictrees.model.baked.BasicBranchBlockBakedModel;
import com.dtteam.dynamictrees.model.baked.ThickBranchBlockBakedModel;
import com.dtteam.dynamictrees.model.loader.BranchBlockModelLoader;
import com.dtteam.dynamictrees.tree.family.Family;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.Nullable;

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
    public BakedModel bake(IGeometryBakingContext context, ModelBaker modelBaker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides itemOverrides) {
        boolean useThickModel = this.useThickModel(this.setFamily(context.getModelName()));
        if (!useThickModel) {
            return new BasicBranchBlockBakedModel(context, this.barkTextureLocation, this.ringsTextureLocation, spriteGetter);
        } else {
            if (this.thickRingsTextureLocation == null)
                this.thickRingsTextureLocation = this.ringsTextureLocation.withSuffix("_thick");
            return new ThickBranchBlockBakedModel(context, this.barkTextureLocation, this.ringsTextureLocation, this.thickRingsTextureLocation, spriteGetter);
        }
    }

    private Family setFamily(String modelName) {
        if (this.family == null) {
            this.family = Family.REGISTRY.get(this.setFamilyName(ResourceLocation.parse(modelName)));
        }
        return this.family;
    }
    private ResourceLocation setFamilyName(final ResourceLocation modelLocation) {
        if (this.familyName == null) {
            this.familyName = ResourceLocation.fromNamespaceAndPath(modelLocation.getNamespace(), modelLocation.getPath().replace("block/", "").replace("_branch", "").replace("stripped_", ""));
        }
        return this.familyName;
    }

    protected boolean useThickModel(final Family family) {
        return this.forceThickness || family.isThick();
    }
}