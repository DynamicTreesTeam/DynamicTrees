package com.ferreusveritas.dynamictrees.items;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockTrunkShell;
import com.ferreusveritas.dynamictrees.blocks.BlockTrunkShell.ShellMuse;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

/**
 * Try the following in a command block to demonstrate the extra tag functionality.
 * /give @p dynamictrees:staff 1 0 {color:"#88FF00",code:"OUiVpPzkbtJ9uSRPbZP",readonly:1,tree:"dynamictrees:birch",maxuses:16,display:{Name:"Frog"}}
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
		super(new Item.Properties().maxStackSize(1)
				.group(DTRegistries.dynamicTreesTab)
				.addToolType(ToolType.AXE, 3));
		setRegistryName(name);
	}
	
	
	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		if(state.getBlock() instanceof BlockBranch || state.getBlock() instanceof BlockTrunkShell) {
			return 64.0f;
		}
		return super.getDestroySpeed(stack, state);
	}
	
	@Override
	public boolean onBlockDestroyed(ItemStack stack, World worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
		if(state.getBlock() instanceof BlockBranch || state.getBlock() instanceof BlockTrunkShell) {
			if(decUses(stack)) {
				stack.shrink(1);
			}
			return true;
		}
		return false;
	}
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		
		ItemStack heldStack = context.getPlayer().getHeldItem(context.getHand());
		
		BlockState clickedBlockState = context.getWorld().getBlockState(context.getPos());
		Block clickedBlock = clickedBlockState.getBlock();
		
		BlockPos pos = context.getPos();
		
		//Dereference proxy trunk shell
		if(clickedBlock instanceof BlockTrunkShell) {
			ShellMuse muse = ((BlockTrunkShell)clickedBlock).getMuse(context.getWorld(), clickedBlockState, context.getPos());
			if(muse != null) {
				clickedBlockState = muse.state;
				pos = muse.pos;
				clickedBlock = clickedBlockState.getBlock();
			}
		}
		
		ITreePart treePart = TreeHelper.getTreePart(clickedBlock);
		BlockPos rootPos = pos;
		
		//Check if the tree part is a branch and look for the root node if so
		BlockBranch branch = TreeHelper.getBranch(treePart);
		if(branch != null) {
			MapSignal signal = branch.analyse(clickedBlockState, context.getWorld(), pos, null, new MapSignal());//Analyze entire tree network to find root node
			if(signal.found) {
				rootPos = signal.root;
				treePart = TreeHelper.getTreePart(context.getWorld().getBlockState(rootPos));
			}
		}
		
		//Get the code from a tree or rooty dirt and set it in the staff
		if(!isReadOnly(heldStack) && treePart.isRootNode()) {
			Species species = TreeHelper.getExactSpecies(context.getWorld(), rootPos);
			if(species.isValid()) {
				if(!context.getPlayer().isSneaking()) {
					String code = new JoCode(context.getWorld(), rootPos, context.getPlayer().getHorizontalFacing()).toString();
					setCode(heldStack, code);
					if(context.getWorld().isRemote) {//Make sure this doesn't run on the server
						//						GuiScreen.setClipboardString(code);//Put the code in the system clipboard to annoy everyone.
					}
				}
				setSpecies(heldStack, species);
				return ActionResultType.SUCCESS;
			}
		}
		
		//Create a tree from right clicking on soil
		Species species = getSpecies(heldStack);
		if(species.isValid() && species.isAcceptableSoil(context.getWorld(), pos, clickedBlockState)) {
			species.getJoCode(getCode(heldStack)).setCareful(true).generate(context.getWorld(), species, pos, context.getWorld().getBiome(pos), context.getPlayer().getHorizontalFacing(), 8, SafeChunkBounds.ANY);
			if(hasMaxUses(heldStack)) {
				if(decUses(heldStack)) {
					heldStack.shrink(1);//If the player is in creative this will have no effect.
				}
			} else {
				heldStack.shrink(1);//If the player is in creative this will have no effect.
			}
			return ActionResultType.SUCCESS;
		}
		
		return ActionResultType.FAIL;
	}
	
	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return hasMaxUses(stack);
	}
	
	//	@Override
	//	public int getMaxItemUseDuration(ItemStack stack) {
	//		return super.getMaxItemUseDuration(stack);
	//	}
	
	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		double damage = getUses(stack) / (double)getMaxUses(stack);
		return 1 - damage;
	}

	public boolean isReadOnly(ItemStack itemStack) {
		return itemStack.getOrCreateTag().getBoolean(READONLY);
	}
	
	public Staff setReadOnly(ItemStack itemStack, boolean readonly) {
		itemStack.getOrCreateTag().putBoolean(READONLY, readonly);
		return this;
	}
	
	public Staff setSpecies(ItemStack itemStack, Species species) {
		String name;

		if (species == Species.NULLSPECIES) name = "null";
		else name = species.toString();

		itemStack.getOrCreateTag().putString(TREE, name);
		return this;
	}
	
	public Staff setCode(ItemStack itemStack, String code) {
		itemStack.getOrCreateTag().putString(CODE, code);
		return this;
	}
	
	public Species getSpecies(ItemStack itemStack) {
		CompoundNBT nbt = itemStack.getOrCreateTag();
		
		if(nbt.contains(TREE)) {
			return TreeRegistry.findSpecies(new ResourceLocation(nbt.getString(TREE)));
		} else {
			//			Species species = TreeRegistry.findSpeciesSloppy("oak");
			//			setSpecies(itemStack, species);
			//			return species;
			return Species.NULLSPECIES;
		}
	}
	
	public int getUses(ItemStack itemStack) {
		CompoundNBT nbt = itemStack.getOrCreateTag();
		
		if(nbt.contains(USES)) {
			return nbt.getInt(USES);
		} else {
			int uses = getMaxUses(itemStack);
			setUses(itemStack, uses);
			return uses;
		}
		
	}
	
	public Staff setUses(ItemStack itemStack, int value) {
		itemStack.getOrCreateTag().putInt(USES, value);
		return this;
	}
	
	public int getMaxUses(ItemStack itemStack) {
		CompoundNBT nbt = itemStack.getOrCreateTag();
		
		if(nbt.contains(MAXUSES)) {
			return nbt.getInt(MAXUSES);
		}
		
		return 0;
	}
	
	public Staff setMaxUses(ItemStack itemStack, int value) {
		itemStack.getOrCreateTag().putInt(MAXUSES, value);
		return this;
	}
	
	public boolean hasMaxUses(ItemStack itemStack) {
		return itemStack.getOrCreateTag().contains(MAXUSES);
	}
	
	public boolean decUses(ItemStack itemStack) {
		int uses = Math.max(0, getUses(itemStack) - 1);
		setUses(itemStack, uses);
		return uses <= 0;
	}
	
	public int getColor(ItemStack itemStack, int tint) {
		final CompoundNBT nbt = itemStack.getOrCreateTag();

		if (tint == 0) {
			int color = 0x005b472f; // Original brown wood color
			
			Species species = getSpecies(itemStack);
			
			if (nbt.contains(HANDLE)) {
				try {
					color = Color.decode(nbt.getString(HANDLE)).getRGB();
				} catch (NumberFormatException e) {
					nbt.remove(HANDLE);
				}
			}
			else if (species.isValid()) {
				color = species.getFamily().getWoodColor();
			}
			
			return color;
		} else if (tint == 1) {
			int color = 0x0000FFFF; // Cyan crystal like Radagast the Brown's staff.

			if (nbt.contains(COLOR)) {
				try {
					color = Color.decode(nbt.getString(COLOR)).getRGB();
				} catch (NumberFormatException e) {
					nbt.remove(COLOR);
				}
			}

			return color;
		}
		
		
		return 0xFFFFFFFF;//white
	}
	
	public Staff setColor(ItemStack itemStack, String colStr) {
		itemStack.getOrCreateTag().putString(COLOR, colStr);
		return this;
	}
	
	public String getCode(ItemStack itemStack) {
		String code = "P";//Code of a sapling

		if (itemStack.getOrCreateTag().contains(CODE)) {
			code = itemStack.getTag().getString(CODE);
		} else {
			itemStack.getTag().putString(CODE, code);
		}
		
		return code;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		Species species = getSpecies(stack);
		tooltip.add(new StringTextComponent("Tree: " + ((species.isValid()) ? species : "none")));
		tooltip.add(new StringTextComponent("Code: ").append(new StringTextComponent(TextFormatting.GOLD + this.getCode(stack))));
	}

	/**
	 * Gets a map of item attribute modifiers, used by ItemSword to increase hit damage.
	 */

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack) {
		Multimap<Attribute, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);
//		if (slot == EquipmentSlotType.MAINHAND) {
//			multimap.put(Attributes.ATTACK_SPEED, new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 5.0, AttributeModifier.Operation.ADDITION));
//			multimap.put(Attributes.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4, AttributeModifier.Operation.ADDITION));
//		}
		return multimap;
	}
	
	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////
	
}
