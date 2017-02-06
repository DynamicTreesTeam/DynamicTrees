package com.ferreusveritas.growingtrees.items;

import java.awt.Color;
import java.util.List;

import com.ferreusveritas.growingtrees.GrowingTrees;
import com.ferreusveritas.growingtrees.TreeHelper;
import com.ferreusveritas.growingtrees.TreeRegistry;
import com.ferreusveritas.growingtrees.blocks.BlockBranch;
import com.ferreusveritas.growingtrees.blocks.ITreePart;
import com.ferreusveritas.growingtrees.blocks.MapSignal;
import com.ferreusveritas.growingtrees.trees.GrowingTree;
import com.ferreusveritas.growingtrees.worldgen.JoCode;
import com.google.common.collect.Multimap;

import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**

Try the following in a command block to demonstrate the extra tag functionality.
/give @p growingtrees:staff 1 0 tag:{color:"#88FF00",code:"OUiVpPzkbtJ9uSRPbZP",readonly:1,seed:"growingtrees:birchseed",display:{Name:"Frog"}}

*/
public class Staff extends Item {

    private static final String name = "staff";
	IIcon overlayIcon;
	IIcon emptyIcon;
    
	public Staff(){
		setCreativeTab(GrowingTrees.growingTreesTab);
		setTextureName(GrowingTrees.MODID + ":" + name);
		setUnlocalizedName(GrowingTrees.MODID + "_" + name);
		GameRegistry.registerItem(this, name);
		setMaxStackSize(1);
	}
	
	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float sideX, float sideY, float sideZ){

		Block clickedBlock = world.getBlock(x, y, z);
		ITreePart treePart = TreeHelper.getSafeTreePart(clickedBlock);
		int rootX = x;
		int rootY = y;
		int rootZ = z;

		/*if(clickedBlock instanceof BlockGrowingLeaves ){
			if(world.isRemote){
				int metadata = world.getBlockMetadata(x, y, z);
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Leaves Metadata: " + Integer.toBinaryString(metadata)));
			}
		}*/
		
		//Check if the tree part is a branch and look for the root node if so
		BlockBranch branch = TreeHelper.getBranch(treePart);
		if(branch != null){
			MapSignal signal = branch.analyse(world, x, y, z, ForgeDirection.UNKNOWN, new MapSignal());//Analyze entire tree network to find root node
			if(signal.found){
				rootX = signal.rootX;
				rootY = signal.rootY;
				rootZ = signal.rootZ;
				treePart = TreeHelper.getSafeTreePart(world, rootX, rootY, rootZ);
			}
		}

		//Get the code from a tree or rooty dirt and set it in the staff
		if(!isReadOnly(itemStack) && treePart.isRootNode()){
			GrowingTree tree = treePart.getTree(world, rootX, rootY, rootZ);
			if(tree != null){
				if(!player.isSneaking()){
					String code = new JoCode().buildFromTree(world, rootX, rootY, rootZ).toString();
					setCode(itemStack, code);
					GuiScreen.setClipboardString(code);
				}
				setSeed(itemStack, tree.getSeed());
				return true;
			}
		}

		//Create a tree from right clicking on soil
		Seed seed = getSeed(itemStack);
		if(seed != null && seed.isAcceptableSoil(clickedBlock)){
			new JoCode(getCode(itemStack)).setCareful(true).growTree(world, seed.getTree(), x, y, z);
			itemStack.stackSize--;		
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
	public NBTTagCompound getNBT(ItemStack itemStack){
	    return itemStack.hasTagCompound() ? itemStack.getTagCompound() : new NBTTagCompound();
	}
	
	public boolean isReadOnly(ItemStack itemStack){
	    return getNBT(itemStack).getBoolean("readonly");
	}
	
	public void setSeed(ItemStack itemStack, Seed seed) {
	    NBTTagCompound nbt = getNBT(itemStack);
		nbt.setString("seed", GameData.getItemRegistry().getNameForObject(seed));
		itemStack.setTagCompound(nbt);
	}

	public void setCode(ItemStack itemStack, String code) {
		NBTTagCompound nbt = getNBT(itemStack);
	    nbt.setString("code", code);
	    itemStack.setTagCompound(nbt);
	}
	
	public Seed getSeed(ItemStack itemStack) {
		Seed seed = TreeRegistry.findTree("oak").getSeed();
		NBTTagCompound nbt = getNBT(itemStack);

   		if(nbt.hasKey("seed")) {
   			Item item = GameData.getItemRegistry().getObject(nbt.getString("seed"));
   			seed = (item instanceof Seed) ? (Seed)item : seed;
 		} else {
 			nbt.setString("seed", GameData.getItemRegistry().getNameForObject(seed));
			itemStack.setTagCompound(nbt);
 		}
		
    	return seed;
	}
	
	public int getColor(ItemStack itemStack){
		NBTTagCompound nbt = getNBT(itemStack);

		if(nbt.hasKey("color")){
			return Color.decode(nbt.getString("color")).getRGB();
		}

		return 0x00FFFFFF;
	}
	
	public void setColor(ItemStack itemStack, String colStr){
		NBTTagCompound nbt = getNBT(itemStack);
		nbt.setString("color", colStr);
		itemStack.setTagCompound(nbt);
	}
	
	public String getCode(ItemStack itemStack){
		String code = "P";//Code of a sapling
		NBTTagCompound nbt = getNBT(itemStack);
		
	    if(nbt.hasKey("code")){
	    	code = nbt.getString("code");
	    } else {
	    	nbt.setString("code", code);
			itemStack.setTagCompound(nbt);
	    }
	    
		return code;
	}

	public ForgeDirection getPlayerDirection(EntityPlayer player){
		switch(MathHelper.floor_float(player.rotationYaw * 4.0F / 360.0F + 0.5F) & 3){
			case 0: return ForgeDirection.SOUTH;
			case 1: return ForgeDirection.WEST;
			case 2: return ForgeDirection.NORTH;
			case 3: return ForgeDirection.EAST;
			default: return ForgeDirection.UNKNOWN;
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
    public void addInformation(ItemStack stack, EntityPlayer player, List lores, boolean advancedTooltips){
    	lores.add("Tree: " + getSeed(stack).getTree().getName());
    	lores.add("Code: §6" + getCode(stack));
    }

    /**
     * Gets a map of item attribute modifiers, used by ItemSword to increase hit damage.
     */
    @Override
	public Multimap getItemAttributeModifiers() {
        Multimap multimap = super.getItemAttributeModifiers();
        multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", 5.0, 0));
        return multimap;
    }
 
    @Override
	@SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses() {
        return true;
    }

    @Override
	@SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack stack, int pass) {
    	return pass == 0 ? 0x00FFFFFF : getColor(stack);
    }
    
    /**
     * Gets an icon index based on an item's damage value and the given render pass
     */
    @Override
	@SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack itemStack, int pass) {
		if(pass == 0){
			return this.itemIcon;
		} 
		else {
			NBTTagCompound nbt = getNBT(itemStack);
			return nbt.hasKey("color") ? this.overlayIcon : this.emptyIcon;
		}
    }
    
    @Override
	@SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        super.registerIcons(iconRegister);
        overlayIcon = iconRegister.registerIcon(this.getIconString() + "_overlay");
        emptyIcon = iconRegister.registerIcon(this.getIconString() + "_empty");
    }
    
}
