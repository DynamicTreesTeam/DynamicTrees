package com.ferreusveritas.dynamictrees.blocks;

import java.util.Random;

import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.backport.IRegisterable;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.api.network.IBurningListener;
import com.ferreusveritas.dynamictrees.util.MathHelper;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockTNT;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

public class BlockVerboseFire extends BlockFire implements IRegisterable {

	public BlockVerboseFire() {
		setRegistryName("fire");
		setHardness(0.0F);
		setLightLevel(1.0F);
		setStepSound(soundTypeCloth);
		setUnlocalizedNameReg("fire");
		disableStats();
	}
	
	
	
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (worldIn.getGameRules().getBoolean("doFireTick")) {
            if (!this.canPlaceBlockAt(worldIn, pos)) {
                worldIn.setBlockToAir(pos);
            }

            Block block = worldIn.getBlockState(pos.down()).getBlock();
            boolean onFireSource = block.isFireSource(worldIn.real(), pos.getX(), pos.down().getY(), pos.getZ(), EnumFacing.UP.toForgeDirection());

            int age = ((Integer)state.getValue(AGE)).intValue();

            if (!onFireSource && worldIn.isRaining() && this.canDie(worldIn, pos) && rand.nextFloat() < 0.2F + (float)age * 0.03F) {
                worldIn.setBlockToAir(pos);
            }
            else {
                if (age < 15) {
                    state = state.withProperty(AGE, Integer.valueOf(age + rand.nextInt(3) / 2));
                    worldIn.setBlockState(pos, state, 4);
                }

                worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn.real()) + rand.nextInt(10));

                if (!onFireSource) {
                    if (!this.canNeighborCatchFire(worldIn, pos)) {
                        if (!worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), EnumFacing.UP) || age > 3) {
                            worldIn.setBlockToAir(pos);
                        }

                        return;
                    }

                    if (!this.canCatchFire(worldIn, pos.getX(), pos.down().getY(), pos.getZ(), EnumFacing.UP.toForgeDirection()) && age == 15 && rand.nextInt(4) == 0) {
                        worldIn.setBlockToAir(pos);
                        return;
                    }
                }

                int humidityFactor = worldIn.isBlockinHighHumidity(pos) ? 1 : 0;
                int chanceDelta = humidityFactor * -50;

                for(EnumFacing dir: EnumFacing.VALUES) {
                	int baseChance = ((dir.getAxis() == EnumFacing.Axis.Y) ? 250 : 300);
                    this.tryCatchFire(worldIn, pos.offset(dir), baseChance + chanceDelta, rand, age, dir.getOpposite());
                }

                for(BlockPos dPos : BlockPos.getAllInBox(pos.add(-1,-1,-1), pos.add(1,4,1))) {
                	if(dPos.equals(pos)) {
                		continue;
                	}
                	
                	int distance = 100;
                	
                	int y = dPos.getY() - pos.getY();
                	if(y > 1) {
                		distance += (y - 1) * 100;
                	}
                	
                	int neighEncrg = this.getNeighborEncouragement(worldIn, dPos);
                	
                	if (neighEncrg > 0) {
                		int heat = ((neighEncrg + 40 + worldIn.getDifficulty().getDifficultyId() * 7) / (age + 30)) >> humidityFactor;
                		
                		//Chance to age
                		if (heat > 0 && rand.nextInt(distance) <= heat && (!worldIn.isRaining() || !this.canDie(worldIn, dPos))) {
                			int newAge = MathHelper.clamp(age + rand.nextInt(5) / 4, 0, 15);
                			worldIn.setBlockState(dPos, Blocks.fire.getDefaultState().withProperty(AGE, Integer.valueOf(newAge)), 3);
                		}
                	}
                }
            }
        }
        
    }



	public void tryCatchFire(World worldIn, BlockPos pos, int chance, Random random, int age, EnumFacing face) {
        int flammability = worldIn.getBlockState(pos).getBlock().getFlammability(worldIn, pos, face);
        
        if (random.nextInt(chance) < flammability) {
            IBlockState iblockstate = worldIn.getBlockState(pos);
            
            if (random.nextInt(age + 10) < 5 && !worldIn.isRainingAt(pos)) {
    			int newAge = MathHelper.clamp(age + random.nextInt(5) / 4, 0, 15);
                worldIn.setBlockState(pos, Blocks.fire.getDefaultState().withProperty(AGE, Integer.valueOf(newAge)), 3);
            }
            else {
                worldIn.setBlockToAir(pos);
                Block block = iblockstate.getBlock();
            	if(block instanceof IBurningListener) {
            		((IBurningListener)block).onBurned(worldIn, iblockstate, pos);
            	}
            }
            
            if (iblockstate.getBlock() == Blocks.tnt) {
                Blocks.tnt.onBlockDestroyedByPlayer(worldIn, pos, iblockstate.withProperty(BlockTNT.EXPLODE, Boolean.valueOf(true)));
            }
        }
    }

    public boolean canNeighborCatchFire(World worldIn, BlockPos pos) {
        for (EnumFacing enumfacing : EnumFacing.values()) {
            if (this.canCatchFire(worldIn, pos.offset(enumfacing), enumfacing.getOpposite())) {
                return true;
            }
        }

        return false;
    }
    
    public int getNeighborEncouragement(World worldIn, BlockPos pos) {
    	if (worldIn.isAirBlock(pos)) {
    		int encouragementAccumulator = 0;

    		for (EnumFacing enumfacing : EnumFacing.values()) {
    			encouragementAccumulator = Math.max(worldIn.getBlockState(pos.offset(enumfacing)).getBlock().getFireSpreadSpeed(worldIn, pos.offset(enumfacing), enumfacing.getOpposite()), encouragementAccumulator);
    		}

    		return encouragementAccumulator;
    	}
        
        return 0;
    }

    
	//////////////////////////////
	// REGISTRATION
	//////////////////////////////

	ResourceLocation name;
	
	public void setRegistryName(String name) {
		ModContainer mc = Loader.instance().activeModContainer();
		String domain = mc.getModId().toLowerCase();
		setRegistryName(new ResourceLocation(domain, name));
	}
	
	@Override
	public void setRegistryName(ResourceLocation name) {
		this.name = name;
	}

	@Override
	public ResourceLocation getRegistryName() {
		return name;
	}

	@Override
	public void setUnlocalizedNameReg(String unlocalName) {
		setBlockName(unlocalName);
	}
}
