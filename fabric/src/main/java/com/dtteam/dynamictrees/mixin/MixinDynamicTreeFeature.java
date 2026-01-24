package com.dtteam.dynamictrees.mixin;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.dtteam.dynamictrees.api.worldgen.RandomXOR;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.worldgen.BiomeDatabase;
import com.dtteam.dynamictrees.worldgen.feature.DynamicTreeFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.util.List;

@Mixin(value = DynamicTreeFeature.class, remap = false)
public abstract class MixinDynamicTreeFeature {

    @Unique
    private static boolean dynamictrees$loggedCherrySelector = false;

    @Unique
    private static final RandomXOR dynamictrees$debugRandom = new RandomXOR();

    @Inject(method = "getSpeciesSelector", at = @At("RETURN"), remap = false)
    private void onGetSpeciesSelector(BiomeDatabase.EntryReader biomeEntry, CallbackInfoReturnable<BiomePropertySelectors.SpeciesSelector> cir) {
        if (dynamictrees$loggedCherrySelector) {
            return;
        }
        if (biomeEntry != null && biomeEntry.getBiomeKey() != null) {
            String biomeName = biomeEntry.getBiomeKey().location().toString();
            if (biomeName.contains("cherry")) {
                dynamictrees$loggedCherrySelector = true;
                BiomePropertySelectors.SpeciesSelector selector = cir.getReturnValue();
                DynamicTrees.LOG.info("Cherry biome species selector type: {} for biome {}",
                        selector.getClass().getSimpleName(), biomeName);

                dynamictrees$debugRandom.setXOR(BlockPos.ZERO);
                try {
                    BiomePropertySelectors.SpeciesSelection selection = selector.getSpecies(
                            BlockPos.ZERO, Blocks.DIRT.defaultBlockState(), dynamictrees$debugRandom);
                    Species species = selection.getSpecies();
                    DynamicTrees.LOG.info("Cherry biome would select species: {} (valid: {}, handled: {})",
                            species.getRegistryName(), species.isValid(), selection.isHandled());
                } catch (Exception e) {
                    DynamicTrees.LOG.error("Failed to test species selection for cherry biome", e);
                }

                if (selector instanceof BiomePropertySelectors.RandomSpeciesSelector randomSelector) {
                    dynamictrees$logRandomSelectorContents(randomSelector);
                }
            }
        }
    }

    @Unique
    private void dynamictrees$logRandomSelectorContents(BiomePropertySelectors.RandomSpeciesSelector selector) {
        try {
            Field decisionTableField = BiomePropertySelectors.RandomSpeciesSelector.class.getDeclaredField("decisionTable");
            decisionTableField.setAccessible(true);
            List<?> decisionTable = (List<?>) decisionTableField.get(selector);

            DynamicTrees.LOG.info("RandomSpeciesSelector contains {} entries:", decisionTable.size());

            for (Object entry : decisionTable) {
                Field decisionField = entry.getClass().getDeclaredField("decision");
                Field weightField = entry.getClass().getDeclaredField("weight");
                decisionField.setAccessible(true);
                weightField.setAccessible(true);

                BiomePropertySelectors.SpeciesSelection decision = (BiomePropertySelectors.SpeciesSelection) decisionField.get(entry);
                int weight = (int) weightField.get(entry);

                DynamicTrees.LOG.info("  - Species: {}, Weight: {}, Handled: {}",
                        decision.getSpecies().getRegistryName(), weight, decision.isHandled());
            }
        } catch (Exception e) {
            DynamicTrees.LOG.error("Failed to log RandomSpeciesSelector contents", e);
        }
    }
}
