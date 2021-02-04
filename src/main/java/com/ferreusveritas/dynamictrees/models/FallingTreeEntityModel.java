package com.ferreusveritas.dynamictrees.models;

import java.util.*;
import java.util.stream.Collectors;

import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.client.QuadManipulator;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.models.modeldata.ModelConnections;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.pipeline.LightUtil;

public class FallingTreeEntityModel extends EntityModel<EntityFallingTree> {
	
	protected final List<BakedQuad> quads;
	protected final int leavesColor;
	protected final int entityId;
	
	public FallingTreeEntityModel(EntityFallingTree entity) {
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
		// BlockState.getPackedLightmapCoords no longer a method. Temporarily using getLightValue?
		return world.getBlockState(destructionData.cutPos).getLightValue(world, destructionData.cutPos);
	}
	
	public static List<BakedQuad> generateTreeQuads(EntityFallingTree entity) {
		BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
		BranchDestructionData destructionData = entity.getDestroyData();
		Direction cutDir = destructionData.cutDir;
		
		ArrayList<BakedQuad> treeQuads = new ArrayList<>();
		
		int[] connectionArray = new int[6];
		
		if(destructionData.getNumBranches() > 0) {

			BlockState exState = destructionData.getBranchBlockState(0);
			if(exState != null) {
				IBakedModel branchModel = dispatcher.getModelForState(exState);
				//Draw the ring texture cap on the cut block
				BlockPos offsetPos = BlockPos.ZERO.offset(cutDir);
				float offset = (8 - Math.min(((BranchBlock) exState.getBlock()).getRadius(exState), BranchBlock.RADMAX_NORMAL) ) / 16f;
				treeQuads.addAll(QuadManipulator.getQuads(branchModel, exState, new Vector3d(offsetPos.getX(), offsetPos.getY(), offsetPos.getZ()).scale(offset), new Direction[]{null}, new ModelConnections(cutDir)));

				for(Direction face: Direction.values()) {
					connectionArray[face.getIndex()] = face == cutDir.getOpposite() ? 8 : 0;
				}
				ModelConnections connections = new ModelConnections(connectionArray);

				//Draw the rest of the tree/branch
				for(int index = 0; index < destructionData.getNumBranches(); index++) {
					Block previousBranch = exState.getBlock();
					exState = destructionData.getBranchBlockState(index);
					if (!previousBranch.equals(exState.getBlock())) //Update the branch model only if the block is different
						branchModel = dispatcher.getModelForState(exState);
					BlockPos relPos = destructionData.getBranchRelPos(index);
					destructionData.getConnections(index, connectionArray);
					treeQuads.addAll(QuadManipulator.getQuads(branchModel, exState, new Vector3d(relPos.getX(), relPos.getY(), relPos.getZ()), connections.setAllRadii(connectionArray)));
				}
				
				//Draw the leaves
				HashMap<BlockPos, BlockState> leavesClusters = destructionData.species.getFellingLeavesClusters(destructionData);
				if(leavesClusters != null) {
					for(Map.Entry<BlockPos, BlockState> leafLoc : leavesClusters.entrySet()) {
						BlockState leafState = leafLoc.getValue();
						List<BakedQuad> leavesQuads = QuadManipulator.getQuads(dispatcher.getModelForState(leafState), leafState, new Vector3d(leafLoc.getKey().getX(), leafLoc.getKey().getY(), leafLoc.getKey().getZ()), EmptyModelData.INSTANCE);
						for (BakedQuad quad : leavesQuads){
							Direction quadFace = quad.getFace();
							if (!(quadFace == Direction.UP || quadFace == Direction.SOUTH || quadFace == Direction.WEST) || !leavesClusters.containsKey(leafLoc.getKey().offset(quadFace))){
								treeQuads.add(quad);
							}
						}
					}
				} else {
					List<BlockPos> relPosList = new LinkedList<>();
					for(BlockPos relPos : destructionData.getPositions(BranchDestructionData.PosType.LEAVES, false)) {
						relPosList.add(relPos);
					}
					for(int index = 0; index < destructionData.getNumLeaves(); index++) {
						BlockPos relPos = destructionData.getLeavesRelPos(index);
						BlockState state = destructionData.getLeavesBlockState(index);
						IBakedModel leavesModel = dispatcher.getModelForState(state);
						List<BakedQuad> leavesQuads = QuadManipulator.getQuads(leavesModel, state, new Vector3d(relPos.getX(), relPos.getY(), relPos.getZ()), EmptyModelData.INSTANCE);
						for (BakedQuad quad : leavesQuads){
							Direction quadFace = quad.getFace();
							if (!(quadFace == Direction.UP || quadFace == Direction.SOUTH || quadFace == Direction.WEST) || !relPosList.contains(relPos.offset(quadFace))){
								treeQuads.add(quad);
							}
						}
					}
				}
			}
		}
		
		return treeQuads;
	}

	@Override
	public void setRotationAngles(EntityFallingTree entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		for(BakedQuad bakedQuad : getQuads()) {
			float r = 1, g = 1, b = 1;
			if (bakedQuad.hasTintIndex()) {
				r = red;
				g = green;
				b = blue;
			}
			int diffuseAverage = 3;
			if(bakedQuad.applyDiffuseLighting()) {
				float diffuse = (LightUtil.diffuseLight(bakedQuad.getFace()) + diffuseAverage) / (diffuseAverage+1);
				r *= diffuse;
				g *= diffuse;
				b *= diffuse;
			}
			buffer.addQuad(matrixStack.getLast(), bakedQuad, r, g, b, packedLight, packedOverlay);
		}
	}
}
