package com.ferreusveritas.dynamictrees.models.bakedmodels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.BlockBranchBasic;
import com.ferreusveritas.dynamictrees.client.ModelUtils;
import com.google.common.collect.Maps;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.BlockFaceUV;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.BlockPart;
import net.minecraft.client.renderer.model.BlockPartFace;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelRotation;
import net.minecraft.client.renderer.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

@OnlyIn(Dist.CLIENT)
public class BakedModelBlockBranchBasic implements IDynamicBakedModel {
	
	protected BlockModel modelBlock;
	
	TextureAtlasSprite barkParticles;
	
	//74 Baked models per tree family to achieve this. I guess it's not my problem.  Wasn't my idea anyway.
	private IBakedModel[][] sleeves = new IBakedModel[6][7];
	private IBakedModel[][] cores = new IBakedModel[3][8]; //8 Cores for 3 axis with the bark texture all all 6 sides rotated appropriately.
	private IBakedModel[] rings = new IBakedModel[8]; //8 Cores with the ring textures on all 6 sides
	
	public BakedModelBlockBranchBasic(ResourceLocation barkRes, ResourceLocation ringsRes) {
		this.modelBlock = new BlockModel(null, null, null, false, false, ItemCameraTransforms.DEFAULT, null);
		
		TextureAtlasSprite barkIcon = Minecraft.getInstance().getTextureMap().getSprite(barkRes);
		TextureAtlasSprite ringIcon = Minecraft.getInstance().getTextureMap().getSprite(ringsRes);
		barkParticles = barkIcon;
		
		for(int i = 0; i < 8; i++) {
			int radius = i + 1;
			if(radius < 8) {
				for(Direction dir: Direction.values()) {
					sleeves[dir.getIndex()][i] = bakeSleeve(radius, dir, barkIcon);
				}
			}
			cores[0][i] = bakeCore(radius, Axis.Y, barkIcon); //DOWN<->UP
			cores[1][i] = bakeCore(radius, Axis.Z, barkIcon); //NORTH<->SOUTH
			cores[2][i] = bakeCore(radius, Axis.X, barkIcon); //WEST<->EAST
			
			rings[i] = bakeCore(radius, Axis.Y, ringIcon);
		}
		
	}
	
	public IBakedModel bakeSleeve(int radius, Direction dir, TextureAtlasSprite bark) {
		//Work in double units(*2)
		int dradius = radius * 2;
		int halfSize = (16 - dradius) / 2;
		int halfSizeX = dir.getXOffset() != 0 ? halfSize : dradius;
		int halfSizeY = dir.getYOffset() != 0 ? halfSize : dradius;
		int halfSizeZ = dir.getZOffset() != 0 ? halfSize : dradius;
		int move = 16 - halfSize;
		int centerX = 16 + (dir.getXOffset() * move);
		int centerY = 16 + (dir.getYOffset() * move);
		int centerZ = 16 + (dir.getZOffset() * move);
		
		Vector3f posFrom = new Vector3f((centerX - halfSizeX) / 2f, (centerY - halfSizeY) / 2f, (centerZ - halfSizeZ) / 2f);
		Vector3f posTo = new Vector3f((centerX + halfSizeX) / 2f, (centerY + halfSizeY) / 2f, (centerZ + halfSizeZ) / 2f);
		
		boolean negative = dir.getAxisDirection() == AxisDirection.NEGATIVE;
		if(dir.getAxis() == Axis.Z) {//North/South
			negative = !negative;
		}
		
		Map<Direction, BlockPartFace> mapFacesIn = Maps.newEnumMap(Direction.class);
		
		for(Direction face: Direction.values()) {
			if(dir.getOpposite() != face) { //Discard side of sleeve that faces core
				BlockFaceUV uvface = null;
				if(dir == face) {//Side of sleeve that faces away from core
					if(radius == 1) { //We're only interested in end faces for radius == 1
						uvface = new BlockFaceUV(new float[] {8 - radius, 8 - radius, 8 + radius, 8 + radius}, 0);
					}
				} else { //UV for Bark texture
					uvface = new BlockFaceUV(new float[]{ 8 - radius, negative ? 16 - halfSize : 0, 8 + radius, negative ? 16 : halfSize }, getFaceAngle(dir.getAxis(), face));
				}
				if(uvface != null) {
					mapFacesIn.put(face, new BlockPartFace(null, -1, null, uvface));
				}
			}
		}
		
		BlockPart part = new BlockPart(posFrom, posTo, mapFacesIn, null, true);
		SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(modelBlock, ItemOverrideList.EMPTY).setTexture(bark);
		
		for(Map.Entry<Direction, BlockPartFace> e : part.mapFaces.entrySet()) {
			Direction face = e.getKey();
			builder.addFaceQuad(face, ModelUtils.makeBakedQuad(part, e.getValue(), bark, face, ModelRotation.X0_Y0, false));
		}
		
		return builder.build();
	}
	
