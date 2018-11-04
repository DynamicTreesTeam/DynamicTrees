package com.ferreusveritas.dynamictrees.models;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.lwjgl.util.vector.Vector3f;

import com.ferreusveritas.dynamictrees.blocks.BlockSurfaceRoot;
import com.google.common.collect.Maps;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BakedModelBlockSurfaceRoot implements IBakedModel {
	
	protected ModelBlock modelBlock;
	
	TextureAtlasSprite barkParticles;
	
	private IBakedModel sleeves[][] = new IBakedModel[4][7];
	private IBakedModel cores[][] = new IBakedModel[2][8]; //8 Cores for 2 axis(X, Z) with the bark texture on all 6 sides rotated appropriately.
	
	public BakedModelBlockSurfaceRoot(ResourceLocation barkRes, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {		
		this.modelBlock = new ModelBlock(null, null, null, false, false, ItemCameraTransforms.DEFAULT, null);
		
		TextureAtlasSprite barkIcon = bakedTextureGetter.apply(barkRes);
		barkParticles = barkIcon;
		
		for(int r = 0; r < 8; r++) {
			int radius = r + 1;
			if(radius < 8) {
				for(EnumFacing dir: EnumFacing.HORIZONTALS) {
					int horIndex = dir.getHorizontalIndex();
					sleeves[horIndex][r] = bakeSleeve(radius, dir, barkIcon);
				}
			}
			cores[0][r] = bakeCore(radius, Axis.Z, barkIcon); //NORTH<->SOUTH
			cores[1][r] = bakeCore(radius, Axis.X, barkIcon); //WEST<->EAST
		}

	}

	public IBakedModel bakeSleeve(int radius, EnumFacing dir, TextureAtlasSprite bark) {		
		//Work in double units(*2)
		int dradius = radius * 2;
		int halfSize = (16 - dradius) / 2;
		int halfSizeX = dir.getFrontOffsetX() != 0 ? halfSize : dradius;
		int halfSizeZ = dir.getFrontOffsetZ() != 0 ? halfSize : dradius;
		int move = 16 - halfSize;
		int centerX = 16 + (dir.getFrontOffsetX() * move);
		int centerZ = 16 + (dir.getFrontOffsetZ() * move);
	
		Vector3f posFrom = new Vector3f((centerX - halfSizeX) / 2, 0, (centerZ - halfSizeZ) / 2);
		Vector3f posTo = new Vector3f((centerX + halfSizeX) / 2, radius + 1, (centerZ + halfSizeZ) / 2);

		boolean sleeveNegative = dir.getAxisDirection() == AxisDirection.NEGATIVE;
		if(dir.getAxis() == Axis.Z) {// North/South
			sleeveNegative = !sleeveNegative;
		}
		
		Map<EnumFacing, BlockPartFace> mapFacesIn = Maps.newEnumMap(EnumFacing.class);
		
		for(EnumFacing face: EnumFacing.VALUES) {
			if(dir.getOpposite() != face) { //Discard side of sleeve that faces core
				BlockFaceUV uvface = null;
					if(face.getAxis().isHorizontal()) {
						boolean facePositive = face.getAxisDirection() == AxisDirection.POSITIVE;
						uvface = new BlockFaceUV(new float[]{ facePositive ? 16 - (radius + 1) : 0, (sleeveNegative ? 16 - halfSize: 0), facePositive ? 16 : radius + 1, (sleeveNegative ? 16 : halfSize) }, getFaceAngle(dir.getAxis(), face));
					} else {
						uvface = new BlockFaceUV(new float[]{ 8 - radius, sleeveNegative ? 16 - halfSize : 0, 8 + radius, sleeveNegative ? 16 : halfSize }, getFaceAngle(dir.getAxis(), face));
					}
				if(uvface != null) {
					mapFacesIn.put(face, new BlockPartFace(null, -1, null, uvface));
				}
			}
		}
		
		BlockPart part = new BlockPart(posFrom, posTo, mapFacesIn, null, true);
		SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(modelBlock, ItemOverrideList.NONE).setTexture(bark);
		
		for(Map.Entry<EnumFacing, BlockPartFace> e : part.mapFaces.entrySet()) {
			EnumFacing face = e.getKey();
			builder.addFaceQuad(face, makeBakedQuad(part, e.getValue(), bark, face, ModelRotation.X0_Y0, false));
		}
		
		return builder.makeBakedModel();
	}
	
	public IBakedModel bakeCore(int radius, Axis axis, TextureAtlasSprite icon) {
		
		Vector3f posFrom = new Vector3f(8 - radius, 0, 8 - radius);
		Vector3f posTo = new Vector3f(8 + radius, radius + 1, 8 + radius);
		
		Map<EnumFacing, BlockPartFace> mapFacesIn = Maps.newEnumMap(EnumFacing.class);
		
		for(EnumFacing face: EnumFacing.VALUES) {
			BlockFaceUV uvface;
			if(face.getAxis().isHorizontal()) {
				boolean positive = face.getAxisDirection() == AxisDirection.POSITIVE;
				uvface = new BlockFaceUV(new float[]{ positive ? 16 - radius - 1 : 0 , 8 - radius, positive ? 16 : radius + 1, 8 + radius}, getFaceAngle(axis, face));
			} else {
				uvface = new BlockFaceUV(new float[]{ 8 - radius, 8 - radius, 8 + radius, 8 + radius }, getFaceAngle(axis, face));
			}
			
			mapFacesIn.put(face, new BlockPartFace(null, -1, null, uvface));
		}
		
		BlockPart part = new BlockPart(posFrom, posTo, mapFacesIn, null, true);
		SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(modelBlock, ItemOverrideList.NONE).setTexture(icon);
		
		for(Map.Entry<EnumFacing, BlockPartFace> e : part.mapFaces.entrySet()) {
			EnumFacing face = e.getKey();
			builder.addFaceQuad(face, makeBakedQuad(part, e.getValue(), icon, face, ModelRotation.X0_Y0, false));
		}

		return builder.makeBakedModel();
	}

	/**
	 * A Hack to determine the UV face angle for a block column on a certain axis
	 * 
	 * @param axis
	 * @param face
	 * @return
	 */
	public int getFaceAngle (Axis axis, EnumFacing face) {
		if(axis == Axis.Y) { //UP / DOWN
			return 0;
		}
		else if(axis == Axis.Z) {//NORTH / SOUTH
			switch(face) {
				case UP: return 0;
				case WEST: return 270;
				case DOWN: return 180;
				case NORTH: return 270;
				default: return 90;
			}
		} else { //EAST/WEST
			return (face == EnumFacing.NORTH) ? 270 : 90;
		}
	}
	
	protected BakedQuad makeBakedQuad(BlockPart blockPart, BlockPartFace partFace, TextureAtlasSprite atlasSprite, EnumFacing dir, net.minecraftforge.common.model.ITransformation transform, boolean uvlocked) {
		return new FaceBakery().makeBakedQuad(blockPart.positionFrom, blockPart.positionTo, partFace, atlasSprite, dir, transform, blockPart.partRotation, uvlocked, blockPart.shade);
	}
	
	@Override
	public List<BakedQuad> getQuads(IBlockState blockState, EnumFacing side, long rand) {
		List<BakedQuad> quadsList = new LinkedList<BakedQuad>();
		IExtendedBlockState extendedBlockState = (IExtendedBlockState)blockState;
		if (blockState instanceof IExtendedBlockState) {
			int coreRadius = getRadius(blockState);
			int[] connections = pollConnections(coreRadius, extendedBlockState);
			
			//The source direction is the biggest connection from one of the 6 directions
			EnumFacing sourceDir = getSourceDir(coreRadius, connections);
			if(sourceDir == null) {
				sourceDir = EnumFacing.DOWN;
			}
			int coreDir = resolveCoreDir(sourceDir);
			
			//Get quads for core model
			quadsList.addAll(cores[coreDir][coreRadius-1].getQuads(blockState, side, rand));
			
			//Get quads for sleeves models
			if(coreRadius != 8) { //Special case for r!=8.. If it's a solid block so it has no sleeves
				for(EnumFacing connDir : EnumFacing.HORIZONTALS) {
					int idx = connDir.getHorizontalIndex();
					int connRadius = connections[idx];
					//If the connection side matches the quadpull side then cull the sleeve face.  Don't cull radius 1 connections for leaves(which are partly transparent).
					if (connRadius > 0) {//  && (connRadius == 1 || side != connDir)) {
						quadsList.addAll(sleeves[idx][connRadius-1].getQuads(extendedBlockState, side, rand));
					}
				}
			}
		} else {
			//Not extended block state
		}
		
		return quadsList;
	}

	/**
	 * Checks all neighboring tree parts to determine the connection radius for each side of this branch block.
	 * 
	 * @param coreRadius the radius of this block
	 * @param extendedBlockState
	 * @return an array of 6 integers, one for the radius of each connecting side. DUNSWE.
	 */
	protected int[] pollConnections(int coreRadius, IExtendedBlockState extendedBlockState) {
		int[] connections = new int[4];
		for(EnumFacing dir: EnumFacing.HORIZONTALS) {
			int horIndex = dir.getHorizontalIndex();
			int connection = getConnectionRadius(extendedBlockState, BlockSurfaceRoot.CONNECTIONS[horIndex]);
			connections[horIndex] = MathHelper.clamp(connection, 0, coreRadius);//Do not allow connections to exceed core radius
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
	protected EnumFacing getSourceDir(int coreRadius, int[] connections) {
		int largestConnection = 0;
		EnumFacing sourceDir = null;
		
		for(EnumFacing dir: EnumFacing.HORIZONTALS){
			int horIndex = dir.getHorizontalIndex();
			int connRadius = connections[horIndex];
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
	protected int resolveCoreDir(EnumFacing dir) {
		return dir.getAxis() == EnumFacing.Axis.X ? 1 : 0; 
	}
	
	protected int getRadius(IBlockState blockState) {
		// This way works with branches that don't have the RADIUS property, like cactus
		return ((BlockSurfaceRoot) blockState.getBlock()).getRadius(blockState);
	}
	
	/**
	 * Get the connection radius of a direction from the ExtendedBlockState
	 * 
	 * @param iExtendedBlockState
	 * @param whichConnection
	 * @return
	 */
	protected int getConnectionRadius(IExtendedBlockState iExtendedBlockState, IUnlistedProperty<Integer> whichConnection) {
		Integer connection = iExtendedBlockState.getValue(whichConnection);
		return connection != null ? connection.intValue() : 0;
	}
	
	@Override
	public boolean isAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isGui3d() {
		return false;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return true;
	}

	// used for block breaking shards
	@Override
	public TextureAtlasSprite getParticleTexture() {
		return barkParticles;
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return sleeves[0][0].getItemCameraTransforms();
	}

	@Override
	public ItemOverrideList getOverrides() {
		return null;
	}

}
