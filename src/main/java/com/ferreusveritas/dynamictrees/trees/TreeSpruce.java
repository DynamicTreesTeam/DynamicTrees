package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.VanillaTreeData;
import com.ferreusveritas.dynamictrees.api.backport.BlockAccess;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.api.cells.Cells;
import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.network.GrowSignal;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.cells.CellConiferBranch;
import com.ferreusveritas.dynamictrees.cells.CellConiferLeaf;
import com.ferreusveritas.dynamictrees.cells.CellConiferTopBranch;
import com.ferreusveritas.dynamictrees.special.BottomListenerPodzol;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class TreeSpruce extends DynamicTree {
	
	public TreeSpruce() {
		super(VanillaTreeData.EnumType.SPRUCE);
		
		//Spruce are conical thick slower growing trees
		setBasicGrowingParameters(0.25f, 16.0f, 3, 3, 0.9f);
		
		envFactor(Type.HOT, 0.50f);
		envFactor(Type.DRY, 0.25f);
		envFactor(Type.WET, 0.75f);
		
		setCellSolver(Cells.coniferSolver);
		setSmotherLeavesMax(3);
		
		registerBottomListener(new BottomListenerPodzol());
	}
	
	@Override
	protected int[] customDirectionManipulation(World world, BlockPos pos, int radius, GrowSignal signal, int probMap[]) {
		
		EnumFacing originDir = signal.dir.getOpposite();
		
		//Alter probability map for direction change
		probMap[0] = 0;//Down is always disallowed for spruce
		probMap[1] = signal.isInTrunk() ? getUpProbability(): 0;
		probMap[2] = probMap[3] = probMap[4] = probMap[5] = //Only allow turns when we aren't in the trunk(or the branch is not a twig and step is odd)
				!signal.isInTrunk() || (signal.isInTrunk() && signal.numSteps % 2 == 1 && radius > 1) ? 2 : 0;
		probMap[originDir.ordinal()] = 0;//Disable the direction we came from
		probMap[signal.dir.ordinal()] += signal.isInTrunk() ? 0 : signal.numTurns == 1 ? 2 : 1;//Favor current travel direction 
		
		return probMap;
	}
	
	@Override
	protected EnumFacing newDirectionSelected(EnumFacing newDir, GrowSignal signal) {
		if(signal.isInTrunk() && newDir != EnumFacing.UP){//Turned out of trunk
			signal.energy /= 3.0f;
		}
		return newDir;
	}
	
	protected static final ICell spruceBranch = new CellConiferBranch();
	protected static final ICell spruceTopBranch = new CellConiferTopBranch();

	public ICell getCellForBranch(IBlockAccess blockAccess, BlockPos pos, IBlockState blockState, EnumFacing dir, BlockBranch branch) {
		if(branch.getRadius(blockState) == 1) {
			return new BlockAccess(blockAccess).getBlock(pos.down()) == branch ? spruceTopBranch : spruceBranch;
		} else {
			return Cells.nullCell;
		}
	}

	protected static final ICell spruceLeafCells[] = {
		Cells.nullCell,
		new CellConiferLeaf(1),
		new CellConiferLeaf(2),
		new CellConiferLeaf(3),
		new CellConiferLeaf(4)
	};
	
	@Override
	public ICell getCellForLeaves(int hydro) {
		return spruceLeafCells[hydro];
	}
	
	//Spruce trees are so similar that it makes sense to randomize their height for a little variation
	//but we don't want the trees to always be the same height all the time when planted in the same location
	//so we feed the hash function the in-game month
	@Override
	public float getEnergy(World world, BlockPos pos) {
		long day = world.real().getTotalWorldTime() / 24000L;
		int month = (int)day / 30;//Change the hashs every in-game month
		
		return super.getEnergy(world, pos) * biomeSuitability(world, pos) + (coordHashCode(pos.up(month)) % 5);//Vary the height energy by a psuedorandom hash function
	}
	
	public static int coordHashCode(BlockPos pos) {
		int hash = (pos.getX() * 9973 ^ pos.getY() * 8287 ^ pos.getZ() * 9721) >> 1;
		return hash & 0xFFFF;
	}
	
	@Override
	public boolean isBiomePerfect(BiomeGenBase biome) {
		return BiomeDictionary.isBiomeOfType(biome, Type.CONIFEROUS);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public int foliageColorMultiplier(IBlockAccess blockAccess, int x, int y, int z) {
		return ColorizerFoliage.getFoliageColorPine();
	}
	
	@Override
	public void createLeafCluster() {
		
		setLeafCluster(new SimpleVoxmap(5, 2, 5, new byte[] {
				
				//Layer 0(Bottom)
				0, 0, 1, 0, 0,
				0, 1, 2, 1, 0,
				1, 2, 0, 2, 1,
				0, 1, 2, 1, 0,
				0, 0, 1, 0, 0,
				
				//Layer 1 (Top)
				0, 0, 0, 0, 0,
				0, 0, 1, 0, 0,
				0, 1, 1, 1, 0,
				0, 0, 1, 0, 0,
				0, 0, 0, 0, 0
				
		}).setCenter(new BlockPos(2, 0, 2)));
	}
}
