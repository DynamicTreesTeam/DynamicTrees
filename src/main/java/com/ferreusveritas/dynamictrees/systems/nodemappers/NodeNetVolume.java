package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.HashMap;
import java.util.Map;

public class NodeNetVolume implements INodeInspector {

	public static final class Volume {
		public static final int VOXELSPERLOG = 4096; //A log contains 4096 voxels of wood material(16x16x16 pixels)

		private Map<Integer, Integer> branchVolumes; //VOLUME IN VOXELS
		int maxBranch;

		public Volume() { this(0); }
		public Volume(int volume) {
			branchVolumes = new HashMap<>();
			branchVolumes.put(0, volume);
			maxBranch = 0;
		}
		public Volume(int... volumes) {
			branchVolumes = new HashMap<>();
			for (int i=0; i< volumes.length; i++){
				if (volumes[i] != 0){
					branchVolumes.put(i, volumes[i]);
				}
			}
			maxBranch = volumes.length;
		}

		public void addVolume (int volume) {
			addVolume(volume, 0);
		}

		public void addVolume (int volume, int branch) {
			if (branchVolumes.containsKey(branch)){
				branchVolumes.computeIfPresent(branch, (b,v)->v+volume);
			} else {
				branchVolumes.put(branch, volume);
			}
			if (branch > maxBranch) maxBranch = branch;
		}

		public void multiplyVolume (double multiplier) {
			branchVolumes.forEach((a,b)-> b = (int)(b*multiplier));
		}

		public int[] getVolumesArray(){
			int[] volumes = new int[maxBranch];
			for (int i = 0; i<maxBranch; i++){
				int vol = 0;
				if (branchVolumes.containsKey(i))
					vol = branchVolumes.get(i);
				volumes[i] = vol;
			}
			return volumes;
		}

		public float getVolume() {
			return getRawVolume() / (float) VOXELSPERLOG;
		}
		public float getVolume(int branch) {
			return getRawVolume(branch) / (float) VOXELSPERLOG;
		}

		public int getRawVolume() {
			int totalVolume = 0;
			for (int i=0; i<branchVolumes.size(); i++){
				totalVolume+=getRawVolume(i);
			}
			return totalVolume;
		}
		public int getRawVolume(int branch){
			return branchVolumes.get(branch);
		}

	}

	private final Volume volume = new Volume();//number of voxels(1x1x1 pixels) of wood accumulated from network analysis

	@Override
	public boolean run(BlockState state, IWorld world, BlockPos pos, Direction fromDir) {
		if(TreeHelper.isBranch(state)) {
			ITreePart treePart = TreeHelper.getTreePart(state);
			int radius = treePart.getRadius(state);
			volume.addVolume(radius * radius * 64);//Integrate volume of this tree part into the total volume calculation
		}
		return true;
	}
	
	@Override
	public boolean returnRun(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir) {
		return false;
	}
	
	public Volume getVolume () {
		return volume;
	}

}
