package com.ferreusveritas.dynamictrees.entities.animation;

import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;

import net.minecraft.util.EnumFacing;

/**
 * 
 * This class hold different animation handlers for EntityFallingTree.
 * The idea is that a unique animation could be used for a certain harvesting circumstance.
 * 
 * @author ferreusveritas
 *
 */
public class AnimationHandlers {
	
	//This is what is run when the tree felling option is disabled
	public static final IAnimationHandler voidAnimationHandler = new AnimationHandlerVoid();
	
	public static final IAnimationHandler defaultAnimationHandler = new AnimationHandlerPhysics() {
		@Override public String getName() { return "default"; };
		
		@Override
		public void initMotion(EntityFallingTree entity) {
			super.initMotion(entity);
			
			EnumFacing cutDir = entity.getDestroyData().cutDir;
			entity.motionX += cutDir.getOpposite().getFrontOffsetX() * 0.1;
			entity.motionY += cutDir.getOpposite().getFrontOffsetY() * 0.1;
			entity.motionZ += cutDir.getOpposite().getFrontOffsetZ() * 0.1;
		}
		
	};
	
	public static final IAnimationHandler blastAnimationHandler = new AnimationHandlerPhysics() {
		@Override public String getName() { return "blast"; };
		
		@Override
		public void initMotion(EntityFallingTree entity) {
			super.initMotion(entity);
		}
		
		public boolean shouldDie(EntityFallingTree entity) {
			return entity.landed || entity.ticksExisted > 200;
		}
		
	};
	
	public static final IAnimationHandler demoAnimationHandler = new AnimationHandlerDemo();
	
	public static final IAnimationHandler falloverAnimationHandler = new AnimationHandlerFallover();
	
}
