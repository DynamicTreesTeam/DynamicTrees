package com.ferreusveritas.dynamictrees.items;

import com.ferreusveritas.dynamictrees.ModTabs;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockTrunkShell;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.google.common.collect.Multimap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.List;
import java.util.Optional;

/**
 * Try the following in a command block to demonstrate the extra tag functionality. /give @p dynamictrees:staff 1 0
 * {color:"#88FF00",code:"OUiVpPzkbtJ9uSRPbZP",readonly:1,tree:"dynamictrees:birch",maxuses:16,display:{Name:"Frog"}}
 */
public class Staff extends Item {

	public final static String HANDLE = "handle";
	public final static String COLOR = "color";

	public final static String READONLY = "readonly";
	public final static String TREE = "tree";
	public final static String CODE = "code";
	public final static String USES = "uses";
	public final static String MAXUSES = "maxuses";

	public Staff() {
		this("staff");
	}

	public Staff(String name) {
		setRegistryName(name);
		setUnlocalizedName(getRegistryName().toString());
		setMaxStackSize(1);
		setHarvestLevel("axe", 3);
		setCreativeTab(ModTabs.dynamicTreesTab);
	}

	@Override
	public float getDestroySpeed(ItemStack stack, IBlockState state) {
		if (state.getBlock() instanceof BlockBranch || state.getBlock() instanceof BlockTrunkShell) {
			return 64.0f;
		}
		return super.getDestroySpeed(stack, state);
	}

	@Override
	public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
		if (state.getBlock() instanceof BlockBranch || state.getBlock() instanceof BlockTrunkShell) {
			if (decUses(stack)) {
				stack.shrink(1);
			}
			return true;
		}
		return false;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

		ItemStack heldStack = player.getHeldItem(hand);
		IBlockState clickedBlockState = world.getBlockState(pos);

		if (!isReadOnly(heldStack)) {
			Species species = TreeHelper.getBestGuessSpecies(world, pos);
			if (species.isValid()) {
				setSpecies(heldStack, species);
				if (!player.isSneaking()) {
					EnumFacing playerFacing = player.getHorizontalFacing();
					Optional<JoCode> joCode = TreeHelper.getJoCode(world, pos, playerFacing, species);

					if (joCode.isPresent()) {
						String code = joCode.get().toString();
						setCode(heldStack, code);
						if (world.isRemote) {//Make sure this doesn't run on the server
							GuiScreen.setClipboardString(code);//Put the code in the system clipboard to annoy everyone.
						}
					}
				}

				return EnumActionResult.SUCCESS;
			}
		}

		//Create a tree from right clicking on soil
		Species species = getSpecies(heldStack);
		if (species.isValid() && species.isAcceptableSoil(world, pos, clickedBlockState)) {
			species.getJoCode(getCode(heldStack)).setCareful(true).generate(world, species, pos, world.getBiome(pos), player.getHorizontalFacing(), 8, SafeChunkBounds.ANY);
			if (!player.isCreative()) {
				if (hasMaxUses(heldStack)) {
					if (decUses(heldStack)) {
						heldStack.shrink(1);//If the player is in creative this will have no effect.
					}
				} else {
					heldStack.shrink(1);//If the player is in creative this will have no effect.
				}
			}
			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.FAIL;
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return hasMaxUses(stack);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return super.getMaxItemUseDuration(stack);
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		double damage = getUses(stack) / (double) getMaxUses(stack);
		return 1 - damage;
	}

	/**
	 * Gets the NBT for the itemStack or creates a new one if it doesn't exist
	 *
	 * @param itemStack
	 * @return
	 */
	public NBTTagCompound getNBT(ItemStack itemStack) {
		return itemStack.hasTagCompound() ? itemStack.getTagCompound() : new NBTTagCompound();
	}

	public boolean isReadOnly(ItemStack itemStack) {
		return getNBT(itemStack).getBoolean(READONLY);
	}

	public Staff setReadOnly(ItemStack itemStack, boolean readonly) {
		NBTTagCompound nbt = getNBT(itemStack);
		nbt.setBoolean(READONLY, readonly);
		itemStack.setTagCompound(nbt);
		return this;
	}

	public Staff setSpecies(ItemStack itemStack, Species species) {
		NBTTagCompound nbt = getNBT(itemStack);
		nbt.setString(TREE, species.toString());
		itemStack.setTagCompound(nbt);
		return this;
	}

