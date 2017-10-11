package com.ferreusveritas.dynamictrees.trees;


import com.ferreusveritas.dynamictrees.VanillaTreeData;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.network.GrowSignal;
import com.ferreusveritas.dynamictrees.ConfigHandler;
import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.inspectors.NodeFruit;
import com.ferreusveritas.dynamictrees.inspectors.NodeFruitCocoa;
import com.ferreusveritas.dynamictrees.special.BottomListenerPodzol;
import com.ferreusveritas.dynamictrees.special.BottomListenerVine;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class TreeJungle extends DynamicTree {

	public TreeJungle() {
		super(VanillaTreeData.EnumType.JUNGLE);
		
		//Jungle Trees are tall, wildly growing, fast growing trees with low branches to provide inconvenient obstruction and climbing
		setBasicGrowingParameters(0.2f, 28.0f, 3, 2, 1.0f);

		envFactor(Type.COLD, 0.15f);
		envFactor(Type.DRY,  0.20f);
		envFactor(Type.HOT, 1.1f);
		envFactor(Type.WET, 1.1f);

		canSupportCocoa = true;

		if(ConfigHandler.vineGen) {
			registerBottomListener(new BottomListenerPodzol(), new BottomListenerVine());
		}

	}

	@Override
	public boolean onTreeActivated(World world, BlockPos pos, EntityPlayer player, int facing, float px, float py, float pz) {
		ItemStack heldItem = player.getCurrentEquippedItem();
		
		if(heldItem != null) {
			if(heldItem.getItem() == Items.dye && heldItem.getItemDamage() == 3) {
				BlockBranch branch = TreeHelper.getBranch(world, pos);
				if(branch != null && branch.getRadius(world, pos) == 8) {
					EnumFacing side = EnumFacing.getOrientation(facing);					
					if(side != EnumFacing.UP && side != EnumFacing.DOWN) {
						pos = pos.offset(side);
					}
					if (pos.isAirBlock(world)) {
						int meta = DynamicTrees.blockFruitCocoa.onBlockPlaced(world, pos.getX(), pos.getY(), pos.getZ(), facing, px, py, pz, 0);
						world.setBlock(pos.getX(), pos.getY(), pos.getZ(), DynamicTrees.blockFruitCocoa, meta, 2);
						if (!player.capabilities.isCreativeMode) {
							--heldItem.stackSize;
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	protected int[] customDirectionManipulation(World world, BlockPos pos, int radius, GrowSignal signal, int probMap[]) {

		EnumFacing originDir = signal.dir.getOpposite();

		int treeHash = coordHashCode(signal.origin);
		int posHash = coordHashCode(pos);

		//Alter probability map for direction change
		probMap[0] = 0;//Down is always disallowed for jungle
		probMap[1] = signal.isInTrunk() ? getUpProbability(): 0;
		probMap[2] = probMap[3] = probMap[4] = probMap[5] = 0;
		int sideTurn = !signal.isInTrunk() || (signal.isInTrunk() && ((signal.numSteps + treeHash) % 5 == 0) && (radius > 1) ) ? 2 : 0;//Only allow turns when we aren't in the trunk(or the branch is not a twig)

		int height = 18 + ((treeHash % 7829) % 8);

		if(signal.delta.getY() < height ) {
			probMap[2 + (posHash % 4)] = sideTurn;
		} else {
			probMap[1] = probMap[2] = probMap[3] = probMap[4] = probMap[5] = 2;//At top of tree allow any direction
		}

		probMap[originDir.ordinal()] = 0;//Disable the direction we came from
		probMap[signal.dir.ordinal()] += signal.isInTrunk() ? 0 : signal.numTurns == 1 ? 2 : 1;//Favor current travel direction 

		return probMap;
	}

	@Override
	protected EnumFacing newDirectionSelected(EnumFacing newDir, GrowSignal signal) {
		if(signal.isInTrunk() && newDir != EnumFacing.UP) {//Turned out of trunk
			signal.energy = 4.0f;
		}
		return newDir;
	}

	public static int coordHashCode(BlockPos pos) {
		int hash = (pos.getX() * 9973 ^ pos.getY() * 8287 ^ pos.getZ() * 9721) >> 1;
		return hash & 0xFFFF;
	}

	//Jungle trees grow taller in suitable biomes
	@Override
	public float getEnergy(World world, BlockPos pos) {
		return super.getEnergy(world, pos) * biomeSuitability(world, pos);
	}

	@Override
	public boolean isBiomePerfect(BiomeGenBase biome) {
		return BiomeDictionary.isBiomeOfType(biome, Type.JUNGLE);
	};

	@Override
	public NodeFruit getNodeFruit(World world, BlockPos pod) {
		return world.rand.nextInt() % 16 == 0 ? new NodeFruitCocoa(this) : null;
	}

}
