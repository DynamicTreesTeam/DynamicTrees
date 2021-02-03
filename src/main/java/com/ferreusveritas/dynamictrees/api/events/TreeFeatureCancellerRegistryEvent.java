package com.ferreusveritas.dynamictrees.api.events;

import com.ferreusveritas.dynamictrees.api.worldgen.ITreeFeatureCanceller;

import java.util.List;

/**
 * Addons can use this to register custom tree feature cancellers.
 *
 * @author Harley O'Connor
 */
public class TreeFeatureCancellerRegistryEvent {

    private final List<ITreeFeatureCanceller> featureCancellers;

    public TreeFeatureCancellerRegistryEvent(List<ITreeFeatureCanceller> featureCancellers) {
        this.featureCancellers = featureCancellers;
    }

    public List<ITreeFeatureCanceller> getFeatureCancellers() {
        return featureCancellers;
    }

}
