package com.dtteam.dynamictrees.treepack.loader;

import com.dtteam.dynamictrees.api.resource.loading.preparation.JsonRegistryResourceLoader;
import com.dtteam.dynamictrees.block.soil.SoilHelper;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.block.soil.SpreadableSoilProperties;
import com.dtteam.dynamictrees.deserialization.JsonHelper;
import com.dtteam.dynamictrees.deserialization.deserializer.IdentifierDeserializer;
import com.dtteam.dynamictrees.deserialization.result.JsonResult;
import com.dtteam.dynamictrees.utility.IdentifierUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Max Hyper and Harley O'Connor
 */
public final class SoilPropertiesResourceLoader extends JsonRegistryResourceLoader<SoilProperties> {

    private static final Logger LOGGER = LogManager.getLogger();

    public SoilPropertiesResourceLoader() {
        super(SoilProperties.REGISTRY, SOIL_PROPERTIES);
    }

    @Override
    public void registerAppliers() {
        this.loadAppliers
                .register("substitute_soil", Identifier.class,
                        (soilProperties, registryName) -> {
                            soilProperties.setGenerateBlock(false);
                            registryName = IdentifierUtils.parseDTLocation(registryName);
                            SoilProperties.REGISTRY.runOnNextLock(SoilProperties.REGISTRY.generateIfValidRunnable(registryName,
                                    soilProperties::setSubstitute, setSubstituteWarn(soilProperties, registryName)));
                        })
                .register("foliage_tint_layer_count", Integer.class, SoilProperties::setFoliageTintLayerCount);

        // Primitive soil is needed before gathering data.
        this.gatherDataAppliers
                .register("primitive_soil", Block.class, SoilProperties::setPrimitiveSoilBlock)
                .register("only_if_loaded", String.class, SoilProperties::setOnlyIfLoaded)
                .registerArrayApplier("only_if_loaded",String.class,SoilProperties::setOnlyIfLoaded)
                .registerMapApplier("model_overrides", Identifier.class, SoilProperties::setModelOverrides)
                .registerMapApplier("texture_overrides", Identifier.class, SoilProperties::setTextureOverrides);

        this.setupAppliers
                .register("primitive_soil", Block.class, SoilProperties::setPrimitiveSoilBlock);

//        this.reloadAppliers
//                .register("foliage_tint_index", Integer.class, SoilProperties::setFoliageTintIndex)
//                .register("roots_tint_index", Integer.class, SoilProperties::setRootsTintIndex);
        this.registerSpreadableAppliers();

        this.commonAppliers
                .registerArrayApplier("acceptable_soils", String.class, this::registerAcceptableSoil);

        super.registerAppliers();
    }

    private static Runnable setSubstituteWarn(final SoilProperties soilProperties, final Identifier registryName) {
        return () -> LOGGER.warn("Could not set soil substitute for \"{}\" as soil with name \"{}\" was not found.", soilProperties, registryName);
    }

    private void registerAcceptableSoil(SoilProperties soilProperties, String acceptableSoil) {
        if (SoilHelper.getSoilFlags(acceptableSoil) == 0) {
            SoilHelper.createNewAdjective(acceptableSoil);
        }
        SoilHelper.registerSoil(soilProperties, acceptableSoil);
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
    protected void applyLoadAppliers(LoadData loadData, JsonObject json) {
        this.readCustomBlockRegistryName(loadData.getResource(), json);
        super.applyLoadAppliers(loadData, json);
    }

    private void readCustomBlockRegistryName(SoilProperties soilProperties, JsonObject json) {
        JsonResult.forInput(json)
                .mapIfContains("block_registry_name", JsonElement.class, input ->
                        IdentifierDeserializer.create(soilProperties.getRegistryName().getNamespace())
                                .deserialize(input).orElseThrow(), soilProperties.getBlockRegistryName()
                ).ifSuccessOrElse(
                        soilProperties::setBlockRegistryName,
                        error -> this.logError(soilProperties.getRegistryName(), error),
                        warning -> this.logWarning(soilProperties.getRegistryName(), warning)
                );
    }

    @Override
    protected void postLoadOnLoad(LoadData loadData, JsonObject json) {
        super.postLoadOnLoad(loadData, json);
        SoilProperties soilProperties = loadData.getResource();
        if (soilProperties.shouldGenerateBlock()) {
            this.generateSoilBlock(soilProperties, json);
        }
    }

    private void generateSoilBlock(SoilProperties soilProperties, JsonObject json) {
        soilProperties.generateBlock(JsonHelper.getBlockProperties(
                json,
                soilProperties::getDefaultBlockProperties,
                error -> this.logError(soilProperties.getRegistryName(), error),
                warning -> this.logWarning(soilProperties.getRegistryName(), warning)
        ));

    }

}
