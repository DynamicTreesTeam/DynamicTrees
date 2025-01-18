package com.dtteam.dynamictrees.treepack.loader;

import com.dtteam.dynamictrees.api.resource.ResourceAccessor;
import com.dtteam.dynamictrees.api.resource.loading.AbstractResourceLoader;
import com.dtteam.dynamictrees.api.resource.loading.ApplierResourceLoader;
import com.dtteam.dynamictrees.api.resource.loading.preparation.MultiJsonResourcePreparer;
import com.dtteam.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.dtteam.dynamictrees.api.worldgen.FeatureCanceller;
import com.dtteam.dynamictrees.deserialization.DeserializationException;
import com.dtteam.dynamictrees.deserialization.JsonPropertyAppliers;
import com.dtteam.dynamictrees.deserialization.applier.PropertyApplierResult;
import com.dtteam.dynamictrees.deserialization.result.JsonResult;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.util.IgnoreThrowable;
import com.dtteam.dynamictrees.util.JsonMapWrapper;
import com.dtteam.dynamictrees.worldgen.BiomeDatabase;
import com.dtteam.dynamictrees.worldgen.BiomeDatabases;
import com.dtteam.dynamictrees.worldgen.IDTBiomeHolderSet;
import com.dtteam.dynamictrees.worldgen.featurecancellation.FeatureCancellationRegistry;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.levelgen.GenerationStep;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Deque;
import java.util.function.Consumer;

import static com.dtteam.dynamictrees.deserialization.JsonHelper.throwIfShouldNotLoad;

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

        Services.EVENT.postCancellationApplierEvent(this.cancellationAppliers, CANCELLATION_APPLIERS);
    }

    @Override
    public void applyOnReload(ResourceAccessor<Iterable<JsonElement>> resourceAccessor, ResourceManager resourceManager) {
        BiomeDatabases.reset();
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
                this.readCancellers(defaultPopulator.location(), defaultPopulator.resource().pollFirst())
        );
    }

    private void readTreePackCancellers(ResourceAccessor<Deque<JsonElement>> defaultPopulators) {
        defaultPopulators.getAllResources().forEach(defaultPopulator ->
                defaultPopulator.resource().forEach(json ->
                        this.readCancellers(defaultPopulator.location(), json)
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
        } catch (DeserializationException e) {
            LOGGER.error("Error whilst loading cancellers from populator \"{}\": {}", location,
                    e.getMessage());
        }
    }

    private void readCancellersInSection(final ResourceLocation location, final JsonObject json)
            throws DeserializationException, IgnoreThrowable {

        final Consumer<String> errorConsumer = error -> LOGGER.error("Error loading populator \"{}\": {}",
                location, error);
        final Consumer<String> warningConsumer = warning -> LOGGER.warn("Warning whilst loading populator " +
                "\"{}\": {}", location, warning);

        throwIfShouldNotLoad(json);

        if (!json.has("cancellers")) {
            return;
        }

        final IDTBiomeHolderSet biomes = BiomePopulatorsResourceLoader.collectBiomes(json, warningConsumer);

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
                                                 Consumer<String> warningConsumer, IDTBiomeHolderSet biomes,
                                                 JsonObject json) {
        var cancellation = new BiomePropertySelectors.NormalFeatureCancellation();
        this.applyCancellationAppliers(location, json, cancellation);
        cancellation.cancelDuringDefaultIfNoneSpecified();

        final BiomeDatabase.Operation operation = JsonResult.forInput(json)
                .mapIfContains(BiomePopulatorsResourceLoader.METHOD, BiomeDatabase.Operation.class, op -> op, BiomeDatabase.Operation.SPLICE_AFTER)
                .forEachWarning(warningConsumer)
                .orElse(BiomeDatabase.Operation.SPLICE_AFTER, errorConsumer, warningConsumer);

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

}
