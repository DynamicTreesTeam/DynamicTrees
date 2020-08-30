package com.ferreusveritas.dynamictrees.items;

import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class DirtBucket extends Item {
	
	public static final String name = "dirt_bucket";
	
	public DirtBucket() {
		this(name);
	}

	public DirtBucket(String name) {
		super(new Item.Properties().maxStackSize(1).group(DTRegistries.dynamicTreesTab));
		setRegistryName(name);
	}

	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {

		ItemStack itemStack = player.getHeldItem(hand);
		BlockRayTraceResult blockRayTraceResult;
		{
			RayTraceResult raytrace = rayTrace(world, player, RayTraceContext.FluidMode.NONE);
			if (raytrace instanceof BlockRayTraceResult && raytrace.getType() == RayTraceResult.Type.BLOCK) {
				blockRayTraceResult = (BlockRayTraceResult) raytrace;
			} else {
				return new ActionResult(ActionResultType.FAIL, itemStack);
			}
		}
		if (DTConfigs.dirtBucketPlacesDirt.get()) {
			if (blockRayTraceResult.getType() != RayTraceResult.Type.BLOCK) {
				return new ActionResult(ActionResultType.PASS, itemStack);
			}
			else {

				BlockPos blockpos = blockRayTraceResult.getPos();

				if (!world.isBlockModifiable(player, blockpos)) {
					return new ActionResult(ActionResultType.FAIL, itemStack);
				}
				else {
					boolean isReplacable = world.getBlockState(blockpos).getMaterial().isReplaceable();
					BlockPos workingBlockPos = isReplacable && blockRayTraceResult.getFace() == Direction.UP ? blockpos : blockpos.offset(blockRayTraceResult.getFace());

					if (!player.canPlayerEdit(workingBlockPos, blockRayTraceResult.getFace(), itemStack)) {
						return new ActionResult(ActionResultType.FAIL, itemStack);
					}
					else if (this.tryPlaceContainedDirt(player, world, workingBlockPos)) {
//						player.addStat(Stats.BLOCK_USED.getObjectUseStats(this));
						return !player.isCreative() ? new ActionResult(ActionResultType.SUCCESS, new ItemStack(Items.BUCKET)) : new ActionResult(ActionResultType.SUCCESS, itemStack);
					}
					else {
						return new ActionResult(ActionResultType.FAIL, itemStack);
					}
				}
			}
		}
		else {
			return new ActionResult(ActionResultType.PASS, itemStack);
		}
	}
	
	public boolean tryPlaceContainedDirt(@Nullable PlayerEntity player, World world, BlockPos posIn) {
		BlockState blockstate = world.getBlockState(posIn);
		boolean replaceable = blockstate.getMaterial().isReplaceable();

		if(replaceable) {
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
