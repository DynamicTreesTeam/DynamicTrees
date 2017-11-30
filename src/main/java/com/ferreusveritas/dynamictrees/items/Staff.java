package com.ferreusveritas.dynamictrees.items;

import java.awt.Color;
import java.util.List;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.google.common.collect.Multimap;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/**
* Try the following in a command block to demonstrate the extra tag functionality.
* /give @p dynamictrees:staff 1 0 tag:{color:"#88FF00",code:"OUiVpPzkbtJ9uSRPbZP",readonly:1,tree:"birch",display:{Name:"Frog"}}
*/
public class Staff extends ItemReg {

	IIcon overlayIcon;
	IIcon glimmerIcon;

	public Staff() {
		this("staff");
	}
	
	public Staff(String name) {
		setCreativeTab(DynamicTrees.dynamicTreesTab);
		setMaxStackSize(1);
		setTextureName(DynamicTrees.MODID + ":" + name);
		setUnlocalizedNameReg(name);
		setRegistryName(name);
	}
	
	@Override
	public boolean onItemUse(ItemStack heldStack, EntityPlayer player, World world, int x, int y, int z, int side, float sideX, float sideY, float sideZ) {

		BlockPos pos = new BlockPos(x, y, z);
		IBlockState clickedBlock = pos.getBlockState(world);
		ITreePart treePart = TreeHelper.getSafeTreePart(clickedBlock.getBlock());
		BlockPos rootPos = pos;
		
		/*if(clickedBlock instanceof BlockGrowingLeaves ){
			if(world.isRemote){
				int metadata = world.getBlockMetadata(x, y, z);
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Leaves Metadata: " + Integer.toBinaryString(metadata)));
			}
		}*/

		//Check if the tree part is a branch and look for the root node if so
		BlockBranch branch = TreeHelper.getBranch(treePart);
		if(branch != null) {
			MapSignal signal = branch.analyse(world, pos, null, new MapSignal());//Analyze entire tree network to find root node
			if(signal.found) {
				rootPos = signal.root;
				treePart = TreeHelper.getSafeTreePart(world, rootPos);
				
				if(world.isRemote && treePart.isRootNode()) {
					BlockRootyDirt rootyDirt = (BlockRootyDirt) treePart;
					int soilLife = rootyDirt.getSoilLife(world, rootPos);
					Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Rooty Soil Life: " + soilLife));
				}
			}
		}

		//Get the code from a tree or rooty dirt and set it in the staff
		if(!isReadOnly(heldStack) && treePart.isRootNode()) {
			DynamicTree tree = treePart.getTree(world, rootPos);
			if(tree != null) {
				if(!player.isSneaking()) {
					String code = new JoCode().buildFromTree(world, rootPos, getPlayerDirection(player)).toString();
					setCode(heldStack, code);
					GuiScreen.setClipboardString(code);//Put the code in the system clipboard to annoy everyone.
				}
				setTree(heldStack, tree);
				return true;
			}
		}

		//Create a tree from right clicking on soil
		DynamicTree tree = getTree(heldStack);
		if(tree != null && tree.isAcceptableSoil(world, pos, clickedBlock)) {
			new JoCode(getCode(heldStack)).setCareful(true).generate(world, tree, pos, getPlayerDirection(player), 8);
			heldStack.stackSize--;//If the player is in creative this will have no effect.
			return true;
		}

		return false;
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
		return getNBT(itemStack).getBoolean("readonly");
	}

	public Staff setReadOnly(ItemStack itemStack, boolean readonly) {
		NBTTagCompound nbt = getNBT(itemStack);
		nbt.setBoolean("readonly", readonly);
		itemStack.setTagCompound(nbt);
		return this;
	}

	public Staff setTree(ItemStack itemStack, DynamicTree tree) {
		NBTTagCompound nbt = getNBT(itemStack);
		nbt.setString("tree", tree.getName());
		itemStack.setTagCompound(nbt);
		return this;
	}

	public Staff setCode(ItemStack itemStack, String code) {
		NBTTagCompound nbt = getNBT(itemStack);
		nbt.setString("code", code);
		itemStack.setTagCompound(nbt);
		return this;
	}

	public DynamicTree getTree(ItemStack itemStack) {
		NBTTagCompound nbt = getNBT(itemStack);

		if(nbt.hasKey("tree")) {
			return TreeRegistry.findTree(nbt.getString("tree"));
		} else {
			DynamicTree tree = TreeRegistry.findTree("oak");
			setTree(itemStack, tree);
			return tree;
		}
	}

	public int getColor(ItemStack itemStack) {
		NBTTagCompound nbt = getNBT(itemStack);

		int color = 0x0000FFFF;
		
		if(nbt.hasKey("color")) {
			try {
				color = Color.decode(nbt.getString("color")).getRGB();
			} catch (NumberFormatException e) {
				nbt.removeTag("color");
			}
		}

		return color;
	}

	public Staff setColor(ItemStack itemStack, String colStr) {
		NBTTagCompound nbt = getNBT(itemStack);
		nbt.setString("color", colStr);
		itemStack.setTagCompound(nbt);
		return this;
	}

	public String getCode(ItemStack itemStack) {
		String code = "P";//Code of a sapling
		NBTTagCompound nbt = getNBT(itemStack);

		if(nbt.hasKey("code")) {
			code = nbt.getString("code");
		} else {
			nbt.setString("code", code);
			itemStack.setTagCompound(nbt);
		}

		return code;
	}

	public EnumFacing getPlayerDirection(EntityPlayer player) {
		switch(MathHelper.floor_float(player.rotationYaw * 4.0F / 360.0F + 0.5F) & 3) {
			case 0: return EnumFacing.SOUTH;
			case 1: return EnumFacing.WEST;
			case 2: return EnumFacing.NORTH;
			case 3: return EnumFacing.EAST;
			default: return null;
		}
	}
	
	/**
	* returns the action that specifies what animation to play when the items are being used
	*/
	@Override
	public EnumAction getItemUseAction(ItemStack itemStack) {
		return EnumAction.block;
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
	public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advancedTooltips) {
		DynamicTree tree = getTree(stack);
		tooltip.add("Tree: " + ((tree != null) ? tree.getFullName() : "none"));
		tooltip.add("Code: §6" + getCode(stack));
	}

	/**
	* Gets a map of item attribute modifiers, used by ItemSword to increase hit damage.
	*/
	@Override
	public Multimap getAttributeModifiers(ItemStack stack) {
		Multimap multimap = super.getAttributeModifiers(stack);
		multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", 5.0, 0));
		return multimap;
	}

	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////

	@Override
	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@Override
	public int getRenderPasses(int metadata) {
		return 3;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int pass) {
		return pass == 1 ? getColor(stack) : 0x00FFFFFF;
	}

	/**
	* Gets an icon index based on an item's damage value and the given render pass
	*/
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(ItemStack itemStack, int pass) {
		switch(pass) {
			default:
			case 0: return itemIcon;
			case 1: return overlayIcon;
			case 2: return glimmerIcon;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {
		super.registerIcons(iconRegister);
		overlayIcon = iconRegister.registerIcon(this.getIconString() + "-overlay");
		glimmerIcon = iconRegister.registerIcon(this.getIconString() + "-glimmer");
	}

}
