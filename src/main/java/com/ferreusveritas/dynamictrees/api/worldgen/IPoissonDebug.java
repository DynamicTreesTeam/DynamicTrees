package com.ferreusveritas.dynamictrees.api.worldgen;

import java.util.List;
import java.util.Map;

import com.ferreusveritas.dynamictrees.systems.poissondisc.PoissonDisc;
import com.ferreusveritas.dynamictrees.systems.poissondisc.Vec2i;

public interface IPoissonDebug {

	void begin(int chunkX, int chunkZ);
	
	void collectSolved(List<PoissonDisc> discs);

	void doEdgeMasking(List<PoissonDisc> discs);

	void maskSolvedDiscs(List<PoissonDisc> discs);

	void createRootDisc(List<PoissonDisc> discs, PoissonDisc rootDisc);

	void gatherUnsolved(List<PoissonDisc> unsolvedDiscs, List<PoissonDisc> discs);

	void pickMasterDisc(PoissonDisc master, List<PoissonDisc> unsolvedDiscs);

	void getRadius(PoissonDisc master, int radius);

	void findSecondDisc(PoissonDisc master, Vec2i slavePos);

	void maskMasterSlave(PoissonDisc master, PoissonDisc slave);

	void intersectingList(PoissonDisc slave, Map<Integer, PoissonDisc> intersecting, List<PoissonDisc> discs);

	void findThirdDiscCandidate(PoissonDisc master1, PoissonDisc master2, PoissonDisc slave);

	void findThirdDiscSolved(PoissonDisc slave);

	void solveDiscs(List<PoissonDisc> unsolvedDiscs, List<PoissonDisc> discs);

	void gatherUnsolved2(List<PoissonDisc> unsolvedDiscs, List<PoissonDisc> discs);

	default void unsolvable(int chunkX, int chunkZ, int interations, List<PoissonDisc> unsolvedDiscs, List<PoissonDisc> discs) {
		System.err.println("-----" + unsolvedDiscs.size() + " unsolved circles-----");
		System.err.println("@ chunk x:" + chunkX + ", z:" + chunkZ);
		System.err.println("after " + interations + " iterations" );
		for(PoissonDisc c: discs) {
			System.err.println((c.hasFreeAngles() ? "->" : "  ") +  c);
		}
		
		//This has been moved to the debugging program
		/*if(ModConfigs.poissonDiscImageWrite) {
			PoissonDiscDebug.outputCirclesToPng(discs, chunkX, chunkZ, "");  
		}*/
	}

	
}
