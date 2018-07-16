package com.ferreusveritas.dynamictrees.render;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 
 * This class hold different animation handlers for EntityFallingTree.
 * The idea is that a unique animation could be used for a certain harvesting circumstance.
 * 
 * @author ferreusveritas
 *
 */
public class AnimationHandlers {
	
	
	public static final AnimationHandler voidAnimationHandler = new AnimationHandler() {
		@Override public boolean shouldDie(EntityFallingTree entity) { return true; }
		@Override public void renderTransform(EntityFallingTree entity, float entityYaw, float partialTicks) { }
		@Override public void initMotion(EntityFallingTree entity) { }
		@Override public void handleMotion(EntityFallingTree entity) { }
		@Override public void dropPayload(EntityFallingTree entity) { EntityFallingTree.standardDropPayload(entity); }
	};
	
	public static final AnimationHandler defaultAnimationHandler = new AnimationHandler() {
		
		@Override
		public void initMotion(EntityFallingTree entity) {			
			entity.motionY = 0.2;
			entity.motionX = 0.1 * (entity.world.rand.nextFloat() - 0.5f);
			entity.motionZ = 0.1 * (entity.world.rand.nextFloat() - 0.5f);
			
			float mass = entity.getDestroyData().woodVolume;
			float inertialMass = MathHelper.clamp(mass / 2048, 1, 3);
			
			entity.motionX /= inertialMass;
			entity.motionY /= inertialMass;
			entity.motionZ /= inertialMass;
			
			entity.motionX += entity.getDestroyData().cutDir.getOpposite().getFrontOffsetX() * 0.1;
			entity.motionY += entity.getDestroyData().cutDir.getOpposite().getFrontOffsetY() * 0.1;
			entity.motionZ += entity.getDestroyData().cutDir.getOpposite().getFrontOffsetZ() * 0.1;
		}
		
		@Override
		public void handleMotion(EntityFallingTree entity) {
			
			//This will function as an inaccurate moment of inertia for the time being
			float mass = entity.getDestroyData().woodVolume;
			float inertialMass = MathHelper.clamp(mass / 2048, 1, 3);
			
			entity.motionY -= 0.02;//Gravity
			//entity.motionY = 0.0;
			entity.posX += entity.motionX;
			entity.posY += entity.motionY;
			entity.posZ += entity.motionZ;
			entity.rotationYaw += 1.25 / inertialMass;
			entity.rotationPitch += 4 / inertialMass;
			
			entity.rotationPitch = MathHelper.wrapDegrees(entity.rotationPitch);
			entity.rotationYaw = MathHelper.wrapDegrees(entity.rotationYaw);
			
			int radius = 8;
			IBlockState state = entity.getDestroyData().getBranchBlockState(0);
			if(TreeHelper.isBranch(state)) {
				radius = ((BlockBranch)state.getBlock()).getRadius(state);
			}
			World world = entity.world;
			AxisAlignedBB fallBox = new AxisAlignedBB(entity.posX - radius, entity.posY, entity.posZ - radius, entity.posX + radius, entity.posY + 1.0, entity.posZ + radius);
			BlockPos pos = new BlockPos(entity.posX, entity.posY, entity.posZ);
			IBlockState collState = world.getBlockState(pos);
			if(!TreeHelper.isLeaves(collState)) {
				AxisAlignedBB collBox = collState.getCollisionBoundingBox(world, pos);
			
				if(collBox != null) {
					collBox = collBox.offset(pos);
					if(fallBox.intersects(collBox)) {
						entity.motionY = 0;
						entity.posY = collBox.maxY;
						entity.prevPosY = entity.posY;
						entity.landed = true;
						entity.onGround = true;
					}
				}
			}
			
		}
		
		@Override
		public void dropPayload(EntityFallingTree entity) {
			World world = entity.world;
			entity.getPayload().forEach(i -> Block.spawnAsEntity(world, new BlockPos(entity.posX, entity.posY, entity.posZ), i));
			entity.getDestroyData().leavesDrops.forEach(bis -> Block.spawnAsEntity(world, entity.getDestroyData().cutPos.add(bis.pos), bis.stack));
		}
		
		public boolean shouldDie(EntityFallingTree entity) {
			return entity.landed || entity.ticksExisted > 60;
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public void renderTransform(EntityFallingTree entity, float entityYaw, float partialTicks) {
			
			float yaw = MathHelper.wrapDegrees(com.ferreusveritas.dynamictrees.util.MathHelper.angleDegreesInterpolate(entity.prevRotationYaw, entity.rotationYaw, partialTicks));
			float pit = MathHelper.wrapDegrees(com.ferreusveritas.dynamictrees.util.MathHelper.angleDegreesInterpolate(entity.prevRotationPitch, entity.rotationPitch, partialTicks));			
			
			Vec3d mc = entity.getMassCenter();
			GlStateManager.translate(mc.x, mc.y, mc.z);
			GlStateManager.rotate(-yaw, 0, 1, 0);
			GlStateManager.rotate(pit, 1, 0, 0);
			GlStateManager.translate(-mc.x - 0.5, -mc.y, -mc.z - 0.5);
		}
	};
	
	public static final AnimationHandler demoAnimationHandler = new AnimationHandler() {
		
		@Override
		public void initMotion(EntityFallingTree entity) { }
		
		@Override
		public void handleMotion(EntityFallingTree entity) {
			entity.rotationYaw += 6;
			entity.rotationPitch += 2;
			
			entity.rotationPitch = MathHelper.wrapDegrees(entity.rotationPitch);
			entity.rotationYaw = MathHelper.wrapDegrees(entity.rotationYaw);
		}
		
		@Override
		public void dropPayload(EntityFallingTree entity) { }
		
		@Override
		public boolean shouldDie(EntityFallingTree entity) {
			return false;
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public void renderTransform(EntityFallingTree entity, float entityYaw, float partialTicks) {
			
			float yaw = MathHelper.wrapDegrees(com.ferreusveritas.dynamictrees.util.MathHelper.angleDegreesInterpolate(entity.prevRotationYaw, entity.rotationYaw, partialTicks));
			float pit = MathHelper.wrapDegrees(com.ferreusveritas.dynamictrees.util.MathHelper.angleDegreesInterpolate(entity.prevRotationPitch, entity.rotationPitch, partialTicks));			
			
			Vec3d mc = entity.getMassCenter();
			GlStateManager.translate(mc.x, mc.y, mc.z);
			GlStateManager.rotate(-yaw, 0, 1, 0);
			GlStateManager.rotate(pit, 1, 0, 0);
			GlStateManager.translate(-mc.x - 0.5, -mc.y, -mc.z - 0.5);			
		}
		
	};
	
	public static final AnimationHandler falloverAnimationHandler = new AnimationHandler() {
		
		class HandlerData extends AnimationHandlerData {
			float fallSpeed = 0;
		}
		
		HandlerData getData(EntityFallingTree entity) {
			return entity.animationHandlerData instanceof HandlerData ? (HandlerData) entity.animationHandlerData : new HandlerData();
		}
		
		@Override
		public void initMotion(EntityFallingTree entity) {
			entity.animationHandlerData = new HandlerData();
			
			BlockPos belowBlock = entity.getDestroyData().cutPos.down();
			if(entity.world.getBlockState(belowBlock).isSideSolid(entity.world, belowBlock, EnumFacing.UP)) {
				entity.onGround = true;
				return;
			}
		}
		
		@Override
		public void handleMotion(EntityFallingTree entity) {
			
			float fallSpeed = getData(entity).fallSpeed;
			
			if(entity.onGround) {
				float height = (float) entity.getMassCenter().y * 2;
				fallSpeed += (0.2 / height);
				addRotation(entity, fallSpeed);
			}				
			
			entity.motionY -= 0.03f;
			entity.posY += entity.motionY;
			
			{//Handle entire entity falling and collisions with it's base and the ground
				World world = entity.world;
				int radius = 8;
				IBlockState state = entity.getDestroyData().getBranchBlockState(0);
				if(TreeHelper.isBranch(state)) {
					radius = ((BlockBranch)state.getBlock()).getRadius(state);
				}
				AxisAlignedBB fallBox = new AxisAlignedBB(entity.posX - radius, entity.posY, entity.posZ - radius, entity.posX + radius, entity.posY + 1.0, entity.posZ + radius);
				BlockPos pos = new BlockPos(entity.posX, entity.posY, entity.posZ);
				IBlockState collState = world.getBlockState(pos);
				AxisAlignedBB collBox = collState.getCollisionBoundingBox(world, pos);
				
				if(collBox != null) {
					collBox = collBox.offset(pos);
					if(fallBox.intersects(collBox)) {
						entity.motionY = 0;
						entity.posY = collBox.maxY;
						entity.prevPosY = entity.posY;
						entity.onGround = true;
					}
				}
			}
			
			if(fallSpeed > 0 && testCollision(entity)) {
				addRotation(entity, -fallSpeed);//pull back to before the collision
				fallSpeed *= -0.25f;//bounce with elasticity
				entity.landed = Math.abs(fallSpeed) < 0.01f;//The entity has landed if after a bounce it has little velocity
			}
			
			//TODO: Add collision detection for living entities and produce damage from it
			
			getData(entity).fallSpeed = fallSpeed;
		}
		
		/**
		 * This tests a bounding box cube for each block of the trunk.
		 * Processing is approximately equivalent to the same number of {@link EntityItem}s in the world. 
		 * 
		 * @param entity
		 * @return true if collision is detected
		 */
		private boolean testCollision(EntityFallingTree entity) {
			EnumFacing toolDir = entity.getDestroyData().toolDir;
			
			float actingAngle = toolDir.getAxis() == EnumFacing.Axis.X ? entity.rotationYaw : entity.rotationPitch;

			int offsetX = toolDir.getFrontOffsetX();
			int offsetZ = toolDir.getFrontOffsetZ();
			float h = MathHelper.sin((float) Math.toRadians(actingAngle)) * (offsetX | offsetZ);
			float v = MathHelper.cos((float) Math.toRadians(actingAngle));
			float xbase = (float) (entity.posX + offsetX * ( - (0.5f) + (v * 0.5f) + (h * 0.5f) ) );
			float ybase = (float) (entity.posY - (h * 0.5f) + (v * 0.5f));
			float zbase = (float) (entity.posZ + offsetZ * ( - (0.5f) + (v * 0.5f) + (h * 0.5f) ) );
			
			int trunkHeight = entity.getDestroyData().trunkHeight;
			float maxRadius = entity.getDestroyData().getBranchRadius(0) / 16.0f;
			
			for(int segment = 0; segment < trunkHeight; segment++) {
				float segX = xbase + h * segment * offsetX;
				float segY = ybase + v * segment;
				float segZ = zbase + h * segment * offsetZ;
				float tex = 0.0625f;
				float half = MathHelper.clamp(tex * (segment + 1) * 2, tex, maxRadius);
				AxisAlignedBB testBB = new AxisAlignedBB(segX - half, segY - half, segZ - half, segX + half, segY + half, segZ + half);
				
				if(!entity.world.getCollisionBoxes(entity, testBB).isEmpty()) {
					return true;
				}
			}
			
			return false;
		}
		
		private void addRotation(EntityFallingTree entity, float delta) {
			EnumFacing toolDir = entity.getDestroyData().toolDir;
			
			switch(toolDir) {
				case NORTH: entity.rotationPitch += delta; break;
				case SOUTH: entity.rotationPitch -= delta; break;
				case WEST: entity.rotationYaw += delta; break;
				case EAST: entity.rotationYaw -= delta; break;
				default: break;
			}
			
			entity.rotationPitch = MathHelper.wrapDegrees(entity.rotationPitch);
			entity.rotationYaw = MathHelper.wrapDegrees(entity.rotationYaw);
		}
		
		@Override
		public void dropPayload(EntityFallingTree entity) {
			EntityFallingTree.standardDropPayload(entity);
		}
		
		@Override
		public boolean shouldDie(EntityFallingTree entity) {
			return Math.abs(entity.rotationPitch) >= 160 || 
					Math.abs(entity.rotationYaw) >= 160 ||
					entity.landed ||
					entity.ticksExisted > 150 * 10;
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public void renderTransform(EntityFallingTree entity, float entityYaw, float partialTicks) {
			
			float yaw = MathHelper.wrapDegrees(com.ferreusveritas.dynamictrees.util.MathHelper.angleDegreesInterpolate(entity.prevRotationYaw, entity.rotationYaw, partialTicks));
			float pit = MathHelper.wrapDegrees(com.ferreusveritas.dynamictrees.util.MathHelper.angleDegreesInterpolate(entity.prevRotationPitch, entity.rotationPitch, partialTicks));			
			
			//Vec3d mc = entity.getMassCenter();
			
			int radius = entity.getDestroyData().getBranchRadius(0);
			
			EnumFacing toolDir = entity.getDestroyData().toolDir;
			Vec3d toolVec = new Vec3d(toolDir.getFrontOffsetX(), toolDir.getFrontOffsetY(), toolDir.getFrontOffsetZ()).scale(radius / 16.0f);
			
			GlStateManager.translate(-toolVec.x, -toolVec.y, -toolVec.z);
			GlStateManager.rotate(-yaw, 0, 0, 1);
			GlStateManager.rotate(pit, 1, 0, 0);
			GlStateManager.translate(toolVec.x, toolVec.y, toolVec.z);
			
			GlStateManager.translate(-0.5, 0, -0.5);
		}
		
	};
}
