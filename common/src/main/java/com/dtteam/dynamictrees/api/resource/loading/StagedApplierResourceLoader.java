package com.dtteam.dynamictrees.api.resource.loading;

import com.dtteam.dynamictrees.api.resource.loading.preparation.ResourcePreparer;
import com.dtteam.dynamictrees.deserialization.PropertyAppliers;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.treepack.Resources;

import java.util.function.Function;

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
     * Appliers that should only be applied on GatherDataEvent or equivalent.
     */
    protected final PropertyAppliers<R, I> gatherDataAppliers;

    /**
     * Appliers that should only be applied on FMLCommonSetupEvent or equivalent.
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
    public void registerAppliers() {
        Services.EVENT.postApplierEvent(ApplierStage.LOAD, this.loadAppliers, this.appliersIdentifier);
        Services.EVENT.postApplierEvent(ApplierStage.GATHER_DATA, this.gatherDataAppliers, this.appliersIdentifier);
        Services.EVENT.postApplierEvent(ApplierStage.SETUP, this.setupAppliers, this.appliersIdentifier);
        Services.EVENT.postApplierEvent(ApplierStage.RELOAD, this.reloadAppliers, this.appliersIdentifier);
        Services.EVENT.postApplierEvent(ApplierStage.COMMON, this.commonAppliers, this.appliersIdentifier);
    }

    public enum ApplierStage {
        LOAD, GATHER_DATA, SETUP, RELOAD, COMMON
    }

}
