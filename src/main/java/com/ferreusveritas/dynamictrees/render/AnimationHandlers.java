package com.ferreusveritas.dynamictrees.render;

import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 
 * This class hold different animation handlers for EntityFallingTree.
 * The idea is that a unique animation could be used for a certain circumstance.
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
			entity.motionY = 0.4;
			entity.motionX = 0.2 * (entity.world.rand.nextFloat() - 0.5f);
			entity.motionZ = 0.2 * (entity.world.rand.nextFloat() - 0.5f);
			
			float mass = entity.getDestroyData().woodVolume;
			float inertia = (512 / mass);
			
			entity.motionX *= inertia;
			entity.motionY *= inertia;
			entity.motionZ *= inertia;
			
			entity.motionX = MathHelper.clamp(entity.motionX, 0.0f, 0.6f);
			entity.motionY = MathHelper.clamp(entity.motionY, 0.2f, 0.6f);
			entity.motionZ = MathHelper.clamp(entity.motionZ, 0.0f, 0.6f);
		}
		
		@Override
		public void handleMotion(EntityFallingTree entity) {
			entity.motionY -= 0.02;//Gravity
			//entity.motionY = 0.0;
			entity.posX += entity.motionX;
			entity.posY += entity.motionY;
			entity.posZ += entity.motionZ;
			entity.rotationYaw += 1.25;
			entity.rotationPitch += 4;
			
			entity.rotationPitch = MathHelper.wrapDegrees(entity.rotationPitch);
			entity.rotationYaw = MathHelper.wrapDegrees(entity.rotationYaw);
			
		}
		
		@Override
		public void dropPayload(EntityFallingTree entity) {
			EntityFallingTree.standardDropPayload(entity);
		}
		
		public boolean shouldDie(EntityFallingTree entity) {
			return entity.ticksExisted > 25;
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
		
		@Override
		public void initMotion(EntityFallingTree entity) { }
		
		@Override
		public void handleMotion(EntityFallingTree entity) {
			
			BranchDestructionData destroyData = entity.getDestroyData();
			EnumFacing toolDir = destroyData.toolDir;
			
			float height = (float) entity.getMassCenter().y;
			float fallSpeed = height >= 1.5f ? entity.ticksExisted / (8.0f * height) : 4.0f;
			
			switch(toolDir) {
				case NORTH: entity.rotationPitch += fallSpeed; break;
				case SOUTH: entity.rotationPitch -= fallSpeed; break;
				case WEST: entity.rotationYaw += fallSpeed; break;
				case EAST: entity.rotationYaw -= fallSpeed; break;
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
			return Math.abs(entity.rotationPitch) >= 90 || 
					Math.abs(entity.rotationYaw) >= 90 || 
					entity.ticksExisted > 120;
		}
		
		@Override
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
