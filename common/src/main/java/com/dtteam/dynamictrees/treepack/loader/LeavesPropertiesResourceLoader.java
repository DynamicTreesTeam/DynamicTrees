package com.dtteam.dynamictrees.treepack.loader;

import com.dtteam.dynamictrees.api.cell.CellKit;
import com.dtteam.dynamictrees.api.resource.loading.preparation.JsonRegistryResourceLoader;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.block.leaves.PalmLeavesProperties;
import com.dtteam.dynamictrees.block.leaves.ScruffyLeavesProperties;
import com.dtteam.dynamictrees.deserialization.JsonDeserializers;
import com.dtteam.dynamictrees.deserialization.JsonHelper;
import com.dtteam.dynamictrees.deserialization.applier.Applier;
import com.dtteam.dynamictrees.deserialization.applier.PropertyApplierResult;
import com.dtteam.dynamictrees.deserialization.deserializer.IdentifierDeserializer;
import com.dtteam.dynamictrees.deserialization.result.JsonResult;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.utility.IdentifierUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

/**
 * @author Harley O'Connor
 */
public final class LeavesPropertiesResourceLoader extends JsonRegistryResourceLoader<LeavesProperties> {

    public LeavesPropertiesResourceLoader() {
        super(LeavesProperties.REGISTRY, LEAVES_PROPERTIES);
    }

    @Override
    public void registerAppliers() {
        this.loadAppliers.register("color", String.class, LeavesProperties::setColorString)
                .register("foliage_tint_layer_count", Integer.class, LeavesProperties::setFoliageTintLayerCount);

        // Primitive leaves are needed before gathering data.
        this.gatherDataAppliers
                .register("primitive_leaves", Block.class, LeavesProperties::setPrimitiveLeaves)
                .register("only_if_loaded",String.class,LeavesProperties::setOnlyIfLoaded)
                .registerArrayApplier("only_if_loaded",String.class,LeavesProperties::setOnlyIfLoaded)
                .registerListApplier("seed_drop_chances", Float.class, LeavesProperties::setSeedDropChances)
                .registerMapApplier("texture_overrides", Identifier.class, LeavesProperties::setTextureOverrides)
                .registerMapApplier("model_overrides", Identifier.class, LeavesProperties::setModelOverrides)
                .register("frond_model_loader", PalmLeavesProperties.class, Identifier.class, PalmLeavesProperties::setFrondLoader)
                .registerMapApplier("lang_overrides", String.class, LeavesProperties::setLangOverrides);

        // Primitive leaves are needed both client and server (so cannot be done on load).
        this.setupAppliers.register("primitive_leaves", Block.class, LeavesProperties::setPrimitiveLeaves)
                .register("family", Identifier.class, (leavesProperties, registryName) -> {
                    final Identifier processedRegName = IdentifierUtils.parseDTLocation(registryName);
                    Family.REGISTRY.runOnNextLock(Family.REGISTRY.generateIfValidRunnable(
                            processedRegName,
                            leavesProperties::setFamily,
                            () -> this.logWarning(leavesProperties.getRegistryName(),
                                    "Could not set family for leaves properties with name \"" + leavesProperties
                                            + "\" as family \"" + processedRegName + "\" was not found.")
                    ));
                })
                .register("particle", JsonElement.class, this::processParticle)
                .register("particle_color", Integer.class, LeavesProperties::setForceParticleColor);

        this.reloadAppliers.register("requires_shears", Boolean.class, LeavesProperties::setRequiresShears)
                .register("cell_kit", CellKit.class, LeavesProperties::setCellKit)
                .register("smother", Integer.class, LeavesProperties::setSmotherLeavesMax)
                .register("light_requirement", Integer.class, LeavesProperties::setLightRequirement)
                .register("fire_spread", Integer.class, LeavesProperties::setFireSpreadSpeed)
                .register("flammability", Integer.class, LeavesProperties::setFlammability)
                .register("connect_any_radius", Boolean.class, LeavesProperties::setConnectAnyRadius)
                .register("does_age", String.class, (Applier<LeavesProperties, String>) this::readDoesAge)
                .register("ageing_configuration", LeavesProperties.AgeingConfiguration.class, LeavesProperties::setAgeingConfiguration)
                .register("can_grow_on_ground", Boolean.class, LeavesProperties::setCanGrowOnGround)
                .register("water_resistant", Boolean.class, LeavesProperties::setWaterResistant)
                .register("scruffy_leaf_chance", ScruffyLeavesProperties.class, Float.class, ScruffyLeavesProperties::setLeafChance)
                .register("scruffy_max_hydro", ScruffyLeavesProperties.class, Integer.class, ScruffyLeavesProperties::setMaxHydro);

        super.registerAppliers();
    }

