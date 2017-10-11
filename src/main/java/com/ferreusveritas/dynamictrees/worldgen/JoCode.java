package com.ferreusveritas.dynamictrees.worldgen;

import java.util.ArrayList;

import com.ferreusveritas.dynamictrees.api.IAgeable;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockGrowingLeaves;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.inspectors.NodeInflator;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.inspectors.NodeCoder;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

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
	//static private SimpleVoxmap leafMap = new SimpleVoxmap(17, 32, 17).setCenter(new Vec3d(8, 0, 8));

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

	int dirmap[][] = {
		//  {D, U, N, S, W, E, F, R}
			{0, 1, 2, 3, 4, 5, 6, 7},//FACING DOWN:	 Same as NORTH
			{0, 1, 2, 3, 4, 5, 6, 7},//FACING UP:	 Same as NORTH
			{0, 1, 2, 3, 4, 5, 6, 7},//FACING NORTH: N->N S->S W->W E->E 0
			{0, 1, 3, 2, 5, 4, 6, 7},//FACING SOUTH: N->S S->N W->E E->W 180
			{0, 1, 5, 4, 2, 3, 6, 7},//FACING WEST:	 N->E S->W W->N E->S 90 CW
			{0, 1, 4, 5, 3, 2, 6, 7},//FACING EAST:	 N->W S->E W->S E->N 90 CCW
		};

	int facingMap[] = dirmap[2];
	int unfacingMap[] = dirmap[2];

	private int getCode(int pos) {
		return unfacingMap[instructions.get(pos)];
	}

	public JoCode setFacing(EnumFacing facing) {
		facingMap = dirmap[facing.ordinal()];
		int faceNum = facing.ordinal();
		faceNum = (faceNum == 4) ? 5 : (faceNum == 5) ? 4 : faceNum;//Swap West and East
		unfacingMap = dirmap[faceNum];
		return this;
	}

	public JoCode rotate(EnumFacing dir) {
		setFacing(dir);
		for(int c = 0; c < instructions.size(); c++) {
			instructions.set(c, (byte) facingMap[instructions.get(c)]);
		}
		return this;
	}

	/**
	* 
	* @param world The world
	* @param seed The seed used to create the tree
	* @param x X-Axis of rootyDirtBlock
	* @param y Y-Axis of rootyDirtBlock
	* @param z Z-Axis of rootyDirtBlock
	* @param facing Direction of tree
	* @param radius Constraint radius
	*/
	public void growTree(World world, DynamicTree tree, BlockPos pos, EnumFacing facing, int radius) {
		world.setBlockState(pos, tree.getRootyDirtBlock().getDefaultState().withProperty(BlockRootyDirt.LIFE, 0));//Set to unfertilized rooty dirt

		//Create tree
		setFacing(facing);
		growTreeFork(world, tree, 0, pos, false);

		radius = MathHelper.clamp(radius, 2, 8);

		//Fix branch thicknesses and map out leaf locations
		BlockBranch branch = TreeHelper.getBranch(world, pos.up());
		if(branch != null){//If a branch exists then the growth was successful
			SimpleVoxmap leafMap = new SimpleVoxmap(radius * 2 + 1, 32, radius * 2 + 1).setMapAndCenter(pos.up(), new BlockPos(radius, 0, radius));
			NodeInflator integrator = new NodeInflator(leafMap);
			MapSignal signal = new MapSignal(integrator);
			branch.analyse(world, pos.up(), EnumFacing.DOWN, signal);

			smother(leafMap, branch.getTree());

			BlockGrowingLeaves leavesBlock = branch.getTree().getGrowingLeaves();
			int treeSub = branch.getTree().getGrowingLeavesSub();

			final int maxH = 32;

			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();

			for(int iy = y + 1; iy < y + maxH + 1; iy++) {
				if(leafMap.isYTouched(iy)) {
					for(int iz = z - radius; iz < z + radius; iz++) {
						for(int ix = x - radius; ix < x + radius; ix++) {
							byte value = leafMap.getVoxel(new BlockPos(ix, iy, iz));
							if((value & 7) != 0) {
								BlockPos iPos = new BlockPos(ix, iy, iz);
								Block testBlock = world.getBlockState(iPos).getBlock();
								if(testBlock.isReplaceable(world, iPos)) {
									world.setBlockState(iPos, leavesBlock.getDefaultState().withProperty(BlockGrowingLeaves.TREE, treeSub).withProperty(BlockGrowingLeaves.HYDRO, MathHelper.clamp(value, 1, 4)), careful ? 2 : 0);
								}
							}
						}
					}
				}
			}

			for(int pass = 0; pass < 5; pass++) {
				for(int iy = y + 1; iy < y + maxH + 1; iy++) {
					if(leafMap.isYTouched(iy)) {
						for(int iz = z - radius; iz < z + radius; iz++) {
							for(int ix = x - radius; ix < x + radius; ix++) {
								byte value = leafMap.getVoxel(new BlockPos(ix, iy, iz));
								if(value != 0) {
									IBlockState blockState = world.getBlockState(new BlockPos(ix, iy, iz));
									Block block = blockState.getBlock();
									if(block instanceof IAgeable) {
										((IAgeable)block).age(world, new BlockPos(ix, iy, iz), blockState, world.rand, true);
									}
								}
							}
						}
					}
				}
			}
		} else { //The growth failed.. turn the soil to plain dirt
			world.setBlockState(pos, Blocks.DIRT.getDefaultState(), careful ? 3 : 2);
		}

	}

	private int growTreeFork(World world, DynamicTree tree, int codePos, BlockPos pos, boolean disabled) {

		while(codePos < instructions.size()) {
			int code = getCode(codePos);
			if(code == forkCode) {
				codePos = growTreeFork(world, tree, codePos + 1, pos, disabled);
			} else if(code == returnCode) {
				return codePos + 1;
			} else {
				EnumFacing dir = EnumFacing.getFront(code);
				pos = pos.offset(dir);
				if(!disabled) {
					if(world.getBlockState(pos).getBlock().isReplaceable(world, pos) && (!careful || isClearOfNearbyBranches(world, pos, dir.getOpposite()))) {
						world.setBlockState(pos, tree.getGrowingBranch().getDefaultState(), careful ? 3 : 2);
					} else {
						disabled = true;
					}
				}
				codePos++;
			}
		}

		return codePos;
	}

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
					if(v == 0) {
						count = 0;//Reset the count
					} else
					if(v <= 4) {
						count++;
						if(count > tree.getSmotherLeavesMax()){//Smother value
							leafMap.setVoxel(new BlockPos(ix, iy, iz), (byte)0);
						}
					} else
					if(v == 16) {//Twig
						count++;
						leafMap.setVoxel(new BlockPos(ix, iy + 1, iz), (byte)4);
					}
				}
			}
		}

		//Precompute leaf death from dryness
		for(int pass = 0; pass < 2; pass++) {
			for(int iz = 0; iz < leafMap.getLenZ(); iz++) {
				for(int ix = 0; ix < leafMap.getLenX(); ix++){
					for(int iy = startY; iy >= 0; iy--) {
						int v = leafMap.getVoxel(new BlockPos(ix, iy, iz));
						if(v > 0 && v <= 4) {
							int nv[] = new int[16];
							for(EnumFacing dir: EnumFacing.VALUES) {
								int h = leafMap.getVoxel(new BlockPos(ix, iy, iz).offset(dir));
								if(h == 16){
									h = 5;
								}
								nv[h & 15]++;
							}
							leafMap.setVoxel(new BlockPos(ix, iy, iz), (byte) BlockGrowingLeaves.solveCell(nv, tree.getCellSolution()));//Find center cell's value from neighbors  
						}
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
	* @param x X-Axis coordinate of rootyDirt block
	* @param y Y-Axis coordinate of rootyDirt block
	* @param z Z-Axis coordinate of rootyDirt block
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
