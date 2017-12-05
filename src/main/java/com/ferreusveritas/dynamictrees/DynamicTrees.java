package com.ferreusveritas.dynamictrees;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.compat.CommonProxyCompat;
import com.ferreusveritas.dynamictrees.proxy.CommonProxy;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
@Mod(modid = ModConstants.MODID, version=ModConstants.VERSION, dependencies="after:computercraft;after:quark")
public class DynamicTrees {
	
	@Instance(ModConstants.MODID)
	public static DynamicTrees instance;

	@SidedProxy(clientSide = "com.ferreusveritas.dynamictrees.proxy.ClientProxy", serverSide = "com.ferreusveritas.dynamictrees.proxy.CommonProxy")
	public static CommonProxy proxy;
	
	@SidedProxy(clientSide = "com.ferreusveritas.dynamictrees.compat.ClientProxyCompat", serverSide = "com.ferreusveritas.dynamictrees.compat.CommonProxyCompat")
	public static CommonProxyCompat compatProxy;

	public static final CreativeTabs dynamicTreesTab = new CreativeTabs(ModConstants.MODID) {
        @SideOnly(Side.CLIENT)
		@Override
		public ItemStack getTabIconItem() {
			return TreeRegistry.findTree("oak").getSeedStack();
		}
	};
		
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		//Run before anything else. Read your config, create blocks, items, etc.

		ConfigHandler.preInit(event);

		TreeGenerator.preInit();
		
		//Blocks and Items
		ModBlocks.preInit();
		ModItems.preInit();
		ModTrees.preInit();
		
		//Register Trees
		ModTrees.registerTrees();
		
		proxy.preInit();
		compatProxy.preInit();
		
		proxy.registerEventHandlers();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		//Do your mod setup. Build whatever data structures you care about.
		
		TreeGenerator.init();
		
		proxy.init();
		compatProxy.init();
	}
	
}