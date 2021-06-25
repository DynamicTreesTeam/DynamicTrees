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
 * A drop creator that drops fruit just like Vanilla apples.
 *
 * @author ferreusveritas
 */
public class FruitDropCreator extends DropCreator {

	public static final FruitDropCreator instance = new FruitDropCreator();
	public ItemStack fruit = new ItemStack(Items.APPLE);
	protected final float rarity;

	public FruitDropCreator() {
		this(1.0f);
	}

	public FruitDropCreator(float rarity) {
		super(new ResourceLocation(DynamicTrees.MOD_ID, "apple"));
		this.rarity = rarity;
	}

	public FruitDropCreator setFruitItem (Item fruitItem){
		this.fruit = new ItemStack(fruitItem);
		return this;
	}

	protected float getLeavesRarity() {
		return rarity;
	}

	@Override
	protected void registerProperties() { }

	@Override
	public List<ItemStack> getHarvestDrops(ConfiguredDropCreator<DropCreator> configuration, World world, Species species, BlockPos leafPos, Random random, List<ItemStack> drops, int fertility, int fortune) {
		return getFruit(drops, random, fortune);
	}

	@Override
	public List<ItemStack> getLeavesDrops(ConfiguredDropCreator<DropCreator> configuration, World world, Species species, BlockPos breakPos, Random random, List<ItemStack> drops, int fortune) {
		return getFruit(drops, random, fortune);
	}

	private List<ItemStack> getFruit (List<ItemStack> dropList, Random random, int fortune){
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
