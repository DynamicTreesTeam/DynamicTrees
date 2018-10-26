package com.ferreusveritas.dynamictrees.api.worldgen;

import java.util.List;
import java.util.Map;

import com.ferreusveritas.dynamictrees.util.PoissonDisc;
import com.ferreusveritas.dynamictrees.util.Vec2i;

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

	
}
