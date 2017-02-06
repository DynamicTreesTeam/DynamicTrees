package com.ferreusveritas.growingtrees.worldgen;

import java.util.ArrayList;

import com.ferreusveritas.growingtrees.TreeHelper;
import com.ferreusveritas.growingtrees.blocks.BlockBranch;
import com.ferreusveritas.growingtrees.blocks.BlockGrowingLeaves;
import com.ferreusveritas.growingtrees.blocks.MapSignal;
import com.ferreusveritas.growingtrees.inspectors.NodeInflator;
import com.ferreusveritas.growingtrees.inspectors.NodeCoder;
import com.ferreusveritas.growingtrees.trees.GrowingTree;
import com.ferreusveritas.growingtrees.util.SimpleVoxmap;
import com.ferreusveritas.growingtrees.util.Vec3d;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

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
	
	ArrayList<Byte> instructions;
	private boolean careful = false;//If true the code checks for surrounding branches while building to avoid making frankentrees.  Safer but slower.
	
	public JoCode(){
		instructions = new ArrayList<Byte>();
	}
	
	public JoCode(String code){
		loadCode(code);
	}

	public JoCode loadCode(String code){
		instructions = decode(code);
		return this;
	}
	
	public JoCode setCareful(boolean c){
		careful = c;
		return this;
	}
	
    /**
     * 
     * @param world The world
     * @param seed The seed used to create the tree
     * @param x X-Axis of rootyDirtBlock
     * @param y Y-Axis of rootyDirtBlock
     * @param z Z-Axis of rootyDirtBlock
     */
	public void growTree(World world, GrowingTree tree, int x, int y, int z){
		world.setBlock(x, y, z, tree.getRootyDirtBlock(), 0, 3);//Set to fully fertilized rooty dirt

		//Create tree
		growTreeFork(world, tree, instructions, 0, x, y, z, false);

		//Fix branch thicknesses and map out leaf locations
		BlockBranch branch = TreeHelper.getBranch(world, x, y + 1, z);
		if(branch != null){//If a branch exists then the growth was successful
			SimpleVoxmap leafMap = new SimpleVoxmap(17, 16, 17).setMapAndCenter(new Vec3d(x, y + 1, z), new Vec3d(8, 0, 8));
			NodeInflator integrator = new NodeInflator(leafMap);
			MapSignal signal = new MapSignal(integrator);
			branch.analyse(world, x, y + 1, z, ForgeDirection.DOWN, signal);

			smother(leafMap, branch.getTree());

			BlockGrowingLeaves leavesBlock = branch.getTree().getGrowingLeaves();
			int sub = branch.getTree().getGrowingLeavesSub();

			for(int iy = y + 1; iy < y + 17; iy++){
				if(leafMap.isYTouched(iy)){
					for(int iz = z - 8; iz < z + 8; iz++){
						for(int ix = x - 8; ix < x + 8; ix++){
							byte value = leafMap.getVoxel(ix, iy, iz);
							if((value & 7) != 0){
								Block testBlock = world.getBlock(ix, iy, iz);
								if(testBlock.isReplaceable(world, ix, iy, iz)){
									world.setBlock(ix, iy, iz, leavesBlock, ((sub << 2) & 12) | ((value - 1) & 3), careful ? 3 : 2);
								}
							}
						}
					}
				}
			}
		} else { //The growth failed.. turn the soil to plain dirt
			world.setBlock(x, y, z, Blocks.dirt, 0, careful ? 3 : 2);
		}
		
		
	}
	
    private int growTreeFork(World world, GrowingTree tree, ArrayList<Byte> codes, int pos, int x, int y, int z, boolean disabled){
    	
    	while(pos < codes.size()) {
    		byte code = codes.get(pos);
    		if(code == forkCode) {
    			pos = growTreeFork(world, tree, codes, pos + 1, x, y, z, disabled);
    		} else if(code == returnCode) {
    			return pos + 1;
    		} else {
    			ForgeDirection dir = ForgeDirection.getOrientation(code);
    			x += dir.offsetX;
    			y += dir.offsetY;
    			z += dir.offsetZ;
    			if(!disabled){
    				if(world.getBlock(x, y, z).isReplaceable(world, x, y, z) && (!careful || isClearOfNearbyBranches(world, x, y, z, dir.getOpposite()))) {
    					world.setBlock(x, y, z, tree.getGrowingBranch(), 0, careful ? 3 : 2);
    				} else {
    					disabled = true;
    				}
    			}
    			pos++;
    		}
    	}
    	
    	return pos;
    }
    
	private void smother(SimpleVoxmap leafMap, GrowingTree tree){
		Vec3d saveCenter = new Vec3d(leafMap.getCenter());
    	leafMap.setCenter(new Vec3d());
    	
    	int startY;
    	
		for(startY = leafMap.getLenY() - 1; startY >= 0; startY--){
			if(leafMap.isYTouched(startY)){
				break;
			}
		}
    	
    	for(int iz = 0; iz < leafMap.getLenZ(); iz++){
    		for(int ix = 0; ix < leafMap.getLenX(); ix++){
    			int count = 0;
    			for(int iy = startY; iy >= 0; iy--){
    				int v = leafMap.getVoxel(ix, iy, iz);
    				if(v == 0) {
    					count = 0;//Reset the count
    				} else
    				if(v <= 4) {
    					count++;
    					if(count > tree.smotherLeavesMax){//Smother value
    						leafMap.setVoxel(ix, iy, iz, (byte)0);
    					}
    				} else
    				if(v == 16) {//Twig
    					count++;
   						leafMap.setVoxel(ix, iy + 1, iz, (byte)4);
    				}
    			}
    		}
    	}
    	
    	for(int pass = 0; pass < 2; pass++){
    		for(int iz = 0; iz < leafMap.getLenZ(); iz++){
    			for(int ix = 0; ix < leafMap.getLenX(); ix++){
    				int count = 0;
    				for(int iy = startY; iy >= 0; iy--){
    					int v = leafMap.getVoxel(ix, iy, iz);
    					if(v > 0 && v <= 4){
    						int nv[] = new int[16];
    						for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS){
    							int h = leafMap.getVoxel(ix + dir.offsetX, iy + dir.offsetY, iz + dir.offsetZ);
    							if(h == 16){
    								h = 5;
    							}
    							nv[h & 15]++;
    						}

    						leafMap.setVoxel(ix, iy, iz, (byte) BlockGrowingLeaves.solveCell(nv, tree.cellSolution));//Find center cell's value from neighbors  
    					}    					
    				}
    			}
    		}
    	}
    	
    	leafMap.setCenter(saveCenter);
	}
	
	
    private boolean isClearOfNearbyBranches(World world, int x, int y, int z, ForgeDirection except){
    	
    	for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS){
    		if(dir != except && TreeHelper.getBranch(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ) != null){
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
	public JoCode buildFromTree(World world, int x, int y, int z){
		BlockBranch branch = TreeHelper.getBranch(world, x, y + 1, z);
		if(branch != null){
			NodeCoder coder = new NodeCoder();
			branch.analyse(world, x, y, z, ForgeDirection.DOWN, new MapSignal(coder));
			coder.compile(this);
			instructions.trimToSize();
		}
		return this;
	}
	
	static public String encode(ArrayList<Byte> instructions){
		if((instructions.size() & 1) == 1){//Check if odd
			instructions.add(returnCode);//Add a return code to even up the series
		}

		//Smallest Base64 encoder ever.
		String code = "";
		for(int b = 0; b < instructions.size(); b+=2){
			code += base64.charAt(instructions.get(b) << 3 | instructions.get(b + 1));
		}
		
		return code;
	}
	
	static public ArrayList<Byte> decode(String code){
		ArrayList<Byte> instructions = new ArrayList<Byte>(code.length() * 2);
		
		//Smallest Base64 decoder ever.
    	for(int i = 0; i < code.length(); i++){
    		int sixbits = base64.indexOf(code.charAt(i));
    		if(sixbits != -1){
    			instructions.add((byte) (sixbits >> 3));
    			instructions.add((byte) (sixbits & 7));
    		}
    	}
    	
    	return instructions;
	}
	
	public void addDirection(byte dir){
		if(dir >= 0){
			instructions.add((byte) (dir & 7));
		}
	}
	
	public void addReturn(){
		instructions.add(returnCode);
	}

	public void addFork(){
		instructions.add(forkCode);
	}
	
	@Override
	public String toString(){
		return encode(instructions);
	}
}
