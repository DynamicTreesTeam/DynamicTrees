package com.ferreusveritas.dynamictrees.systems.substances;

import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * Holds lingering {@link ISubstanceEffect} classes and a {@link Supplier} to instantiate them with.
 * Stored in a {@link LinkedHashMap} so that {@link com.ferreusveritas.dynamictrees.entities.LingeringEffectorEntity}
 * can send the relevant substance index in a packet.
 *
 * @author Harley O'Connor
 */
@SuppressWarnings("unchecked")
public final class LingeringSubstances {

    public static final LinkedHashMap<Class<ISubstanceEffect>, Supplier<ISubstanceEffect>> LINGERING_SUBSTANCES = new LinkedHashMap<>();

    public static <S extends ISubstanceEffect> void registerLingeringSubstance(final Class<S> substanceClass, Supplier<S> substanceSupplier) {
        LINGERING_SUBSTANCES.put((Class<ISubstanceEffect>) substanceClass, (Supplier<ISubstanceEffect>) substanceSupplier);
    }

    public static <S extends ISubstanceEffect> int indexOf(final Class<S> substanceClass) {
        return new ArrayList<>(LINGERING_SUBSTANCES.keySet()).indexOf(substanceClass);
    }

    public static <S extends ISubstanceEffect> Supplier<S> fromIndex(final int index) {
        return (Supplier<S>) new ArrayList<>(LINGERING_SUBSTANCES.values()).get(index);
    }

    static {
        // It's important that the order in which these are added is equal on the client and server.
        registerLingeringSubstance(GrowthSubstance.class, GrowthSubstance::new);
        registerLingeringSubstance(HarvestSubstance.class, HarvestSubstance::new);
    }

}
