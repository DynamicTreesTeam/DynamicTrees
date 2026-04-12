package com.dtteam.dynamictrees.model.geometry;

import com.dtteam.dynamictrees.model.loader.SurfaceRootBlockModelLoader;
import com.dtteam.dynamictrees.tree.family.Family;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Bakes {@link SurfaceRootBlockBakedModel} from bark texture location given by {@link SurfaceRootBlockModelLoader}.
 *
 * @author Harley O'Connor
 */
public class RootsBlockModelGeometry extends BranchBlockModelGeometry {

    public RootsBlockModelGeometry(@Nullable final Identifier barkTextureLocation, @Nullable final Identifier ringsTextureLocation, @Nullable final Identifier familyName) {
        super(barkTextureLocation, ringsTextureLocation, familyName, false);
    }

//    @Override
//    public BakedModel bake(IGeometryBakingContext context, ModelBaker modelBaker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides itemOverrides) {
//        return new BasicRootsBlockBakedModel(context, this.barkTextureLocation, this.ringsTextureLocation, spriteGetter);
//    }

    @Override
    protected boolean useThickModel(final Family family) {
        return false;
    }

}