package com.ferreusveritas.dynamictrees.cells;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.cells.CellNull;
import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.cells.CellKit;
import com.ferreusveritas.dynamictrees.api.cells.ICellSolver;
import com.ferreusveritas.dynamictrees.api.registry.Registry;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;

import net.minecraft.util.Direction;

public class CellKits {

	public static final CellKit DECIDUOUS = new CellKit(DynamicTrees.resLoc("deciduous")) {

		private final ICell[] normalCells = {
				CellNull.NULL_CELL,
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
			return radius == 1 ? branchCell : CellNull.NULL_CELL;
		}
		
		@Override
		public SimpleVoxmap getLeafCluster() {
			return LeafClusters.DECIDUOUS;
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

	public static final CellKit CONIFER = new CellKit(DynamicTrees.resLoc("conifer")) {
		
		private final ICell coniferBranch = new ConiferBranchCell();
		private final ICell coniferTopBranch = new ConiferTopBranchCell();

		private final ICell[] coniferLeafCells = {
				CellNull.NULL_CELL,
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
				return CellNull.NULL_CELL;
			}
		}
		
		@Override
		public SimpleVoxmap getLeafCluster() {
			return LeafClusters.CONIFER;
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

	public static final CellKit ACACIA = new CellKit(DynamicTrees.resLoc("acacia")) {
		
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
				CellNull.NULL_CELL,
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
			return radius == 1 ? acaciaBranch : CellNull.NULL_CELL;
		}
		
		@Override
		public SimpleVoxmap getLeafCluster() {
			return LeafClusters.ACACIA;
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

	public static final CellKit DARK_OAK = new CellKit(DynamicTrees.resLoc("dark_oak")) {
		
		/** Typical branch with hydration 5 */
		private final ICell branchCell = new NormalCell(5);
		
		private final ICell[] darkOakLeafCells = {
				CellNull.NULL_CELL,
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
			return radius == 1 ? branchCell : CellNull.NULL_CELL;
		}

		@Override
		public SimpleVoxmap getLeafCluster() {
			return LeafClusters.DARK_OAK;
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

	public static final CellKit BARE = new CellKit(DynamicTrees.resLoc("bare")) {
		
		private final ICellSolver solver = new BasicSolver(new short[]{});
		
		@Override
		public ICell getCellForLeaves(int hydro) {
			return CellNull.NULL_CELL;
		}
		
		@Override
		public ICell getCellForBranch(int radius, int meta) {
			return CellNull.NULL_CELL;
		}
		
		@Override
		public SimpleVoxmap getLeafCluster() {
			return LeafClusters.BARE;
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
	
	public static final CellKit PALM = new CellKit(DynamicTrees.resLoc("palm")) {
		
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
				CellNull.NULL_CELL,
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
			return LeafClusters.PALM;
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

	public static final CellKit NETHER_FUNGUS = new CellKit(DynamicTrees.resLoc("nether_fungus")) {

		private final ICell[] netherCells = {
				CellNull.NULL_CELL,
				new NetherFungusLeafCell(1),
				new NetherFungusLeafCell(2),
				new NetherFungusLeafCell(3),
				new NetherFungusLeafCell(4),
				new NetherFungusLeafCell(5),
				new NetherFungusLeafCell(6),
				new NetherFungusLeafCell(7)
		};

		/** Typical branch with hydration 5 */
		private final ICell branchCell = new NormalCell(8);

		private final BasicSolver netherFungusSolver = new BasicSolver(new short[]{0x0817, 0x0726, 0x0635, 0x0513, 0x0312, 0x0211});

		@Override
		public ICell getCellForLeaves(int hydro) {
			return netherCells[hydro];
		}

		@Override
		public ICell getCellForBranch(int radius, int meta) { return radius == 3 ? branchCell : CellNull.NULL_CELL; }

		@Override
		public SimpleVoxmap getLeafCluster() {
			return LeafClusters.NETHER_FUNGUS;
		}

		@Override
		public ICellSolver getCellSolver() {
			return netherFungusSolver;
		}

		@Override
		public int getDefaultHydration() {
			return 7;
		}

	};

	public static void register(final Registry<CellKit> registry) {
		registry.registerAll(DECIDUOUS, CONIFER, ACACIA, DARK_OAK, BARE, PALM, NETHER_FUNGUS);
	}

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
