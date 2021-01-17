package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

/**
 * A drop creator that drops apples just like Vanilla trees.
 * No longer used by any of the trees in the base mod on account of
 * the addition of the apple species.  Left for demonstration purposes
 * or in case I change my mind about something.
 * 
 * @author ferreusveritas
 *
 */
public class FruitDropCreator extends DropCreator {

	public static final FruitDropCreator instance = new FruitDropCreator();
	public ItemStack fruit = new ItemStack(Items.APPLE);
	protected final float rarity;

	public FruitDropCreator(){
		this(1.0f);
	}

	public FruitDropCreator(float rarity) {
		super(new ResourceLocation(DynamicTrees.MODID, "apple"));
		this.rarity = rarity;
	}

	public FruitDropCreator setFruitDrop (Item fruitItem){
		this.fruit = new ItemStack(fruitItem);
		return this;
	}

	protected float getLeavesRarity() {
		return rarity;
	}

	@Override
	public List<ItemStack> getLeavesDrop(World access, Species species, BlockPos breakPos, Random random, List<ItemStack> dropList, int fortune) {
		//More fortune contrivances here.  Vanilla compatible returns.
		int chance = 200; //1 in 200 chance of returning an "apple"
		if (fortune > 0) {
			chance -= 10 << fortune;
			if (chance < 40) {
				chance = 40;
			}
		}
		
		if(random.nextInt((int) (chance / getLeavesRarity())) == 0) {
			dropList.add(fruit);
		}
		return dropList;
	}

}
