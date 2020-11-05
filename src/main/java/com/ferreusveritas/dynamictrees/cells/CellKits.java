package com.ferreusveritas.dynamictrees.cells;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.cells.CellNull;
import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.cells.ICellKit;
import com.ferreusveritas.dynamictrees.api.cells.ICellSolver;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;

import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

public class CellKits {
	
	private static final ICellSolver NULLCELLSOLVER = cells -> 0;
	
	public static final ICellKit NULLCELLKIT = new ICellKit() {
		@Override public ICell getCellForLeaves(int hydro) { return CellNull.NULLCELL; }
		@Override public ICell getCellForBranch(int radius, int meta) { return CellNull.NULLCELL; }
		@Override public ICellSolver getCellSolver() { return NULLCELLSOLVER; }
		@Override public SimpleVoxmap getLeafCluster() { return LeafClusters.NULLMAP; }
		@Override public int getDefaultHydration() { return 0; }
	};
	
	public static void setup() {
		new CellKits();
	}
	
	public CellKits() {
		TreeRegistry.registerCellKit(new ResourceLocation(DynamicTrees.MODID, "deciduous"), deciduous);
		TreeRegistry.registerCellKit(new ResourceLocation(DynamicTrees.MODID, "conifer"), conifer);
		TreeRegistry.registerCellKit(new ResourceLocation(DynamicTrees.MODID, "acacia"), acacia);
		TreeRegistry.registerCellKit(new ResourceLocation(DynamicTrees.MODID, "dark_oak"), dark_oak);
		TreeRegistry.registerCellKit(new ResourceLocation(DynamicTrees.MODID, "bare"), bare);
		TreeRegistry.registerCellKit(new ResourceLocation(DynamicTrees.MODID, "palm"), palm);
	}
	
	private final ICellKit deciduous = new ICellKit() {

		private final ICell[] normalCells = {
				CellNull.NULLCELL,
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
		public ICell getCellForBranch(int radius, int meta) {
			return radius == 1 ? branchCell : CellNull.NULLCELL;
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
		
		private final ICell coniferBranch = new CellConiferBranch();
		private final ICell coniferTopBranch = new CellConiferTopBranch();

		private final ICell[] coniferLeafCells = {
				CellNull.NULLCELL,
				new CellConiferLeaf(1),
				new CellConiferLeaf(2),
				new CellConiferLeaf(3),
				new CellConiferLeaf(4),
				new CellConiferLeaf(5),
				new CellConiferLeaf(6),
				new CellConiferLeaf(7)
		};

		private final BasicSolver coniferSolver = new BasicSolver(new short[]{0x0514, 0x0413, 0x0312, 0x0211});
		
		@Override
		public ICell getCellForLeaves(int hydro) {
			return coniferLeafCells[hydro];
		}
		
		@Override
		public ICell getCellForBranch(int radius, int meta) {
			if(meta == CellMetadata.CONIFERTOP) {
				return coniferTopBranch;
			}
			else if(radius == 1) {
				return coniferBranch;
			} else {
				return CellNull.NULLCELL;
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
			
			final int[] map = {0, 3, 5, 5, 5, 5};
			
			@Override
			public int getValueFromSide(Direction side) {
				return map[side.ordinal()];
			}
			
		};

		private final ICell[] acaciaLeafCells = {
				CellNull.NULLCELL,
				new CellAcaciaLeaf(1),
				new CellAcaciaLeaf(2),
				new CellAcaciaLeaf(3),
				new CellAcaciaLeaf(4),
				new CellAcaciaLeaf(5),
				new CellAcaciaLeaf(6),
				new CellAcaciaLeaf(7)
		};

		private final BasicSolver acaciaSolver = new BasicSolver(new short[]{0x0514, 0x0423, 0x0412, 0x0312, 0x0211});
		
		@Override
		public ICell getCellForLeaves(int hydro) {
			return acaciaLeafCells[hydro];
		}
		
		@Override
		public ICell getCellForBranch(int radius, int meta) {
			return radius == 1 ? acaciaBranch : CellNull.NULLCELL;
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
	
	
	private final ICellKit dark_oak = new ICellKit() {
		
		/** Typical branch with hydration 5 */
		private final ICell branchCell = new CellNormal(5);
		
		private final ICell[] darkOakLeafCells = {
				CellNull.NULLCELL,
				new CellDarkOakLeaf(1),
				new CellDarkOakLeaf(2),
				new CellDarkOakLeaf(3),
				new CellDarkOakLeaf(4),
				new CellDarkOakLeaf(5),
				new CellDarkOakLeaf(6),
				new CellDarkOakLeaf(7)
			}; 
		
		private final BasicSolver darkOakSolver = new BasicSolver(new short[] {0x0514, 0x0423, 0x0412, 0x0312, 0x0211});
		
		@Override
		public ICell getCellForLeaves(int hydro) {
			return darkOakLeafCells[hydro];
		}
		
		@Override
		public ICell getCellForBranch(int radius, int meta) {
			return radius == 1 ? branchCell : CellNull.NULLCELL;
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
	
	
	private final ICellKit bare = new ICellKit() {
		
		private final ICellSolver solver = new BasicSolver(new short[]{});
		
		@Override
		public ICell getCellForLeaves(int hydro) {
			return CellNull.NULLCELL;
		}
		
		@Override
		public ICell getCellForBranch(int radius, int meta) {
			return CellNull.NULLCELL;
		}
		
		@Override
		public SimpleVoxmap getLeafCluster() {
			return LeafClusters.bare;
		}
		
		@Override
		public ICellSolver getCellSolver() {
			return solver;
		}
		
		@Override
		public int getDefaultHydration() {
			return 0;
		}
		
	};
	
	private final ICellKit palm = new ICellKit() {
		
		private final ICell palmBranch = new ICell() {
			@Override
			public int getValue() {
				return 5;
			}
						
			@Override
			public int getValueFromSide(Direction side) {
				return side == Direction.UP ? getValue() : 0;
			}
			
		};
		
		private final ICell[] palmFrondCells = {
				CellNull.NULLCELL,
				new CellPalmFrond(1),
				new CellPalmFrond(2),
				new CellPalmFrond(3),
				new CellPalmFrond(4),
				new CellPalmFrond(5),
				new CellPalmFrond(6),
				new CellPalmFrond(7)
			}; 
		
		private final BasicSolver palmSolver = new BasicSolver(new short[]{0x0514, 0x0413, 0x0312, 0x0221});
		
		@Override
		public ICell getCellForLeaves(int hydro) {
			return palmFrondCells[hydro];
		}
		
		@Override
		public ICell getCellForBranch(int radius, int meta) {
			return palmBranch;
		}
		
		@Override
		public SimpleVoxmap getLeafCluster() {
			return LeafClusters.palm;
		}
		
		@Override
		public ICellSolver getCellSolver() {
			return palmSolver;
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
	*	0x0514.. (5 @ 1 = 4)  If there's 1 or more neighbor blocks with hydration 5 then make this block hydration 4
	* 
	* @param nv Array of counts of neighbor hydration values
	* @param solution Array of solver elements to solve the cell automata
	* @return resulting hydration value of the center cell
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
		
		private final short[] codes;
		
		public BasicSolver(short[] codes) {
			this.codes = codes;
		}
		
		@Override
		public int solve(ICell[] cells) {
			int[] nv = new int[16];//neighbor hydration values
			
			for(Direction dir: Direction.values()) {
				nv[cells[dir.ordinal()].getValueFromSide(dir.getOpposite())]++;
			}
						
			return solveCell(nv, codes);
		}
		
	}
	
}
