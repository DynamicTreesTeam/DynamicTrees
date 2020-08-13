package com.ferreusveritas.dynamictrees.systems.featuregen;

import com.ferreusveritas.dynamictrees.api.IFullGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Random;

/**
 * Generates a singular huge mushroom
 * 
 * @author ferreusveritas
 */
public class FeatureGenHugeMushroom implements IFullGenFeature {
		
	private final Block mushroomType;
	private int height = -1;
	
	public FeatureGenHugeMushroom(Block block) {
		this.mushroomType = block;
	}
	
	public FeatureGenHugeMushroom() {
		this.mushroomType = null;
	}
	
	static final SimpleVoxmap brnCap;
	static final SimpleVoxmap brnCapMedium;
	static final SimpleVoxmap brnCapSmall;
	static final SimpleVoxmap redCap;
	static final SimpleVoxmap redCapShort;
	static final SimpleVoxmap redCapSmall;
		
	static {
		
		brnCap = new SimpleVoxmap(7, 1, 7, new byte[] {
			0, 1, 2, 2, 2, 3, 0,
			1, 5, 5, 5, 5, 5, 3,
			4, 5, 5, 5, 5, 5, 6,
			4, 5, 5, 5, 5, 5, 6,
			4, 5, 5, 5, 5, 5, 6,
			7, 5, 5, 5, 5, 5, 9,
			0, 7, 8, 8, 8, 9, 0
		}).setCenter(new BlockPos(3, 0, 3));

		brnCapMedium = new SimpleVoxmap(5, 1, 5, new byte[] {
			0, 1, 2, 3, 0, 1, 5, 5, 5, 3, 4, 5, 5, 5, 6, 7, 5, 5, 5, 9, 0, 7, 8, 9, 0
		}).setCenter(new BlockPos(2, 0, 2));
		
		brnCapSmall = new SimpleVoxmap(3, 1, 3, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }).setCenter(new BlockPos(1, 0, 1));
		
		redCap = new SimpleVoxmap(5, 4, 5, new byte[] {
			0, 1, 2, 3, 0, 1, 0, 0, 0, 3, 4, 0, 10,0, 6, 7, 0, 0, 0, 9, 0, 7, 8, 9, 0,//Bottom
			0, 1, 2, 3, 0, 1, 0, 0, 0, 3, 4, 0, 10,0, 6, 7, 0, 0, 0, 9, 0, 7, 8, 9, 0,
			0, 1, 2, 3, 0, 1, 0, 0, 0, 3, 4, 0, 10,0, 6, 7, 0, 0, 0, 9, 0, 7, 8, 9, 0,
			0, 0, 0, 0, 0, 0, 1, 2, 3, 0, 0, 4, 5, 6, 0, 0, 7, 8, 9, 0, 0, 0, 0, 0, 0//Top
		}).setCenter(new BlockPos(2, 3, 2));
		
		redCapShort = new SimpleVoxmap(5, 3, 5, new byte[] {
			0, 1, 2, 3, 0, 1, 0, 0, 0, 3, 4, 0, 10,0, 6, 7, 0, 0, 0, 9, 0, 7, 8, 9, 0,//Bottom
			0, 1, 2, 3, 0, 1, 0, 0, 0, 3, 4, 0, 10,0, 6, 7, 0, 0, 0, 9, 0, 7, 8, 9, 0,
			0, 0, 0, 0, 0, 0, 1, 2, 3, 0, 0, 4, 5, 6, 0, 0, 7, 8, 9, 0, 0, 0, 0, 0, 0//Top
		}).setCenter(new BlockPos(2, 2, 2));

		redCapSmall = new SimpleVoxmap(3, 2, 3, new byte[] {
			1, 2, 3, 4, 10,6, 7, 8, 9,//Bottom
			1, 2, 3, 4, 5, 6, 7, 8, 9//Top
		}).setCenter(new BlockPos(1, 1, 1));
	
	}
	
	public FeatureGenHugeMushroom setHeight(int height) {
		this.height = height;
		return this;
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
	
	//Override this for custom mushroom heights
	protected int getMushroomHeight(World world, BlockPos rootPos, Biome biome, Random random, int radius, SafeChunkBounds safeBounds) {
		return this.height > 0 ? this.height : random.nextInt(9) + 2;
	}
	
	@Override
	public boolean generate(World world, BlockPos rootPos, Species species, Biome biome, Random random, int radius, SafeChunkBounds safeBounds) {
//
//		BlockPos genPos = rootPos.up();
//
//		int height = getMushroomHeight(world, rootPos, biome, random, radius, safeBounds);
//
//		BlockState soilState = world.getBlockState(rootPos);
//
//		if (species.isAcceptableSoilForWorldgen(world, rootPos, soilState)) {
//			Block mushroomBlock = this.mushroomType;
//
//			if (mushroomBlock == null) {
//				mushroomBlock = random.nextBoolean() ? Blocks.BROWN_MUSHROOM_BLOCK : Blocks.RED_MUSHROOM_BLOCK;
//			}
//
//			SimpleVoxmap capMap = getCapForHeight(mushroomBlock, height);
//
//			BlockPos capPos = genPos.up(height - 1);//Determine the cap position(top block of mushroom cap)
//			BlockBounds capBounds = capMap.getBounds().move(capPos);//Get a bounding box for the entire cap
//
//			if(safeBounds.inBounds(capBounds, true)) {//Check to see if the cap can be generated in safeBounds
//
//				//Check if there's room for a mushroom cap and stem
//				for(MutableBlockPos mutPos : Iterables.concat(BlockPos.getAllInBoxMutable(BlockPos.ZERO.down(capMap.getLenY()), BlockPos.ZERO.down(height - 1)), capMap.getAllNonZero())) {
//					//System.out.println(mutPos);
//					BlockPos dPos = mutPos.add(capPos);
//					BlockState state = world.getBlockState(dPos);
//					if(!state.getBlock().canBeReplacedByLeaves(state, world, dPos) || !state.getBlock().isReplaceable(world, dPos)) {
//						return false;
//					}
//				}
//
//				//Construct the mushroom cap from the voxel map
//				for(Cell cell: capMap.getAllNonZeroCells()) {
//					BlockHugeMushroom.EnumType mushroomType = BlockHugeMushroom.EnumType.byMetadata(cell.getValue());
//					world.setBlockState(capPos.add(cell.getPos()), mushroomBlock.getDefaultState().withProperty(BlockHugeMushroom.VARIANT, mushroomType));
//				}
//
//				//Construct the stem
//				int stemLen = height - capMap.getLenY();
//				BlockState stemBlock = mushroomBlock.getDefaultState().withProperty(BlockHugeMushroom.VARIANT, BlockHugeMushroom.EnumType.STEM);
//				for(int y = 0; y < stemLen; y++) {
//					world.setBlockState(genPos.up(y), stemBlock);
//				}
//
//				return true;
//			}
//		}
//
		return false;
	}
	
}
