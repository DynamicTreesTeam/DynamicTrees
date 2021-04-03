package com.ferreusveritas.dynamictrees.items;

import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class DirtBucket extends Item {

	public DirtBucket() {
		super(new Item.Properties().maxStackSize(1).group(DTRegistries.ITEM_GROUP));
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
		
		final ItemStack itemStack = player.getHeldItem(hand);
		final BlockRayTraceResult blockRayTraceResult;

		{
			blockRayTraceResult = rayTrace(world, player, RayTraceContext.FluidMode.NONE);
			if (blockRayTraceResult.getType() != RayTraceResult.Type.BLOCK) {
				return new ActionResult<>(ActionResultType.FAIL, itemStack);
			}
		}

		if (DTConfigs.dirtBucketPlacesDirt.get()) {
			if (blockRayTraceResult.getType() != RayTraceResult.Type.BLOCK) {
				return new ActionResult<>(ActionResultType.PASS, itemStack);
			} else {
				final BlockPos pos = blockRayTraceResult.getPos();
				
				if (!world.isBlockModifiable(player, pos)) {
					return new ActionResult<>(ActionResultType.FAIL, itemStack);
				} else {
					final boolean isReplacable = world.getBlockState(pos).getMaterial().isReplaceable();
					final BlockPos workingPos = isReplacable && blockRayTraceResult.getFace() == Direction.UP ? pos : pos.offset(blockRayTraceResult.getFace());
					
					if (!player.canPlayerEdit(workingPos, blockRayTraceResult.getFace(), itemStack)) {
						return new ActionResult<>(ActionResultType.FAIL, itemStack);
					} else if (this.tryPlaceContainedDirt(player, world, workingPos)) {
						//						player.addStat(Stats.BLOCK_USED.getObjectUseStats(this));
						return !player.isCreative() ? new ActionResult<>(ActionResultType.SUCCESS, new ItemStack(Items.BUCKET)) : new ActionResult<>(ActionResultType.SUCCESS, itemStack);
					} else {
						return new ActionResult<>(ActionResultType.FAIL, itemStack);
					}
				}
			}
		} else {
			return new ActionResult<>(ActionResultType.PASS, itemStack);
		}
	}
	
	public boolean tryPlaceContainedDirt(@Nullable PlayerEntity player, World world, BlockPos posIn) {
		if (world.getBlockState(posIn).getMaterial().isReplaceable()) {
			if (!world.isRemote) {
				world.destroyBlock(posIn, true);
			}
			
			world.playSound(player, posIn, SoundEvents.BLOCK_GRAVEL_PLACE, SoundCategory.BLOCKS, 1.0F, 0.8F);
			world.setBlockState(posIn, Blocks.DIRT.getDefaultState(), 11);
			return true;
		}

		return false;
	}
	
}
