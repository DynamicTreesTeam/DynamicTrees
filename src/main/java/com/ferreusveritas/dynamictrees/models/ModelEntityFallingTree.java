package com.ferreusveritas.dynamictrees.models;

import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.client.QuadManipulator;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
public class ModelEntityFallingTree {
	
	protected final List<TreeQuadData> quads;
	protected final int entityId;
	
	public ModelEntityFallingTree(EntityFallingTree entity) {
		quads = generateTreeQuads(entity, entity.getEntityWorld());
		entityId = entity.getEntityId();
	}
	
	public List<TreeQuadData> getQuadData() {
		return quads;
	}
	
	public int getEntityId() {
		return entityId;
	}
	
	public static int getBrightness(EntityFallingTree entity) {
		BranchDestructionData destructionData = entity.getDestroyData();
		World world = entity.getEntityWorld();
		return world.getBlockState(destructionData.cutPos).getPackedLightmapCoords(world, destructionData.cutPos);
	}
	
	public List<TreeQuadData> generateTreeQuads(EntityFallingTree entity, World world) {
		final BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
		final BranchDestructionData destructionData = entity.getDestroyData();
		final Species species = destructionData.species;
		final BlockPos cutPos = destructionData.cutPos;
		final EnumFacing cutDir = destructionData.cutDir;
		
		final ArrayList<TreeQuadData> treeQuads = new ArrayList<>();
		
		if (destructionData.getNumBranches() <= 0) {
			return treeQuads;
		}

		// Draw the ring texture cap on the cut block
		IExtendedBlockState exState = destructionData.getBranchBlockState(0);
		
		if (exState == null) {
			return treeQuads;
		}
		
		for (EnumFacing face: EnumFacing.VALUES) {
			exState = exState.withProperty(BlockBranch.CONNECTIONS[face.getIndex()], face == cutDir.getOpposite() ? 8 : 0);
		}
		int radius = ((BlockBranch) exState.getBlock()).getRadius(exState);
		float offset = (8 - Math.min(radius, BlockBranch.RADMAX_NORMAL) ) / 16f;
		IBakedModel branchModel = dispatcher.getModelForState(exState.getClean()); // Since we source the blockState from the destruction data it will always be the same
		treeQuads.addAll(toTreeQuadData(QuadManipulator.getQuads(branchModel, exState, new Vec3d(BlockPos.ORIGIN.offset(cutDir)).scale(offset), new EnumFacing[] { cutDir }), 0xFFFFFFFF, exState.getClean()));

		// Draw the rest of the tree/branch
		for (int index = 0; index < destructionData.getNumBranches(); index++) {
			Block previousBranch = exState.getBlock();
			exState = destructionData.getBranchBlockState(index);
			if (!previousBranch.equals(exState.getBlock())) // Update the branch model only if the block is different
				branchModel = dispatcher.getModelForState(exState.getClean());
			BlockPos relPos = destructionData.getBranchRelPos(index);
			treeQuads.addAll(toTreeQuadData(QuadManipulator.getQuads(branchModel, exState, new Vec3d(relPos)), 0xFFFFFFFF, exState.getClean()));
		}

		// Draw the leaves
		HashMap<BlockPos, IBlockState> leavesClusters = species.getFamily().getFellingLeavesClusters(destructionData);
		if (leavesClusters != null) {
			for(Entry<BlockPos, IBlockState> leafLoc : leavesClusters.entrySet()) {
				IBlockState leafState = leafLoc.getValue();
				if (leafState instanceof IExtendedBlockState) {
					leafState = ((IExtendedBlockState) leafState).getClean();
				}
				treeQuads.addAll(toTreeQuadData(QuadManipulator.getQuads(dispatcher.getModelForState(leafState), leafLoc.getValue(), new Vec3d(leafLoc.getKey())), 
					species.getLeavesProperties().foliageColorMultiplier(leafState, world, cutPos), leafState));				
			}
		} else {
			for (int index = 0; index < destructionData.getNumLeaves(); index++) {
				BlockPos relPos = destructionData.getLeavesRelPos(index);
				IBlockState state = destructionData.getLeavesBlockState(index);
				IBakedModel leavesModel = dispatcher.getModelForState(state);
				treeQuads.addAll(toTreeQuadData(QuadManipulator.getQuads(leavesModel, state, new Vec3d(relPos)), 
					destructionData.getLeavesProperties(index).foliageColorMultiplier(state, world, cutPos.add(relPos)), state));
			}
		}
		
		return treeQuads;
	}
	
	public static List<TreeQuadData> toTreeQuadData (List<BakedQuad> bakedQuads, int color, IBlockState state) {
		return bakedQuads.stream().map(bakedQuad -> new TreeQuadData(bakedQuad, color, state)).collect(Collectors.toList());
	}
	
	public static final class TreeQuadData {
		public final BakedQuad bakedQuad;
		public final IBlockState state;
		public final int color;

		public TreeQuadData(BakedQuad bakedQuad, int color, IBlockState state) {
			this.bakedQuad = bakedQuad;
			this.state = state;
			this.color = color;
		}
	}

}
