package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.api.datapacks.IJsonApplierManager;
import com.ferreusveritas.dynamictrees.api.datapacks.PropertyApplierResult;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.resources.ILoadListener;
import com.ferreusveritas.dynamictrees.resources.MultiJsonReloadListener;
import com.ferreusveritas.dynamictrees.util.json.JsonObjectGetters;
import com.ferreusveritas.dynamictrees.util.json.JsonPropertyApplierList;
import com.ferreusveritas.dynamictrees.util.json.ObjectFetchResult;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.profiler.EmptyProfiler;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manages loading tree families from the <tt>trees</tt> folder.
 *
 * @author Harley O'Connor
 */
public final class TreeFamilyManager extends MultiJsonReloadListener implements ILoadListener, IJsonApplierManager {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final class SpeciesRegistryData {
        private SpeciesType<Species> type = TreeSpecies.TREE_SPECIES;
        private LeavesProperties leavesProperties;
        private ResourceLocation registryName;
        private boolean common = false;
        private boolean generateSeed = false;
        private boolean generateSapling = false;

        public SpeciesRegistryData setRegistryName(ResourceLocation registryName) {
            this.registryName = registryName;
            return this;
        }

        public void setType(SpeciesType<Species> type) {
            this.type = type;
        }

        public void setLeavesProperties(LeavesProperties leavesProperties) {
            this.leavesProperties = leavesProperties;
        }

        public void setCommon(boolean common) {
            this.common = common;
        }

        public void setGenerateSeed(boolean generateSeed) {
            this.generateSeed = generateSeed;
        }

        public void setGenerateSapling(boolean generateSapling) {
            this.generateSapling = generateSapling;
        }
    }

    /** A {@link JsonPropertyApplierList} for applying properties to {@link TreeFamily} objects. */
    private final JsonPropertyApplierList<TreeFamily> appliers = new JsonPropertyApplierList<>(TreeFamily.class);

    /** A {@link JsonPropertyApplierList} for applying properties to {@link SpeciesRegistryData} objects. */
    private final JsonPropertyApplierList<SpeciesRegistryData> speciesRegistryDataAppliers = new JsonPropertyApplierList<>(SpeciesRegistryData.class);

    /** A {@link Set} holding the {@link Species} {@link ResourceLocation} names that have been taken. */
    private final Set<ResourceLocation> speciesToRegister = Sets.newHashSet();

    public TreeFamilyManager() {
        super("families");
        this.registerAppliers();
    }

