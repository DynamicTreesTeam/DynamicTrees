package com.dtteam.dynamictrees.treepack.loader;

import com.dtteam.dynamictrees.api.resource.loading.preparation.JsonRegistryResourceLoader;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.block.soil.SoilHelper;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.deserialization.JsonHelper;
import com.dtteam.dynamictrees.deserialization.applier.Applier;
import com.dtteam.dynamictrees.deserialization.applier.PropertyApplierResult;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.family.UndergroundRootsFamily;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.utility.ResourceLocationUtils;
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
        super(Family.REGISTRY, "families", FAMILY);
    }

    @Override
    public void registerAppliers() {
        this.commonAppliers
                .register("common_species", ResourceLocation.class,
                        (family, registryName) -> {
                    registryName = ResourceLocationUtils.parseDTLocation(registryName);
                    Species.REGISTRY.runOnNextLock(Species.REGISTRY.generateIfValidRunnable(registryName,
                            family::setCommonSpecies, setCommonWarn(family, registryName)));
                })
                .register("common_leaves", LeavesProperties.class, Family::setCommonLeaves)
                .register("max_branch_radius", Integer.class, Family::setMaxBranchRadius);

        // Primitive logs are needed before gathering data.
        this.gatherDataAppliers
                .register("primitive_log", Block.class, Family::setPrimitiveLog)
                .register("primitive_stripped_log", Block.class, Family::setPrimitiveStrippedLog)
                .register("only_if_loaded", String.class, Family::setOnlyIfLoaded)
                .registerArrayApplier("only_if_loaded", String.class, Family::setOnlyIfLoaded)
                .registerMapApplier("texture_overrides", ResourceLocation.class, Family::setTextureOverrides)
                .registerMapApplier("model_overrides", ResourceLocation.class, Family::setModelOverrides)
                .registerMapApplier("lang_overrides", String.class, Family::setLangOverrides)
                .register("stick", Item.class, Family::setStick);

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
                .register("loot_volume_multiplier", Float.class, Family::setLootVolumeMultiplier)
                .register("min_radius_for_stripping", Integer.class, Family::setMinRadiusForStripping)
                .register("reduce_radius_when_stripping", Boolean.class, Family::setReduceRadiusWhenStripping);

        registerMangroveAppliers();

        super.registerAppliers();
    }

    private void registerMangroveAppliers(){
        this.gatherDataAppliers
                .register("primitive_root", UndergroundRootsFamily.class, Block.class, UndergroundRootsFamily::setPrimitiveRoots)
                .register("primitive_filled_root", UndergroundRootsFamily.class, Block.class, UndergroundRootsFamily::setPrimitiveRootsFilled)
                .register("primitive_covered_root", UndergroundRootsFamily.class, Block.class, UndergroundRootsFamily::setPrimitiveRootsCovered)
                //to-do: put in soil properties instead
                .register("default_soil", UndergroundRootsFamily.class, SoilProperties.class, UndergroundRootsFamily::setDefaultSoil);
        this.setupAppliers
                .register("primitive_root", UndergroundRootsFamily.class, Block.class, UndergroundRootsFamily::setPrimitiveRoots)
                .register("primitive_filled_root", UndergroundRootsFamily.class, Block.class, UndergroundRootsFamily::setPrimitiveRootsFilled)
                .register("primitive_covered_root", UndergroundRootsFamily.class, Block.class, UndergroundRootsFamily::setPrimitiveRootsCovered)
                //.register("replaceable_by_roots", MangroveFamily.class , ,)
        ;
        this.reloadAppliers
                .register("default_soil", UndergroundRootsFamily.class, SoilProperties.class, UndergroundRootsFamily::setDefaultSoil)
                .registerArrayApplier("root_system_acceptable_soils", UndergroundRootsFamily.class, String.class, (Applier<UndergroundRootsFamily, String>) this::addAcceptableSoilForRootSystem);
        ;

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
        return () -> LOGGER.warn("Could not set common species for \"{}\" as species with name  \"{}\" was not found.", family, registryName);
    }

    @Override
    protected void applyLoadAppliers(LoadData loadData, JsonObject json) {
        this.setBranchProperties(loadData.getResource(), json);
        super.applyLoadAppliers(loadData, json);
    }

    private void setBranchProperties(Family family, JsonObject json) {
        family.setProperties(JsonHelper.getBlockProperties(
                JsonHelper.getOrDefault(json, "branch_properties", JsonObject.class, new JsonObject()),
                family.getDefaultBranchMapColor(),
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

    private PropertyApplierResult addAcceptableSoilForRootSystem(UndergroundRootsFamily family, String acceptableSoil) {
        return SoilHelper.applyIfSoilIsAcceptable(family, acceptableSoil, UndergroundRootsFamily::addAcceptableSoilsForRootSystem);
    }

}
