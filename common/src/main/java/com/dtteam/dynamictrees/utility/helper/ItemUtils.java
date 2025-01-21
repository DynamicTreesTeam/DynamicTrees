package com.dtteam.dynamictrees.utility.helper;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.platform.services.IConfigHelper;
import com.dtteam.dynamictrees.systems.nodemapper.NetVolumeNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
//import net.minecraftforge.common.ToolActions;

import org.jetbrains.annotations.Nullable;

/**
 * @author Harley O'Connor
 */
public final class ItemUtils {

    /**
     * Spawns an {@link ItemStack} as an {@link ItemEntity} in the {@link Level} at the {@link BlockPos} given.
     *
     * @param level The {@link Level} object to spawn the item in.
     * @param pos   The {@link BlockPos} object to spawn the item at.
     * @param stack The {@link ItemStack} to spawn.
     */
    public static void spawnItemStack(Level level, BlockPos pos, ItemStack stack) {
        spawnItemStack(level, pos, stack, false);
    }

    /**
     * Spawns an {@link ItemStack} as an {@link ItemEntity} in the {@link Level} at the {@link BlockPos} given.
     *
     * @param level        The {@link Level} object to spawn the item in.
     * @param pos          The {@link BlockPos} object to spawn the item at.
     * @param stack        The {@link ItemStack} to spawn.
     * @param searchForAir If true, searches for air for the item to spawn in.
     */
    public static void spawnItemStack(Level level, BlockPos pos, ItemStack stack, boolean searchForAir) {
        if (searchForAir) {
            // Goes up one block at a time until an air block to spawn on is found.
            while (!level.isEmptyBlock(pos)) {
                pos = pos.above();
            }
        }

        // Create the item entity, spawning it in the centre of the position given.
        final ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        // Make sure the item entity has no motion.
        itemEntity.setDeltaMovement(0, 0, 0);
        // Add (spawn) the item to the level.
        level.addFreshEntity(itemEntity);
    }

    /**
     * Damages any axe item depending on radius or volume. Used by branch blocks
     * @param entity        Entity doing the breaking
     * @param heldItem      The axe
     * @param radius        Radius of branch
     * @param woodVolume    Volume of tree
     * @param forBlockBreak If this function was used for breaking a block, as minecraft already did 1 value of damage.
     */
    public static void damageAxe(final LivingEntity entity, @Nullable final ItemStack heldItem, final int radius, final NetVolumeNode.Volume woodVolume, final boolean forBlockBreak) {
        if (heldItem == null || !Services.INTERACTION.canToolAxeDig(heldItem)) {
            return;
        }

        int damage = switch (Services.CONFIG.getConfig(IConfigHelper.AXE_DAMAGE_MODE, DynamicTrees.AxeDamage.class)) {
            case VANILLA -> 1;
            case THICKNESS -> Math.max(1, radius) / 2;
            case VOLUME -> (int) woodVolume.getVolume();
        };

        if (forBlockBreak) {
            damage--; // Minecraft already damaged the tool by one unit
        }

        if (damage > 0) {
            heldItem.hurtAndBreak(damage, entity, EquipmentSlot.MAINHAND);
        }
    }

    public static int getEnchantmentLevel (ResourceKey<Enchantment> enchantment, ItemStack stack, RegistryAccess registryAccess){
        return EnchantmentHelper.getItemEnchantmentLevel(getEnchantment(enchantment, registryAccess), stack);
    }

    public static Holder.Reference<Enchantment> getEnchantment (ResourceKey<Enchantment> enchantment, HolderLookup.Provider registryAccess){
        HolderLookup.RegistryLookup<Enchantment> registrylookup = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
        return registrylookup.getOrThrow(enchantment);
    }

}
