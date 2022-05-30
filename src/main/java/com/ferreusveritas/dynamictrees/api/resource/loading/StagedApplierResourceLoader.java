package com.ferreusveritas.dynamictrees.api.resource.loading;

import com.ferreusveritas.dynamictrees.api.resource.loading.preparation.ResourcePreparer;
import com.ferreusveritas.dynamictrees.api.treepacks.ApplierRegistryEvent;
import com.ferreusveritas.dynamictrees.deserialisation.PropertyAppliers;
import com.ferreusveritas.dynamictrees.resources.Resources;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import java.util.function.Function;

import static com.ferreusveritas.dynamictrees.api.resource.loading.ApplierResourceLoader.postApplierEvent;

/**
 * @author Harley O'Connor
 */
public abstract class StagedApplierResourceLoader<I, R> extends AbstractResourceLoader<I>
        implements ApplierResourceLoader<I> {

    /**
     * Appliers that should only be applied when loading.
     */
    protected final PropertyAppliers<R, I> loadAppliers;

    /**
     * Appliers that should only be applied on {@link GatherDataEvent}.
     */
    protected final PropertyAppliers<R, I> gatherDataAppliers;

    /**
     * Appliers that should only be applied on {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent}.
     */
    protected final PropertyAppliers<R, I> setupAppliers;

    /**
     * Holds appliers that should only be applied when reloading.
     */
    protected final PropertyAppliers<R, I> reloadAppliers;

    /**
     * Appliers that should be applied both when loading and reloading.
     */
    protected final PropertyAppliers<R, I> commonAppliers;

    protected final String appliersIdentifier;

    public StagedApplierResourceLoader(ResourcePreparer<I> resourcePreparer, Class<R> resourceType,
                                       Function<Class<R>, PropertyAppliers<R, I>> appliersConstructor,
                                       String appliersIdentifier) {
        super(resourcePreparer);
        this.loadAppliers = appliersConstructor.apply(resourceType);
        this.gatherDataAppliers = appliersConstructor.apply(resourceType);
        this.setupAppliers = appliersConstructor.apply(resourceType);
        this.reloadAppliers = appliersConstructor.apply(resourceType);
        this.commonAppliers = appliersConstructor.apply(resourceType);
        this.appliersIdentifier = appliersIdentifier;
    }

    /**
     * Called from {@link Resources#setupTreesResourceManager()}. Sub-classes should can override to register
     * their Json appliers, and should call super so their events are posted properly.
     */
    @Override
    public void registerAppliers() {
        postApplierEvent(new ApplierRegistryEvent.Load<>(this.loadAppliers, this.appliersIdentifier));
        postApplierEvent(new ApplierRegistryEvent.GatherData<>(this.gatherDataAppliers, this.appliersIdentifier));
        postApplierEvent(new ApplierRegistryEvent.Setup<>(this.setupAppliers, this.appliersIdentifier));
        postApplierEvent(new ApplierRegistryEvent.Reload<>(this.reloadAppliers, this.appliersIdentifier));
        postApplierEvent(new ApplierRegistryEvent.Common<>(this.commonAppliers, this.appliersIdentifier));
    }

}
