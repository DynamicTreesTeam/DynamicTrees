package com.ferreusveritas.dynamictrees.entities.animation;

import com.ferreusveritas.dynamictrees.entities.FallingTreeEntity;
import net.minecraft.util.Direction;

/**
 * This class hold different animation handlers for EntityFallingTree. The idea is that a unique animation could be used
 * for a certain harvesting circumstance.
 *
 * @author ferreusveritas
 */
public class AnimationHandlers {

    //This is what is run when the tree felling option is disabled
    public static final AnimationHandler voidAnimationHandler = new VoidAnimationHandler();

    public static final AnimationHandler defaultAnimationHandler = new PhysicsAnimationHandler() {
        @Override
        public String getName() {
            return "default";
        }

        @Override
        public void initMotion(FallingTreeEntity entity) {
            super.initMotion(entity);

            Direction cutDir = entity.getDestroyData().cutDir;
            entity.push(cutDir.getOpposite().getStepX() * 0.1, cutDir.getOpposite().getStepY() * 0.1, cutDir.getOpposite().getStepZ() * 0.1);
        }

    };

    public static final AnimationHandler blastAnimationHandler = new PhysicsAnimationHandler() {
        @Override
        public String getName() {
            return "blast";
        }

        @Override
        public void initMotion(FallingTreeEntity entity) {
            super.initMotion(entity);
        }

        public boolean shouldDie(FallingTreeEntity entity) {
            return entity.landed || entity.tickCount > 200;
        }

    };

    public static final AnimationHandler falloverAnimationHandler = new FalloverAnimationHandler();

}
