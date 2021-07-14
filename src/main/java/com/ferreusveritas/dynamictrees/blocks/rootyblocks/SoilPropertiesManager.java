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
        this.reloadAppliers.registerArrayApplier("acceptable_soils", String.class, (soilProperties, acceptableSoil) -> {
            if (DirtHelper.getSoilFlags(acceptableSoil) == 0)
                return PropertyApplierResult.failure("Could not find acceptable soil '" + acceptableSoil + "'.");

            DirtHelper.registerSoil(soilProperties, acceptableSoil);
            return PropertyApplierResult.success();
        });

        // Primitive soil blocks are needed both client and server (so cannot be done on setup).
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

        // dont generate block if the there is a substitute.
        if (!jsonObject.has("substitute_soil"))
            soilProperties.generateDynamicSoil();

    }

    @Override
    protected void postLoad(JsonObject jsonObject, SoilProperties soilProperties, Consumer<String> errorConsumer, Consumer<String> warningConsumer) {
        //set the substitute soil if one exists and is valid
        SoilProperties substitute = JsonHelper.getOrDefault(jsonObject, "substitute_soil", SoilProperties.class, SoilProperties.NULL_PROPERTIES);
        if (substitute != SoilProperties.NULL_PROPERTIES)
            soilProperties.setDynamicSoilBlock(substitute.dynamicSoilBlock);
    }
}
