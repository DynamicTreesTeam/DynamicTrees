package com.ferreusveritas.dynamictrees.worldgen;

import java.util.ArrayList;
import java.util.List;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.backport.Biome;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.inspectors.NodeCoder;
import com.ferreusveritas.dynamictrees.inspectors.NodeFindEnds;
import com.ferreusveritas.dynamictrees.inspectors.NodeInflator;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.MathHelper;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap.Cell;

import net.minecraft.block.Block;

/**
* So named because the base64 codes it generates almost always start with "JO"
* 
* This class provides methods for storing and recreating tree shapes.
* 
* @author ferreusveritas
*/
public class JoCode {

	static private final String base64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	static private final byte forkCode = 6;
	static private final byte returnCode = 7;

	public ArrayList<Byte> instructions;
	private boolean careful = false;//If true the code checks for surrounding branches while building to avoid making frankentrees.  Safer but slower.

	public JoCode() {
		instructions = new ArrayList<Byte>();
	}

	public JoCode(String code) {
		loadCode(code);
	}

	public JoCode loadCode(String code) {
		instructions = decode(code);
		return this;
	}

	public JoCode setCareful(boolean c) {
		careful = c;
		return this;
	}

	/**
	 * A facing matrix for mapping instructions to different rotations
	 */
	private int dirmap[][] = {
		//  {D, U, N, S, W, E, F, R}
			{0, 1, 2, 3, 4, 5, 6, 7},//FACING DOWN:	 Same as NORTH
			{0, 1, 2, 3, 4, 5, 6, 7},//FACING UP:	 Same as NORTH
			{0, 1, 2, 3, 4, 5, 6, 7},//FACING NORTH: N->N S->S W->W E->E 0
			{0, 1, 3, 2, 5, 4, 6, 7},//FACING SOUTH: N->S S->N W->E E->W 180
			{0, 1, 5, 4, 2, 3, 6, 7},//FACING WEST:	 N->E S->W W->N E->S 90 CW
			{0, 1, 4, 5, 3, 2, 6, 7},//FACING EAST:	 N->W S->E W->S E->N 90 CCW
		};

	//"Pointers" to the current rotation direction.
	private int facingMap[] = dirmap[2];//Default to NORTH(Effectively an identity matrix)
	private int unfacingMap[] = dirmap[2];//Default to NORTH(Effectively an identity matrix)

