package com.ferreusveritas.dynamictrees.worldgen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.event.SpeciesPostGenerationEvent;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeCoder;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeFindEnds;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap.Cell;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;

/**
* So named because the base64 codes it generates almost always start with "JO"
* 
* This class provides methods for storing and recreating tree shapes.
* 
* @author ferreusveritas
*/
public class JoCode {
	
	static private final String base64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	static protected final byte forkCode = 6;
	static protected final byte returnCode = 7;
	
	public byte[] instructions = new byte[0];
	protected boolean careful = false;//If true the code checks for surrounding branches while building to avoid making frankentrees.  Safer but slower.
	
	/**
	* @param world The world
	* @param rootPos Block position of rootyDirt block
	* @param facing A final rotation applied to the code after creation
	*/
	public JoCode(World world, BlockPos rootPos, EnumFacing facing) {
		Optional<BlockBranch> branch = TreeHelper.getBranchOpt(world.getBlockState(rootPos.up()));
		
		if(branch.isPresent()) {
			NodeCoder coder = new NodeCoder();
			//Warning!  This sends a RootyBlock BlockState into a branch for the kickstart of the analysis.
			branch.get().analyse(world.getBlockState(rootPos), world, rootPos, EnumFacing.DOWN, new MapSignal(coder));
			instructions = coder.compile(this);
			rotate(facing);
		}
	}
	
	/**
	 * Build a JoCode instruction set from the tree found at pos.
	 * 
	 * @param world The world
	 * @param pos Position of the rooty dirt block
	 */
	public JoCode(World world, BlockPos pos) {
		this(world, pos, EnumFacing.SOUTH);
	}
	
	public JoCode(String code) {
		instructions = decode(code);
	}
	
	public JoCode setCareful(boolean c) {
		careful = c;
		return this;
	}
	
	/**
	 * A facing matrix for mapping instructions to different rotations
	 */
	private byte dirmap[][] = {
		//  {D, U, N, S, W, E, F, R}
			{0, 1, 2, 3, 4, 5, 6, 7},//FACING DOWN:	 Same as NORTH
			{0, 1, 2, 3, 4, 5, 6, 7},//FACING UP:	 Same as NORTH
			{0, 1, 2, 3, 4, 5, 6, 7},//FACING NORTH: N->N S->S W->W E->E 0
			{0, 1, 3, 2, 5, 4, 6, 7},//FACING SOUTH: N->S S->N W->E E->W 180
			{0, 1, 5, 4, 2, 3, 6, 7},//FACING WEST:	 N->E S->W W->N E->S 90 CW
			{0, 1, 4, 5, 3, 2, 6, 7},//FACING EAST:	 N->W S->E W->S E->N 90 CCW
		};
	
	//"Pointers" to the current rotation direction.
	private byte facingMap[] = dirmap[2];//Default to NORTH(Effectively an identity matrix)
	private byte unfacingMap[] = dirmap[2];//Default to NORTH(Effectively an identity matrix)
	
	/**
	 * Get the instruction at a locus.  
	 * Automatically performs rotation based on what facing matrix is selected.
	 * 
	 * @param pos
	 * @return
	 */
	protected int getCode(int pos) {
		return unfacingMap[instructions[pos]];
	}
	
	/**
	 * Sets the active facing matrix to a specific direction
	 * 
	 * @param facing
	 * @return
	 */
	public JoCode setFacing(EnumFacing facing) {
		int faceNum = facing.ordinal();
		facingMap = dirmap[faceNum];
		faceNum = (faceNum == 4) ? 5 : (faceNum == 5) ? 4 : faceNum;//Swap West and East
		unfacingMap = dirmap[faceNum];
		return this;
	}
	
	/**
	 * Rotates the JoCode such that the model's "north" faces a new direction.
	 * 
	 * @param dir
	 * @return
	 */
	public JoCode rotate(EnumFacing dir) {
		setFacing(dir);
		for(int c = 0; c < instructions.length; c++) {
			instructions[c] = facingMap[instructions[c]];
		}
		return this;
	}
	
