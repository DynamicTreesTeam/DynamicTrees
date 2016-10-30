package com.ferreusveritas.growingtrees.blocks;

import java.util.Random;

import com.ferreusveritas.growingtrees.GrowingTrees;
import com.ferreusveritas.growingtrees.TreeHelper;
import com.ferreusveritas.growingtrees.inspectors.NodeDestroyer;
import com.ferreusveritas.growingtrees.inspectors.NodeNetVolume;
import com.ferreusveritas.growingtrees.items.Seed;
import com.ferreusveritas.growingtrees.renderers.RendererBranch;
import com.ferreusveritas.growingtrees.special.BottomListenerDropItems;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockBranch extends Block implements ITreePart {

	public BlockGrowingLeaves growingLeaves;
	public int growingLeavesSub;
	public BlockAndMeta primitiveLog;
	
	/** How quickly the branch thickens on it's own without branch merges */
	public float tapering;
	/** The probability that the direction decider will choose up out of the other possible direction weights */
	public int upProbability;
	/** Thickness of the branch connected to a twig(radius == 1) */
	public float secondaryThickness;
	/** Number of blocks high we have to be before a branch is allowed to form */
	public int lowestBranchHeight;
	/** Number of times a grow signal retries before failing. Affects growing speed */
	public int retries;
	/** Ideal signal energy. Greatest possible height that branches can reach from the root node */
	public float signalEnergy;
	
	/** Ideal growth rate */
	public float growthRate;
	
	/** Ideal soil longevity */
	public int soilLongevity;
	
	public BlockBranch() {
		super(Material.wood);//Trees are made of wood. Brilliant.
		
		//Some generic defaults that will be inherited by all tree branches
		tapering = 0.3f;
		upProbability = 2;
		secondaryThickness = 2.0f;//This should probably always be 2
		lowestBranchHeight = 3;//High enough to walk under
		retries = 0;
		signalEnergy = 16.0f;
		growthRate = 1.0f;
		soilLongevity = 8;
		
		//Setup functions here
        this.setTickRandomly(true);//We need this to facilitate decay when supporting neighbors are lacking
        setPrimitiveLog(Blocks.log, 0);
	}
	
	public void setPrimitiveLog(Block block, int meta){
		primitiveLog = new BlockAndMeta(block, meta);
	}

	public BlockAndMeta getPrimitiveLog() {
		return primitiveLog;
	}
	
	public boolean isSameWood(ITreePart treepart){
		return isSameWood(TreeHelper.getBranch(treepart));
	}
	
	public boolean isSameWood(BlockBranch branch){
		return branch != null && getPrimitiveLog().equals(branch.getPrimitiveLog());
	}
	
	@Override
	public int branchSupport(IBlockAccess blockAccess, BlockBranch branch, int x, int y, int z, ForgeDirection dir, int radius){
		return isSameWood(branch) ? 0x11 : 0;//Other branches of the same type are always valid support.
	}
	
	@Override
    public void updateTick(World world, int x, int y, int z, Random random){
		int radius = getRadius(world, x, y, z);
		if(random.nextInt(radius * 2) == 0){//Thicker branches take longer to rot
			checkForRot(world, x, y, z, radius, random);
		}
	}

	public boolean checkForRot(World world, int x, int y, int z, int radius, Random random){
		//Rooty dirt below the block counts as a branch in this instance
		//Rooty dirt below for saplings counts as 2 neighbors if the soil is not infertile
		int neigh = 0;//High Nybble is count of branches, Low Nybble is any reinforcing treepart(including branches)
		
		for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS){
			int dx = x + dir.offsetX;
			int dy = y + dir.offsetY;
			int dz = z + dir.offsetZ;
			neigh += TreeHelper.getSafeTreePart(world, dx, dy, dz).branchSupport(world, this, dx, dy, dz, dir, radius);
			if(neigh >= 0x10 && (neigh & 0x0F) >= 2){//Need two neighbors..  one of which must be another branch
				return false;//We've proven that this branch is reinforced so there is no need to continue
			}
		}
		
		return rot(world, x, y, z, neigh & 0x0F, radius, random);//Unreinforced branches are destroyed
	}
	
	public boolean rot(World world, int x, int y, int z, int neighborCount, int radius, Random random){
		if(radius <= 1){
			for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS){
				if(getGrowingLeaves().growLeaves(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, getGrowingLeavesSub(), 0)){
					return false;
				}
			}
		} 
		world.setBlockToAir(x, y, z);
		return true;
	}
	
	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////
	
	@Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float px, float py, float pz){
		if(player.getCurrentEquippedItem() != null) {
			if(applySubstance(world, x, y, z, player.getCurrentEquippedItem())){
				player.getCurrentEquippedItem().stackSize--;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean applySubstance(World world, int x, int y, int z, ItemStack itemStack){
		if(world.getBlock(x, y - 1, z) != this){
			return TreeHelper.getSafeTreePart(world, x, y - 1, z).applySubstance(world, x, y - 1, z, itemStack);
		}
		return false;
	}
	
	@Override
	public float getBlockHardness(World world, int x, int y, int z) {
		int radius = getRadius(world, x, y, z);
		return primitiveLog.getBlock().getBlockHardness(world, x, y, z) * (radius * radius) / 64.0f * 8.0f;
	};

	@Override
    public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
		//return 300;
		return primitiveLog.getBlock().getFlammability(world, x, y, z, face);
    }
	
	@Override
	public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
		//return 4096;
		return primitiveLog.getBlock().getFireSpreadSpeed(world, x, y, z, face);
    }

	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////

	@Override
	public boolean isOpaqueCube(){
		return false;
	}
	
	@Override
	public boolean renderAsNormalBlock(){
		return false;
	}
	
	//Bark or wood Ring texture for branches
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return primitiveLog.getIcon((1 << side & RendererBranch.renderRingSides) != 0 ? 0 : 2);//0:Ring, 2:Bark
	}

    @Override
	@SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister) {
    }
	
	//Leaf texture for Saplings
	@SideOnly(Side.CLIENT)
	public IIcon getLeavesIcon() {
		return getPrimitiveLeavesBlockRef().getIcon(0);
	}
	
	@Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess access, int x, int y, int z, int side) {
		if(RendererBranch.renderFaceFlags == RendererBranch.faceAll){
			return super.shouldSideBeRendered(access, x, y, z, side);
		}
		return (1 << side & RendererBranch.renderFaceFlags) != 0;
    }
	
	@Override
	public int getRenderType() {
		return RendererBranch.id;
	}
	
	@Override
	public int getHydrationLevel(IBlockAccess blockAccess, int x, int y, int z, ForgeDirection dir, BlockGrowingLeaves fromBlock, int fromSub) {
		return getRadius(blockAccess, x, y, z) == 1 && isCompatibleGrowingLeaves(fromBlock, fromSub) ? 5 : 0;
	}
	
	public boolean isSapling(IBlockAccess blockAccess, int x, int y, int z){
		return
			TreeHelper.getSafeTreePart(blockAccess, x, y - 1, z).isRootNode() && //Below is rooty dirt
			getRadius(blockAccess, x, y, z) == 1 && //Is a branch has a radius of 1
			TreeHelper.getTreePart(blockAccess, x, y + 1, z) == null; //Above is a non-tree block(hopefully air)
	}
	
	///////////////////////////////////////////
	// GROWTH
	///////////////////////////////////////////

	@Override
	public int getRadius(IBlockAccess blockAccess, int x, int y, int z){
		return (blockAccess.getBlockMetadata(x, y, z) & 7) + 1;
	}
	
	public void setRadius(World world, int x, int y, int z, int radius){
		radius = MathHelper.clamp_int(radius, 0, 8);
		world.setBlockMetadataWithNotify(x, y, z, (radius - 1) & 7, 2);
	}

	public float getGrowthRate(){
		return growthRate;
	}
	
	public float getGrowthRate(World world, int x, int y, int z){
		return getGrowthRate();
	}
	
	//Probability reinforcer for up direction which is arguably the direction most trees generally grow in.
	public int getUpProbability(){
		return upProbability;
	}

	//Probability reinforcer for current travel direction
	public int getReinfTravel(){
		return 1;
	}

	public int getLowestBranchHeight() {
		return lowestBranchHeight;
	}
	
	public int getLowestBranchHeight(World world, int x, int y, int z){
		return getLowestBranchHeight();
	}
	
	//Directionless probability grabber
	@Override
	public int probabilityForBlock(IBlockAccess blockAccess, int x, int y, int z, BlockBranch from) {
		return isSameWood(from) ? getRadius(blockAccess, x, y, z) + 2 : 0;
	}

	//Selects a new direction to turn to.
	//This class uses a probability map to make the decision.  Override for different species.
	public ForgeDirection selectNewDirection(World world, int x, int y, int z, GrowSignal signal) {
		ForgeDirection originDir = signal.dir.getOpposite();

		//prevent branches on the ground
		if(signal.numSteps + 1 <= getLowestBranchHeight(world, signal.originX, signal.originY, signal.originZ)){
			return ForgeDirection.UP;
		}
		
		int probMap[] = new int[6];//6 directions possible DUNSWE

		//Probability taking direction into account
		probMap[ForgeDirection.UP.ordinal()] = signal.dir != ForgeDirection.DOWN ? getUpProbability(): 0;//Favor up 
		probMap[signal.dir.ordinal()] += getReinfTravel(); //Favor current direction
		
		//Create probability map for direction change
		for(int i = 0; i < 6; i++){
			ForgeDirection dir = ForgeDirection.getOrientation(i);
			if(!dir.equals(originDir)){
				int dx = x + dir.offsetX;
				int dy = y + dir.offsetY;
				int dz = z + dir.offsetZ;
				
				//Check probability for surrounding blocks
				//Typically Air:1, Leaves:2, Branches: 2+r
				probMap[i] += TreeHelper.getSafeTreePart(world, dx, dy, dz).probabilityForBlock(world, dx, dy, dz, this);
			}
		}
		
		//Do custom stuff for various species
		probMap = customDirectionManipulation(world, x, y, z, getRadius(world, x, y, z), signal, probMap);
		
		//Select a direction from the probability map
		int choice = selectRandomFromDistribution(signal.rand, probMap);//Select a direction from the probability map
		return ForgeDirection.getOrientation(choice != -1 ? choice : 1);//Default to up if things are screwy
	}
	
	//Override for species dependent decisions
	public int[] customDirectionManipulation(World world, int x, int y, int z, int radius, GrowSignal signal, int probMap[]){
		return probMap;
	}
	
	//Select a random direction weighted from the probability map 
	public static int selectRandomFromDistribution(Random random, int distMap[]){

		int distSize = 0;
		
		for(int i = 0; i < distMap.length; i++){
			distSize += distMap[i];
		}

		if(distSize <= 0){
			return -1;
		}
		
		int rnd = random.nextInt(distSize) + 1;
		
		for(int i = 0; i < 6; i++){
			if(rnd > distMap[i]){
				rnd -= distMap[i];
			} else {
				return i;
			}
		}

		return 0;
	}
	
	public GrowSignal growIntoAir(World world, int x, int y, int z, GrowSignal signal, int fromRadius){
		if(getGrowingLeaves() != null){
			if(fromRadius == 1){//If we came from a twig then just make some leaves
				signal.success = getGrowingLeaves().growLeaves(world, x, y, z, getGrowingLeavesSub(), 0);
			} else {//Otherwise make a proper branch
				return getGrowingLeaves().branchOut(world, x, y, z, signal);
			}
		}
		return signal;
	}

	@Override
	public GrowSignal growSignal(World world, int x, int y, int z, GrowSignal signal) {
		
		if(signal.step()){//This is always placed at the beginning of every growSignal function
			ForgeDirection originDir = signal.dir.getOpposite();//Direction this signal originated from
			ForgeDirection targetDir = selectNewDirection(world, x, y, z, signal);//This must be cached on stack for proper recursion
			signal.doTurn(targetDir);
			
			{
				int dx = x + targetDir.offsetX;
				int dy = y + targetDir.offsetY;
				int dz = z + targetDir.offsetZ;

				//Pass grow signal to next block in path
				ITreePart treepart = TreeHelper.getTreePart(world, dx, dy, dz);
				if(treepart != null){
					signal = treepart.growSignal(world, dx, dy, dz, signal);//Recurse
				} else if(world.isAirBlock(dx, dy, dz)){
					signal = growIntoAir(world, dx, dy, dz, signal, getRadius(world, x, y, z));
				}
			}
			
			//Calculate Branch Thickness based on neighboring branches
			float areaAccum = signal.radius * signal.radius;//Start by accumulating the branch we just came from

			for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS) {
				if(!dir.equals(originDir) && !dir.equals(targetDir)){//Don't count where the signal originated from or the branch we just came back from
					int dx = x + dir.offsetX;
					int dy = y + dir.offsetY;
					int dz = z + dir.offsetZ;

					//If it is decided to implement a special block(like a squirrel hole, tree swing, rotting, burned or infested branch, etc) then this new block could be
					//derived from BlockBranch and this works perfectly.  Should even work with tileEntity blocks derived from BlockBranch.
					ITreePart treepart = TreeHelper.getTreePart(world, dx, dy, dz);
					if(isSameWood(treepart)){
						int branchRadius = treepart.getRadius(world, dx, dy, dz);
						areaAccum += branchRadius * branchRadius;
					}
				}
			}

			//The new branch should be the square root of all of the sums of the areas of the branches coming into it.
			//But it shouldn't be smaller than it's current size(prevents the instant slimming effect when chopping off branches)
			signal.radius = MathHelper.clamp_float((float)Math.sqrt(areaAccum) + getTapering(), getRadius(world, x, y, z), 8);// WOW!
			setRadius(world, x, y, z, (int)Math.floor(signal.radius));
		}

		return signal;
	}
	

	//Used by seed to determine the proper dirt block to use.
	public BlockRootyDirt getRootyDirtBlock(){
		return GrowingTrees.blockRootyDirt;
	}
	
	public float getTapering(){
		return tapering;
	}

	public float getEnergy(){
		return signalEnergy;
	}
	
	public float getEnergy(World world, int x, int y, int z){
		return getEnergy();
	}
	
	public int getSoilLongevity(){
		return soilLongevity;
	}
	
	public int getSoilLongevity(World world, int x, int y, int z){
		return (int)(biomeSuitability(world, x, y, z) * getSoilLongevity());
	}
	
	//Biome suitability 0.0f for completely unsuited.. 1.0f for perfectly suited
	public float biomeSuitability(World world, int x, int y, int z){
        /*
        BIOME CLIMATE DATA FOR REFERENCE: 
        
        BIOME				NAME				TEMP	PRECIP
        ocean				Ocean				0.5		0.5
        plains				Plains				0.8		0.4
        desert				Desert				2.0		0.0
        extremeHills		Extreme Hills		0.2		0.3
        forest				Forest				0.7		0.8
        taiga				Taiga				0.25	0.8
        swampland			Swampland			0.8		0.9
        river				River				0.5		0.5
        hell				Hell				2.0		0.0
        Sky					Sky					0.5		0.5
        frozenOcean			FrozenOcean			0.0		0.5
        frozenRiver			FrozenRiver			0.0		0.5
        icePlains			Ice Plains			0.0		0.5
        iceMountains		Ice Mountains		0.0		0.5
        mushroomIsland		MushroomIsland		0.9		1.0
        mushroomIslandShore	MushroomIslandShore	0.9		1.0
        beach				Beach				0.8		0.4
        desertHills			DesertHills			2.0		0.0
        forestHills			ForestHills			0.7		0.8
        taigaHills			TaigaHills			0.25	0.8
        extremeHillsEdge	Extreme Hills Edge	0.2		0.3
        jungle				Jungle				0.95	0.9
        jungleHills			JungleHills			0.95	0.9
        jungleEdge			JungleEdge			0.95	0.8
        deepOcean			Deep Ocean			0.5		0.5
        stoneBeach			Stone Beach			0.2		0.3
        coldBeach			Cold Beach			0.05	0.3
        birchForest			Birch Forest		0.6		0.6
        birchForestHills	Birch Forest Hills	0.6		0.6
        roofedForest		Roofed Forest		0.7		0.8
        coldTaiga			Cold Taiga			-0.5	0.4
        coldTaigaHills		Cold Taiga Hills	-0.5	0.4
        megaTaiga			Mega Taiga			0.3		0.8
        megaTaigaHills		Mega Taiga Hills	0.3		0.8
        extremeHillsPlus	Extreme Hills+		0.2		0.3
        savanna				Savanna				1.2		0.0
        savannaPlateau		Savanna Plateau		1.0		0.0
        mesa				Mesa				2.0		0.0
        mesaPlateau_F		Mesa Plateau F		2.0		0.0
        mesaPlateau			Mesa Plateau		2.0		0.0
        */
		
        return 1.0f;
	}
	
	public static boolean isOneOfBiomes(BiomeGenBase biomeToCheck, BiomeGenBase ... biomes){
		for(BiomeGenBase biome: biomes){
			if(biomeToCheck.biomeID == biome.biomeID){
				return true;
			}
		}
		return false;
	}

	public static int isOneOfBiomes(BiomeGenBase biomeToCheck, int valueIfTrue, BiomeGenBase ... biomes){
		return isOneOfBiomes(biomeToCheck, biomes) ? valueIfTrue : 0;
	}
	
	///////////////////////////////////////////
	// PHYSICAL BOUNDS
	///////////////////////////////////////////
	
	//This is only so effective because the center of the player must be inside the block that contains the tree trunk.
	//The result is that only thin branches and trunks can be climbed
	@Override
	public boolean isLadder(IBlockAccess world, int x, int y, int z, EntityLivingBase entity) {
		return true;
	}
	
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess blockAccess, int x, int y, int z) {
		int radius = getRadius(blockAccess, x, y, z);

		if(radius == 1 && isSapling(blockAccess, x, y, z)){
			this.setBlockBounds(0.25f, 0.0f, 0.25f, 0.75f, 0.75f, 0.75f);
		}
		else
		if(radius > 0){
			float rad = radius / 16.0f;
			float minx = 0.5f - rad;
			float miny = 0.5f - rad;
			float minz = 0.5f - rad;
			float maxx = 0.5f + rad;
			float maxy = 0.5f + rad;
			float maxz = 0.5f + rad;

			boolean connectionMade = false;
			
			for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS){
				if(getSideConnectionRadius(blockAccess, x, y, z, radius, dir) > 0){
					connectionMade = true;
					switch(dir){
						case DOWN: miny = 0.0f; break;
						case UP: maxy = 1.0f; break;
						case NORTH: minz = 0.0f; break;
						case SOUTH: maxz = 1.0f; break;
						case WEST: minx = 0.0f; break;
						case EAST: maxx = 1.0f; break;
						default: break;
					}
				}
			}

			if(!connectionMade){
				miny = 0.0f;
				maxy = 1.0f;
			}
			
			this.setBlockBounds(minx, miny, minz, maxx, maxy, maxz);
		}
	}
    
	@Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        this.setBlockBoundsBasedOnState(world, x, y, z);
        return AxisAlignedBB.getBoundingBox(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY, z + this.maxZ);
	}

	@Override
	public int getRadiusForConnection(IBlockAccess world, int x, int y, int z, BlockBranch from, int fromRadius) {
		return getRadius(world, x, y, z);
	}

	public int getSideConnectionRadius(IBlockAccess blockAccess, int x, int y, int z, int radius, ForgeDirection side){
		int dx = x + side.offsetX;
		int dy = y + side.offsetY;
		int dz = z + side.offsetZ;
		return TreeHelper.getSafeTreePart(blockAccess, dx, dy, dz).getRadiusForConnection(blockAccess, dx, dy, dz, this, radius);
	}
	
	///////////////////////////////////////////
	// LEAVES AND SEEDS
	///////////////////////////////////////////
	
	public BlockBranch setGrowingLeavesAndSeeds(String name, BlockGrowingLeaves newGrowingLeaves, int sub, Seed seed){
		growingLeaves = newGrowingLeaves;
		growingLeavesSub = sub;
		if(growingLeaves != null){
			growingLeaves.setSpeciesName(sub, name).setSeed(seed, getGrowingLeavesSub());
			growingLeaves.registerBottomSpecials(sub, new BottomListenerDropItems(new ItemStack(getSeed()), 1f/256f));
		}
		
		return this;
	}

	@Override
	public BlockGrowingLeaves getGrowingLeaves(IBlockAccess blockAccess, int x, int y, int z) {
		return getGrowingLeaves();
	}

	@Override
	public int getGrowingLeavesSub(IBlockAccess blockAccess, int x, int y, int z) {
		return getGrowingLeavesSub();
	}
	
	public BlockGrowingLeaves getGrowingLeaves(){
		return growingLeaves;
	}
	
	public int getGrowingLeavesSub(){
		return growingLeavesSub;
	}
	
	public Seed getSeed(){
		if(getGrowingLeaves() != null){
			return getGrowingLeaves().getSeed(getGrowingLeavesSub());
		}
		return null;
	}
	
	public BlockAndMeta getPrimitiveLeavesBlockRef(){
		return getGrowingLeaves().getPrimitiveLeaves(getGrowingLeavesSub());
	}
	
	public boolean isCompatibleGrowingLeaves(IBlockAccess blockAccess, int x, int y, int z){
		return isCompatibleGrowingLeaves(blockAccess, blockAccess.getBlock(x, y, z), x, y, z);
	}

	public boolean isCompatibleGrowingLeaves(IBlockAccess blockAccess, Block block, int x, int y, int z){
		return isCompatibleGrowingLeaves(block, BlockGrowingLeaves.getSubBlockNum(blockAccess, x, y, z));
	}
	
	public boolean isCompatibleGrowingLeaves(Block leaves, int sub){
		return leaves == getGrowingLeaves() && sub == getGrowingLeavesSub();
	}
	
	public boolean isCompatibleVanillaLeaves(IBlockAccess blockAccess, int x, int y, int z){
		return getPrimitiveLeavesBlockRef().matches(blockAccess, x, y, z);
	}
	
	public boolean isCompatibleGenericLeaves(IBlockAccess blockAccess, int x, int y, int z){
		return isCompatibleGrowingLeaves(blockAccess, x, y, z) || isCompatibleVanillaLeaves(blockAccess, x, y, z);
	}
	
	///////////////////////////////////////////
	// NODE ANALYSIS
	///////////////////////////////////////////
	
	@Override
	public MapSignal analyse(World world, int x, int y, int z, ForgeDirection fromDir, MapSignal signal){
		//Note: fromDir will be ForgeDirection.UNKNOWN in the origin node
		
		if(signal.depth++ < 32){//Prevents going too deep into large networks, or worse, being caught in a network loop
			signal.run(world, this, x, y, z, fromDir);//Run the inspectors of choice

			for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS){//Spread signal in various directions
				if(dir != fromDir){//dont count where the signal originated from
					int dx = x + dir.offsetX;
					int dy = y + dir.offsetY;
					int dz = z + dir.offsetZ;

					signal = TreeHelper.getSafeTreePart(world, dx, dy, dz).analyse(world, dx, dy, dz, dir.getOpposite(), signal);

					//This should only be true for the originating block when the root node is found
					if(signal.found && signal.localRootDir == ForgeDirection.UNKNOWN && fromDir == ForgeDirection.UNKNOWN){
						signal.localRootDir = dir;
					}
				}
			}		
		} else {
			world.setBlockToAir(x, y, z);//Destroy one of the offending nodes
			signal.overflow = true;
		}

		signal.depth--;

		return signal;
	}

	//Destroys all branches recursively not facing the branching direction with the root node
	public void destroyTreeFromNode(World world, int x, int y, int z){
		MapSignal signal = analyse(world, x, y, z, ForgeDirection.UNKNOWN, new MapSignal());//Analyze entire tree network to find root node
		NodeNetVolume volumeSum = new NodeNetVolume();
		analyse(world, x, y, z, signal.localRootDir, new MapSignal(volumeSum, new NodeDestroyer(this)));//Analyze only part of the tree beyond the break point and calculate it's volume
		dropWood(world, x, y, z, volumeSum.getVolume());//Drop an amount of wood calculated from the body of the tree network
	}
	
	public void destroyEntireTree(World world, int x, int y, int z){
		NodeNetVolume volumeSum = new NodeNetVolume();
		analyse(world, x, y, z, ForgeDirection.UNKNOWN, new MapSignal(volumeSum, new NodeDestroyer(this)));
		dropWood(world, x, y, z, volumeSum.getVolume());//Drop an amount of wood calculated from the body of the tree network
	}
	
	///////////////////////////////////////////
	// DROPS
	///////////////////////////////////////////
	
	public void dropWood(World world, int x, int y, int z, int volume){
		int logs = volume / 4096;//A log contains 4096 voxels of wood material(16x16x16 pixels)
		int sticks = (volume % 4096) / 512;//A stick contains 512 voxels of wood (1/8th log) (1 log = 4 planks, 2 planks = 4 sticks)
		dropBlockAsItem(world, x, y, z, primitiveLog.toItemStack(logs));//Drop vanilla logs or whatever
		dropBlockAsItem(world, x, y, z, new ItemStack(Items.stick, sticks));//Give him the stick!
	}
	
	@Override
	public void onBlockHarvested(World world, int x, int y, int z, int localMeta, EntityPlayer player) {
		destroyTreeFromNode(world, x, y, z);
	}
	
	//Explosive harvesting methods will likely result in mostly sticks but i'm okay with that since it kinda makes sense.
	@Override
    public void onBlockExploded(World world, int x, int y, int z, Explosion explosion){
		destroyTreeFromNode(world, x, y, z);
	}
	
	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z) {
		//Normally just sets the block to air but we've already done that.
		//False prevents block harvest as we've already done that also.
		return false;
	}

	@Override
    public int getMobilityFlag() {
        return 2;
    }
	
	///////////////////////////////////////////
	// IRRELEVANT
	///////////////////////////////////////////

	@Override
	public boolean isRootNode() {
		return false;
	}
	
}
