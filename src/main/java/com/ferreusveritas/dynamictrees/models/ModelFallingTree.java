package com.ferreusveritas.dynamictrees.models;

import java.util.ArrayList;
import java.util.List;

import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockBranchBasic;
import com.ferreusveritas.dynamictrees.client.QuadManipulator;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData;

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

@SideOnly(Side.CLIENT)
public class ModelFallingTree {
	
	protected final List<BakedQuad> quads;
	protected final int leavesColor;
	protected final int entityId;
	
	public ModelFallingTree(EntityFallingTree entity) {
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
		BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
		BranchDestructionData destructionData = entity.getDestroyData();
		EnumFacing cutDir = destructionData.cutDir;
		
		ArrayList<BakedQuad> treeQuads = new ArrayList<>();
		
		//Draw the ring texture cap on the cut block
		IExtendedBlockState exState = destructionData.getNumBranches() > 0 ? destructionData.getBranchBlockState(0) : null;
		for(EnumFacing face: EnumFacing.VALUES) {
			exState = exState.withProperty(BlockBranchBasic.CONNECTIONS[face.getIndex()], face == cutDir.getOpposite() ? 8 : 0);
		}
		float offset = (8 - ((BlockBranch) exState.getBlock()).getRadius(exState)) / 16f;
		IBakedModel model = dispatcher.getModelForState(exState.getClean());
		treeQuads.addAll(QuadManipulator.getQuads(model, exState, new Vec3d(BlockPos.ORIGIN.offset(cutDir)).scale(offset), new EnumFacing[] { cutDir }));
		
		//Draw the rest of the tree/branch
		for(int index = 0; index < destructionData.getNumBranches(); index++) {
			exState = destructionData.getBranchBlockState(index);
			BlockPos relPos = destructionData.getBranchRelPos(index);
			model = dispatcher.getModelForState(exState.getClean());
			treeQuads.addAll(QuadManipulator.getQuads(model, exState, new Vec3d(relPos)));
		}
		
		//Draw the leaves
		IBlockState state = destructionData.species.getLeavesProperties().getDynamicLeavesState();
		for(int index = 0; index < destructionData.getNumLeaves(); index++) {
			BlockPos relPos = destructionData.getLeavesRelPos(index);
			model = dispatcher.getModelForState(state);
			treeQuads.addAll(QuadManipulator.getQuads(model, state, new Vec3d(relPos)));
		}
		
		treeQuads.trimToSize();
		
		return treeQuads;
	}

}
