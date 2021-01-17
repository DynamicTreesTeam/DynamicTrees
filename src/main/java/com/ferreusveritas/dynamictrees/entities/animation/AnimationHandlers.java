package com.ferreusveritas.dynamictrees.entities.animation;

import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import net.minecraft.util.Direction;

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
	public static final IAnimationHandler voidAnimationHandler = new VoidAnimationHandler();
	
	public static final IAnimationHandler defaultAnimationHandler = new PhysicsAnimationHandler() {
		@Override public String getName() { return "default"; };
		
		@Override
		public void initMotion(EntityFallingTree entity) {
			super.initMotion(entity);
			
			Direction cutDir = entity.getDestroyData().cutDir;
			entity.addVelocity(cutDir.getOpposite().getXOffset() * 0.1,cutDir.getOpposite().getYOffset() * 0.1,cutDir.getOpposite().getZOffset() * 0.1);
		}
		
	};
	
	public static final IAnimationHandler blastAnimationHandler = new PhysicsAnimationHandler() {
		@Override public String getName() { return "blast"; };
		
		@Override
		public void initMotion(EntityFallingTree entity) {
			super.initMotion(entity);
		}
		
		public boolean shouldDie(EntityFallingTree entity) {
			return entity.landed || entity.ticksExisted > 200;
		}
		
	};
	
	public static final IAnimationHandler falloverAnimationHandler = new FalloverAnimationHandler();
	
}
