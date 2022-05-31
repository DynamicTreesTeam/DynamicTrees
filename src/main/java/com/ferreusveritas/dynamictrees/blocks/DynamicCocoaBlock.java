package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.HitResult;

public class DynamicCocoaBlock extends CocoaBlock {

    public DynamicCocoaBlock() {
        super(Block.Properties.of(Material.PLANT)
                .randomTicks()
                .strength(0.2F, 3.0F)
                .sound(SoundType.WOOD));
    }

    /**
     * Can this block stay at this position.  Similar to canPlaceBlockAt except gets checked often with plants.
     */
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        final BlockState logState = worldIn.getBlockState(pos.relative(state.getValue(FACING)));
        final BranchBlock branch = TreeHelper.getBranch(logState);
        return branch != null && branch.getRadius(logState) == 8 && branch.getFamily().canSupportCocoa;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        return new ItemStack(Items.COCOA_BEANS);
    }

}
