package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.api.datapacks.IJsonApplierManager;
import com.ferreusveritas.dynamictrees.api.datapacks.PropertyApplierResult;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.resources.MultiJsonReloadListener;
import com.ferreusveritas.dynamictrees.util.json.JsonObjectGetters;
import com.ferreusveritas.dynamictrees.util.json.JsonPropertyApplierList;
import com.ferreusveritas.dynamictrees.util.json.ObjectFetchResult;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manages loading tree families from the <tt>trees</tt> folder.
 *
 * @author Harley O'Connor
 */
public final class TreeFamilyManager extends MultiJsonReloadListener implements IJsonApplierManager {

    public static final class SpeciesRegistryData {
        private SpeciesType<Species> type = TreeSpecies.TREE_SPECIES;
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
                .register("type", SpeciesType.class, SpeciesRegistryData::setType)
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
                .register("primitive_log", Block.class, TreeFamily::setPrimitiveLog)
                .register("primitive_stripped_log", Block.class, TreeFamily::setPrimitiveStrippedLog)
                .register("sticks", Item.class, TreeFamily::setStick)
                .register("max_branch_radius", Integer.class, TreeFamily::setMaxBranchRadius)
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
                    if (!isRegisteredToTree(treeFamily, registryName))
                        species = treeFamily.getSpecies().stream().filter(currentSpecies -> registryName.equals(currentSpecies.getRegistryName())).findAny().orElse(Species.NULL_SPECIES);
                    else species = registryData.type.construct(registryData.registryName, treeFamily);

                    if (registryData.common)
                        treeFamily.setCommonSpecies(species);
                    else treeFamily.addSpecies(species);

                    if (registryData.generateSeed)
                        species.generateSeed();
                    if (registryData.generateSapling)
                        species.generateSapling();

                    return PropertyApplierResult.SUCCESS;
                });
    }

    private boolean isRegisteredToTree (final TreeFamily treeFamily, final ResourceLocation registryName) {
        return treeFamily.getSpecies().stream().map(Species::getRegistryName).anyMatch(registryName::equals);
    }

    @Override
    protected void apply(Map<ResourceLocation, Map<String, JsonElement>> map, IResourceManager resourceManager, IProfiler profiler) {
        for (final Map.Entry<ResourceLocation, Map<String, JsonElement>> entry : map.entrySet()) {

        }
    }

}
