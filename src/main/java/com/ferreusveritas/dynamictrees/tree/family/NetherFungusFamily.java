package com.ferreusveritas.dynamictrees.tree.family;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.data.DTBlockTags;
import com.ferreusveritas.dynamictrees.data.DTItemTags;
import com.ferreusveritas.dynamictrees.util.BlockBounds;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import java.util.Collections;
import java.util.List;

/**
 * @author Harley O'Connor
 */
public class NetherFungusFamily extends Family {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(NetherFungusFamily::new);

    public NetherFungusFamily(ResourceLocation name) {
        super(name);
    }


    @Override
    public Family setPreReloadDefaults() {
        this.setPrimaryThickness(3);
        this.setSecondaryThickness(4);
        return this;
    }

    @Override
    public MapColor getDefaultBranchMapColor() {
        // TODO 1.20: Customize warped and crimson in default.json
        return MapColor.WARPED_STEM;
    }

    @Override
    public SoundType getDefaultBranchSoundType() {
        return SoundType.STEM;
    }

    @Override
    public boolean isFireProof() {
        return true;
    }

    public BlockBounds expandLeavesBlockBounds(BlockBounds bounds) {
        return bounds.expand(1).expand(Direction.DOWN, 3);
    }

    @Override
    public List<TagKey<Block>> defaultBranchTags() {
        return Collections.singletonList(DTBlockTags.FUNGUS_BRANCHES);
    }

    @Override
    public List<TagKey<Item>> defaultBranchItemTags() {
        return Collections.singletonList(DTItemTags.FUNGUS_BRANCHES);
    }

    @Override
    public List<TagKey<Block>> defaultStrippedBranchTags() {
        return Collections.singletonList(DTBlockTags.STRIPPED_FUNGUS_BRANCHES);
    }

}
