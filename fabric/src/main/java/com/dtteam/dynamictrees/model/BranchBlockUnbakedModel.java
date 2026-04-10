package com.dtteam.dynamictrees.model;

import com.dtteam.dynamictrees.model.baked.BasicBranchBlockBakedModel;
import com.dtteam.dynamictrees.model.baked.ThickBranchBlockBakedModel;
import com.dtteam.dynamictrees.tree.family.Family;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

public class BranchBlockUnbakedModel implements UnbakedModel {

    protected final Identifier barkTextureLocation;
    protected final Identifier ringsTextureLocation;
    protected final Identifier familyName;
    protected final boolean forceThickness;

    public BranchBlockUnbakedModel(Identifier barkTextureLocation, Identifier ringsTextureLocation, @Nullable Identifier familyName, boolean forceThickness) {
        this.barkTextureLocation = barkTextureLocation;
        this.ringsTextureLocation = ringsTextureLocation;
        this.familyName = familyName;
        this.forceThickness = forceThickness;
    }

    @Override
    public Collection<Identifier> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public void resolveParents(Function<Identifier, UnbakedModel> resolver) {
    }

    @Override
    public BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state) {
        TextureAtlasSprite barkSprite = spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, barkTextureLocation));
        TextureAtlasSprite ringsSprite = spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, ringsTextureLocation));

        Family family = familyName != null ? Family.REGISTRY.get(familyName) : null;
        boolean useThickModel = forceThickness || (family != null && family.isThick());

        if (useThickModel) {
            Identifier thickRingsLocation = ringsTextureLocation.withSuffix("_thick");
            TextureAtlasSprite thickRingsSprite = spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, thickRingsLocation));
            return new ThickBranchBlockBakedModel(barkSprite, ringsSprite, thickRingsSprite);
        }

        return new BasicBranchBlockBakedModel(barkSprite, ringsSprite);
    }
}
