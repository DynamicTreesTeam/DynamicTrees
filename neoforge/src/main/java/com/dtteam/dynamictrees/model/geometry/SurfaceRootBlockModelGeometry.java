package com.dtteam.dynamictrees.model.geometry;

import com.dtteam.dynamictrees.model.loader.SurfaceRootBlockModelLoader;
import net.minecraft.resources.Identifier;

/**
 * Bakes {@link SurfaceRootBlockBakedModel} from bark texture location given by {@link SurfaceRootBlockModelLoader}.
 *
 * @author Harley O'Connor
 */
public class SurfaceRootBlockModelGeometry extends BranchBlockModelGeometry {

    public SurfaceRootBlockModelGeometry(final Identifier barkResLoc) {
        super(barkResLoc, null, null, false);
    }

//    @Override
//    public BakedModel bake(IGeometryBakingContext context, ModelBaker modelBaker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides itemOverrides) {
//        return new SurfaceRootBlockBakedModel(this.barkTextureLocation, spriteGetter);
//    }

}