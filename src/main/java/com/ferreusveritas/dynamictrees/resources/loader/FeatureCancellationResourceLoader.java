package com.ferreusveritas.dynamictrees.resources.loader;

import com.ferreusveritas.dynamictrees.api.applier.ApplierRegistryEvent;
import com.ferreusveritas.dynamictrees.api.applier.PropertyApplierResult;
import com.ferreusveritas.dynamictrees.api.resource.ResourceAccessor;
import com.ferreusveritas.dynamictrees.api.resource.loading.AbstractResourceLoader;
import com.ferreusveritas.dynamictrees.api.resource.loading.ApplierResourceLoader;
import com.ferreusveritas.dynamictrees.api.resource.loading.preparation.MultiJsonResourcePreparer;
import com.ferreusveritas.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import com.ferreusveritas.dynamictrees.deserialisation.DeserialisationException;
import com.ferreusveritas.dynamictrees.deserialisation.JsonPropertyAppliers;
import com.ferreusveritas.dynamictrees.deserialisation.result.JsonResult;
import com.ferreusveritas.dynamictrees.util.IgnoreThrowable;
import com.ferreusveritas.dynamictrees.util.JsonMapWrapper;
import com.ferreusveritas.dynamictrees.util.holderset.DTBiomeHolderSet;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDatabase;
import com.ferreusveritas.dynamictrees.worldgen.BiomeDatabases;
import com.ferreusveritas.dynamictrees.worldgen.FeatureCancellationRegistry;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

import static com.ferreusveritas.dynamictrees.deserialisation.JsonHelper.throwIfShouldNotLoad;

/**
 * @author Harley O'Connor
 */