    private PropertyApplierResult readDoesAge(LeavesProperties leavesProperties, String configurationName) {
        LogManager.getLogger().warn("Deprecated use of leaves properties `does_age` property by \"{}\". This has been renamed to `ageing_configuration`.",
                leavesProperties.getRegistryName());
        // Account for refactors: YES -> ALWAYS, NO -> NEVER
        if (configurationName.equalsIgnoreCase("yes")) {
            leavesProperties.setAgeingConfiguration(LeavesProperties.AgeingConfiguration.ALWAYS);
        } else if (configurationName.equalsIgnoreCase("no")) {
            leavesProperties.setAgeingConfiguration(LeavesProperties.AgeingConfiguration.NEVER);
        } else {
            try {
                leavesProperties.setAgeingConfiguration(LeavesProperties.AgeingConfiguration.valueOf(configurationName.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return PropertyApplierResult.failure("Unsupported ageing configuration: \"" + configurationName + "\".");
            }
        }
        return PropertyApplierResult.success();
    }

    @Override
    protected void applyLoadAppliers(JsonRegistryResourceLoader<LeavesProperties>.LoadData loadData, JsonObject json) {
        super.applyLoadAppliers(loadData, json);

        if (this.shouldGenerateBlocks(json)) {
            final LeavesProperties leavesProperties = loadData.getResource();
            this.generateBlocks(leavesProperties, json);
        }
    }

    private Boolean shouldGenerateBlocks(JsonObject json) {
        return JsonHelper.getOrDefault(json, "generate_block", Boolean.class, true);
    }

    private void generateBlocks(LeavesProperties leavesProperties, JsonObject json) {
        final BlockBehaviour.Properties blockProperties = JsonHelper.getBlockProperties(
                json,
                leavesProperties::getDefaultBlockProperties,
                error -> this.logError(leavesProperties.getRegistryName(), error),
                warning -> this.logWarning(leavesProperties.getRegistryName(), warning)
        );

        leavesProperties.setRequiresShears(true);

        readCustomBlockRegistryName(leavesProperties, json);
        readParticleChance(leavesProperties, json);

        leavesProperties.generateDynamicLeaves(blockProperties);
    }

    private void readCustomBlockRegistryName(LeavesProperties leavesProperties, JsonObject json) {
        JsonResult.forInput(json)
                .mapIfContains("block_registry_name", JsonElement.class, input ->
                        IdentifierDeserializer.create(leavesProperties.getRegistryName().getNamespace())
                                .deserialize(input).orElseThrow(), leavesProperties.getBlockRegistryName()
                ).ifSuccessOrElse(
                        leavesProperties::setBlockRegistryName,
                        error -> this.logError(leavesProperties.getRegistryName(), error),
                        warning -> this.logWarning(leavesProperties.getRegistryName(), warning)
                );
    }

    private void readParticleChance(LeavesProperties leavesProperties, JsonObject json) {
        JsonResult.forInput(json)
                .mapIfContains("particle_chance", JsonElement.class, input ->
                        JsonDeserializers.FLOAT.deserialize(input).orElseThrow(), leavesProperties.getLeavesParticleChance()
                ).ifSuccessOrElse(
                        leavesProperties::setLeavesParticleChance,
                        error -> this.logError(leavesProperties.getRegistryName(), error),
                        warning -> this.logWarning(leavesProperties.getRegistryName(), warning)
                );
    }

    @SuppressWarnings("unchecked")
    private void processParticle(LeavesProperties leavesProperties, JsonElement json){
        ParticleType<?> particle = JsonDeserializers.PARTICLE_TYPE.deserialize(json).orElse(null);
        if (particle instanceof SimpleParticleType simpleParticle){
            leavesProperties.setLeavesParticle(simpleParticle);
        } else if (particle != null){
            JsonObject dummy = new JsonObject();
            dummy.addProperty("color", 0xFFFFFF);
            ParticleOptions options = particle.codec().codec()
                    .parse(JsonOps.INSTANCE, dummy).result().orElse(null);
            if (options instanceof ColorParticleOption){
                leavesProperties.setLeavesParticle((ParticleType<@NotNull ColorParticleOption>) particle);
            }
        }
    }

}