	public IBakedModel bakeCore(int radius, Axis axis, TextureAtlasSprite icon) {
		
		Vector3f posFrom = new Vector3f(8 - radius, 8 - radius, 8 - radius);
		Vector3f posTo = new Vector3f(8 + radius, 8 + radius, 8 + radius);
		
		Map<Direction, BlockPartFace> mapFacesIn = Maps.newEnumMap(Direction.class);
		
		for(Direction face: Direction.values()) {
			BlockFaceUV uvface = new BlockFaceUV(new float[]{ 8 - radius, 8 - radius, 8 + radius, 8 + radius }, getFaceAngle(axis, face));
			mapFacesIn.put(face, new BlockPartFace(null, -1, null, uvface));
		}
		
		BlockPart part = new BlockPart(posFrom, posTo, mapFacesIn, null, true);
		SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(modelBlock, ItemOverrideList.EMPTY).setTexture(icon);
		
		for(Map.Entry<Direction, BlockPartFace> e : part.mapFaces.entrySet()) {
			Direction face = e.getKey();
			builder.addFaceQuad(face, ModelUtils.makeBakedQuad(part, e.getValue(), icon, face, ModelRotation.X0_Y0, false));
		}
		
		return builder.build();
	}
	
	/**
	 * A Hack to determine the UV face angle for a block column on a certain axis
	 *
	 * @param axis
	 * @param face
	 * @return
	 */
	public int getFaceAngle (Axis axis, Direction face) {
		if(axis == Axis.Y) { //UP / DOWN
			return 0;
		}
		else if(axis == Axis.Z) {//NORTH / SOUTH
			switch(face) {
				case UP: return 0;
				case WEST: return 270;
				case DOWN: return 180;
				default: return 90;
			}
		} else { //EAST/WEST
			return (face == Direction.NORTH) ? 270 : 90;
		}
	}
	
	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
		
		if (side == null && state != null) {
			List<BakedQuad> quadsList = new ArrayList<>(24);
			
			int coreRadius = getRadius(state);
			
			int[] connections = new int[] {0,0,0,0,0,0};
			if (extraData instanceof Connections){
				connections = ((Connections) extraData).getAllRadii();
			}
			
			//Count number of connections
			int numConnections = 0;
			for(int i: connections) {
				numConnections += (i != 0) ? 1: 0;
			}
			
			//The source direction is the biggest connection from one of the 6 directions
			Direction sourceDir = getSourceDir(coreRadius, connections);
			if(sourceDir == null) {
				sourceDir = Direction.DOWN;
			}
			int coreDir = resolveCoreDir(sourceDir);
			
			//This is for drawing the rings on a terminating branch
			Direction coreRingDir = (numConnections == 1) ? sourceDir.getOpposite() : null;
			
			for(Direction face  : Direction.values()) {
				
				//Get quads for core model
				if(coreRadius != connections[face.getIndex()]) {
					if(coreRingDir == null || coreRingDir != face) {
						quadsList.addAll(cores[coreDir][coreRadius-1].getQuads(state, face, rand, extraData));
					} else {
						quadsList.addAll(rings[coreRadius-1].getQuads(state, face, rand, extraData));
					}
				}
				//Get quads for sleeves models
				if(coreRadius != 8) { //Special case for r!=8.. If it's a solid block so it has no sleeves
					for(Direction connDir : Direction.values()) {
						int idx = connDir.getIndex();
						int connRadius = connections[idx];
						//If the connection side matches the quadpull side then cull the sleeve face.  Don't cull radius 1 connections for leaves(which are partly transparent).
						if (connRadius > 0  && (connRadius == 1 || face != connDir)) {
							quadsList.addAll(sleeves[idx][connRadius-1].getQuads((BlockState)state, face, rand, extraData));
						}
					}
				}
				
			}
			
			return quadsList;
		}
		
