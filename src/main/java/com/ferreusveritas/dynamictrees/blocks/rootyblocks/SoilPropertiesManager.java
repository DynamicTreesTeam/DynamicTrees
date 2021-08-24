package com.ferreusveritas.dynamictrees.blocks.rootyblocks;

import com.ferreusveritas.dynamictrees.api.treepacks.ApplierRegistryEvent;
import com.ferreusveritas.dynamictrees.api.treepacks.PropertyApplierResult;
import com.ferreusveritas.dynamictrees.deserialisation.JsonHelper;
import com.ferreusveritas.dynamictrees.deserialisation.ResourceLocationDeserialiser;
import com.ferreusveritas.dynamictrees.deserialisation.result.JsonResult;
import com.ferreusveritas.dynamictrees.resources.JsonRegistryEntryReloadListener;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

import java.util.function.Consumer;

/**
 * @author Max Hyper
 */
public class SoilPropertiesManager extends JsonRegistryEntryReloadListener<SoilProperties> {

    public SoilPropertiesManager() {
        super(SoilProperties.REGISTRY, ApplierRegistryEvent.SOIL_PROPERTIES);
    }

    @Override
    public void registerAppliers() {
        this.loadAppliers.register("substitute_soil", String.class, (soilProperties, substitute) ->
                soilProperties.setSubstitute(true)
        );

        this.setupAppliers.register("primitive_soil", Block.class, SoilProperties::setPrimitiveSoilBlock);

        this.registerSpreadableAppliers();

        this.commonAppliers.registerArrayApplier("acceptable_soils", String.class,
                (soilProperties, acceptableSoil) -> {
            if (SoilHelper.getSoilFlags(acceptableSoil) == 0) {
                SoilHelper.createNewAdjective(acceptableSoil);
            }

            SoilHelper.registerSoil(soilProperties, acceptableSoil);
            return PropertyApplierResult.success();
        });

        // Primitive soil is needed before gathering data.
        this.gatherDataAppliers.register("primitive_soil", Block.class, SoilProperties::setPrimitiveSoilBlock);

        super.registerAppliers();
    }

    private void registerSpreadableAppliers() {
        this.reloadAppliers
                .register("required_light", SpreadableSoilProperties.class, Integer.class,
                        SpreadableSoilProperties::setRequiredLight)
                .register("spread_item", SpreadableSoilProperties.class, Item.class,
                        SpreadableSoilProperties::setSpreadItem)
                .registerArrayApplier("spreadable_soils", SpreadableSoilProperties.class, SoilProperties.class,
                        (properties, soil) -> SoilProperties.REGISTRY.runOnNextLock(
                                () -> properties.addSpreadableSoils(soil)
                        ));
    }

    @Override
    protected void preLoad(JsonObject jsonObject, SoilProperties soilProperties, Consumer<String> errorConsumer,
                           Consumer<String> warningConsumer) {

        // If a custom block registry name was set, set and use it.
        JsonResult.forInput(jsonObject)
                .mapIfContains("block_registry_name", JsonElement.class, input ->
                        ResourceLocationDeserialiser.create(soilProperties.getRegistryName().getNamespace())
                                .deserialise(input).orElseThrow(), soilProperties.getBlockRegistryName()
                ).ifSuccessOrElse(soilProperties::setBlockRegistryName, errorConsumer, warningConsumer);
    }

    @Override
    protected void postLoad(JsonObject jsonObject, SoilProperties soilProperties, Consumer<String> errorConsumer,
                            Consumer<String> warningConsumer) {

        //set the substitute soil if one exists and is valid
        // dont generate block if the there is a substitute.
        SoilProperties substitute = JsonHelper.getOrDefault(jsonObject, "substitute_soil", SoilProperties.class, SoilProperties.NULL_SOIL_PROPERTIES);
        if (substitute != SoilProperties.NULL_SOIL_PROPERTIES) {
            soilProperties.setDynamicSoilBlock(substitute.dynamicSoilBlock);
        } else {
            soilProperties.generateDynamicSoil(JsonHelper.getBlockProperties(jsonObject,
                    soilProperties.getDefaultMaterial(), soilProperties.getDefaultMaterial().getColor(),
                    soilProperties::getDefaultBlockProperties, errorConsumer, warningConsumer));
        }
    }
}