	/**
	 * Get the instruction at a locus.  
	 * Automatically performs rotation based on what facing matrix is selected.
	 * 
	 * @param pos
	 * @return
	 */
	private int getCode(int pos) {
		return unfacingMap[instructions.get(pos)];
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
		for(int c = 0; c < instructions.size(); c++) {
			instructions.set(c, (byte) facingMap[instructions.get(c)]);
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
	public void generate(World world, Species species, BlockPos rootPos, Biome biome, EnumFacing facing, int radius) {
			IBlockState initialState = world.getBlockState(rootPos);//Save the initial state of the dirt in case this fails
		world.setBlockState(rootPos, species.getRootyDirtBlock().getDefaultState().withProperty(BlockRootyDirt.LIFE, 0));//Set to unfertilized rooty dirt

		//A Tree generation boundary radius is at least 2 and at most 8
		radius = MathHelper.clamp(radius, 2, 8);
		BlockPos treePos = rootPos.up();
		
		//Create tree
		setFacing(facing);
		generateFork(world, species, 0, rootPos, false);

		//Fix branch thicknesses and map out leaf locations
		BlockBranch branch = TreeHelper.getBranch(world, treePos);
		if(branch != null) {//If a branch exists then the growth was successful
			SimpleVoxmap leafMap = new SimpleVoxmap(radius * 2 + 1, 32, radius * 2 + 1).setMapAndCenter(treePos, new BlockPos(radius, 0, radius));
			NodeInflator inflator = new NodeInflator(species, leafMap);//This is responsible for thickening the branches
			NodeFindEnds endFinder = new NodeFindEnds();//This is responsible for gathering a list of branch end points
			MapSignal signal = new MapSignal(inflator, endFinder);//The inflator signal will "paint" a temporary voxmap of all of the leaves and branches.
			branch.analyse(world, treePos, EnumFacing.DOWN, signal);
			List<BlockPos> endPoints = endFinder.getEnds();
			
			smother(leafMap, branch.getTree());//Use the voxmap to precompute leaf smothering so we don't have to age it as many times.
			
			//Establish a zone where we can place leaves without hitting ungenerated chunks.
			SafeChunkBounds safeBounds = new SafeChunkBounds(world, rootPos);//Area that is safe to place leaves during worldgen
			
			//Place Growing Leaves Blocks from voxmap
			IBlockState leavesState = branch.getTree().getDynamicLeavesState();
			for(Cell cell: leafMap.getAllNonZeroCells((byte) 0x0F)) {//Iterate through all of the cells that are leaves(not air or branches)
				BlockPos cellPos = cell.getPos();
				if(safeBounds.inBounds(cellPos)) {
					IBlockState testBlockState = world.getBlockState(cellPos);
					Block testBlock = testBlockState.getBlock();
					if(testBlock.isReplaceable(world, cellPos.getX(), cellPos.getY(), cellPos.getZ())) {
						world.setBlockState(cellPos, leavesState.withProperty(BlockDynamicLeaves.HYDRO, MathHelper.clamp(cell.getValue(), 1, 4)), careful ? 2 : 0);
					}
				} else {
					leafMap.setVoxel(cellPos, (byte) 0);
				}
			}

			//Shrink the safeBounds down by 1 so that the aging process won't look for neighbors outside of the bounds.
			safeBounds.setShrink(1);
			for(Cell cell: leafMap.getAllNonZeroCells((byte) 0x0F)) {
				BlockPos cellPos = cell.getPos();
				if(!safeBounds.inBounds(cellPos)) {
					leafMap.setVoxel(cellPos, (byte) 0);
				}
			}
			
			//Age volume for 3 cycles using a leafmap
			TreeHelper.ageVolume(world, treePos, radius, 32, leafMap, 3);
			
			//Rot the unsupported branches
			species.handleRot(world, endPoints, rootPos, treePos, 0, true);
			
			//Allow for special decorations by the tree itself
			species.postGeneration(world, rootPos, biome, radius, endPoints, !careful);
		
		} else { //The growth failed.. turn the soil back to what it was
			world.setBlockState(rootPos, initialState, careful ? 3 : 2);
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
	private int generateFork(World world, Species species, int codePos, BlockPos pos, boolean disabled) {

		while(codePos < instructions.size()) {
			int code = getCode(codePos);
			if(code == forkCode) {
				codePos = generateFork(world, species, codePos + 1, pos, disabled);
			} else if(code == returnCode) {
				return codePos + 1;
			} else {
				EnumFacing dir = EnumFacing.getFront(code);
				pos = pos.offset(dir);
				if(!disabled) {
					if(world.getBlockState(pos).getBlock().isReplaceable(world, pos.getX(), pos.getY(), pos.getZ()) && (!careful || isClearOfNearbyBranches(world, pos, dir.getOpposite()))) {
						world.setBlockState(pos, species.getTree().getDynamicBranch().getDefaultState(), careful ? 3 : 2);
					} else {
						disabled = true;
					}
				}
				codePos++;
			}
		}

		return codePos;
	}

	/**
	 * Precompute leaf smothering before applying to the world.
	 * 
	 * @param leafMap
	 * @param tree
	 */
	private void smother(SimpleVoxmap leafMap, DynamicTree tree) {
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
						if(count > tree.getSmotherLeavesMax()){//Smother value
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

	private boolean isClearOfNearbyBranches(World world, BlockPos pos, EnumFacing except) {

		for(EnumFacing dir: EnumFacing.VALUES) {
			if(dir != except && TreeHelper.getBranch(world, pos.offset(dir)) != null) {
				return false;
			}
		}

		return true;
	}

	/**
	* @param world The world
	* @param pos Block position of rootyDirt block
	* @return JoCode for chaining
	*/
	public JoCode buildFromTree(World world, BlockPos pos, EnumFacing facing) {
		BlockBranch branch = TreeHelper.getBranch(world, pos.up());
		if(branch != null) {
			NodeCoder coder = new NodeCoder();
			branch.analyse(world, pos, EnumFacing.DOWN, new MapSignal(coder));
			coder.compile(this, facing);
			instructions.trimToSize();
		}
		return this;
	}

	/**
	 * Build a JoCode instruction set from the tree found at pos.
	 * 
	 * @param world The world
	 * @param pos Position of the rooty dirt block
	 * @return resulting JoCode or nul" if no tree is found.
	 */
	public JoCode buildFromTree(World world, BlockPos pos) {
		return buildFromTree(world, pos, EnumFacing.NORTH);
	}

	static public String encode(ArrayList<Byte> instructions) {
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

	static public ArrayList<Byte> decode(String code) {
		ArrayList<Byte> instructions = new ArrayList<Byte>(code.length() * 2);
		
		//Smallest Base64 decoder ever.
		for(int i = 0; i < code.length(); i++) {
			int sixbits = base64.indexOf(code.charAt(i));
			if(sixbits != -1) {
				instructions.add((byte) (sixbits >> 3));
				instructions.add((byte) (sixbits & 7));
			}
		}

		return instructions;
	}

	public void addDirection(byte dir) {
		if(dir >= 0){
			instructions.add((byte) (dir & 7));
		}
	}

	public void addReturn() {
		instructions.add(returnCode);
	}

	public void addFork() {
		instructions.add(forkCode);
	}

	@Override
	public String toString() {
		return encode(instructions);
	}

}
