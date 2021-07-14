package com.ferreusveritas.dynamictrees.blocks.rootyblocks;

import com.ferreusveritas.dynamictrees.api.treepacks.JsonApplierRegistryEvent;
import com.ferreusveritas.dynamictrees.api.treepacks.PropertyApplierResult;
import com.ferreusveritas.dynamictrees.resources.JsonRegistryEntryReloadListener;
import com.ferreusveritas.dynamictrees.util.json.JsonHelper;
import com.ferreusveritas.dynamictrees.util.json.ResourceLocationGetter;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;

import java.util.function.Consumer;

public class SoilPropertiesManager extends JsonRegistryEntryReloadListener<SoilProperties> {

    public SoilPropertiesManager() {
        super(SoilProperties.REGISTRY, JsonApplierRegistryEvent.SOIL_PROPERTIES);
    }

    @Override
    public void registerAppliers() {
        this.loadReloadAppliers.registerArrayApplier("acceptable_soils", String.class, (soilProperties, acceptableSoil) -> {
            if (SoilHelper.getSoilFlags(acceptableSoil) == 0)
                SoilHelper.createNewAdjective(acceptableSoil);

            SoilHelper.registerSoil(soilProperties, acceptableSoil);
            return PropertyApplierResult.success();
        });
 //       .register("properties", JsonObject.class, SoilProperties::setRootyBlockProperties);

        this.loadAppliers.register("primitive_soil", Block.class, SoilProperties::setPrimitiveSoilBlock);

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
        //set the substitute soil if one exists and is valid
        // dont generate block if the there is a substitute.
        SoilProperties substitute = JsonHelper.getOrDefault(jsonObject, "substitute_soil", SoilProperties.class, SoilProperties.NULL_PROPERTIES);
        if (substitute != SoilProperties.NULL_PROPERTIES)
            soilProperties.setDynamicSoilBlock(substitute.dynamicSoilBlock);
        else
            soilProperties.generateDynamicSoil();
    }
}
