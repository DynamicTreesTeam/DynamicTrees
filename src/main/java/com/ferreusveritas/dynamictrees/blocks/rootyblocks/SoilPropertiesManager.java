package com.ferreusveritas.dynamictrees.blocks.rootyblocks;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.treepacks.JsonApplierRegistryEvent;
import com.ferreusveritas.dynamictrees.api.treepacks.PropertyApplierResult;
import com.ferreusveritas.dynamictrees.resources.JsonRegistryEntryReloadListener;
import com.ferreusveritas.dynamictrees.util.json.JsonHelper;
import com.ferreusveritas.dynamictrees.util.json.ResourceLocationGetter;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

/**
 * @author Max Hyper
 */
public class SoilPropertiesManager extends JsonRegistryEntryReloadListener<SoilProperties> {

    private static final Logger LOGGER = LogManager.getLogger();

    public SoilPropertiesManager() {
        super(SoilProperties.REGISTRY, JsonApplierRegistryEvent.SOIL_PROPERTIES);
    }

    @Override
    public void registerAppliers() {
        this.loadAppliers.register("substitute_soil", ResourceLocation.class, (soilProperties, substituteRegName) ->{
            soilProperties.setHasSubstitute(true);
            //set the substitute soil if one exists and is valid
            final ResourceLocation processedRegName = TreeRegistry.processResLoc(substituteRegName);
            SoilProperties.REGISTRY.runOnNextLock(SoilProperties.REGISTRY.generateIfValidRunnable(processedRegName, (substituteSoil)->{
                if (substituteSoil != SoilProperties.NULL_SOIL_PROPERTIES) {
                    soilProperties.setDynamicSoilBlock(substituteSoil.dynamicSoilBlock);
                }
            }, () -> LOGGER.warn("Could not set soil substitution for soil '" + soilProperties + "' as substitute soil '" + processedRegName + "' was not found.")));
        });

        this.loadReloadAppliers.registerArrayApplier("acceptable_soils", String.class, (soilProperties, acceptableSoil) -> {
            if (SoilHelper.getSoilFlags(acceptableSoil) == 0) {
                SoilHelper.createNewAdjective(acceptableSoil);
            }

            SoilHelper.registerSoil(soilProperties, acceptableSoil);
            return PropertyApplierResult.success();
        });

        // Primitive soil is needed before gathering data.
        this.gatherDataAppliers.register("primitive_soil", Block.class, SoilProperties::setPrimitiveSoilBlock);

        this.setupAppliers.register("primitive_soil", Block.class, SoilProperties::setPrimitiveSoilBlock);

        super.registerAppliers();
    }

    @Override
    protected void preLoad(JsonObject jsonObject, SoilProperties soilProperties, Consumer<String> errorConsumer, Consumer<String> warningConsumer) {

        // If a custom block registry name was set, set and use it.
        JsonHelper.JsonObjectReader.of(jsonObject).ifContains("block_registry_name", jsonElement ->
                ResourceLocationGetter.create(soilProperties.getRegistryName().getNamespace()).get(jsonElement)
                        .ifSuccessful(soilProperties::setBlockRegistryName)
        );
    }

    @Override
    protected void postLoad(JsonObject jsonObject, SoilProperties soilProperties, Consumer<String> errorConsumer, Consumer<String> warningConsumer) {
        // dont generate block if the there is a substitute.
        if (!soilProperties.hasSubstitute()){
            soilProperties.generateDynamicSoil(JsonHelper.getBlockProperties(jsonObject,
                    soilProperties.getDefaultMaterial(), soilProperties.getDefaultMaterial().getColor(),
                    soilProperties::getDefaultBlockProperties, errorConsumer, warningConsumer));
        }
    }
}
