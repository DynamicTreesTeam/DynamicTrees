package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.api.worldgen.PoissonDiscProvider;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.world.WorldEvent;

public class PoissonDiscProviderCreateEvent extends WorldEvent {

    private PoissonDiscProvider poissonDiscProvider;

    public PoissonDiscProviderCreateEvent(LevelAccessor world, PoissonDiscProvider poissonDiscProvider) {
        super(world);
        this.poissonDiscProvider = poissonDiscProvider;
    }

    public void setPoissonDiscProvider(PoissonDiscProvider poissonDiscProvider) {
        this.poissonDiscProvider = poissonDiscProvider;
    }

    public PoissonDiscProvider getPoissonDiscProvider() {
        return poissonDiscProvider;
    }

}
