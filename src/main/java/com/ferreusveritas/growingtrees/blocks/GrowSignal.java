package com.ferreusveritas.growingtrees.blocks;

import java.util.Random;

import com.ferreusveritas.growingtrees.trees.GrowingTree;

import net.minecraftforge.common.util.ForgeDirection;

public class GrowSignal {

	//Forward data
	public float energy;
	public ForgeDirection dir;
	public int numTurns;
	public int numSteps;
	private GrowingTree tree;
	public BlockBranch branchBlock;
	
	public int originX;
	public int originY;
	public int originZ;
	
	//Delta coords
	public int dx;
	public int dy;
	public int dz;
	
	//Back data
	float radius;
	float tapering;
	boolean success;

	//Utility
	public Random rand;
	
	public GrowSignal(BlockBranch branch, int x, int y, int z, float energy){
		tree = branch.getTree();
		this.branchBlock = branch;
		this.energy = energy;
		dir = ForgeDirection.UP;
		radius = 0.0f;
		numTurns = 0;
		numSteps = 0;
		tapering = 0.3f;
		rand = new Random();
		success = true;
		
		originX = x;
		originY = y;
		originZ = z;
		dx = dy = dz = 0;
	}
	
	public GrowingTree getTree(){
		return tree;
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
			return true;
		}
		return false;
	}
	
	public boolean isInTrunk(){
		return numTurns > 0;
	}
}
