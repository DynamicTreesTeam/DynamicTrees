package com.ferreusveritas.dynamictrees.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.client.QuadManipulator;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.models.bakedmodels.ModelConnections;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.EmptyModelData;

public class ModelEntityFallingTree extends EntityModel<EntityFallingTree> {
	
	protected final List<BakedQuad> quads;
	protected final int leavesColor;
	protected final int entityId;
	
	public ModelEntityFallingTree(EntityFallingTree entity) {
		World world = entity.getEntityWorld();
		BranchDestructionData destructionData = entity.getDestroyData();
		Species species = destructionData.species;
		
		leavesColor = species.getLeavesProperties().foliageColorMultiplier(species.getLeavesProperties().getDynamicLeavesState(), world, destructionData.cutPos);
		quads = generateTreeQuads(entity);
		entityId = entity.getEntityId();
	}
	
	public List<BakedQuad> getQuads() {
		return quads;
	}
	
	public int getLeavesColor() {
		return leavesColor;
	}
	
	public int getEntityId() {
		return entityId;
	}
	
	public static int getBrightness(EntityFallingTree entity) {
		BranchDestructionData destructionData = entity.getDestroyData();
		World world = entity.getEntityWorld();
		return world.getBlockState(destructionData.cutPos).getPackedLightmapCoords(world, destructionData.cutPos);
	}
	
	public static List<BakedQuad> generateTreeQuads(EntityFallingTree entity) {
		BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
		BranchDestructionData destructionData = entity.getDestroyData();
		Direction cutDir = destructionData.cutDir;
		
		ArrayList<BakedQuad> treeQuads = new ArrayList<>();
		
		int[] connectionArray = new int[6];
		
		if(destructionData.getNumBranches() > 0) {
			
			//Draw the ring texture cap on the cut block
			BlockState exState = destructionData.getBranchBlockState(0);
			destructionData.getConnections(0, connectionArray);
			if(exState != null) {
				for(Direction face: Direction.values()) {
					connectionArray[face.getIndex()] = face == cutDir.getOpposite() ? 8 : 0;
				}
				int radius = ((BlockBranch) exState.getBlock()).getRadius(exState);
				float offset = (8 - Math.min(radius, BlockBranch.RADMAX_NORMAL) ) / 16f;
				IBakedModel branchModel = dispatcher.getModelForState(exState);//Since we source the blockState from the destruction data it will always be the same
				ModelConnections connections = new ModelConnections(connectionArray);
				treeQuads.addAll(QuadManipulator.getQuads(branchModel, exState, new Vec3d(BlockPos.ZERO.offset(cutDir)).scale(offset), new Direction[] { cutDir }, connections));
				
				//Draw the rest of the tree/branch
				for(int index = 0; index < destructionData.getNumBranches(); index++) {
					exState = destructionData.getBranchBlockState(index);
					BlockPos relPos = destructionData.getBranchRelPos(index);
					destructionData.getConnections(index, connectionArray);
					treeQuads.addAll(QuadManipulator.getQuads(branchModel, exState, new Vec3d(relPos), connections.setAllRadii(connectionArray)));
				}
				
				//Draw the leaves
				HashMap<BlockPos, BlockState> leavesClusters = destructionData.species.getFamily().getFellingLeavesClusters(destructionData);
				if(leavesClusters != null) {
					for(Map.Entry<BlockPos, BlockState> leafLoc : leavesClusters.entrySet()) {
						BlockState leafState = leafLoc.getValue();
						treeQuads.addAll(QuadManipulator.getQuads(dispatcher.getModelForState(leafState), leafLoc.getValue(), new Vec3d(leafLoc.getKey()), EmptyModelData.INSTANCE));
					}
				} else {
					BlockState state = destructionData.species.getLeavesProperties().getDynamicLeavesState();
					for(BlockPos relPos : destructionData.getPositions(BranchDestructionData.PosType.LEAVES, false)) {
						treeQuads.addAll(QuadManipulator.getQuads(dispatcher.getModelForState(state), state, new Vec3d(relPos), EmptyModelData.INSTANCE));
					}
				}
			}
		}
		
		return treeQuads;
	}
	
}
