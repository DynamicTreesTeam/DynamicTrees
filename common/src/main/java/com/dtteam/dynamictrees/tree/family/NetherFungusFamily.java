package com.dtteam.dynamictrees.tree.family;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.api.voxmap.BlockPosBounds;
import com.dtteam.dynamictrees.data.tags.DTBlockTags;
import com.dtteam.dynamictrees.data.tags.DTItemTags;
import com.dtteam.dynamictrees.tree.species.NetherFungusSpecies;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
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

    public NetherFungusFamily(Identifier name) {
        super(name);
    }

    public void setCommonSpecies(Species species) {
        super.setCommonSpecies(species);
        if (!(species instanceof NetherFungusSpecies)) {
            DynamicTrees.LOG.warn("Common species {} for nether fungus {} is not of type {}", species.getRegistryName(), getRegistryName(), NetherFungusSpecies.class);
        }
    }

    public Family setPreReloadDefaults() {
        this.setPrimaryThickness(3);
        this.setSecondaryThickness(4);
        return this;
    }

    public boolean isFireProof() {
        return true;
    }

    public BlockPosBounds expandLeavesBlockBounds(BlockPosBounds bounds) {
        return bounds.expand(1).expand(Direction.DOWN, 3);
    }

    public List<TagKey<Block>> defaultBranchTags() {
        return Collections.singletonList(DTBlockTags.FUNGUS_BRANCHES);
    }

    public List<TagKey<Item>> defaultBranchItemTags() {
        return Collections.singletonList(DTItemTags.FUNGUS_BRANCHES);
    }

    public List<TagKey<Block>> defaultStrippedBranchTags() {
        return Collections.singletonList(DTBlockTags.STRIPPED_FUNGUS_BRANCHES);
    }

}