	/**
	* Generate a tree from a JoCode instruction list.
	* 
	* @param world The world
	* @param seed The seed used to create the tree
	* @param rootPos The position of what will become the rootydirt block
	* @param biome The biome of the coordinates.
	* @param facing Direction of tree
	* @param radius Constraint radius
	*/
	public void generate(World world, Species species, BlockPos rootPos, Biome biome, EnumFacing facing, int radius, SafeChunkBounds safeBounds) {
		
		boolean worldGen = safeBounds != SafeChunkBounds.ANY;
		
		//A Tree generation boundary radius is at least 2 and at most 8
		radius = MathHelper.clamp(radius, 2, 8);
		
		setFacing(facing);
		rootPos = species.preGeneration(world, rootPos, radius, facing, safeBounds, this);

		
		if(rootPos != BlockPos.ORIGIN) {
			IBlockState initialDirtState = world.getBlockState(rootPos);//Save the initial state of the dirt in case this fails
			species.placeRootyDirtBlock(world, rootPos, 0);//Set to unfertilized rooty dirt
			
			//Make the tree branch structure
			generateFork(world, species, 0, rootPos, false);

			//Establish a position for the bottom block of the trunk
			BlockPos treePos = rootPos.up();
			
			//Fix branch thicknesses and map out leaf locations
			IBlockState treeState = world.getBlockState(treePos);
			BlockBranch branch = TreeHelper.getBranch(treeState);
			if(branch != null) {//If a branch exists then the growth was successful
				ILeavesProperties leavesProperties = species.getLeavesProperties();
				SimpleVoxmap leafMap = new SimpleVoxmap(radius * 2 + 1, species.getWorldGenLeafMapHeight(), radius * 2 + 1).setMapAndCenter(treePos, new BlockPos(radius, 0, radius));
				INodeInspector inflator = species.getNodeInflator(leafMap);//This is responsible for thickening the branches
				NodeFindEnds endFinder = new NodeFindEnds();//This is responsible for gathering a list of branch end points
				MapSignal signal = new MapSignal(inflator, endFinder);//The inflator signal will "paint" a temporary voxmap of all of the leaves and branches.
				branch.analyse(treeState, world, treePos, EnumFacing.DOWN, signal);
				List<BlockPos> endPoints = endFinder.getEnds();
				
				smother(leafMap, leavesProperties);//Use the voxmap to precompute leaf smothering so we don't have to age it as many times.
				
				//Place Growing Leaves Blocks from voxmap
				for(Cell cell: leafMap.getAllNonZeroCells((byte) 0x0F)) {//Iterate through all of the cells that are leaves(not air or branches)
					MutableBlockPos cellPos = cell.getPos();
					if(safeBounds.inBounds(cellPos, false)) {
						IBlockState testBlockState = world.getBlockState(cellPos);
						Block testBlock = testBlockState.getBlock();
						if(testBlock.isReplaceable(world, cellPos)) {
							world.setBlockState(cellPos, leavesProperties.getDynamicLeavesState(cell.getValue()), worldGen ? 16 : 2);//Flag 16 to prevent observers from causing cascading lag
						}
					} else {
						leafMap.setVoxel(cellPos, (byte) 0);
					}
				}
				
				//Shrink the leafMap down by the safeBounds object so that the aging process won't look for neighbors outside of the bounds.
				for(Cell cell: leafMap.getAllNonZeroCells()) {
					MutableBlockPos cellPos = cell.getPos();
					if(!safeBounds.inBounds(cellPos, true)) {
						leafMap.setVoxel(cellPos, (byte) 0);
					}
				}
				
				//Age volume for 3 cycles using a leafmap
				TreeHelper.ageVolume(world, leafMap, species.getWorldGenAgeIterations(), safeBounds);
				
				//Rot the unsupported branches
				if(species.handleRot(world, endPoints, rootPos, treePos, 0, safeBounds)) {
					return;//The entire tree rotted away before it had a chance
				}
				
				//Allow for special decorations by the tree itself
				species.postGeneration(world, rootPos, biome, radius, endPoints, safeBounds, initialDirtState);
				MinecraftForge.EVENT_BUS.post(new SpeciesPostGenerationEvent(world, species, rootPos, endPoints, safeBounds, initialDirtState));
				
				//Add snow to parts of the tree in chunks where snow was already placed
				addSnow(leafMap, world, rootPos, biome);
				
			} else { //The growth failed.. turn the soil back to what it was
				world.setBlockState(rootPos, initialDirtState, careful ? 3 : 2);
			}
		}
	}
	
	/**
	 * Recursive function that "draws" a branch of a tree
	 * 
	 * @param world
	 * @param species
	 * @param codePos
	 * @param pos
	 * @param disabled
	 * @return
	 */
	protected int generateFork(World world, Species species, int codePos, BlockPos pos, boolean disabled) {
		
		while(codePos < instructions.length) {
			int code = getCode(codePos);
			switch(code) {
				case forkCode: codePos = generateFork(world, species, codePos + 1, pos, disabled); break;
				case returnCode: return codePos + 1;
				default:
					EnumFacing dir = EnumFacing.getFront(code);
					pos = pos.offset(dir);
					if(!disabled) {
						disabled = setBlockForGeneration(world, species, pos, dir, careful);
					}
					codePos++;
					break;
			}
		}
		
		return codePos;
	}
	
	protected boolean setBlockForGeneration(World world, Species species, BlockPos pos, EnumFacing dir, boolean careful) {
		if(world.getBlockState(pos).getBlock().isReplaceable(world, pos) && (!careful || isClearOfNearbyBranches(world, pos, dir.getOpposite()))) {
			species.getFamily().getDynamicBranch().setRadius(world, pos, (int)species.getFamily().getPrimaryThickness(), null, careful ? 3 : 2);
			return false;
		}
		return true;
	}
	
