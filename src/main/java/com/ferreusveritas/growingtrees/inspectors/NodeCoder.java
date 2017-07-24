package com.ferreusveritas.growingtrees.inspectors;

import java.util.ArrayList;

import com.ferreusveritas.growingtrees.worldgen.JoCode;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class NodeCoder implements INodeInspector {

	private class Link {
		long x;
		long y;
		long z;
		int forks;
		Link links[];//Links to the other possible 6 directions
		
		public Link(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
			links = new Link[6];
		}
	}

	private ArrayList<Link> links;

	public NodeCoder() {
		links = new ArrayList<Link>();
	}

	@Override
	public boolean run(World world, Block block, int x, int y, int z, ForgeDirection fromDir) {

		Link link = new Link(x, y, z);
		
		//We've reached the end of a branch and we're starting again.
		for(int i = links.size() - 1; i >= 0; i--) {//We start at the end because that's the most likely place we came from
			Link l = links.get(i);
			if(x + fromDir.offsetX == l.x && y + fromDir.offsetY == l.y && z + fromDir.offsetZ == l.z) {
				//Create linkage
				l.links[fromDir.getOpposite().ordinal()] = link;
				link.links[fromDir.ordinal()] = l;
				l.forks += i != links.size() - 1 ? 1 : 0;//If the link we are working on is not the last in the list then that means we just forked
				break;
			}
		}

		links.add(link);
		
		return false;
	}

	@Override
	public boolean returnRun(World world, Block block, int x, int y, int z, ForgeDirection fromDir) {
		return false;
	}
	
	public void compile(JoCode joCode, ForgeDirection facingDir) {
		if(links.size() > 0) {
			nextLink(links.get(0), null, joCode);
		}
	
		joCode.rotate(facingDir);
	}
	
	private void nextLink(Link link, Link fromLink, JoCode joCode) {
		
		for(int i = 0; i < 6; i++) {
			Link l = link.links[i];
			if(l != null && l != fromLink) {
				if(link.forks > 0){
					joCode.addFork();
				}
				joCode.addDirection((byte) i);
				nextLink(l, link, joCode);
				if(link.forks > 0) {
					joCode.addReturn();
					link.forks--;
				}
			}
		}
	}

}
