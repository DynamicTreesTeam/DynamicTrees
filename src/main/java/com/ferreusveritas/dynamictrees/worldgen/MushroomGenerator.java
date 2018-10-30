package com.ferreusveritas.dynamictrees.worldgen;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHugeMushroom;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;

public class MushroomGenerator {
	
	/** The mushroom type. 0 for brown, 1 for red. */
	private final Block mushroomType;
	
	public MushroomGenerator(Block block) {
		this.mushroomType = block;
	}
	
	public MushroomGenerator() {
		this.mushroomType = null;
	}
	
	public boolean generate(World worldIn, Random rand, BlockPos genPos, int height) {
		Block mushroomBlock = this.mushroomType;
		
		if (mushroomBlock == null) {
			mushroomBlock = Blocks.RED_MUSHROOM_BLOCK;
			//mushroomBlock = rand.nextBoolean() ? Blocks.BROWN_MUSHROOM_BLOCK : Blocks.RED_MUSHROOM_BLOCK;
		}
		
		int stemLength = height - 1;
		
		if(height == 0) {
			stemLength = rand.nextInt(3) + 4;//4 to 6
			
			if (rand.nextInt(12) == 0) { //1 in 12 chance to be twice as high
				stemLength *= 2; 
			}
		}
		
		boolean generatable = true;
		
		if (genPos.getY() >= 1 && genPos.getY() + stemLength + 1 < 256) {//TODO: Make Cubic Chunks Safe
			for (int yi = genPos.getY(); yi <= genPos.getY() + 1 + stemLength; ++yi) {
				//The first 4 blocks will only be checked for the stem obstacles.
				//A 7×7×(height−3) region above that will be checked above that
				int radius = (yi > genPos.getY() + 3) ? 3 : 0; 
				
				MutableBlockPos mutPos = new MutableBlockPos();
				
				for (int xi = genPos.getX() - radius; xi <= genPos.getX() + radius && generatable; ++xi) {
					for (int zi = genPos.getZ() - radius; zi <= genPos.getZ() + radius && generatable; ++zi) {
						mutPos.setPos(xi, yi, zi);
						IBlockState state = worldIn.getBlockState(mutPos);
						//This logic implies that generation could be blocked by something as trivial as tall grass.
						if(!state.getBlock().isReplaceable(worldIn, mutPos) || !state.getBlock().canBeReplacedByLeaves(state, worldIn, mutPos)) {
							generatable = false;
						}
					}
				}
			}
			
			if (generatable) {
				Block soilBlock = worldIn.getBlockState(genPos.down()).getBlock();
				
				if (soilBlock == Blocks.DIRT || soilBlock == Blocks.GRASS || soilBlock == Blocks.MYCELIUM) {
					//Red mushroom have a 4 block tall cap.  Brown mushrooms are a single block thick
					int capBottomYPos = genPos.getY() + stemLength - ((mushroomBlock == Blocks.RED_MUSHROOM_BLOCK) ? 3 : 0);
					if(stemLength == 3 && mushroomBlock == Blocks.RED_MUSHROOM_BLOCK) {
						capBottomYPos++;
					}
					
					for (int capYPos = capBottomYPos; capYPos <= genPos.getY() + stemLength; ++capYPos) {
						int radius = 1;//Size for top of red mushroom
						
						if (capYPos < genPos.getY() + stemLength) {
							++radius;//If we're below the top layer of the red mushroom cap then expand the radius by one block
						}
						
						if (mushroomBlock == Blocks.BROWN_MUSHROOM_BLOCK) {
							radius = 3;//If we're a brown mushroom then override completely
						}
						
						//Set up min/max bounds for mushroom cap 
						int xMin = genPos.getX() - radius;
						int xMax = genPos.getX() + radius;
						int zMin = genPos.getZ() - radius;
						int zMax = genPos.getZ() + radius;
						
						for (int xi = xMin; xi <= xMax; ++xi) {
							for (int zi = zMin; zi <= zMax; ++zi) {

								//Center of mushroom cap by default
                                int metadata = BlockHugeMushroom.EnumType.CENTER.getMetadata();

                                if (xi == xMin) {
                                    --metadata;//Add West
                                }
                                else if (xi == xMax) {
                                    ++metadata;//Add East
                                }

                                if (zi == zMin) {
                                    metadata -= 3;//Add North
                                }
                                else if (zi == zMax) {
                                    metadata += 3;//Add South
                                }

                                BlockHugeMushroom.EnumType mushroomType = BlockHugeMushroom.EnumType.byMetadata(metadata);
								
								//This applies to the entire brown mushroom cap or the "skirt" of the red mushroom cap
								if (mushroomBlock == Blocks.BROWN_MUSHROOM_BLOCK || capYPos < genPos.getY() + stemLength) {

									//This is for the corners of the mushrooms(the "rounded" outside part) which should be air(ungenerated)
									if ((xi == xMin || xi == xMax) && (zi == zMin || zi == zMax)) {
										continue;//One of the four outside corners
									}
									
									if (xi == genPos.getX() - (radius - 1) && zi == zMin) {
										mushroomType = BlockHugeMushroom.EnumType.NORTH_WEST;
									}
									
									if (xi == xMin && zi == genPos.getZ() - (radius - 1)) {
										mushroomType = BlockHugeMushroom.EnumType.NORTH_WEST;
									}
									
									if (xi == genPos.getX() + (radius - 1) && zi == zMin) {
										mushroomType = BlockHugeMushroom.EnumType.NORTH_EAST;
									}
									
									if (xi == xMax && zi == genPos.getZ() - (radius - 1)) {
										mushroomType = BlockHugeMushroom.EnumType.NORTH_EAST;
									}
									
									if (xi == genPos.getX() - (radius - 1) && zi == zMax) {
										mushroomType = BlockHugeMushroom.EnumType.SOUTH_WEST;
									}
									
									if (xi == xMin && zi == genPos.getZ() + (radius - 1)) {
										mushroomType = BlockHugeMushroom.EnumType.SOUTH_WEST;
									}
									
									if (xi == genPos.getX() + (radius - 1) && zi == zMax) {
										mushroomType = BlockHugeMushroom.EnumType.SOUTH_EAST;
									}
									
									if (xi == xMax && zi == genPos.getZ() + (radius - 1)) {
										mushroomType = BlockHugeMushroom.EnumType.SOUTH_EAST;
									}
								}
								
							//  if ( [ block tells we're not on the edge of the cap ] && [ we're below the top layer of cap] ) {
								if (mushroomType == BlockHugeMushroom.EnumType.CENTER && capYPos < genPos.getY() + stemLength) {
									mushroomType = BlockHugeMushroom.EnumType.ALL_INSIDE;//Anything set to this is air(not generated)
								}
								
							//  if (     [ We're on the top layer of the cap ]      ||  [ the mushroom block is effectively marked null ]   ) {
								if (genPos.getY() >= genPos.getY() + stemLength - 1 || mushroomType != BlockHugeMushroom.EnumType.ALL_INSIDE) {
									BlockPos blockpos = new BlockPos(xi, capYPos, zi);
									IBlockState state = worldIn.getBlockState(blockpos);
									
									if (state.getBlock().isReplaceable(worldIn, blockpos) || state.getBlock().canBeReplacedByLeaves(state, worldIn, blockpos)) {
										worldIn.setBlockState(blockpos, mushroomBlock.getDefaultState().withProperty(BlockHugeMushroom.VARIANT, mushroomType));
									}
								}
							}
						}
					}
					
					//Build the mushroom stem
					for (int stemi = 0; stemi < stemLength; ++stemi) {
						IBlockState iblockstate = worldIn.getBlockState(genPos.up(stemi));
						
						if (iblockstate.getBlock().isReplaceable(worldIn, genPos.up(stemi)) || iblockstate.getBlock().canBeReplacedByLeaves(iblockstate, worldIn, genPos.up(stemi))) {
							worldIn.setBlockState(genPos.up(stemi), mushroomBlock.getDefaultState().withProperty(BlockHugeMushroom.VARIANT, BlockHugeMushroom.EnumType.STEM));
						}
					}
					
					return true;
				}//if usable soil 
			}//if generatable
		}//if in world Y bounds
		
		return false;
	}
	
}
