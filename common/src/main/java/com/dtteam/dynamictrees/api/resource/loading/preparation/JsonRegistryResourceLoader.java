package com.dtteam.dynamictrees.api.resource.loading.preparation;

import com.dtteam.dynamictrees.api.registry.RegistryEntry;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.api.resource.DTResource;
import com.dtteam.dynamictrees.api.resource.ResourceAccessor;
import com.dtteam.dynamictrees.api.resource.loading.ApplicationException;
import com.dtteam.dynamictrees.api.resource.loading.StagedApplierResourceLoader;
import com.dtteam.dynamictrees.deserialization.JsonHelper;
import com.dtteam.dynamictrees.deserialization.JsonPropertyAppliers;
import com.dtteam.dynamictrees.treepack.Resettable;
import com.dtteam.dynamictrees.util.IgnoreThrowable;
import com.dtteam.dynamictrees.util.JsonMapWrapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.dtteam.dynamictrees.deserialization.JsonHelper.throwIfNotJsonObject;
import static com.dtteam.dynamictrees.deserialization.JsonHelper.throwIfShouldNotLoad;

/**
 * @author Harley O'Connor
 */
public abstract class JsonRegistryResourceLoader<R extends RegistryEntry<R> & Resettable<R>> extends
        StagedApplierResourceLoader<JsonElement, R> {

    public static final String FRUITS = "fruits";
    public static final String PODS = "pods";
    public static final String SPECIES = "species";
    public static final String FAMILY = "family";
    public static final String LEAVES_PROPERTIES = "leaves_properties";
    public static final String SOIL_PROPERTIES = "soil_properties";

    private static final Logger LOGGER = LogManager.getLogger();

    private final TypedRegistry<R> registry;
    private final String registryName;

    public JsonRegistryResourceLoader(TypedRegistry<R> registry, String folderName) {
        this(registry, folderName, folderName);
    }

    public JsonRegistryResourceLoader(TypedRegistry<R> registry, String folderName, String appliersIdentifier) {
        super(new JsonResourcePreparer(folderName), registry.getType(), JsonPropertyAppliers::new, appliersIdentifier);
        this.registry = registry;
        this.registryName = registry.getName();
    }

    //////////////////////////////
    // LOAD
    //////////////////////////////

    @Override
    public void applyOnLoad(ResourceAccessor<JsonElement> resourceAccessor, ResourceManager resourceManager) {
        resourceAccessor.forEach(resource -> {
            try {
                final JsonObject object = this.prepareJson(resource);
                final LoadData loadData = this.loadResourceOnLoad(resource.location(), object);
                this.postLoadOnLoad(loadData, object);
            } catch (ApplicationException e) {
                this.logException(resource.location(), e);
            } catch (IgnoreThrowable ignored) {}
        });
    }

    protected LoadData loadResourceOnLoad(ResourceLocation name, JsonObject json) throws IgnoreThrowable {
        final LoadData loadData = new LoadData(this.loadNewResource(name, json));
        this.applyLoadAppliers(loadData, json);
        return loadData;
    }

    protected void postLoadOnLoad(LoadData loadData, JsonObject json) {
        this.applyCommonAppliers(loadData, json);
        this.postLoad(loadData);
    }

    protected void applyLoadAppliers(LoadData loadData, JsonObject json) {
        final ResourceLocation resourceName = loadData.getResourceName();
        this.loadAppliers.applyAll(new JsonMapWrapper(json), loadData.resource)
                .forEachError(error -> this.logError(resourceName, error))
                .forEachWarning(warning -> this.logWarning(resourceName, warning));
    }

    //////////////////////////////
    // GATHER DATA
    //////////////////////////////

    @Override
    public void applyOnGatherData(ResourceAccessor<JsonElement> resourceAccessor,
                                  ResourceManager resourceManager) {
        resourceAccessor.forEach(resource -> {
            try {
                final JsonObject object = this.prepareJson(resource);
                final LoadData loadData = this.loadResource(resource.location(), object);
                this.postLoadOnGatherData(loadData, object);
            } catch (ApplicationException e) {
                this.logException(resource.location(), e);
            } catch (IgnoreThrowable ignored) {}
        });
    }

    private void postLoadOnGatherData(LoadData loadData, JsonObject json) {
        this.applyGatherDataAppliers(loadData, json);
        loadData.resource.setGenerateData(
                JsonHelper.getOrDefault(json, "generate_data", Boolean.class, true)
        );
        this.postLoad(loadData);
    }

    protected void applyGatherDataAppliers(LoadData loadData, JsonObject json) {
        final ResourceLocation resourceName = loadData.getResourceName();
        this.gatherDataAppliers.applyAll(new JsonMapWrapper(json), loadData.resource)
                .forEachError(error -> this.logError(resourceName, error))
                .forEachWarning(warning -> this.logWarning(resourceName, warning));
    }

    //////////////////////////////
    // SETUP
    //////////////////////////////

    @Override
    public void applyOnSetup(ResourceAccessor<JsonElement> resourceAccessor, ResourceManager resourceManager) {
        resourceAccessor.forEach(resource -> {
            try {
                final JsonObject object = this.prepareJson(resource);
                final LoadData loadData = this.loadResourceOnSetup(resource.location());
                this.applySetupAppliers(object, loadData);
            } catch (ApplicationException e) {
                this.logException(resource.location(), e);
            } catch (IgnoreThrowable ignored) {}
        });
    }

    private LoadData loadResourceOnSetup(ResourceLocation name) throws IgnoreThrowable {
        final LoadData loadData = new LoadData();
        loadData.wasAlreadyRegistered = this.registry.has(name);
        if (!loadData.wasAlreadyRegistered) {
            throw IgnoreThrowable.INSTANCE;
        }
        loadData.resource = this.registry.get(name);
        return loadData;
    }

    protected void applySetupAppliers(JsonObject json, LoadData loadData) {
        final ResourceLocation resourceName = loadData.getResourceName();
        this.setupAppliers.applyAll(new JsonMapWrapper(json), loadData.resource)
                .forEachError(error -> this.logError(resourceName, error))
                .forEachWarning(warning -> this.logWarning(resourceName, warning));
    }

    //////////////////////////////
    // RELOAD
    //////////////////////////////

    @Override
    public void applyOnReload(ResourceAccessor<JsonElement> resourceAccessor, ResourceManager resourceManager) {
        this.registry.unlock();
        resourceAccessor.forEach(resource -> {
            try {
                final JsonObject object = this.prepareJson(resource);
                final LoadData loadData = this.loadResourceOnReload(resource.location(), object);
                this.postLoadOnReload(loadData, object);
            } catch (ApplicationException e) {
                this.logException(resource.location(), e);
            } catch (IgnoreThrowable ignored) {}
        });
        this.registry.lock();
    }

    private LoadData loadResourceOnReload(ResourceLocation name, JsonObject json) throws IgnoreThrowable {
        final LoadData loadData = this.loadResource(name, json);
        if (loadData.wasAlreadyRegistered) {
            loadData.resource.reset().setPreReloadDefaults();
        } else {
            loadData.resource.setPreReloadDefaults();
        }
        return loadData;
    }

    protected void postLoadOnReload(LoadData loadData, JsonObject json) {
        this.applyReloadAppliers(loadData, json);
        this.applyCommonAppliers(loadData, json);
        loadData.resource.setPostReloadDefaults();
        this.postLoad(loadData);
    }

    private void applyReloadAppliers(LoadData loadData, JsonObject json) {
        final ResourceLocation resourceName = loadData.getResourceName();
        this.reloadAppliers.applyAll(new JsonMapWrapper(json), loadData.resource)
                .forEachError(error -> this.logError(resourceName, error))
                .forEachWarning(warning -> this.logWarning(resourceName, warning));
    }

    //////////////////////////////
    // COMMON
    //////////////////////////////

    private JsonObject prepareJson(DTResource<JsonElement> resource)
            throws ApplicationException, IgnoreThrowable {
        throwIfNotJsonObject(resource.resource(),
                () -> new ApplicationException("Root element is not a Json object."));
        final JsonObject object = TypedRegistry.putJsonRegistryName(resource.resource().getAsJsonObject(),
                resource.location());
        throwIfShouldNotLoad(object);
        return object;
    }

    private LoadData loadResource(ResourceLocation name, JsonObject json) throws IgnoreThrowable {
        final LoadData loadData = new LoadData();
        loadData.wasAlreadyRegistered = this.registry.has(name);

        if (loadData.wasAlreadyRegistered) {
            loadData.resource = this.registry.get(name);
        } else {
            loadData.resource = this.loadNewResource(name, json);
        }
        return loadData;
    }

    private R loadNewResource(ResourceLocation name, JsonObject json) throws IgnoreThrowable {
        final R resource = this.registry.getType(json, name).decode(json);
        // Stop loading this entry (error should have been logged already).
        if (resource == null) {
            throw IgnoreThrowable.INSTANCE;
        }
        return resource;
    }

    private void postLoad(LoadData loadData) {
        if (loadData.wasAlreadyRegistered) {
            LOGGER.debug("Loaded type \"{}\" data: {}.", this.registryName, loadData.resource.toReloadDataString());
        } else {
            this.registry.register(loadData.resource);
            LOGGER.debug("Loaded and registered type \"{}\": {}.", this.registryName, loadData.resource.toLoadDataString());
        }
    }

    private void logException(ResourceLocation name, ApplicationException e) {
        LOGGER.error("Error whilst loading type \"" + this.registryName + "\" with name \"" + name + "\".", e);
    }

    protected void applyCommonAppliers(LoadData loadData, JsonObject json) {
        final ResourceLocation resourceName = loadData.getResourceName();
        this.commonAppliers.applyAll(new JsonMapWrapper(json), loadData.resource)
                .forEachError(error -> this.logError(resourceName, error))
                .forEachWarning(warning -> this.logWarning(resourceName, warning));
    }

    protected void logError(ResourceLocation name, String error) {
        LOGGER.error("Error whilst loading type \"" + this.registryName + "\" with name \"" + name + "\": {}", error);
    }

    protected void logWarning(ResourceLocation name, String warning) {
        LOGGER.warn("Warning whilst loading type \"" + this.registryName + "\" with name \"" + name + "\": {}", warning);
    }

    public class LoadData {
        private R resource;
        private boolean wasAlreadyRegistered;

        public LoadData() {
        }

        public LoadData(R resource) {
            this.resource = resource;
        }

        public ResourceLocation getResourceName() {
            return this.resource.getRegistryName();
        }

        public R getResource() {
            return resource;
        }

        public boolean wasAlreadyRegistered() {
            return wasAlreadyRegistered;
        }
    }

}
