package com.ferreusveritas.dynamictrees.worldgen;

import java.util.Random;

import com.ferreusveritas.dynamictrees.util.BlockBounds;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap.Cell;
import com.google.common.collect.Iterables;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHugeMushroom;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;

public class MushroomGenerator {
	
	private final Block mushroomType;
	
	public MushroomGenerator(Block block) {
		this.mushroomType = block;
	}
	
	public MushroomGenerator() {
		this.mushroomType = null;
	}
	
	static final SimpleVoxmap brnCap;
	static final SimpleVoxmap brnCapMedium;
	static final SimpleVoxmap brnCapSmall;
	static final SimpleVoxmap redCap;
	static final SimpleVoxmap redCapShort;
	static final SimpleVoxmap redCapSmall;
		
	static {
		
		brnCap = new SimpleVoxmap(7, 7, 1, new byte[] {
			0, 1, 2, 2, 2, 3, 0,
			1, 5, 5, 5, 5, 5, 3,
			4, 5, 5, 5, 5, 5, 6,
			4, 5, 5, 5, 5, 5, 6,
			4, 5, 5, 5, 5, 5, 6,
			7, 5, 5, 5, 5, 5, 9,
			0, 7, 8, 8, 8, 9, 0
		}).setCenter(new BlockPos(3, 0, 3));

		brnCapMedium = new SimpleVoxmap(5, 5, 1, new byte[] {
			0, 1, 2, 3, 0, 1, 5, 5, 5, 3, 4, 5, 5, 5, 6, 7, 5, 5, 5, 9, 0, 7, 8, 9, 0
		}).setCenter(new BlockPos(2, 0, 2));
		
		brnCapSmall = new SimpleVoxmap(3, 3, 1, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }).setCenter(new BlockPos(1, 0, 1));
		
		redCap = new SimpleVoxmap(5, 5, 4, new byte[] {
			0, 1, 2, 3, 0, 1, 0, 0, 0, 3, 4, 0, 10,0, 6, 7, 0, 0, 0, 9, 0, 7, 8, 9, 0,//Bottom
			0, 1, 2, 3, 0, 1, 0, 0, 0, 3, 4, 0, 10,0, 6, 7, 0, 0, 0, 9, 0, 7, 8, 9, 0,
			0, 1, 2, 3, 0, 1, 0, 0, 0, 3, 4, 0, 10,0, 6, 7, 0, 0, 0, 9, 0, 7, 8, 9, 0,
			0, 0, 0, 0, 0, 0, 1, 2, 3, 0, 0, 4, 5, 6, 0, 0, 7, 8, 9, 0, 0, 0, 0, 0, 0//Top
		}).setCenter(new BlockPos(2, 3, 2));
		
		redCapShort = new SimpleVoxmap(5, 5, 3, new byte[] {
			0, 1, 2, 3, 0, 1, 0, 0, 0, 3, 4, 0, 10,0, 6, 7, 0, 0, 0, 9, 0, 7, 8, 9, 0,//Bottom
			0, 1, 2, 3, 0, 1, 0, 0, 0, 3, 4, 0, 10,0, 6, 7, 0, 0, 0, 9, 0, 7, 8, 9, 0,
			0, 0, 0, 0, 0, 0, 1, 2, 3, 0, 0, 4, 5, 6, 0, 0, 7, 8, 9, 0, 0, 0, 0, 0, 0//Top
		}).setCenter(new BlockPos(2, 3, 2));

		redCapSmall = new SimpleVoxmap(3, 3, 2, new byte[] {
			1, 2, 3, 4, 5, 6, 7, 8, 9,//Bottom
			1, 2, 3, 4, 10,6, 7, 8, 9//Top
		}).setCenter(new BlockPos(1, 1, 1));
	
	}
	
	/**
	 * Select the appropriate sized cap for a huge mushroom type
	 * 
	 * @param mushroomBlock Red or Brown mushroom block
	 * @param height The height of the huge mushroom
	 * @return a voxmap of the cap to create
	 */
	protected SimpleVoxmap getCapForHeight(Block mushroomBlock, int height) {

		//Brown Cap mushroom
		if(mushroomBlock == Blocks.BROWN_MUSHROOM_BLOCK) {
			switch(height) {
				case 2:
				case 3: return brnCapSmall;
				case 4:
				case 5: return brnCapMedium;
				default: return brnCap;
			}
		}

		//Red Cap mushroom
		switch (height) {
			case 2: return brnCapSmall;
			case 3: return redCapSmall;
			case 4: return redCapShort;
			default: return redCap;
		}
	}
	
	public boolean generate(World worldIn, Random rand, BlockPos genPos, int height, SafeChunkBounds safeBounds) {
		Block soilBlock = worldIn.getBlockState(genPos.down()).getBlock();
		
		if (soilBlock == Blocks.DIRT || soilBlock == Blocks.GRASS || soilBlock == Blocks.MYCELIUM) {
			Block mushroomBlock = this.mushroomType;
			
			if (mushroomBlock == null) {
				mushroomBlock = rand.nextBoolean() ? Blocks.BROWN_MUSHROOM_BLOCK : Blocks.RED_MUSHROOM_BLOCK;
			}
			
			SimpleVoxmap capMap = getCapForHeight(mushroomBlock, height);
			
			BlockPos capPos = genPos.up(height - 1);//Determine the cap position(top block of mushroom cap)
			BlockBounds capBounds = capMap.getBounds().move(capPos);//Get a bounding box for the entire cap
			
			if(safeBounds.inBounds(capBounds, true)) {//Check to see if the cap can be generated in safeBounds
				//Check if there's room for a mushroom cap and stem
				for(MutableBlockPos mutPos : Iterables.concat(BlockPos.getAllInBoxMutable(genPos, genPos.up(height - 2)), capMap.getAllNonZero())) {
					IBlockState state = worldIn.getBlockState(mutPos);
					if(!state.getBlock().canBeReplacedByLeaves(state, worldIn, mutPos) || !state.getBlock().isReplaceable(worldIn, mutPos)) {
						return false;
					}
				}
				
				//Construct the mushroom cap from the voxel map
				for(Cell cell: capMap.getAllNonZeroCells()) {
					BlockHugeMushroom.EnumType mushroomType = BlockHugeMushroom.EnumType.byMetadata(cell.getValue());
					worldIn.setBlockState(capPos.add(cell.getPos()), mushroomBlock.getDefaultState().withProperty(BlockHugeMushroom.VARIANT, mushroomType));
				}
				
				//Construct the stem
				int stemLen = height - capMap.getLenY();
				IBlockState stemBlock = mushroomBlock.getDefaultState().withProperty(BlockHugeMushroom.VARIANT, BlockHugeMushroom.EnumType.STEM);
				for(int y = 0; y < stemLen; y++) {
					worldIn.setBlockState(genPos.up(y), stemBlock);
				}
				
				return true;
			}
		}
		
		return false;
	}
	
}
