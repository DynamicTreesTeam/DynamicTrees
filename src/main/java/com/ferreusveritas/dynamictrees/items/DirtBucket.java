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
		super(new Item.Properties().stacksTo(1).tab(DTRegistries.ITEM_GROUP));
	}

	@Override
	public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		
		final ItemStack itemStack = player.getItemInHand(hand);
		final BlockRayTraceResult blockRayTraceResult;

		{
			blockRayTraceResult = getPlayerPOVHitResult(world, player, RayTraceContext.FluidMode.NONE);
			if (blockRayTraceResult.getType() != RayTraceResult.Type.BLOCK) {
				return new ActionResult<>(ActionResultType.FAIL, itemStack);
			}
		}

		if (DTConfigs.dirtBucketPlacesDirt.get()) {
			if (blockRayTraceResult.getType() != RayTraceResult.Type.BLOCK) {
				return new ActionResult<>(ActionResultType.PASS, itemStack);
			} else {
				final BlockPos pos = blockRayTraceResult.getBlockPos();
				
				if (!world.mayInteract(player, pos)) {
					return new ActionResult<>(ActionResultType.FAIL, itemStack);
				} else {
					final boolean isReplacable = world.getBlockState(pos).getMaterial().isReplaceable();
					final BlockPos workingPos = isReplacable && blockRayTraceResult.getDirection() == Direction.UP ? pos : pos.relative(blockRayTraceResult.getDirection());
					
					if (!player.mayUseItemAt(workingPos, blockRayTraceResult.getDirection(), itemStack)) {
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
			if (!world.isClientSide) {
				world.destroyBlock(posIn, true);
			}
			
			world.playSound(player, posIn, SoundEvents.GRAVEL_PLACE, SoundCategory.BLOCKS, 1.0F, 0.8F);
			world.setBlock(posIn, Blocks.DIRT.defaultBlockState(), 11);
			return true;
		}

		return false;
	}
	
}
