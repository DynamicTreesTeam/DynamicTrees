package com.ferreusveritas.dynamictrees.models.geometry;

import com.ferreusveritas.dynamictrees.models.baked.BasicRootsBlockBakedModel;
import com.ferreusveritas.dynamictrees.models.baked.SurfaceRootBlockBakedModel;
import com.ferreusveritas.dynamictrees.models.loader.SurfaceRootBlockModelLoader;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Bakes {@link SurfaceRootBlockBakedModel} from bark texture location given by {@link SurfaceRootBlockModelLoader}.
 *
 * @author Harley O'Connor
 */
public class RootsBlockModelGeometry extends BranchBlockModelGeometry {

    public RootsBlockModelGeometry(@Nullable final ResourceLocation barkTextureLocation, @Nullable final ResourceLocation ringsTextureLocation, @Nullable final ResourceLocation familyName) {
        super(barkTextureLocation, ringsTextureLocation, familyName, false);
    }

    @Override
    public BakedModel bake(IGeometryBakingContext owner, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
        return new BasicRootsBlockBakedModel(owner, modelLocation, this.barkTextureLocation, this.ringsTextureLocation, spriteGetter);
    }

    @Override
    protected boolean useThickModel(final Family family) {
        return false;
    }

}