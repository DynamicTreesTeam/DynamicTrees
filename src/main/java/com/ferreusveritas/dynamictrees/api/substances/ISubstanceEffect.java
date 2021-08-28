package com.ferreusveritas.dynamictrees.api.substances;

import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.entities.EntityLingeringEffector;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Represents a type of effect, somewhat like a potion effect, but for trees.
 * <p>
 * Effects may either be lingering or instant, which can be checked by {@link #isLingering()}. Lingering potion effects
 * can use {@link #apply(World, BlockPos)} to set up the effect and {@link #update(World, BlockPos, int, int)} to apply 
 * the effect on each tick. These types are controlled by a {@linkplain EntityLingeringEffector parent entity}. 
 * <p>
 * Instant effects should use {@link #apply(World, BlockPos)} to apply the effect and return {@code false} on {@link 
 * #update(World, BlockPos, int, int)}. 
 *
 * @author ferreusveritas
 * @see EntityLingeringEffector
 */
public interface ISubstanceEffect {

	/**
	 * Applies the substance effect. For lingering effects this may be used for setup.
	 *
	 * @param world the current world
	 * @param rootPos the position of the {@linkplain BlockRooty root block}
	 * @return {@code true} if the application was successful; {@code false} otherwise
	 * @deprecated replaced by {@link #apply(World, BlockPos, BlockPos)}
	 */
	@Deprecated
	default boolean apply(World world, BlockPos rootPos) {
		return this.apply(world, rootPos, BlockPos.ORIGIN).success();
	}

	/**
	 * Applies the substance effect. For lingering effects this may be used for setup.
	 *
	 * @param world the current world
	 * @param rootPos the position of the {@linkplain BlockRooty root block}
	 * @param hitPos the position at which the effect was applied
	 * @return {@code true} if the application was successful; {@code false} otherwise
	 */
	Result apply(World world, BlockPos rootPos, BlockPos hitPos);

	/**
	 * Updates a lingering effect. Called every tick by the {@linkplain EntityLingeringEffector controlling entity}.
	 *
	 * @param world the current world
	 * @param rootPos the position of the {@linkplain BlockRooty root block}
	 * @param deltaTicks the number of ticks since the effect started
	 * @param fertility the fertility of the tree 
	 * @return {@code true} if the effect should linger; {@code false} otherwise
	 */
	boolean update(World world, BlockPos rootPos, int deltaTicks, int fertility);

	/**
	 * Returns the name of the effect. Used to compare existing effects in the environment.
	 *
	 * @return the name of the effect
	 */
	String getName();

	/**
	 * Determines if the effect is continuous or instant. 
	 *
	 * @return {@code true} if continuous; {@code false} if instant
	 */
	boolean isLingering();

	/**
	 * The result of an application. This may be a success or failure result, indicated by nullness of the {@linkplain 
	 * #errorKey error key}. 
	 * <p>
	 * If the result is a failure, the key will be {@code non-null}, and the error should be displayed in some form 
	 * using a {@link TextComponentTranslation} with the {@link #args}.
	 * <p>
	 * If the result is a success, however, used items should be consumed and the player's arm should be swung (if the 
	 * substance was applied by a player). 
	 */
	class Result {
		/** The translation key of the error message to display, or {@code null} if successful. */
		@Nullable
		private final String errorKey;
		/** The error {@linkplain TextComponentTranslation translation text component}'s arguments. */
		private final Object[] args;

		public Result(@Nullable String errorKey, Object... args) {
			this.errorKey = errorKey;
			this.args = args;
		}

		/**
		 * @return the translation key of the error message to display, or {@code null} if successful
		 */
		@Nullable
		public String getErrorKey() {
			return errorKey;
		}

		/**
		 * @return the error {@linkplain TextComponentTranslation translation text component}'s arguments
		 */
		public Object[] getArgs() {
			return args;
		}

		/**
		 * @return {@code true} if this result is successful; {@code false} otherwise
		 */
		public boolean success() {
			return errorKey == null;
		}

		/**
		 * @return a successful result
		 */
		public static Result successful() {
			return new Result(null);
		}

		/**
		 * Creates a failed result with an error message.
		 * 
		 * @param errorKey the translation key of the error message to display
		 * @param args the error {@linkplain TextComponentTranslation translation text component}'s arguments
		 * @return the failed result
		 */
		public static Result failure(String errorKey, Object... args) {
			return new Result(errorKey, args);
		}

		/**
		 * Creates a failed result without an error message. 
		 * 
		 * @return the failed result
		 */
		public static Result failure() {
			return new Result("");
		}
	}

}