	/**
	 * Precompute leaf smothering before applying to the world.
	 * 
	 * @param leafMap
	 * @param leavesProperties
	 */
	protected void smother(SimpleVoxmap leafMap, ILeavesProperties leavesProperties) {
		
		int smotherMax = leavesProperties.getSmotherLeavesMax();
		
		if(smotherMax != 0) { //Smothering is disabled if set to 0
			
			BlockPos saveCenter = leafMap.getCenter();
			leafMap.setCenter(new BlockPos(0, 0, 0));
			
			int startY;
			
			//Find topmost block in build volume
			for(startY = leafMap.getLenY() - 1; startY >= 0; startY--) {
				if(leafMap.isYTouched(startY)) {
					break;
				}
			}
			
			//Precompute smothering
			for(int iz = 0; iz < leafMap.getLenZ(); iz++) {
				for(int ix = 0; ix < leafMap.getLenX(); ix++) {
					int count = 0;
					for(int iy = startY; iy >= 0; iy--) {
						int v = leafMap.getVoxel(new BlockPos(ix, iy, iz));
						if(v == 0) {//Air
							count = 0;//Reset the count
						} else
						if((v & 0x0F) != 0) {//Leaves
							count++;
							if(count > smotherMax){//Smother value
								leafMap.setVoxel(new BlockPos(ix, iy, iz), (byte)0);
							}
						} else
						if((v & 0x10) != 0) {//Twig
							count++;
							leafMap.setVoxel(new BlockPos(ix, iy + 1, iz), (byte)4);
						}
					}
				}
			}
			
			leafMap.setCenter(saveCenter);
		}
		
	}
	
	protected boolean isClearOfNearbyBranches(World world, BlockPos pos, EnumFacing except) {
		
		for(EnumFacing dir: EnumFacing.VALUES) {
			if(dir != except && TreeHelper.getBranch(world.getBlockState(pos.offset(dir))) != null) {
				return false;
			}
		}
		
		return true;
	}
	
	protected void addSnow(SimpleVoxmap leafMap, World world, BlockPos rootPos, Biome biome) {
		
		if(biome.getDefaultTemperature() < 0.4f) {
			for ( MutableBlockPos top : leafMap.getTops() ) {
				if ( world.canSnowAt(top, false) ) {
					MutableBlockPos iPos = new MutableBlockPos(top);
					int yOffset = 0;
					do {
						IBlockState state = world.getBlockState(iPos);
						if(state.getMaterial() == Material.AIR) {
							world.setBlockState(iPos, ModBlocks.blockStates.snowLayer, 2);
							break;
						}
						else if (state.getBlock() == ModBlocks.blockLeavesSnow || state.getBlock() == Blocks.SNOW_LAYER) {
							break;
						}
						iPos.setY(iPos.getY() + 1);
					} while (yOffset++ < 4);
				}
			}
		}
		
	}
	
	static public String encode(byte[] array) {
		
		//Convert byte array to ArrayList of Byte
		ArrayList<Byte> instructions = new ArrayList<>(array.length + (array.length & 1));
		for(byte b : array) {
			instructions.add(b);
		}
		
		if((instructions.size() & 1) == 1) {//Check if odd
			instructions.add(returnCode);//Add a return code to even up the series
		}
		
		//Smallest Base64 encoder ever.
		String code = "";
		for(int b = 0; b < instructions.size(); b+=2) {
			code += base64.charAt(instructions.get(b) << 3 | instructions.get(b + 1));
		}
		
		return code;
	}
	
	static public byte[] decode(String code) {
		return new CodeCompiler(code).compile();
	}
	
	@Override
	public String toString() {
		return encode(instructions);
	}
	
	/**
	 * A tidy class for handling byte code adding and conversion to byte array 
	 */
	public static class CodeCompiler {
		
		ArrayList<Byte> instructions;
		
		public CodeCompiler() {
			instructions = new ArrayList<>();
		}

		public CodeCompiler(int size) {
			instructions = new ArrayList<>(size);
		}
		
		public CodeCompiler(String code) {
			instructions = new ArrayList<>(code.length() * 2);
			
			//Smallest Base64 decoder ever.
			for(int i = 0; i < code.length(); i++) {
				int sixbits = base64.indexOf(code.charAt(i));
				if(sixbits != -1) {
					addInstruction((byte) (sixbits >> 3));
					addInstruction((byte) (sixbits & 7));
				}
			}
		}
		
		public void addDirection(byte dir) {
			if(dir >= 0){
				instructions.add((byte) (dir & 7));
			}
		}
		
		public void addInstruction(byte instruction) {
			instructions.add(instruction);
		}
		
		public void addReturn() {
			instructions.add(returnCode);
		}
		
		public void addFork() {
			instructions.add(forkCode);
		}
		
		public byte[] compile() {
			byte array[] = new byte[instructions.size()];
			Iterator<Byte> i = instructions.iterator();
			
			int pos = 0;
			while(i.hasNext()) {
				array[pos++] = i.next().byteValue();
			}
			
			return array;
		}
	}
	
}
