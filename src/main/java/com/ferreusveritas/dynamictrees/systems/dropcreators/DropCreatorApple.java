package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.List;
import java.util.Random;

/**
 * A drop creator that drops apples just like Vanilla trees. No longer used by any of the trees in the base mod on
 * account of the addition of the apple species.  Left for demonstration purposes or in case I change my mind about
 * something.
 *
 * @author ferreusveritas
 */
public class DropCreatorApple extends DropCreator {

	public static final DropCreatorApple instance = new DropCreatorApple();

	public DropCreatorApple() {
		super(new ResourceLocation(ModConstants.MODID, "apple"));
	}

	@Override
	public List<ItemStack> getLeavesDrop(IBlockAccess access, Species species, BlockPos breakPos, Random random, List<ItemStack> dropList, int fortune) {
		//More fortune contrivances here.  Vanilla compatible returns.
		int chance = 200; //1 in 200 chance of returning an "apple"
		if (fortune > 0) {
			chance -= 10 << fortune;
			if (chance < 40) {
				chance = 40;
			}
		}

		if (random.nextInt(chance) == 0) {
			dropList.add(new ItemStack(Items.APPLE));
		}
		return dropList;
	}

}
