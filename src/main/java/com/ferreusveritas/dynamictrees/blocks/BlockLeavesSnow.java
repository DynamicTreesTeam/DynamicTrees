package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.seasons.SeasonHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.Random;

public class BlockLeavesSnow extends BlockSnow {

	public final String name = "leaves_snow";

	public BlockLeavesSnow() {
		setUnlocalizedName(name);
		setRegistryName(new ResourceLocation(ModConstants.MODID, name));
		setHardness(0.1f);
		setSoundType(SoundType.SNOW);
		setLightOpacity(0);
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(ItemBlock.getItemFromBlock(Objects.requireNonNull(Block.getBlockFromName("snow_layer"))));
	}

	@Override
	public String getHarvestTool(IBlockState state) {
		return "shovel";
	}

	@Override
	public int getHarvestLevel(IBlockState state) {
		return 0;
	}

	@Override
	public void randomTick(World world, BlockPos pos, IBlockState state, Random random) {
		super.randomTick(world, pos, state, random);
		if (SeasonHelper.shouldSnowMelt(world, pos)) {
			world.setBlockToAir(pos);
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return NULL_AABB;
	}
	
}
