package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.api.worldgen.PoissonDiscProvider;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.level.LevelEvent;

public class PoissonDiscProviderCreateEvent extends LevelEvent {

    private PoissonDiscProvider poissonDiscProvider;

    public PoissonDiscProviderCreateEvent(LevelAccessor level, PoissonDiscProvider poissonDiscProvider) {
        super(level);
        this.poissonDiscProvider = poissonDiscProvider;
    }

    public void setPoissonDiscProvider(PoissonDiscProvider poissonDiscProvider) {
        this.poissonDiscProvider = poissonDiscProvider;
    }

    public PoissonDiscProvider getPoissonDiscProvider() {
        return poissonDiscProvider;
    }

}
