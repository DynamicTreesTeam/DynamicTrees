package com.ferreusveritas.dynamictrees.worldgen;

/**
 *
 * This serves only to cancel select decoration events such as trees and cactus.
 *
 * @author ferreusveritas
 */
public class TreeGenCancelEventHandler {

	/*@SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
	public void onEvent(DecorateBiomeEvent.Decorate event) {
		int dimensionId = event.getWorld().provider.getDimension();
		BiomeDataBase dbase = TreeGenerator.getTreeGenerator().getBiomeDataBase(dimensionId);
		if(dbase != TreeGenerator.DIMENSIONBLACKLISTED && !ModConfigs.dimensionBlacklist.contains(dimensionId)) {
			Biome biome = event.getWorld().getBiome(event.getPos());
			switch(event.getType()) {
				case CACTUS: if(ModConfigs.vanillaCactusWorldGen) { break; }
				case BIG_SHROOM:
				case TREE:
					if(dbase.getEntry(biome).shouldCancelVanillaTreeGen()) {
						event.setResult(Result.DENY);
					}
				default: break;
			}
		}
	}*/

    /*@SubscribeEvent
    public void onEvent (BiomeEvent event) {
        for (ConfiguredFeature feature : event.getBiome().getFeatures(GenerationStage.Decoration.VEGETAL_DECORATION)) {
            System.out.println("Vegetal Feature: " + feature.feature.toString());
        }
    }*/

}