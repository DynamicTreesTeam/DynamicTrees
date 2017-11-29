package com.ferreusveritas.dynamictrees.api.network;

import java.util.Random;

import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;

public class GrowSignal {

	//Forward data
	public float energy;
	public EnumFacing dir;
	public int numTurns;
	public int numSteps;
	private DynamicTree tree;
	public BlockBranch branchBlock;

	public BlockPos origin;
	public BlockPos delta;

	//Back data
	public float radius;
	public float tapering;
	public boolean success;

	//Utility
	public Random rand;

	public GrowSignal(BlockBranch branch, BlockPos pos, float energy) {
		tree = branch.getTree();
		this.branchBlock = branch;
		this.energy = energy;
		dir = EnumFacing.UP;
		radius = 0.0f;
		numTurns = 0;
		numSteps = 0;
		tapering = 0.3f;
		rand = new Random();
		success = true;

		origin = pos;
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
