package com.dtteam.dynamictrees.event;

import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

import java.util.List;

public class VoluntarySeedDropEvent extends Event implements ICancellableEvent {

    private final Level level;
    private final BlockPos rootPos;
    private final Species species;
    private final List<ItemStack> dropList;

    public VoluntarySeedDropEvent(Level level, BlockPos rootPos, Species species, List<ItemStack> dropList) {
        this.level = level;
        this.rootPos = rootPos;
        this.species = species;
        this.dropList = dropList;
    }

    public Level getLevel() {
        return level;
    }

    public BlockPos getRootPos() {
        return rootPos;
    }

    public Species getSpecies() {
        return species;
    }

    public List<ItemStack> getDropList() {
        return dropList;
    }

}
