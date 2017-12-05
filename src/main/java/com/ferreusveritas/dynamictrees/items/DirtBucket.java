package com.ferreusveritas.dynamictrees.items;

import javax.annotation.Nullable;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.ModItems;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public class DirtBucket extends Item {

	public static final String name = "dirtbucket";

	public DirtBucket() {
		this(name);
	}

	public DirtBucket(String name) {
		setCreativeTab(DynamicTrees.dynamicTreesTab);
		setUnlocalizedName(name);
		setRegistryName(name);
		setMaxStackSize(1);
		setContainerItem(this);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

        ItemStack itemStack = player.getHeldItem(hand);
		RayTraceResult raytraceresult = this.rayTrace(world, player, false);

		if (raytraceresult == null) {
			return new ActionResult(EnumActionResult.PASS, itemStack);
		}
		else if (raytraceresult.typeOfHit != RayTraceResult.Type.BLOCK) {
			return new ActionResult(EnumActionResult.PASS, itemStack);
		}
		else {
			BlockPos blockpos = raytraceresult.getBlockPos();

			if (!world.isBlockModifiable(player, blockpos)) {
				return new ActionResult(EnumActionResult.FAIL, itemStack);
			}
			else {
				boolean isReplacable = world.getBlockState(blockpos).getBlock().isReplaceable(world, blockpos);
				BlockPos workingBlockPos = isReplacable && raytraceresult.sideHit == EnumFacing.UP ? blockpos : blockpos.offset(raytraceresult.sideHit);

				if (!player.canPlayerEdit(workingBlockPos, raytraceresult.sideHit, itemStack)) {
					return new ActionResult(EnumActionResult.FAIL, itemStack);
				}
				else if (this.tryPlaceContainedDirt(player, world, workingBlockPos)) {
					player.addStat(StatList.getObjectUseStats(this));
					return !player.capabilities.isCreativeMode ? new ActionResult(EnumActionResult.SUCCESS, new ItemStack(Items.BUCKET)) : new ActionResult(EnumActionResult.SUCCESS, itemStack);
				}
				else {
					return new ActionResult(EnumActionResult.FAIL, itemStack);
				}
			}
		}
	}

	public boolean tryPlaceContainedDirt(@Nullable EntityPlayer player, World world, BlockPos posIn) {
		IBlockState iblockstate = world.getBlockState(posIn);
		boolean replaceable = iblockstate.getBlock().isReplaceable(world, posIn);

		if(replaceable) {
			if (!world.isRemote) {
				world.destroyBlock(posIn, true);
			}

			SoundEvent soundevent = SoundEvents.BLOCK_GRASS_PLACE;
			world.playSound(player, posIn, soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
			world.setBlockState(posIn, Blocks.DIRT.getDefaultState(), 11);
			return true;
		}
		
		return false;
	}

	public DirtBucket registerRecipes(IForgeRegistry<IRecipe> registry) {
		//Create a dirt bucket from dirt and a bucket
		GameRegistry.addShapelessRecipe(new ResourceLocation(ModConstants.MODID, "dirtbucket"), null, new ItemStack(ModItems.dirtBucket), 
				new Ingredient[]{ Ingredient.fromItem(Items.BUCKET), Ingredient.fromItem(ItemBlock.getItemFromBlock(Blocks.DIRT))});

		return this;
	}
	
}
