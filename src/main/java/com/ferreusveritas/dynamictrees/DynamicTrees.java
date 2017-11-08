package com.ferreusveritas.dynamictrees;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;
import com.ferreusveritas.dynamictrees.blocks.BlockBonsaiPot;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSapling;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSaplingVanilla;
import com.ferreusveritas.dynamictrees.blocks.BlockFruitCocoa;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.compat.CommonProxyCompat;
import com.ferreusveritas.dynamictrees.items.DendroPotion;
import com.ferreusveritas.dynamictrees.items.DirtBucket;
import com.ferreusveritas.dynamictrees.items.Staff;
import com.ferreusveritas.dynamictrees.proxy.CommonProxy;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.trees.TreeAcacia;
import com.ferreusveritas.dynamictrees.trees.TreeBirch;
import com.ferreusveritas.dynamictrees.trees.TreeDarkOak;
import com.ferreusveritas.dynamictrees.trees.TreeJungle;
import com.ferreusveritas.dynamictrees.trees.TreeOak;
import com.ferreusveritas.dynamictrees.trees.TreeSpruce;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.MissingMappings;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Mapping;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.registries.IForgeRegistry;

/**
* <p><pre><tt><b>
*  ╭─────────────────╮
*  │                 │
*  │       ▓▓        │
*  │      ▓▓▓▓▓      │
*  │     ▓▓▓┼▓▓▓     │
*  │    ▓▓▓▓│▓▓▓     │
*  │    ▓▓└─┧ ▓▓▓    │
*  │     ▓  ┠─┘▓     │
*  │        ┃        │
*  │  █████████████  │
*  │  ▒▒▒▒▒▒▒▒▒▒▒▒▒  │
*  │  ░░░░░░░░░░░░░  │
*  ╞═════════════════╡
*  │  DYNAMIC TREES  │
*  ╰─────────────────╯
* </b></tt></pre></p>
* 
* <p>
* 2016-2017 Ferreusveritas
* </p>
* 
* @author ferreusveritas
* @version 0.6.6
*
*/
@Mod(modid = DynamicTrees.MODID, version=DynamicTrees.VERSION,dependencies="after:computercraft;after:quark")
public class DynamicTrees {

	public static final String MODID = "dynamictrees";
	public static final String VERSION = "0.6.6";

	public static final DynamicTreesTab dynamicTreesTab = new DynamicTreesTab(MODID);
	
	public static BlockRootyDirt blockRootyDirt;
	public static BlockDynamicSapling blockDynamicSapling;
	public static BlockFruitCocoa blockFruitCocoa;
	public static Staff treeStaff;
	public static DendroPotion dendroPotion;
	public static DirtBucket dirtBucket;
	public static BlockBonsaiPot blockBonsaiPot;
	public static TreeGenerator treeGenerator;

	@Instance(MODID)
	public static DynamicTrees instance;

	@SidedProxy(clientSide = "com.ferreusveritas.dynamictrees.proxy.ClientProxy", serverSide = "com.ferreusveritas.dynamictrees.proxy.CommonProxy")
	public static CommonProxy proxy;
	
	@SidedProxy(clientSide = "com.ferreusveritas.dynamictrees.compat.ClientProxyCompat", serverSide = "com.ferreusveritas.dynamictrees.compat.CommonProxyCompat")
	public static CommonProxyCompat compatProxy;

	public static ArrayList<DynamicTree> baseTrees = new ArrayList<DynamicTree>();
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		//Run before anything else. Read your config, create blocks, items, etc, and register them with the GameRegistry.

		//CircleDebug.initCircleTests(); //This is for circle generation testing purposes only

		ConfigHandler.preInit(event);

		if(WorldGenRegistry.isWorldGenEnabled()) {
			treeGenerator = new TreeGenerator();
		}
		
		//Dirt
		blockRootyDirt = new BlockRootyDirt();
		blockDynamicSapling = new BlockDynamicSaplingVanilla("sapling");
		
		//Trees
		baseTrees.add(new TreeOak());
		baseTrees.add(new TreeSpruce());
		baseTrees.add(new TreeBirch());
		baseTrees.add(new TreeJungle());
		baseTrees.add(new TreeAcacia());
		baseTrees.add(new TreeDarkOak());
		
		//Register Trees
		TreeRegistry.registerTrees(baseTrees);
		
		//Potions
		dendroPotion = new DendroPotion();
		
		//Dirt Bucket
		dirtBucket = new DirtBucket();
		
		//Bonsai Pot
		blockBonsaiPot = new BlockBonsaiPot();
		
		//Fruit
		blockFruitCocoa = new BlockFruitCocoa();
		
		//Creative Mode Stuff
		treeStaff = new Staff();
		
