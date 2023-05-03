package com.ferreusveritas.dynamictrees.resources.loader;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.applier.ApplierRegistryEvent;
import com.ferreusveritas.dynamictrees.api.resource.loading.preparation.JsonRegistryResourceLoader;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.deserialisation.JsonHelper;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Harley O'Connor
 */
public final class FamilyResourceLoader extends JsonRegistryResourceLoader<Family> {

    private static final Logger LOGGER = LogManager.getLogger();

    public FamilyResourceLoader() {
        super(Family.REGISTRY, "families", ApplierRegistryEvent.FAMILY);
    }

    @Override
    public void registerAppliers() {
        this.commonAppliers
                .register("common_species", ResourceLocation.class, (family, registryName) -> {
                    registryName = TreeRegistry.processResLoc(registryName);
                    Species.REGISTRY.runOnNextLock(Species.REGISTRY.generateIfValidRunnable(registryName,
                            family::setupCommonSpecies, setCommonWarn(family, registryName)));
                })
                .register("common_leaves", LeavesProperties.class, Family::setCommonLeaves)
                .register("max_branch_radius", Integer.class, Family::setMaxBranchRadius);

        // Primitive logs are needed before gathering data.
        this.gatherDataAppliers
                .register("primitive_log", Block.class, Family::setPrimitiveLog)
                .register("primitive_stripped_log", Block.class, Family::setPrimitiveStrippedLog);

        this.setupAppliers
                .register("primitive_log", Block.class, Family::setPrimitiveLog)
                .register("primitive_stripped_log", Block.class, Family::setPrimitiveStrippedLog)
                .register("stick", Item.class, Family::setStick);

        this.loadAppliers
                .register("generate_surface_root", Boolean.class, Family::setHasSurfaceRoot)
                .register("generate_stripped_branch", Boolean.class, Family::setHasStrippedBranch)
                .register("fire_proof", Boolean.class, Family::setIsFireProof);

        this.reloadAppliers
                .register("primary_thickness", Integer.class, Family::setPrimaryThickness)
                .register("secondary_thickness", Integer.class, Family::setSecondaryThickness)
                .register("branch_is_ladder", Boolean.class, Family::setBranchIsLadder)
                .register("max_signal_depth", Integer.class, Family::setMaxSignalDepth)
                .register("loot_volume_multiplier", Float.class, Family::setLootVolumeMultiplier);

        super.registerAppliers();
    }

    /**
     * Generates a runnable for if there was not a registered {@link Species} under the specified {@code registryName}
     * to set as common for the specified {@code family}.
     *
     * @param family       the family
     * @param registryName the registry name of the requested family
     * @return a {@link Runnable} that logs the warning
     */
    private static Runnable setCommonWarn(final Family family, final ResourceLocation registryName) {
        return () -> LOGGER.warn("Could not set common species for \"" + family + "\" as species with name  \"" +
                registryName + "\" was not found.");
    }

    @Override
    protected void applyLoadAppliers(LoadData loadData, JsonObject json) {
        this.setBranchProperties(loadData.getResource(), json);
        super.applyLoadAppliers(loadData, json);
    }

    private void setBranchProperties(Family family, JsonObject json) {
        family.setProperties(JsonHelper.getBlockProperties(
                JsonHelper.getOrDefault(json, "branch_properties", JsonObject.class, new JsonObject()),
                family.getDefaultBranchMaterial(),
                family.getDefaultBranchMaterial().getColor(),
                family::getDefaultBranchProperties,
                error -> this.logError(family.getRegistryName(), error),
                warning -> this.logWarning(family.getRegistryName(), warning)
        ));
    }

    @Override
    protected void postLoadOnLoad(LoadData loadData, JsonObject json) {
        super.postLoadOnLoad(loadData, json);
        loadData.getResource().setupBlocks();
    }

}
