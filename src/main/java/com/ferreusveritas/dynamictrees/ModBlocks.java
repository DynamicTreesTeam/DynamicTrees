package com.ferreusveritas.dynamictrees;

import com.ferreusveritas.dynamictrees.blocks.BlockBonsaiPot;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSapling;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSaplingVanilla;
import com.ferreusveritas.dynamictrees.blocks.BlockFruitCocoa;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;

public class ModBlocks {
	
	public static BlockRootyDirt blockRootyDirt;
	public static BlockDynamicSapling blockDynamicSapling;
	public static BlockFruitCocoa blockFruitCocoa;
	public static BlockBonsaiPot blockBonsaiPot;
	
	public static void preInit() {
	
		//Dirt
		blockRootyDirt = new BlockRootyDirt();
		
		//Dynamic version of a Vanilla sapling
		blockDynamicSapling = new BlockDynamicSaplingVanilla("sapling");
		
		//Bonsai Pot
		blockBonsaiPot = new BlockBonsaiPot();
		
		//Fruit
		blockFruitCocoa = new BlockFruitCocoa();
		
	}
	
}
