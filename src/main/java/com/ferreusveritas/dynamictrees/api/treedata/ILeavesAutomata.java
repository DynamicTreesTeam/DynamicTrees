package com.ferreusveritas.dynamictrees.api.treedata;

import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;

public interface ILeavesAutomata {


	
	/** Maximum amount of leaves in a stack before the bottom-most leaf block dies [default = 4] **/
	public int getSmotherLeavesMax();
	/** Minimum amount of light necessary for a leaves block to be created. **/
	public int getLightRequirement();
	/** The default hydration level of a newly created leaf block [default = 4]**/
	public byte getDefaultHydration();
	/** Automata input data for cell solver [default = Deciduous]*/
	public short[] getCellSolution();
	/** A voxel map of leaves blocks that are "stamped" on to the tree during generation */
	public SimpleVoxmap getLeafCluster();
	
}
