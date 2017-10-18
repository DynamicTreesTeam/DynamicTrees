package com.ferreusveritas.dynamictrees.api.treedata;

import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;

public interface ILeavesAutomata {

	public static final short cellSolverDeciduous[] = {0x0514, 0x0423, 0x0322, 0x0411, 0x0311, 0x0211};
	public static final short cellSolverConifer[] = {0x0514, 0x0413, 0x0312, 0x0211};
	public static final short hydroSolverDeciduous[] = null;
	public static final short hydroSolverConifer[] = {0x02F0, 0x0144, 0x0742, 0x0132, 0x0730};
	
	/** Maximum amount of leaves in a stack before the bottom-most leaf block dies [default = 4] **/
	public int getSmotherLeavesMax();
	/** Minimum amount of light necessary for a leaves block to be created. **/
	public int getLightRequirement();
	/** The default hydration level of a newly created leaf block [default = 4]**/
	public byte getDefaultHydration();
	/** Automata input data for hydration solver [default = Deciduous]*/
	public short[] getHydroSolution();
	/** Automata input data for cell solver [default = Deciduous]*/
	public short[] getCellSolution();
	/** A voxel map of leaves blocks that are "stamped" on to the tree during generation */
	public SimpleVoxmap getLeafCluster();
	
}