    @Override
    public void registerAppliers() {
        this.speciesRegistryDataAppliers.register("name", ResourceLocation.class, SpeciesRegistryData::setRegistryName)
                .register("type", TreeSpecies.CLASS, SpeciesRegistryData::setType)
                .register("leaves_properties", LeavesProperties.class, SpeciesRegistryData::setLeavesProperties)
                .register("common", Boolean.class, SpeciesRegistryData::setCommon)
                .register("generate_seed", Boolean.class, SpeciesRegistryData::setGenerateSeed)
                .register("generate_sapling", Boolean.class, SpeciesRegistryData::setGenerateSeed);

        JsonObjectGetters.register(SpeciesRegistryData.class, jsonElement -> {
            final SpeciesRegistryData registryData = new SpeciesRegistryData();

            if (!jsonElement.isJsonObject()) {
                final ObjectFetchResult<ResourceLocation> fetchResult = JsonObjectGetters.RESOURCE_LOCATION_GETTER.get(jsonElement);

                if (!fetchResult.wasSuccessful())
                    ObjectFetchResult.failureFromOther(fetchResult);

                return ObjectFetchResult.success(registryData.setRegistryName(fetchResult.getValue()));
            }

            this.speciesRegistryDataAppliers.applyAll(jsonElement.getAsJsonObject(), registryData);
            return ObjectFetchResult.success(registryData);
        });

        this.appliers.register("common_leaves", LeavesProperties.class, TreeFamily::setCommonLeaves)
                .register("primitive_log", ResourceLocation.class, TreeFamily::setPrimitiveLogRegName)
                .register("primitive_stripped_log", ResourceLocation.class, TreeFamily::setPrimitiveStrippedLogRegName)
                .register("stick", ResourceLocation.class, TreeFamily::setStickRegName)
                .register("max_branch_radius", Integer.class, TreeFamily::setMaxBranchRadius)
                .register("conifer_variants", Boolean.class, TreeFamily::setHasConiferVariants)
                .register("generate_surface_root", Boolean.class, TreeFamily::setHasSurfaceRoot)
                .register("generate_stripped_branch", Boolean.class, TreeFamily::setHasStrippedBranch)
                .register("replace_species", Boolean.class, (treeFamily, replaceSpecies) -> {
                    this.speciesToRegister.removeAll(treeFamily.getSpecies().stream().map(Species::getRegistryName).collect(Collectors.toList()));

                    if (replaceSpecies)
                        treeFamily.resetSpecies();
                })
                .registerArrayApplier("species", SpeciesRegistryData.class, (treeFamily, registryData) -> {
                    final ResourceLocation registryName = registryData.registryName;

                    if (registryName == null)
                        return new PropertyApplierResult("Skipping registering species as registry name was not set.");

                    // If this species is registered to another tree family skip this.
                    if (speciesToRegister.contains(registryName) && isRegisteredToTree(treeFamily, registryName))
                        return new PropertyApplierResult("Skipping registering species for family '" + treeFamily.getRegistryName() + "' as registry name '" + registryData.registryName + "' was taken.");

                    speciesToRegister.add(registryData.registryName);

                    final Species species;

                    // Get the species or construct it using the correct species class for the SpeciesType given.
                    if (isRegisteredToTree(treeFamily, registryName))
                        species = treeFamily.getSpecies().stream().filter(currentSpecies -> registryName.equals(currentSpecies.getRegistryName())).findAny().orElse(Species.NULL_SPECIES);
                    else species = registryData.type.construct(registryData.registryName, treeFamily);

                    if (registryData.common)
                        treeFamily.setupCommonSpecies(species);
                    else treeFamily.addSpecies(species);

                    if (registryData.generateSeed)
                        species.generateSeed();
                    if (registryData.generateSapling)
                        species.generateSapling();

                    // TODO: Move leaves block creation to leaves properties Json.
                    if (registryData.leavesProperties != null)
                        species.setLeavesProperties(registryData.leavesProperties);

                    return PropertyApplierResult.SUCCESS;
                });
    }

    private boolean isRegisteredToTree (final TreeFamily treeFamily, final ResourceLocation registryName) {
        return treeFamily.getSpecies().stream().map(Species::getRegistryName).anyMatch(registryName::equals);
    }

    @Override
    protected void apply(Map<ResourceLocation, List<Pair<String, JsonElement>>> map, IResourceManager resourceManager, IProfiler profiler) {
        for (final Map.Entry<ResourceLocation, List<Pair<String, JsonElement>>> entry : map.entrySet()) {
            final ResourceLocation registryName = entry.getKey();
            final TreeFamily family = new TreeFamily(registryName);

            for (Pair<String, JsonElement> elementPair : entry.getValue()) {
                final String fileName = elementPair.getKey();
                final JsonElement jsonElement = elementPair.getValue();

                if (!jsonElement.isJsonObject()) {
                    LOGGER.warn("Skipping loading tree family data for {} from {} as its root element is not a Json object.", registryName, fileName);
                    return;
                }

                this.appliers.applyAll(jsonElement.getAsJsonObject(), family).forEach(failureResult -> LOGGER.warn("Error whilst loading tree family data for {} from {}: {}", registryName, fileName, failureResult.getErrorMessage()));
            }

            family.setupBlocks();
            TreeFamily.REGISTRY.register(family);
            LOGGER.debug("Loaded and registered tree family data: {}.", family.getDisplayString());
        }
    }

    @Override
    public void load(IResourceManager resourceManager) {
        this.apply(this.prepare(resourceManager, EmptyProfiler.INSTANCE), resourceManager, EmptyProfiler.INSTANCE);
    }
}
