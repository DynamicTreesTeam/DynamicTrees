package com.ferreusveritas.dynamictrees.systems.dropcreators.context;

import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * Defines context variables about a specific drop.
 *
 * @author Harley O'Connor
 */
public class DropContext {

    private final World world;
    private final Random random;
    private final BlockPos pos;

    private final Species species;
    private final List<ItemStack> dropList;

    private final int soilLife;
    private final int fortune;

    public DropContext(@Nullable World world, BlockPos pos, Species species, List<ItemStack> dropList) {
        this(world, pos, species, dropList, -1, 0);
    }

    public DropContext(@Nullable World world, BlockPos pos, Species species, List<ItemStack> dropList, int soilLife, int fortune) {
        this.world = world;
        this.random = world == null ? new Random() : world.random;
        this.pos = pos;
        this.species = species;
        this.dropList = dropList;
        this.soilLife = soilLife;
        this.fortune = fortune;
    }

    public World world() {
        return world;
    }

    public Random random() {
        return this.random;
    }

    public BlockPos pos() {
        return pos;
    }

    public Species species() {
        return species;
    }

    public List<ItemStack> drops() {
        return dropList;
    }

    /**
     * Returns the soil life of the relevant tree, or {@code -1} if it
     * was not available.
     *
     * @return The soil life of the related tree, or {@code -1} if it
     *         was unavailable.
     */
    public int soilLife() {
        return soilLife;
    }

    public int fortune() {
        return fortune;
    }

}