		return Collections.emptyList();
	}
	
	private static class Connections implements IModelData{
		
		private int[] radii;
		//		ModelProperty<Integer>[] radii = new ModelProperty<Integer>[6];
		
		public Connections (){
			radii = new int[] {0,0,0,0,0,0};
		}
		
		public void setRadius (Direction dir, int radius){
			radii[dir.getIndex()] = radius;
		}
		
		public int[] getAllRadii (){
			return radii;
		}
		
		@Override
		public boolean hasProperty(ModelProperty<?> prop) {
			return false;
		}
		
		@Nullable
		@Override
		public <T> T getData(ModelProperty<T> prop) {
			return null;
		}
		
		@Nullable
		@Override
		public <T> T setData(ModelProperty<T> prop, T data) {
			return null;
		}
	}
	
	/**
	 * Checks all neighboring tree parts to determine the connection radius for each side of this branch block.
	 *
	 */
	@Nonnull
	@Override
	public IModelData getModelData(@Nonnull IEnviromentBlockReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData) {
		
		Connections connections = new Connections();
		
		if(state.getBlock() instanceof BlockBranchBasic) {
			BlockBranchBasic thisBranch = (BlockBranchBasic) state.getBlock();
			int coreRadius = thisBranch.getRadius(state);
			for(Direction dir: Direction.values()) {
				BlockPos deltaPos = pos.offset(dir);
				BlockState neighborBlockState = world.getBlockState(deltaPos);
				int sideRadius = TreeHelper.getTreePart(neighborBlockState).getRadiusForConnection(neighborBlockState, world, deltaPos, thisBranch, dir, coreRadius);
				connections.setRadius(dir, MathHelper.clamp(sideRadius, 0, coreRadius));
			}
		}
		
		return connections;
	}
	
	/**
	 * Locates the side with the largest neighbor radius that's equal to or greater than this branch block
	 *
	 * @param coreRadius
	 * @param connections an array of 6 integers, one for the radius of each connecting side. DUNSWE.
	 * @return
	 */
	protected Direction getSourceDir(int coreRadius, int[] connections) {
		int largestConnection = 0;
		Direction sourceDir = null;
		
		for(Direction dir: Direction.values()){
			int connRadius = connections[dir.getIndex()];
			if(connRadius > largestConnection){
				largestConnection = connRadius;
				sourceDir = dir;
			}
		}
		
		if(largestConnection < coreRadius){
			sourceDir = null;//Has no source node
		}
		return sourceDir;
	}
	
	/**
	 * Converts direction DUNSWE to 3 axis numbers for Y,Z,X
	 *
	 * @param dir
	 * @return
	 */
	protected int resolveCoreDir(Direction dir) {
		return dir.getIndex() >> 1;
	}
	
	protected int getRadius(BlockState blockState) {
		// This way works with branches that don't have the RADIUS property, like cactus
		return ((BlockBranchBasic) blockState.getBlock()).getRadius(blockState);
	}
	
	@Override
	public boolean isAmbientOcclusion() {
		return true;
	}
	
	@Override
	public TextureAtlasSprite getParticleTexture(@Nonnull IModelData data) {
		return getParticleTexture();
	}
	@Override
	public TextureAtlasSprite getParticleTexture() {
		return barkParticles;
	}
	
	@Override
	public boolean isGui3d() {
		return false;
	}
	
	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}
	
	@Nonnull
	@Override
	public ItemOverrideList getOverrides() {
		return ItemOverrideList.EMPTY;
	}
	
	@Override
	public boolean doesHandlePerspectives() {
		return false;
	}
	
}
