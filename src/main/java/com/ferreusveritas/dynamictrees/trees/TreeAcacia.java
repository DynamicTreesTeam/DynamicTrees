package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.VanillaTreeData;
import com.ferreusveritas.dynamictrees.api.backport.Biome;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.IBlockAccess;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.backport.SpeciesRegistry;
import com.ferreusveritas.dynamictrees.api.cells.Cells;
import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.cells.CellAcaciaLeaf;
import com.ferreusveritas.dynamictrees.util.CompatHelper;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;

import net.minecraftforge.common.BiomeDictionary.Type;

public class TreeAcacia extends DynamicTree {
		
	public class SpeciesAcacia extends Species {

		SpeciesAcacia(DynamicTree treeFamily) {
			super(treeFamily.getName(), treeFamily);
			
			//Acacia Trees are short, very slowly growing trees
			setBasicGrowingParameters(0.15f, 12.0f, 0, 3, 0.7f);
		
			envFactor(Type.COLD, 0.25f);
			envFactor(Type.NETHER, 0.75f);
			envFactor(Type.WET, 0.75f);
			
		}
		
		@Override
		public boolean isBiomePerfect(Biome biome) {
			return CompatHelper.biomeHasType(biome, Type.SAVANNA);
		}
		
	}
	
	Species species;
	
	public TreeAcacia() {
		super(VanillaTreeData.EnumType.ACACIA);

		setCellSolver(Cells.acaciaSolver);
		
		setSmotherLeavesMax(2);//very thin canopy
	}

	@Override
	public void createSpecies() {
		species = new SpeciesAcacia(this);
	}
	
	@Override
	public void registerSpecies(SpeciesRegistry speciesRegistry) {
		speciesRegistry.register(species);
	}
	
	@Override
	public Species getCommonSpecies() {
		return species;
	}
	
	protected static final ICell acaciaBranch = new ICell() {
		@Override
		public int getValue() {
			return 5;
		}

		final int map[] = {0, 3, 5, 5, 5, 5};
		
		@Override
		public int getValueFromSide(EnumFacing side) {
			return map[side.ordinal()];
		}
		
	};
	
	@Override
	public ICell getCellForBranch(IBlockAccess blockAccess, BlockPos pos, IBlockState blockState, EnumFacing dir, BlockBranch branch) {
		return (branch.getRadius(blockState) == 1) ? acaciaBranch : Cells.nullCell;
	}
	
	protected static final ICell acaciaLeafCells[] = {
			Cells.nullCell,
			new CellAcaciaLeaf(1),
			new CellAcaciaLeaf(2),
			new CellAcaciaLeaf(3),
			new CellAcaciaLeaf(4)
		}; 
	
	@Override
	public ICell getCellForLeaves(int hydro) {
		return acaciaLeafCells[hydro];
	}
	
	@Override
	public void createLeafCluster(){
		
		setLeafCluster(new SimpleVoxmap(7, 2, 7, new byte[] {
				
				//Layer 0(Bottom)
				0, 0, 1, 1, 1, 0, 0,
				0, 1, 2, 2, 2, 1, 0,
				1, 2, 3, 4, 3, 2, 1,
				1, 2, 4, 0, 4, 2, 1,
				1, 2, 3, 4, 3, 2, 1,
				0, 1, 2, 2, 2, 1, 0,
				0, 0, 1, 1, 1, 0, 0,
				
				//Layer 1 (Top)
				0, 0, 0, 0, 0, 0, 0,
				0, 0, 1, 1, 1, 0, 0,
				0, 1, 2, 2, 2, 1, 0,
				0, 1, 2, 2, 2, 1, 0,
				0, 1, 2, 2, 2, 1, 0,
				0, 0, 1, 1, 1, 0, 0,
				0, 0, 0, 0, 0, 0, 0
				
		}).setCenter(new BlockPos(3, 0, 3)));
		
	}
	
}