public class FeatureCancellationResourceLoader extends AbstractResourceLoader<Iterable<JsonElement>>
        implements ApplierResourceLoader<Iterable<JsonElement>> {

    private static final MultiJsonResourcePreparer RESOURCE_PREPARER = new MultiJsonResourcePreparer("world_gen");

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String CANCELLATION_FILE = "feature_cancellers";

    public static final String CANCELLATION_APPLIERS = "cancellations";
    public static final String CANCELLERS = "cancellers";

    static boolean isCancellationFile(final ResourceLocation key) {
        return key.getPath().equals(CANCELLATION_FILE);
    }

    private final JsonPropertyAppliers<BiomePropertySelectors.FeatureCancellation> cancellationAppliers = new JsonPropertyAppliers<>(BiomePropertySelectors.FeatureCancellation.class);

    public FeatureCancellationResourceLoader() {
        super(RESOURCE_PREPARER);
    }

    @Override
    public void registerAppliers() {
        this.cancellationAppliers
                .register("namespace", String.class, BiomePropertySelectors.FeatureCancellation::cancelWithNamespace)
                .registerArrayApplier("namespaces", String.class, BiomePropertySelectors.FeatureCancellation::cancelWithNamespace)
                .register("type", FeatureCanceller.class, BiomePropertySelectors.FeatureCancellation::cancelUsing)
                .registerArrayApplier("types", FeatureCanceller.class, BiomePropertySelectors.FeatureCancellation::cancelUsing)
                .register("stage", GenerationStep.Decoration.class, BiomePropertySelectors.FeatureCancellation::cancelDuring)
                .registerArrayApplier("stages", GenerationStep.Decoration.class, BiomePropertySelectors.FeatureCancellation::cancelDuring);

        ApplierResourceLoader.postApplierEvent(new CancellationApplierRegistryEvent<>(this.cancellationAppliers, CANCELLATION_APPLIERS));
    }

    public static final class CancellationApplierRegistryEvent<O> extends ApplierRegistryEvent<O, JsonElement> {
        public CancellationApplierRegistryEvent(JsonPropertyAppliers<O> appliers, String identifier) {
            super(appliers, identifier);
        }
    }

    @Override
    public void applyOnSetup(ResourceAccessor<Iterable<JsonElement>> resourceAccessor,
                             ResourceManager resourceManager) {
        BiomeDatabases.reset();
//        if (BiomePopulatorsResourceLoader.isWorldGenDisabled()) {
//            return;
//        }
        this.readCancellers(
                resourceAccessor.filtered(FeatureCancellationResourceLoader::isCancellationFile).map(BiomePopulatorsResourceLoader::toLinkedList)
        );
    }

    private void readCancellers(ResourceAccessor<Deque<JsonElement>> defaultPopulators) {
        this.readModCancellers(defaultPopulators);
        this.readTreePackCancellers(defaultPopulators);
    }

    private void readModCancellers(ResourceAccessor<Deque<JsonElement>> defaultPopulators) {
        defaultPopulators.getAllResources().forEach(defaultPopulator ->
                this.readCancellers(defaultPopulator.getLocation(), defaultPopulator.getResource().pollFirst())
        );
    }

    private void readTreePackCancellers(ResourceAccessor<Deque<JsonElement>> defaultPopulators) {
        defaultPopulators.getAllResources().forEach(defaultPopulator ->
                defaultPopulator.getResource().forEach(json ->
                        this.readCancellers(defaultPopulator.getLocation(), json)
                )
        );
    }

    private void readCancellers(final ResourceLocation location, final JsonElement json) {
        LOGGER.debug("Reading cancellers from Json biome populator \"{}\".", location);

        try {
            JsonResult.forInput(json)
                    .mapEachIfArray(JsonObject.class, object -> {
                        try {
                            this.readCancellersInSection(location, object);
                        } catch (IgnoreThrowable ignored) {
                        }
                        return PropertyApplierResult.success();
                    }).forEachWarning(warning ->
                            LOGGER.warn("Warning whilst loading cancellers from populator \"{}\": {}",
                                    location, warning)
                    ).orElseThrow();
        } catch (DeserialisationException e) {
            LOGGER.error("Error whilst loading cancellers from populator \"{}\": {}", location,
                    e.getMessage());
        }
    }

    private void readCancellersInSection(final ResourceLocation location, final JsonObject json)
            throws DeserialisationException, IgnoreThrowable {

        final Consumer<String> errorConsumer = error -> LOGGER.error("Error loading populator \"{}\": {}",
                location, error);
        final Consumer<String> warningConsumer = warning -> LOGGER.warn("Warning whilst loading populator " +
                "\"{}\": {}", location, warning);

        throwIfShouldNotLoad(json);

        if (!json.has("cancellers")) {
            return;
        }

        final DTBiomeHolderSet biomes = BiomePopulatorsResourceLoader.collectBiomes(json, warningConsumer);

//        if (biomes.getList().isEmpty()) {
//            BiomePopulatorsResourceLoader.warnNoBiomesSelected(json);
//            return;
//        }

        JsonResult.forInput(json)
                .mapIfContains(CANCELLERS, JsonObject.class, cancellerObject ->
                                this.applyCanceller(location, errorConsumer, warningConsumer,
                                        biomes, cancellerObject),
                        PropertyApplierResult.success()
                )
                .forEachWarning(warningConsumer)
                .orElseThrow();
    }


    private PropertyApplierResult applyCanceller(ResourceLocation location,
                                                 Consumer<String> errorConsumer,
                                                 Consumer<String> warningConsumer, DTBiomeHolderSet biomes,
                                                 JsonObject json) {
        var cancellation = new BiomePropertySelectors.NormalFeatureCancellation();
        this.applyCancellationAppliers(location, json, cancellation);
        cancellation.cancelDuringDefaultIfNoneSpecified();

        final BiomeDatabase.Operation operation = JsonResult.forInput(json)
                .mapIfContains(BiomePopulatorsResourceLoader.METHOD, BiomeDatabase.Operation.class, op -> op, BiomeDatabase.Operation.SPLICE_AFTER)
                .forEachWarning(warningConsumer)
                .orElse(BiomeDatabase.Operation.SPLICE_AFTER, errorConsumer, warningConsumer);

//        if (operation == BiomeDatabase.Operation.REPLACE) {
//            this.replaceCancellationsWith(cancellation, biomes.getList());
//        } else {
//            var list = biomes.getList();
//            this.addCancellationsTo(cancellation, list);
//        }
        FeatureCancellationRegistry.addCancellations(biomes, operation, cancellation);
        return PropertyApplierResult.success();
    }

    private void applyCancellationAppliers(ResourceLocation location, JsonObject json, BiomePropertySelectors.FeatureCancellation cancellation) {
        this.cancellationAppliers.applyAll(new JsonMapWrapper(json), cancellation)
                .forEachErrorWarning(
                        error -> LOGGER.error("Error whilst applying feature cancellations " +
                                "in \"{}\" " + "populator: {}", location, error),
                        warning -> LOGGER.warn("Warning whilst applying feature " +
                                "cancellations in \"{}\" populator: {}", location, warning)
                );
    }

    private void replaceCancellationsWith(BiomePropertySelectors.NormalFeatureCancellation cancellation, List<Holder<Biome>> biomes) {
        biomes.forEach(biome -> {
            var currentCancellations = BiomeDatabases.getDefault().getEntry(biome).getOrCreateFeatureCancellation();
            currentCancellations.replaceFrom(cancellation);
        });
    }

    private void addCancellationsTo(BiomePropertySelectors.NormalFeatureCancellation cancellation, List<Holder<Biome>> biomes) {
        biomes.forEach(biome -> {
            var currentCancellation = BiomeDatabases.getDefault().getEntry(biome).getOrCreateFeatureCancellation();
            currentCancellation.addFrom(cancellation);
        });
    }

}
