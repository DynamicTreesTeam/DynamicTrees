package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.treepacks.JsonApplierRegistryEvent;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.resources.JsonRegistryEntryReloadListener;
import com.ferreusveritas.dynamictrees.util.json.JsonHelper;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

/**
 * Manages loading tree families from the <tt>trees</tt> folder.
 *
 * @author Harley O'Connor
 */
public final class FamilyManager extends JsonRegistryEntryReloadListener<Family> {

    private static final Logger LOGGER = LogManager.getLogger();

    public FamilyManager() {
        super(Family.REGISTRY, "families", JsonApplierRegistryEvent.FAMILY);
    }

    @Override
    public void registerAppliers() {
        this.loadReloadAppliers.register("common_species", ResourceLocation.class, (family, registryName) -> {
                    registryName = TreeRegistry.processResLoc(registryName);
                    Species.REGISTRY.runOnNextLock(Species.REGISTRY.generateIfValidRunnable(registryName, family::setupCommonSpecies, setCommonWarn(family, registryName)));
                })
                .register("common_leaves", LeavesProperties.class, Family::setCommonLeaves)
                .register("max_branch_radius", Integer.class, Family::setMaxBranchRadius);

        // Primitive logs are needed before gathering data.
        this.gatherDataAppliers.register("primitive_log", Block.class, Family::setPrimitiveLog)
                .register("primitive_stripped_log", Block.class, Family::setPrimitiveStrippedLog);

        this.setupAppliers.register("primitive_log", Block.class, Family::setPrimitiveLog)
                .register("primitive_stripped_log", Block.class, Family::setPrimitiveStrippedLog)
                .register("stick", Item.class, Family::setStick);

        this.loadAppliers.register("generate_surface_root", Boolean.class, Family::setHasSurfaceRoot)
                .register("generate_stripped_branch", Boolean.class, Family::setHasStrippedBranch)
                .register("fire_proof", Boolean.class, Family::setIsFireProof);

        this.reloadAppliers.register("conifer_variants", Boolean.class, Family::setHasConiferVariants)
                .register("can_support_cocoa", Boolean.class, Family::setCanSupportCocoa)
                .register("primary_thickness", Integer.class, Family::setPrimaryThickness)
                .register("secondary_thickness", Integer.class, Family::setSecondaryThickness)
                .register("branch_is_ladder", Boolean.class, Family::setBranchIsLadder)
                .register("max_signal_depth", Integer.class, Family::setMaxSignalDepth);

        super.registerAppliers();
    }

    /**
     * Generates a runnable for if there was not a registered {@link Species} under the {@link ResourceLocation} given
     * to set as common for the {@link Family} given.
     *
     * @param family       The {@link Family} object.
     * @param registryName The registry name {@link ResourceLocation}.
     * @return A {@link Runnable} that warns the user of the error.
     */
    private static Runnable setCommonWarn(final Family family, final ResourceLocation registryName) {
        return () -> LOGGER.warn("Could not set common species for '" + family + "' as Species '" + registryName + "' was not found.");
    }

    @Override
    protected void preLoad(JsonObject jsonObject, Family family, Consumer<String> errorConsumer, Consumer<String> warningConsumer) {
        family.setProperties(JsonHelper.getBlockProperties(
                JsonHelper.getOrDefault(jsonObject, "branch_properties", JsonObject.class, new JsonObject()),
                family.getDefaultBranchMaterial(), family.getDefaultBranchMaterial().getColor(),
                family::getDefaultBranchProperties, errorConsumer, warningConsumer));
    }

    @Override
    protected void postLoad(JsonObject jsonObject, Family family, Consumer<String> errorConsumer, Consumer<String> warningConsumer) {
        family.setupBlocks();
    }

}
