package com.ferreusveritas.dynamictrees.api.cells;

import net.minecraft.util.EnumFacing;

public class Cells {

	/** A cell that always returns 0 */
	public static final ICell nullCell = new CellNull();

	/** Typical branch with hydration 5 */
	public static final ICell branchCell = new CellNormal(5);
	
	public static final ICell normalCells[] = {
			nullCell,
			new CellNormal(1),
			new CellNormal(2),
			new CellNormal(3),
			new CellNormal(4),
			new CellNormal(5),
			new CellNormal(6),
			new CellNormal(7)
	};
	
	//General purpose solvers
	public static final BasicSolver deciduousSolver = new BasicSolver(new short[]{0x0514, 0x0423, 0x0322, 0x0411, 0x0311, 0x0211});
	public static final BasicSolver coniferSolver = new BasicSolver(new short[]{0x0514, 0x0413, 0x0312, 0x0211});
	public static final BasicSolver acaciaSolver = new Cells.BasicSolver(new short[]{0x0514, 0x0423, 0x0412, 0x0312, 0x0211});
	public static final BasicSolver darkOakSolver = new Cells.BasicSolver(new short[] {0x0514, 0x0423, 0x0412, 0x0312, 0x0211});

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