	public Staff setCode(ItemStack itemStack, String code) {
		NBTTagCompound nbt = getNBT(itemStack);
		nbt.setString(CODE, code);
		itemStack.setTagCompound(nbt);
		return this;
	}

	public Species getSpecies(ItemStack itemStack) {
		NBTTagCompound nbt = getNBT(itemStack);

		if (nbt.hasKey(TREE)) {
			return TreeRegistry.findSpecies(new ResourceLocation(nbt.getString(TREE)));
		} else {
			Species species = TreeRegistry.findSpeciesSloppy("oak");
			setSpecies(itemStack, species);
			return species;
		}
	}

	public int getUses(ItemStack itemStack) {
		NBTTagCompound nbt = getNBT(itemStack);

		if (nbt.hasKey(USES)) {
			return nbt.getInteger(USES);
		} else {
			int uses = getMaxUses(itemStack);
			setUses(itemStack, uses);
			return uses;
		}

	}

	public void setUses(ItemStack itemStack, int value) {
		getNBT(itemStack).setInteger(USES, value);
	}

	public int getMaxUses(ItemStack itemStack) {
		NBTTagCompound nbt = getNBT(itemStack);

		if (nbt.hasKey(MAXUSES)) {
			return nbt.getInteger(MAXUSES);
		}

		return 0;
	}

	public void setMaxUses(ItemStack itemStack, int value) {
		getNBT(itemStack).setInteger(MAXUSES, value);
	}

	public boolean hasMaxUses(ItemStack itemStack) {
		return getNBT(itemStack).hasKey(MAXUSES);
	}

	public boolean decUses(ItemStack itemStack) {
		int uses = Math.max(0, getUses(itemStack) - 1);
		setUses(itemStack, uses);
		return uses <= 0;
	}

	public int getColor(ItemStack itemStack, int tint) {
		if (tint == 0) {
			NBTTagCompound nbt = getNBT(itemStack);

			int color = 0x005b472f;//Original brown wood color

			if (nbt.hasKey(HANDLE)) {
				try {
					color = Color.decode(nbt.getString(HANDLE)).getRGB();
				} catch (NumberFormatException e) {
					nbt.removeTag(HANDLE);
				}
			} else {
				color = getSpecies(itemStack).getFamily().getWoodColor();
			}

			return color;
		} else if (tint == 1) {
			NBTTagCompound nbt = getNBT(itemStack);

			int color = 0x0000FFFF;//Cyan crystal like Radagast the Brown's staff.

			if (nbt.hasKey(COLOR)) {
				try {
					color = Color.decode(nbt.getString(COLOR)).getRGB();
				} catch (NumberFormatException e) {
					nbt.removeTag(COLOR);
				}
			}

			return color;
		}


		return 0xFFFFFFFF;//white
	}

	public Staff setColor(ItemStack itemStack, String colStr) {
		NBTTagCompound nbt = getNBT(itemStack);
		nbt.setString(COLOR, colStr);
		itemStack.setTagCompound(nbt);
		return this;
	}

	public String getCode(ItemStack itemStack) {
		String code = "P";//Code of a sapling
		NBTTagCompound nbt = getNBT(itemStack);

		if (nbt.hasKey(CODE)) {
			code = nbt.getString(CODE);
		} else {
			nbt.setString(CODE, code);
			itemStack.setTagCompound(nbt);
		}

		return code;
	}

	/**
	 * returns the action that specifies what animation to play when the items are being used
	 */
	@Override
	public EnumAction getItemUseAction(ItemStack itemStack) {
		return EnumAction.BLOCK;
	}

	/**
	 * Make the player hold the staff like a sword
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isFull3D() {
		return true;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flagIn) {
		Species species = getSpecies(stack);
		tooltip.add(getTranslationText("tooltip.woodland_staff.tree") + " " +
			((species != null) ? "§a" + species.getLocalizedName() : getTranslationText("tooltip.woodland_staff.tree_not_set")));
		tooltip.add("JoCode: §6" + getCode(stack));
	}

	/**
	 * Gets a map of item attribute modifiers, used by ItemSword to increase hit damage.
	 */
	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot equipmentSlot, ItemStack stack) {
		Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(equipmentSlot, stack);
		if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 5.0, 0));
			multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4, 0));
		}
		return multimap;
	}

	private static String getTranslationText(String path) {
		return new TextComponentTranslation(path).getUnformattedComponentText();
	}

}
