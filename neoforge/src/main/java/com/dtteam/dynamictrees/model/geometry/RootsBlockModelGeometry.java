package com.dtteam.dynamictrees.model.geometry;

import com.dtteam.dynamictrees.model.baked.BasicRootsBlockBakedModel;
import com.dtteam.dynamictrees.model.baked.SurfaceRootBlockBakedModel;
import com.dtteam.dynamictrees.model.loader.SurfaceRootBlockModelLoader;
import com.dtteam.dynamictrees.tree.family.Family;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import org.jetbrains.annotations.Nullable;

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
    public BakedModel bake(IGeometryBakingContext context, ModelBaker modelBaker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides itemOverrides) {
        return new BasicRootsBlockBakedModel(context, this.barkTextureLocation, this.ringsTextureLocation, spriteGetter);
    }

    @Override
    protected boolean useThickModel(final Family family) {
        return false;
    }

}