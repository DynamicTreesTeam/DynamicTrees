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
		TreeRegistry.registerCellKit(new ResourceLocation(DynamicTrees.MOD_ID, "deciduous"), deciduous);
		TreeRegistry.registerCellKit(new ResourceLocation(DynamicTrees.MOD_ID, "conifer"), conifer);
		TreeRegistry.registerCellKit(new ResourceLocation(DynamicTrees.MOD_ID, "acacia.json"), acacia);
		TreeRegistry.registerCellKit(new ResourceLocation(DynamicTrees.MOD_ID, "dark_oak"), dark_oak);
		TreeRegistry.registerCellKit(new ResourceLocation(DynamicTrees.MOD_ID, "bare"), bare);
		TreeRegistry.registerCellKit(new ResourceLocation(DynamicTrees.MOD_ID, "palm"), palm);
	}
	
	private final ICellKit deciduous = new ICellKit() {

		private final ICell[] normalCells = {
				CellNull.NULLCELL,
				new NormalCell(1),
				new NormalCell(2),
				new NormalCell(3),
				new NormalCell(4),
				new NormalCell(5),
				new NormalCell(6),
				new NormalCell(7)
		};
		
		/** Typical branch with hydration 5 */
		private final ICell branchCell = new NormalCell(5);
		
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
		
		private final ICell coniferBranch = new ConiferBranchCell();
		private final ICell coniferTopBranch = new ConiferTopBranchCell();

		private final ICell[] coniferLeafCells = {
				CellNull.NULLCELL,
				new ConiferLeafCell(1),
				new ConiferLeafCell(2),
				new ConiferLeafCell(3),
				new ConiferLeafCell(4),
				new ConiferLeafCell(5),
				new ConiferLeafCell(6),
				new ConiferLeafCell(7)
		};

		private final BasicSolver coniferSolver = new BasicSolver(new short[]{0x0514, 0x0413, 0x0312, 0x0211});
		
		@Override
		public ICell getCellForLeaves(int hydro) {
			return coniferLeafCells[hydro];
		}
		
		@Override
		public ICell getCellForBranch(int radius, int meta) {
			if(meta == MetadataCell.CONIFERTOP) {
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
				new AcaciaLeafCell(1),
				new AcaciaLeafCell(2),
				new AcaciaLeafCell(3),
				new AcaciaLeafCell(4),
				new AcaciaLeafCell(5),
				new AcaciaLeafCell(6),
				new AcaciaLeafCell(7)
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
		private final ICell branchCell = new NormalCell(5);
		
		private final ICell[] darkOakLeafCells = {
				CellNull.NULLCELL,
				new DarkOakLeafCell(1),
				new DarkOakLeafCell(2),
				new DarkOakLeafCell(3),
				new DarkOakLeafCell(4),
				new DarkOakLeafCell(5),
				new DarkOakLeafCell(6),
				new DarkOakLeafCell(7)
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
				new PalmFrondCell(1),
				new PalmFrondCell(2),
				new PalmFrondCell(3),
				new PalmFrondCell(4),
				new PalmFrondCell(5),
				new PalmFrondCell(6),
				new PalmFrondCell(7)
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
