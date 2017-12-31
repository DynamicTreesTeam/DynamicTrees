package com.ferreusveritas.dynamictrees.api.cells;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.cells.CellAcaciaLeaf;
import com.ferreusveritas.dynamictrees.cells.CellConiferBranch;
import com.ferreusveritas.dynamictrees.cells.CellConiferLeaf;
import com.ferreusveritas.dynamictrees.cells.CellConiferTopBranch;
import com.ferreusveritas.dynamictrees.cells.CellDarkOakLeaf;
import com.ferreusveritas.dynamictrees.misc.LeafClusters;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class CellKits {
	
	/** A cell that always returns 0 */
	public static final ICell nullCell = new CellNull();
	
	public static void preInit() {
		new CellKits();
	}
	
	public CellKits() {
		TreeRegistry.registerCellKit(new ResourceLocation(ModConstants.MODID, "deciduous"), deciduous);
		TreeRegistry.registerCellKit(new ResourceLocation(ModConstants.MODID, "conifer"), conifer);
		TreeRegistry.registerCellKit(new ResourceLocation(ModConstants.MODID, "acacia"), acacia);
		TreeRegistry.registerCellKit(new ResourceLocation(ModConstants.MODID, "darkoak"), darkoak);
	}
	
	private final ICellKit deciduous = new ICellKit() {

		private final ICell normalCells[] = {
				nullCell,
				new CellNormal(1),
				new CellNormal(2),
				new CellNormal(3),
				new CellNormal(4),
				new CellNormal(5),
				new CellNormal(6),
				new CellNormal(7)
		};

		/** Typical branch with hydration 5 */
		private final ICell branchCell = new CellNormal(5);

		private final BasicSolver deciduousSolver = new BasicSolver(new short[]{0x0514, 0x0423, 0x0322, 0x0411, 0x0311, 0x0211});
		
		@Override
		public ICell getCellForLeaves(int hydro) {
			return normalCells[hydro];
		}

		@Override
		public ICell getCellForBranch(int radius) {
			return radius == 1 ? branchCell : nullCell;
		}

		@Override
		public SimpleVoxmap getLeafCluster() {
			return LeafClusters.deciduous;
		}

		@Override
		public ICellSolver getCellSolver() {
			return deciduousSolver;
		}

		@Override
		public int getDefaultHydration() {
			return 4;
		}
		
	};

	
	
	private final ICellKit conifer = new ICellKit() {
		
		private final ICell spruceBranch = new CellConiferBranch();
		private final ICell spruceTopBranch = new CellConiferTopBranch();

		private final ICell spruceLeafCells[] = {
			nullCell,
			new CellConiferLeaf(1),
			new CellConiferLeaf(2),
			new CellConiferLeaf(3),
			new CellConiferLeaf(4)
		};
		
		private final BasicSolver coniferSolver = new BasicSolver(new short[]{0x0514, 0x0413, 0x0312, 0x0211});
		
		@Override
		public ICell getCellForLeaves(int hydro) {
			return spruceLeafCells[hydro];
		}

		@Override
		public ICell getCellForBranch(int radius) {
			if(radius == 1) {
				return spruceBranch;
			} else if(radius == 128) {
				return spruceTopBranch;
			} else {
				return nullCell;
			}
		}

		@Override
		public SimpleVoxmap getLeafCluster() {
			return LeafClusters.conifer;
		}

		@Override
		public ICellSolver getCellSolver() {
			return coniferSolver;
		}

		@Override
		public int getDefaultHydration() {
			return 4;
		}
		
	};
	
	
	
	private final ICellKit acacia = new ICellKit() {

		private final ICell acaciaBranch = new ICell() {
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
		
		private final ICell acaciaLeafCells[] = {
				nullCell,
				new CellAcaciaLeaf(1),
				new CellAcaciaLeaf(2),
				new CellAcaciaLeaf(3),
				new CellAcaciaLeaf(4)
			}; 

		private final BasicSolver acaciaSolver = new BasicSolver(new short[]{0x0514, 0x0423, 0x0412, 0x0312, 0x0211});
		
		@Override
		public ICell getCellForLeaves(int hydro) {
			return acaciaLeafCells[hydro];
		}

		@Override
		public ICell getCellForBranch(int radius) {
			return radius == 1 ? acaciaBranch : nullCell;
		}

		@Override
		public SimpleVoxmap getLeafCluster() {
			return LeafClusters.acacia;
		}

		@Override
		public ICellSolver getCellSolver() {
			return acaciaSolver;
		}

		@Override
		public int getDefaultHydration() {
			return 4;
		}
		
	};
	
	
	private final ICellKit darkoak = new ICellKit() {

		/** Typical branch with hydration 5 */
		private final ICell branchCell = new CellNormal(5);
		
		private final ICell darkOakLeafCells[] = {
				nullCell,
				new CellDarkOakLeaf(1),
				new CellDarkOakLeaf(2),
				new CellDarkOakLeaf(3),
				new CellDarkOakLeaf(4)
			}; 
		
		private final BasicSolver darkOakSolver = new BasicSolver(new short[] {0x0514, 0x0423, 0x0412, 0x0312, 0x0211});
		
		@Override
		public ICell getCellForLeaves(int hydro) {
			return darkOakLeafCells[hydro];
		}

		@Override
		public ICell getCellForBranch(int radius) {
			return radius == 1 ? branchCell : nullCell;
		}

		@Override
		public SimpleVoxmap getLeafCluster() {
			return LeafClusters.darkoak;
		}

		@Override
		public ICellSolver getCellSolver() {
			return darkOakSolver;
		}

		@Override
		public int getDefaultHydration() {
			return 4;
		}
		
	};
	
	
	/**
	* Cellular automata function that determines the behavior of the center cell from it's neighbors.
	* Values here are the number of neighbors for each hydration level.  Must be 16 elements.
	* Override member function to create unique species behavior
	*	4 Hex digits.. 0xXHCR  
	*	X: Reserved
	*	H: Selected hydration value
	*	C: Minimum count of neighbor blocks with selected hydration H
	*	R: Resulting Hydration
	*
	* Example:
	*	exampleSolver = 0x0514, 0x0413, 0x0312, 0x0211
	*	0x0514.. (5 X 1 = 4)  If there's 1 or more neighbor blocks with hydration 5 then make this block hydration 4
	* 
	* @param nv Array of counts of neighbor hydration values
	* @param solution Array of solver elements to solve the cell automata
	* @return
	*/
	public static int solveCell(int[] nv, short[] solution) {
		for(int d: solution) {
			if(nv[(d >> 8) & 15] >= ((d >> 4) & 15)) {
				return d & 15;
			}
		}
		return 0;
	}
	
	static public class BasicSolver implements ICellSolver {

		private final short codes[];
		
		public BasicSolver(short codes[]) {
			this.codes = codes;
		}
		
		@Override
		public int solve(ICell[] cells) {
			int nv[] = new int[16];//neighbor hydration values
			
			for(EnumFacing dir: EnumFacing.VALUES) {
				nv[cells[dir.ordinal()].getValueFromSide(dir.getOpposite())]++;
			}
						
			return solveCell(nv, codes);
		}
		
	}
	
}
