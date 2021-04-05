package com.ferreusveritas.dynamictrees.models;

import java.util.*;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.client.QuadManipulator;
import com.ferreusveritas.dynamictrees.entities.FallingTreeEntity;
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
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.pipeline.LightUtil;

public class FallingTreeEntityModel extends EntityModel<FallingTreeEntity> {

	protected final List<BakedQuad> quads;
	protected Map<BakedQuad, Integer> quadTints;
	protected final int leavesColor;
	protected final int entityId;

	public FallingTreeEntityModel(FallingTreeEntity entity) {
		World world = entity.getCommandSenderWorld();
		BranchDestructionData destructionData = entity.getDestroyData();
		Species species = destructionData.species;

		quads = generateTreeQuads(entity);
		quadTints = entity.getQuadTints();
		leavesColor = species.getLeavesProperties().foliageColorMultiplier(species.getLeavesProperties().getDynamicLeavesState(), world, destructionData.cutPos);
		entityId = entity.getId();
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
	
	public static int getBrightness(FallingTreeEntity entity) {
		BranchDestructionData destructionData = entity.getDestroyData();
		World world = entity.getCommandSenderWorld();
		// BlockState.getPackedLightmapCoords no longer a method. Temporarily using getLightValue?
		return world.getBlockState(destructionData.cutPos).getLightValue(world, destructionData.cutPos);
	}
	
	public static List<BakedQuad> generateTreeQuads(FallingTreeEntity entity) {
		BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
		BranchDestructionData destructionData = entity.getDestroyData();
		Direction cutDir = destructionData.cutDir;
		
		ArrayList<BakedQuad> treeQuads = new ArrayList<>();
		
		int[] connectionArray = new int[6];
		
		if(destructionData.getNumBranches() > 0) {
			BlockState exState = destructionData.getBranchBlockState(0);
			if(exState != null) {

				//Draw the rooty block if it is set to fall too
				BlockPos bottomPos = entity.blockPosition().below();
				BlockState bottomState = entity.level.getBlockState(bottomPos);
				boolean rootyBlockAdded = false;
				if (TreeHelper.isRooty(bottomState)){
					RootyBlock rootyBlock = TreeHelper.getRooty(bottomState);
					if (rootyBlock.fallWithTree(bottomState,entity.level, bottomPos)){
						IBakedModel rootyModel = dispatcher.getBlockModel(bottomState);
						List<BakedQuad> quads = QuadManipulator.getQuads(rootyModel, bottomState, new Vector3d(0, -1, 0), EmptyModelData.INSTANCE);
						treeQuads.addAll(quads);
						entity.addTintedQuads(destructionData.species.getFamily().getRootColor(bottomState, rootyBlock.getColorFromBark()), quads.toArray(new BakedQuad[]{}));
						rootyBlockAdded = true;
					}
				}

				IBakedModel branchModel = dispatcher.getBlockModel(exState);
				//Draw the ring texture cap on the cut block if the bottom connection is above 0
				destructionData.getConnections(0, connectionArray);
				boolean bottomRingsAdded = false;
				if (!rootyBlockAdded && connectionArray[cutDir.get3DDataValue()] > 0){
					BlockPos offsetPos = BlockPos.ZERO.relative(cutDir);
					float offset = (8 - Math.min(((BranchBlock) exState.getBlock()).getRadius(exState), BranchBlock.MAX_RADIUS) ) / 16f;
					treeQuads.addAll(QuadManipulator.getQuads(branchModel, exState, new Vector3d(offsetPos.getX(), offsetPos.getY(), offsetPos.getZ()).scale(offset), new Direction[]{null}, new ModelConnections(cutDir)));
					bottomRingsAdded = true;
				}

				//Draw the rest of the tree/branch
				for(int index = 0; index < destructionData.getNumBranches(); index++) {
					Block previousBranch = exState.getBlock();
					exState = destructionData.getBranchBlockState(index);
					if (!previousBranch.equals(exState.getBlock())) //Update the branch model only if the block is different
						branchModel = dispatcher.getBlockModel(exState);
					BlockPos relPos = destructionData.getBranchRelPos(index);
					destructionData.getConnections(index, connectionArray);
					ModelConnections modelConnections = new ModelConnections(connectionArray);
					if (index == 0 && bottomRingsAdded) modelConnections.setForceRing(cutDir);
					treeQuads.addAll(QuadManipulator.getQuads(branchModel, exState, new Vector3d(relPos.getX(), relPos.getY(), relPos.getZ()), modelConnections));
				}
				
				//Draw the leaves
				HashMap<BlockPos, BlockState> leavesClusters = destructionData.species.getFellingLeavesClusters(destructionData);
				if(leavesClusters != null) {
					for(Map.Entry<BlockPos, BlockState> leafLoc : leavesClusters.entrySet()) {
						BlockState leafState = leafLoc.getValue();
						treeQuads.addAll(QuadManipulator.getQuads(dispatcher.getBlockModel(leafState), leafState, new Vector3d(leafLoc.getKey().getX(), leafLoc.getKey().getY(), leafLoc.getKey().getZ()), EmptyModelData.INSTANCE));
					}
				} else {
					for(int index = 0; index < destructionData.getNumLeaves(); index++) {
						BlockPos relPos = destructionData.getLeavesRelPos(index);
						BlockState state = destructionData.getLeavesBlockState(index);
						IBakedModel leavesModel = dispatcher.getBlockModel(state);
						treeQuads.addAll(QuadManipulator.getQuads(leavesModel, state, new Vector3d(relPos.getX(), relPos.getY(), relPos.getZ()), EmptyModelData.INSTANCE));
					}
				}

			}
		}
		
		return treeQuads;
	}

	@Override
	public void setupAnim(FallingTreeEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		int leavesColor = getLeavesColor();
		for(BakedQuad bakedQuad : getQuads()) {
			float r = 1, g = 1, b = 1;
			if (bakedQuad.isTinted()) {
				if (quadTints.containsKey(bakedQuad)){
					int barkColor = quadTints.get(bakedQuad);
					r = (float)(barkColor >> 16 & 255) / 255.0F;
					g = (float)(barkColor >> 8 & 255) / 255.0F;
					b = (float)(barkColor & 255) / 255.0F;
				} else {
					r = (float)(leavesColor >> 16 & 255) / 255.0F;
					g = (float)(leavesColor >> 8 & 255) / 255.0F;
					b = (float)(leavesColor & 255) / 255.0F;
				}
			}
			int diffuseAverage = 3;
			if(bakedQuad.isShade()) {
				float diffuse = (LightUtil.diffuseLight(bakedQuad.getDirection()) + diffuseAverage) / (diffuseAverage+1);
				r *= diffuse;
				g *= diffuse;
				b *= diffuse;
			}
			buffer.putBulkData(matrixStack.last(), bakedQuad, r, g, b, packedLight, packedOverlay);
		}
	}
}