		//Set the creative tabs icon
		dynamicTreesTab.setTabIconItemStack(new ItemStack(TreeRegistry.findTree("oak").getSeed()));
		
		proxy.preInit();
		compatProxy.preInit();
		
		proxy.registerEventHandlers();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		//Do your mod setup. Build whatever data structures you care about.
		
		if(WorldGenRegistry.isWorldGenEnabled()) {
			treeGenerator.biomeTreeHandler.init();
		}
		
		proxy.init();
		compatProxy.init();
	}

	@EventHandler
	public void PostInit(FMLPostInitializationEvent e){
		//Handle interaction with other mods, complete your setup based on this.
	}

	///////////////////////////////////////////
	// MISSING REMAPPING
	///////////////////////////////////////////

	/**
	 * Here we'll simply remap the old "growingtrees" modid to the new modid for old blocks and items.
	 * 
	 * @param event
	 */
	//Missing Blocks Resolved Here
	@EventHandler
	public void missingBlockMappings(MissingMappings<Block> event) {
		for(Mapping<Block> missing: event.getMappings()) {
			ResourceLocation resLoc = missing.key;
			String domain = resLoc.getResourceDomain();
			String path = resLoc.getResourcePath();
			if(domain.equals("growingtrees")) {
				Logger.getLogger(MODID).log(Level.CONFIG, "Remapping Missing Object: " + path);
				Block mappedBlock = Block.REGISTRY.getObject(new ResourceLocation(DynamicTrees.MODID, path));
				if(mappedBlock != Blocks.AIR) { //Air is what you get when do don't get what you're looking for.
					missing.remap(mappedBlock);
				}
			}
		}
	}

	//Missing Items Resolved Here
	@EventHandler
	public void missingItemMappings(MissingMappings<Item> event) {		
		for(Mapping<Item> missing: event.getMappings()) {
			ResourceLocation resLoc = missing.key;
			String domain = resLoc.getResourceDomain();
			String path = resLoc.getResourcePath();
			if(domain.equals("growingtrees")) {
				Logger.getLogger(MODID).log(Level.CONFIG, "Remapping Missing Object: " + path);
				Item mappedItem = Item.REGISTRY.getObject(new ResourceLocation(DynamicTrees.MODID, path));
				if(mappedItem != null) { //Null is what you get when do don't get what you're looking for.
					missing.remap(mappedItem);
				}
			}
		}
	}

	///////////////////////////////////////////
	// REGISTRATION
	///////////////////////////////////////////
	
	@Mod.EventBusSubscriber(modid = DynamicTrees.MODID)
	public static class RegistrationHandler {

		@SubscribeEvent
		public static void registerBlocks(final RegistryEvent.Register<Block> event) {
			final IForgeRegistry<Block> registry = event.getRegistry();

			registry.register(blockRootyDirt);
			registry.register(blockDynamicSapling);
			registry.register(blockBonsaiPot);
			registry.register(blockFruitCocoa);

			for(DynamicTree tree: baseTrees) {
				tree.registerBlocks(registry);
			}

			for(BlockDynamicLeaves leavesBlock: TreeHelper.getLeavesMapForModId(MODID).values()) {
				registry.register(leavesBlock);
			}

			compatProxy.registerBlocks(event);
		}
		
		@SubscribeEvent
		public static void registerItems(final RegistryEvent.Register<Item> event) {
			final IForgeRegistry<Item> registry = event.getRegistry();

			registry.register(treeStaff);

			for(DynamicTree tree: baseTrees) {
				tree.registerItems(registry);
			}

			registry.register(dendroPotion);
			registry.register(dirtBucket);
			
			for(BlockDynamicLeaves leavesBlock: TreeHelper.getLeavesMapForModId(MODID).values()) {
				registry.register(new ItemBlock(leavesBlock).setRegistryName(leavesBlock.getRegistryName()));
			}

			ItemBlock itemBlock = new ItemBlock(blockRootyDirt);
			itemBlock.setRegistryName(blockRootyDirt.getRegistryName());
			registry.register(itemBlock);

			ItemBlock itemBonsaiBlock = new ItemBlock(blockBonsaiPot);
			itemBonsaiBlock.setRegistryName(blockBonsaiPot.getRegistryName());
			registry.register(itemBonsaiBlock);
			
			compatProxy.registerItems(event);
		}

		@SubscribeEvent(priority = EventPriority.LOWEST)
		public static void registerRecipes(final RegistryEvent.Register<IRecipe> event) {
			final IForgeRegistry<IRecipe> registry = event.getRegistry();

			for(DynamicTree tree: baseTrees) {
				tree.registerRecipes(registry);
			}

			dirtBucket.registerRecipes(registry);			
			dendroPotion.registerRecipes(registry);
			
			compatProxy.registerRecipes(event);
		}
		
	}

}