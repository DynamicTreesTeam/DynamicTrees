package com.ferreusveritas.dynamictrees.models.bakedmodels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.lwjgl.util.vector.Vector3f;

import com.ferreusveritas.dynamictrees.blocks.BlockSurfaceRoot;
import com.ferreusveritas.dynamictrees.client.ModelUtils;
import com.google.common.collect.Maps;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.client.renderer.block.model.BlockPartFace;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BakedModelBlockSurfaceRoot implements IBakedModel {
	
	protected ModelBlock modelBlock;
	
	TextureAtlasSprite barkParticles;
	
	private IBakedModel sleeves[][] = new IBakedModel[4][7];
	private IBakedModel cores[][] = new IBakedModel[2][8]; //8 Cores for 2 axis(X, Z) with the bark texture on all 6 sides rotated appropriately.
	private IBakedModel verts[][] = new IBakedModel[4][8];
	
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
					verts[horIndex][r] = bakeVert(radius, dir, barkIcon);
				}
			}
			cores[0][r] = bakeCore(radius, Axis.Z, barkIcon); //NORTH<->SOUTH
			cores[1][r] = bakeCore(radius, Axis.X, barkIcon); //WEST<->EAST
		}
		
	}
	
	public int getRadialHeight(int radius) {
		return radius * 2;
	}
	
	public IBakedModel bakeSleeve(int radius, EnumFacing dir, TextureAtlasSprite bark) {
		
		int radialHeight = getRadialHeight(radius);
		
		//Work in double units(*2)
		int dradius = radius * 2;
		int halfSize = (16 - dradius) / 2;
		int halfSizeX = dir.getFrontOffsetX() != 0 ? halfSize : dradius;
		int halfSizeZ = dir.getFrontOffsetZ() != 0 ? halfSize : dradius;
		int move = 16 - halfSize;
		int centerX = 16 + (dir.getFrontOffsetX() * move);
		int centerZ = 16 + (dir.getFrontOffsetZ() * move);
	
		Vector3f posFrom = new Vector3f((centerX - halfSizeX) / 2, 0, (centerZ - halfSizeZ) / 2);
		Vector3f posTo = new Vector3f((centerX + halfSizeX) / 2, radialHeight, (centerZ + halfSizeZ) / 2);

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
						uvface = new BlockFaceUV(new float[]{ facePositive ? 16 - radialHeight : 0, (sleeveNegative ? 16 - halfSize: 0), facePositive ? 16 : radialHeight, (sleeveNegative ? 16 : halfSize) }, ModelUtils.getFaceAngle(dir.getAxis(), face));
					} else {
						uvface = new BlockFaceUV(new float[]{ 8 - radius, sleeveNegative ? 16 - halfSize : 0, 8 + radius, sleeveNegative ? 16 : halfSize }, ModelUtils.getFaceAngle(dir.getAxis(), face));
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
			builder.addFaceQuad(face, ModelUtils.makeBakedQuad(part, e.getValue(), bark, face, ModelRotation.X0_Y0, false));
		}
		
		return builder.makeBakedModel();
	}
	
	private IBakedModel bakeVert(int radius, EnumFacing dir, TextureAtlasSprite bark) {
		int radialHeight = getRadialHeight(radius);
		SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(modelBlock, ItemOverrideList.NONE).setTexture(bark);
		
		AxisAlignedBB partBoundary = new AxisAlignedBB(8 - radius, radialHeight, 8 - radius, 8 + radius, 16 + radialHeight, 8 + radius)
			.offset(dir.getFrontOffsetX() * 7, 0, dir.getFrontOffsetZ() * 7);
		
		for(int i = 0; i < 2; i++) {
			AxisAlignedBB pieceBoundary = partBoundary.intersect(new AxisAlignedBB(0, 0, 0, 16, 16, 16).offset(0, 16 * i, 0));
			
			for (EnumFacing face: EnumFacing.VALUES) {
				Map<EnumFacing, BlockPartFace> mapFacesIn = Maps.newEnumMap(EnumFacing.class);
				
				BlockFaceUV uvface = new BlockFaceUV(ModelUtils.modUV(ModelUtils.getUVs(pieceBoundary, face)), ModelUtils.getFaceAngle(Axis.Y, face));
				mapFacesIn.put(face, new BlockPartFace(null, -1, null, uvface));
				
				Vector3f limits[] = ModelUtils.AABBLimits(pieceBoundary);
				
				BlockPart part = new BlockPart(limits[0], limits[1], mapFacesIn, null, true);
				builder.addFaceQuad(face, ModelUtils.makeBakedQuad(part, part.mapFaces.get(face), bark, face, ModelRotation.X0_Y0, false));
			}
		}
		
		return builder.makeBakedModel();
	}
	
	public IBakedModel bakeCore(int radius, Axis axis, TextureAtlasSprite icon) {
		
		int radialHeight = getRadialHeight(radius);
		
		Vector3f posFrom = new Vector3f(8 - radius, 0, 8 - radius);
		Vector3f posTo = new Vector3f(8 + radius, radialHeight, 8 + radius);
		
		Map<EnumFacing, BlockPartFace> mapFacesIn = Maps.newEnumMap(EnumFacing.class);
		
		for(EnumFacing face: EnumFacing.VALUES) {
			BlockFaceUV uvface;
			if(face.getAxis().isHorizontal()) {
				boolean positive = face.getAxisDirection() == AxisDirection.POSITIVE;
				uvface = new BlockFaceUV(new float[]{ positive ? 16 - radialHeight : 0 , 8 - radius, positive ? 16 : radialHeight, 8 + radius}, ModelUtils.getFaceAngle(axis, face));
			} else {
				uvface = new BlockFaceUV(new float[]{ 8 - radius, 8 - radius, 8 + radius, 8 + radius }, ModelUtils.getFaceAngle(axis, face));
			}
			
			mapFacesIn.put(face, new BlockPartFace(null, -1, null, uvface));
		}
		
		BlockPart part = new BlockPart(posFrom, posTo, mapFacesIn, null, true);
		SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(modelBlock, ItemOverrideList.NONE).setTexture(icon);
		
		for(Map.Entry<EnumFacing, BlockPartFace> e : part.mapFaces.entrySet()) {
			EnumFacing face = e.getKey();
			builder.addFaceQuad(face, ModelUtils.makeBakedQuad(part, e.getValue(), icon, face, ModelRotation.X0_Y0, false));
		}
		
		return builder.makeBakedModel();
	}
	
	@Override
	public List<BakedQuad> getQuads(IBlockState blockState, EnumFacing side, long rand) {
		List<BakedQuad> quadsList = new ArrayList<>(24);
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
			
			boolean isGrounded = extendedBlockState.getValue(BlockSurfaceRoot.GROUNDED) == Boolean.TRUE;
			
			//Get quads for core model
			if(isGrounded) {
				quadsList.addAll(cores[coreDir][coreRadius-1].getQuads(blockState, side, rand));
			}
			
			//Get quads for sleeves models
			if(coreRadius != 8) { //Special case for r!=8.. If it's a solid block so it has no sleeves
				for(EnumFacing connDir : EnumFacing.HORIZONTALS) {
					int idx = connDir.getHorizontalIndex();
					int connRadius = connections[idx];
					//If the connection side matches the quadpull side then cull the sleeve face.  Don't cull radius 1 connections for leaves(which are partly transparent).
					if (connRadius > 0) {//  && (connRadius == 1 || side != connDir)) {
						if(isGrounded) {
							quadsList.addAll(sleeves[idx][connRadius-1].getQuads(extendedBlockState, side, rand));	
						}
						if(extendedBlockState.getValue(BlockSurfaceRoot.LEVELS[idx]) == BlockSurfaceRoot.ConnectionLevel.HIGH) {
							quadsList.addAll(verts[idx][connRadius-1].getQuads(extendedBlockState, side, rand));
						}
					}
				}
			}
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
