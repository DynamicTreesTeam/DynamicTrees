package com.ferreusveritas.dynamictrees.worldgen;

//import com.ferreusveritas.dynamictrees.ModConfigs;
//import net.minecraft.world.biome.Biome;
//import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
//import net.minecraftforge.fml.common.eventhandler.Event.Result;
//import net.minecraftforge.fml.common.eventhandler.EventPriority;
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//
///**
// *
// * This serves only to cancel select decoration events such as trees and cactus.
// *
// * @author ferreusveritas
// */
//public class TreeGenCancelEventHandler {
//
//	@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
//	public void onEvent(DecorateBiomeEvent.Decorate event) {
//		int dimensionId = event.getWorld().provider.getDimension();
//		BiomeDataBase dbase = TreeGenerator.getTreeGenerator().getBiomeDataBase(dimensionId);
//		if(dbase != TreeGenerator.DIMENSIONBLACKLISTED && !ModConfigs.dimensionBlacklist.contains(dimensionId)) {
//			Biome biome = event.getWorld().getBiome(event.getPos());
//			switch(event.getType()) {
//				case CACTUS: if(ModConfigs.vanillaCactusWorldGen) { break; }
//				case BIG_SHROOM:
//				case TREE:
//					if(dbase.getEntry(biome).shouldCancelVanillaTreeGen()) {
//						event.setResult(Result.DENY);
//					}
//				default: break;
//			}
//		}
//	}
//}