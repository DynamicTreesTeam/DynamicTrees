package com.ferreusveritas.dynamictrees.api.events;

import com.ferreusveritas.dynamictrees.api.worldgen.IBiomeDatabasePopulator;
import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.List;

public class BiomeDatabasePopulatorRegistryEvent extends Event {

    private final List<IBiomeDatabasePopulator> biomePopulators = new ArrayList<>();

    public void register(IBiomeDatabasePopulator populator) {
        this.biomePopulators.add(populator);
    }

    public void replaceAll(IBiomeDatabasePopulator populator) {
        this.biomePopulators.clear();
        this.biomePopulators.add(populator);
    }

    public IBiomeDatabasePopulator getPopulator() {
        return biomeDataBase -> this.biomePopulators.forEach(p -> p.populate(biomeDataBase));
    }

}
