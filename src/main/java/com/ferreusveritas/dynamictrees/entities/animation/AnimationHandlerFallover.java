package com.ferreusveritas.dynamictrees.entities.animation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nullable;

import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AnimationHandlerFallover implements IAnimationHandler {
	@Override public String getName() { return "fallover"; };
	
	class HandlerData extends AnimationHandlerData {
		float fallSpeed = 0;
		int bounces = 0;
		HashSet<EntityLiving> entitiesHit = new HashSet<>();//A record of the entities that have taken damage to ensure they are only damaged a single time
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
		
		entity.motionY -= AnimationConstants.TREE_GRAVITY;
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
			getData(entity).bounces++;
			fallSpeed *= -AnimationConstants.TREE_ELASTICITY;//bounce with elasticity
			entity.landed = Math.abs(fallSpeed) < 0.01f;//The entity has landed if after a bounce it has little velocity
		}
		
		//Crush living things with clumsy dead trees
		World world = entity.world;
		if(ModConfigs.enableFallingTreeDamage && !world.isRemote) {
			List<EntityLiving> elist = testEntityCollision(entity);
			for(EntityLiving living: elist) {
				if(!getData(entity).entitiesHit.contains(living)) {
					getData(entity).entitiesHit.add(living);
					float damage = entity.getDestroyData().woodVolume * Math.abs(fallSpeed) * 0.001f;
					if(getData(entity).bounces == 0 && damage > 2) {
						//System.out.println("damage: " + damage);
						living.motionX += world.rand.nextFloat() * entity.getDestroyData().toolDir.getOpposite().getFrontOffsetX() * damage * 0.2f;
						living.motionX += world.rand.nextFloat() - 0.5;
						living.motionY += world.rand.nextFloat() * fallSpeed * 0.25f;
						living.motionZ += world.rand.nextFloat() * entity.getDestroyData().toolDir.getOpposite().getFrontOffsetZ() * damage * 0.2f;
						living.motionZ += world.rand.nextFloat() - 0.5;
						living.attackEntityFrom(AnimationConstants.TREE_DAMAGE, damage);
					}
				}
			}
		}
		
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
	
	public List<EntityLiving> testEntityCollision(EntityFallingTree entity) {
		
		World world = entity.world;
		
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
		float segX = xbase + h * (trunkHeight - 1) * offsetX;
		float segY = ybase + v * (trunkHeight - 1);
		float segZ = zbase + h * (trunkHeight - 1) * offsetZ;
		
		float maxRadius = entity.getDestroyData().getBranchRadius(0) / 16.0f;
		
		Vec3d vec3d1 = new Vec3d(xbase, ybase, zbase);
		Vec3d vec3d2 = new Vec3d(segX, segY, segZ);
		
		List<Entity> list = world.getEntitiesInAABBexcluding(entity, new AxisAlignedBB(vec3d1, vec3d2), Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>() {
			public boolean apply(@Nullable Entity apply) {
				return apply != null && apply.canBeCollidedWith();
			}
		}));
		
		List<EntityLiving> entities = new ArrayList<>();
				
		Entity pointedEntity = null;
		
		for(Entity entity1: list) {
			AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow(maxRadius);
			RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(vec3d1, vec3d2);
			
			if (axisalignedbb.contains(vec3d1)) {
				pointedEntity = entity1;
			}
			else if (raytraceresult != null) {
				if (entity1.getLowestRidingEntity() == entity.getLowestRidingEntity() && !entity1.canRiderInteract()) {
					pointedEntity = entity1;
				}
				else {
					pointedEntity = entity1;
				}
			}
			
			if(pointedEntity instanceof EntityLiving) {
				entities.add((EntityLiving) pointedEntity);
			}
		}
		
		return entities;
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
				entity.ticksExisted > 120;
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
}
