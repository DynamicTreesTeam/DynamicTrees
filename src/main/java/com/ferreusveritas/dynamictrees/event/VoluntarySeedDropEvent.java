package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

@Cancelable
public class VoluntarySeedDropEvent extends Event {

    private final Level world;
    private final BlockPos rootPos;
    private final Species species;
    private final List<ItemStack> dropList;

    public VoluntarySeedDropEvent(Level world, BlockPos rootPos, Species species, List<ItemStack> dropList) {
        this.world = world;
        this.rootPos = rootPos;
        this.species = species;
        this.dropList = dropList;
    }

    public Level getWorld() {
        return world;
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
