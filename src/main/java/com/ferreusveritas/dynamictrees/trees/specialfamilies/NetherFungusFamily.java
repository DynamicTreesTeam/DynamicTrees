package com.ferreusveritas.dynamictrees.trees.specialfamilies;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.util.BlockBounds;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

/**
 * @author Harley O'Connor
 */
public final class NetherFungusFamily extends Family {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(NetherFungusFamily::new);

    public NetherFungusFamily(ResourceLocation name) {
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

    public BlockBounds expandLeavesBlockBounds(BlockBounds bounds){
        return bounds.expand(1).expand(Direction.DOWN, 3);
    }
}
