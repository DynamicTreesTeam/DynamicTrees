package com.ferreusveritas.dynamictrees.entities.animation;

import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AnimationHandlerDemo implements IAnimationHandler {
	@Override public String getName() { return "demo"; };

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
	
}
