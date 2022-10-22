package com.ferreusveritas.dynamictrees.systems.dropcreators.context;

import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * Defines context variables about a specific drop.
 *
 * @author Harley O'Connor
 */
public class DropContext {

    private final Level world;
    private final Random random;
    private final BlockPos pos;

    private final Species species;
    private final List<ItemStack> dropList;

    private final ItemStack tool;
    private final int fertility;
    private final int fortune;

    public DropContext(@Nullable Level world, BlockPos pos, Species species, List<ItemStack> dropList) {
        this(world, pos, species, dropList, ItemStack.EMPTY, -1, 0);
    }

    public DropContext(Level world, Random random, BlockPos pos, Species species, List<ItemStack> dropList, int fertility, int fortune) {
        this(world, pos, species, dropList, ItemStack.EMPTY, fertility, fortune);
    }

    public DropContext(@Nullable Level world, BlockPos pos, Species species, List<ItemStack> dropList, ItemStack tool, int fertility, int fortune) {
        this.world = world;
        this.random = world == null ? new Random() : world.random;
        this.pos = pos;
        this.species = species;
        this.dropList = dropList;
        this.tool = tool;
        this.fertility = fertility;
        this.fortune = fortune;
    }

    public Level world() {
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

    public ItemStack tool() {
        return tool;
    }

    /**
     * Returns the fertility of the relevant tree, or {@code -1} if it was not available.
     *
     * @return The fertility of the related tree, or {@code -1} if it was unavailable.
     */
    public int fertility() {
        return fertility;
    }

    public int fortune() {
        return fortune;
    }

}
