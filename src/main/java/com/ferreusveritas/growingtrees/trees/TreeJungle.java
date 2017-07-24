package com.ferreusveritas.growingtrees.trees;

import com.ferreusveritas.growingtrees.ConfigHandler;
import com.ferreusveritas.growingtrees.GrowingTrees;
import com.ferreusveritas.growingtrees.TreeHelper;
import com.ferreusveritas.growingtrees.blocks.BlockBranch;
import com.ferreusveritas.growingtrees.blocks.GrowSignal;
import com.ferreusveritas.growingtrees.inspectors.NodeFruit;
import com.ferreusveritas.growingtrees.inspectors.NodeFruitCocoa;
import com.ferreusveritas.growingtrees.special.BottomListenerPodzol;
import com.ferreusveritas.growingtrees.special.BottomListenerVine;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.util.ForgeDirection;

public class TreeJungle extends GrowingTree {

	public TreeJungle(int seq) {
		super("jungle", seq);

		//Jungle Trees are tall, wildly growing, fast growing trees with low branches to provide inconvenient obstruction and climbing
		setBasicGrowingParameters(0.2f, 28.0f, 3, 2, 1.0f);

		setPrimitiveLeaves(Blocks.leaves, 3);//Vanilla Jungle Leaves
		setPrimitiveLog(Blocks.log, 3);//Vanilla Jungle Log
		setPrimitiveSapling(Blocks.sapling, 3);

		envFactor(Type.COLD, 0.15f);
		envFactor(Type.DRY,  0.20f);
		envFactor(Type.HOT, 1.1f);
		envFactor(Type.WET, 1.1f);

		canSupportCocoa = true;

		if(ConfigHandler.vineGen) {
			registerBottomSpecials(new BottomListenerPodzol(), new BottomListenerVine());
		}

	}

	@Override
	public boolean onTreeActivated(World world, int x, int y, int z, EntityPlayer player, int side, float px, float py, float pz) {
		ItemStack equippedItem = player.getCurrentEquippedItem();
		
		if(equippedItem != null) {
			if(equippedItem.getItem() == Items.dye && equippedItem.getItemDamage() == 3) {
				BlockBranch branch = TreeHelper.getBranch(world, x, y, z);
				if(branch != null && branch.getRadius(world, x, y, z) == 8) {
					switch(side) {
						default: return false;
						case 2: z--; break;
						case 3: z++; break;
						case 4: x--; break;
						case 5: x++; break;
					}
					if (world.isAirBlock(x, y, z)) {
						int meta = GrowingTrees.blockFruitCocoa.onBlockPlaced(world, x, y, z, side, px, py, pz, 0);
						world.setBlock(x, y, z, GrowingTrees.blockFruitCocoa, meta, 2);
						if (!player.capabilities.isCreativeMode) {
							--equippedItem.stackSize;
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	protected int[] customDirectionManipulation(World world, int x, int y, int z, int radius, GrowSignal signal, int probMap[]) {

		ForgeDirection originDir = signal.dir.getOpposite();

		int treeHash = coordHashCode(signal.originX, signal.originY, signal.originZ);
		int posHash = coordHashCode(x, y, z);

		//Alter probability map for direction change
		probMap[0] = 0;//Down is always disallowed for jungle
		probMap[1] = signal.isInTrunk() ? getUpProbability(): 0;
		probMap[2] = probMap[3] = probMap[4] = probMap[5] = 0;
		int sideTurn = !signal.isInTrunk() || (signal.isInTrunk() && ((signal.numSteps + treeHash) % 5 == 0) && (radius > 1) ) ? 2 : 0;//Only allow turns when we aren't in the trunk(or the branch is not a twig)

		int height = 18 + ((treeHash % 7829) % 8);

		if(signal.dy < height ) {
			probMap[2 + (posHash % 4)] = sideTurn;
		} else {
			probMap[1] = probMap[2] = probMap[3] = probMap[4] = probMap[5] = 2;//At top of tree allow any direction
		}

		probMap[originDir.ordinal()] = 0;//Disable the direction we came from
		probMap[signal.dir.ordinal()] += signal.isInTrunk() ? 0 : signal.numTurns == 1 ? 2 : 1;//Favor current travel direction 

		return probMap;
	}

	@Override
	protected ForgeDirection newDirectionSelected(ForgeDirection newDir, GrowSignal signal) {
		if(signal.isInTrunk() && newDir != ForgeDirection.UP) {//Turned out of trunk
			signal.energy = 4.0f;
		}
		return newDir;
	}

	public static int coordHashCode(int x, int y, int z) {
		int hash = (x * 9973 ^ y * 8287 ^ z * 9721) >> 1;
		return hash & 0xFFFF;
	}

	//Jungle trees grow taller in suitable biomes
	@Override
	public float getEnergy(World world, int x, int y, int z) {
		return super.getEnergy(world, x, y, z) * biomeSuitability(world, x, y, z);
	}

	@Override
	public boolean isBiomePerfect(BiomeGenBase biome) {
		return BiomeDictionary.isBiomeOfType(biome, Type.JUNGLE);
	};

	@Override
	public NodeFruit getNodeFruit(World world, int x, int y, int z) {
		return world.rand.nextInt() % 16 == 0 ? new NodeFruitCocoa(this) : null;
	}

}
