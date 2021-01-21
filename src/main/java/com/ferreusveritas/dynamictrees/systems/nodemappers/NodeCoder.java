package com.ferreusveritas.dynamictrees.systems.nodemappers;

import java.util.ArrayList;

import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class NodeCoder implements INodeInspector {
	
	private class Link {
		BlockPos pos;
		int forks;
		Link[] links;//Links to the other possible 6 directions
		
		public Link(BlockPos pos) {
			this.pos = pos;
			links = new Link[6];
		}
	}
	
	private ArrayList<Link> links;
	
	public NodeCoder() {
		links = new ArrayList<Link>();
	}
	
	@Override
	public boolean run(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir) {
		
		Link link = new Link(pos);
		
		//We've reached the end of a branch and we're starting again.
		for(int i = links.size() - 1; i >= 0; i--) {//We start at the end because that's the most likely place we came from
			Link l = links.get(i);
			if(	pos.getX() + fromDir.getXOffset() == l.pos.getX() &&
				pos.getY() + fromDir.getYOffset() == l.pos.getY() &&
				pos.getZ() + fromDir.getZOffset() == l.pos.getZ()) {
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
	public boolean returnRun(BlockState blockState, IWorld world, BlockPos pos, Direction fromDir) {
		return false;
	}

	public byte[] compile(JoCode joCode) {
		JoCode.CodeCompiler cc = new JoCode.CodeCompiler();

		if(links.size() > 0) {
			nextLink(cc, links.get(0), null);
		}

		return cc.compile();
	}
	
	private void nextLink(JoCode.CodeCompiler cc, Link link, Link fromLink) {

		for(int dir = 0; dir < 6; dir++) {
			Link l = link.links[dir];
			if(l != null && l != fromLink) {
				if(link.forks > 0){
					cc.addFork();
				}
				cc.addDirection((byte) dir);
				nextLink(cc, l, link);
				if(link.forks > 0) {
					cc.addReturn();
					link.forks--;
				}
			}
		}
	}
	
}
