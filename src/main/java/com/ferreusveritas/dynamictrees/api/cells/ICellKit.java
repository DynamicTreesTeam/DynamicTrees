package com.ferreusveritas.dynamictrees.api.cells;

import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;

public interface ICellKit {

	ICell getCellForLeaves(int hydro);
	
	ICell getCellForBranch(int radius, int meta);
	
	ICellSolver getCellSolver();
	
	/** A voxel map of leaves blocks that are "stamped" on to the tree during generation */
	SimpleVoxmap getLeafCluster();
	
	/** The default hydration level of a newly created leaf block [default = 4]**/
	int getDefaultHydration();
	
}
