package com.ferreusveritas.dynamictrees.api.network;

import java.util.Random;

import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class GrowSignal {

	//Forward data
	public float energy;
	public EnumFacing dir;
	public int numTurns;
	public int numSteps;
	private DynamicTree tree;

	public BlockPos rootPos;
	public BlockPos delta;

	//Back data
	public float radius;
	public float tapering;
	public boolean success;

	//Utility
	public Random rand;

	public GrowSignal(DynamicTree tree, BlockPos rootPos, float energy) {
		this.tree = tree;
		this.energy = energy;
		dir = EnumFacing.UP;
		radius = 0.0f;
		numTurns = 0;
		numSteps = 0;
		tapering = 0.3f;
		rand = new Random();
		success = true;

		this.rootPos = rootPos;
		delta = new BlockPos(0, 0, 0);
	}

	public DynamicTree getTree() {
		return tree;
	}

	public boolean step() {
		numSteps++;

		delta = delta.offset(dir);

		if(--energy <= 0.0f) {
			success = false;//Ran out of energy before it could grow
		}

		return success;
	}

	public boolean doTurn(EnumFacing targetDir) {
		if(dir != targetDir) {//Check for a direction change
			dir = targetDir;
			numTurns++;
			return true;
		}
		return false;
	}

	public boolean isInTrunk() {
		return numTurns == 0;
	}
}
