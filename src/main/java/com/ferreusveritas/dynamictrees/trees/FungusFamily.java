package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;

/**
 * @author Harley O'Connor
 */
public final class FungusFamily extends Family {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(FungusFamily::new);

    public FungusFamily (ResourceLocation name) {
        super(name);
    }

    @Override
    public int getPrimaryThickness() {
        return 3;
    }

    @Override
    public int getSecondaryThickness() {
        return 4;
    }

    @Override
    public Material getDefaultBranchMaterial() {
        return Material.NETHER_WOOD;
    }

    @Override
    public SoundType getDefaultBranchSoundType() {
        return SoundType.STEM;
    }

    @Override
    public boolean isFireProof() { return true; }

}
