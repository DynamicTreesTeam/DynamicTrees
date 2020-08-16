package com.ferreusveritas.dynamictrees.api.worldgen;

import com.ferreusveritas.dynamictrees.systems.poissondisc.PoissonDisc;

import java.util.List;
import java.util.Map;

public interface IPoissonDebug {

	void begin(int chunkX, int chunkZ);

	void collectSolved(List<PoissonDisc> discs);

	void doEdgeMasking(List<PoissonDisc> discs);

	void maskSolvedDiscs(List<PoissonDisc> discs);

	void createRootDisc(List<PoissonDisc> allDiscs, PoissonDisc rootDisc);

	void gatherUnsolved(List<PoissonDisc> unsolvedDiscs, List<PoissonDisc> allDiscs);

	void updateCount(int count, List<PoissonDisc> unsolvedDiscs, List<PoissonDisc> allDiscs);

	void pickMasterDisc(PoissonDisc master, List<PoissonDisc> unsolvedDiscs, List<PoissonDisc> allDiscs);

	void getRadius(PoissonDisc master, int radius, List<PoissonDisc> unsolvedDiscs, List<PoissonDisc> allDiscs);

	void findSecondDisc(PoissonDisc master, PoissonDisc slave, List<PoissonDisc> unsolvedDiscs, List<PoissonDisc> allDiscs);

	void maskMasterSlave(PoissonDisc master, PoissonDisc slave, List<PoissonDisc> unsolvedDiscs, List<PoissonDisc> allDiscs);

	void intersectingList(PoissonDisc slave, Map<Integer, PoissonDisc> intersecting, List<PoissonDisc> allDiscs);

	void findThirdDiscCandidate(PoissonDisc master1, PoissonDisc master2, PoissonDisc slave, List<PoissonDisc> unsolvedDiscs, List<PoissonDisc> allDiscs);

	void thirdCircleCandidateIntersects(PoissonDisc master1, PoissonDisc master2, PoissonDisc slave, PoissonDisc intersecting, List<PoissonDisc> unsolvedDiscs, List<PoissonDisc> allDiscs);

	void findThirdDiscSolved(PoissonDisc slave, List<PoissonDisc> unsolvedDiscs, List<PoissonDisc> allDiscs);

	void solveDiscs(List<PoissonDisc> unsolvedDiscs, List<PoissonDisc> allDiscs);

	void gatherUnsolved2(List<PoissonDisc> unsolvedDiscs, List<PoissonDisc> allDiscs);

	default void unsolvable(int chunkX, int chunkZ, int interations, List<PoissonDisc> unsolvedDiscs, List<PoissonDisc> discs) {
		System.err.println("-----" + unsolvedDiscs.size() + " unsolved circles-----");
		System.err.println("@ chunk x:" + chunkX + ", z:" + chunkZ);
		System.err.println("after " + interations + " iterations" );
		for(PoissonDisc c: discs) {
			System.err.println((c.hasFreeAngles() ? "->" : "  ") +  c);
		}

		//This has been moved to the debugging program
		/*if(DTConfigs.poissonDiscImageWrite) {
			PoissonDiscDebug.outputCirclesToPng(discs, chunkX, chunkZ, "");
		}*/
	}

}
