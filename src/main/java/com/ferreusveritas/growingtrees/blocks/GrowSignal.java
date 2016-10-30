package com.ferreusveritas.growingtrees.blocks;

import java.util.Random;

import net.minecraftforge.common.util.ForgeDirection;

public class GrowSignal {

	//Forward data
	float energy;
	ForgeDirection dir;
	int numTurns;
	int numSteps;
	boolean inTrunk;
	BlockBranch branchBlock;
	
	int originX;
	int originY;
	int originZ;
	
	//Delta coords
	int dx;
	int dy;
	int dz;
	
	//Back data
	float radius;
	float tapering;
	boolean success;

	//Utility
	Random rand;
	
	public GrowSignal(BlockBranch branch, int x, int y, int z, float energy){
		this.branchBlock = branch;
		this.energy = energy;
		dir = ForgeDirection.UP;
		radius = 0.0f;
		numTurns = 0;
		numSteps = 0;
		tapering = 0.3f;
		inTrunk = true;
		rand = new Random();
		success = true;
		
		originX = x;
		originY = y;
		originZ = z;
		dx = dy = dz = 0;
	}
	
	public boolean step(){
		numSteps++;
		
		dx += dir.offsetX;
		dy += dir.offsetY;
		dz += dir.offsetZ;
		
		if(--energy <= 0.0f){
			success = false;//Ran out of energy before it could grow
		}
		
		return success;
	}
	
	public boolean doTurn(ForgeDirection targetDir){
		if(dir != targetDir) {//Check for a direction change
			dir = targetDir;
			numTurns++;
			inTrunk = false;
			return true;
		}
		return false;
	}
	
}
