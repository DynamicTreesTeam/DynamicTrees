package com.ferreusveritas.dynamictrees.systems.nodemappers;

import com.ferreusveritas.dynamictrees.api.network.INodeInspector;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.ferreusveritas.dynamictrees.worldgen.JoCode.CodeCompiler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

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

	private final ArrayList<Link> links;

	public NodeCoder() {
		links = new ArrayList<Link>();
	}

	@Override
	public boolean run(IBlockState blockState, World world, BlockPos pos, EnumFacing fromDir) {

		Link link = new Link(pos);

		//We've reached the end of a branch and we're starting again.
		for (int i = links.size() - 1; i >= 0; i--) {//We start at the end because that's the most likely place we came from
			Link l = links.get(i);
			if (pos.getX() + fromDir.getFrontOffsetX() == l.pos.getX() &&
				pos.getY() + fromDir.getFrontOffsetY() == l.pos.getY() &&
				pos.getZ() + fromDir.getFrontOffsetZ() == l.pos.getZ()) {
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
	public boolean returnRun(IBlockState blockState, World world, BlockPos pos, EnumFacing fromDir) {
		return false;
	}

	public byte[] compile(JoCode joCode) {
		CodeCompiler cc = new CodeCompiler();

		if (links.size() > 0) {
			nextLink(cc, links.get(0), null);
		}

		return cc.compile();
	}

	private void nextLink(CodeCompiler cc, Link link, Link fromLink) {

		for (int dir = 0; dir < 6; dir++) {
			Link l = link.links[dir];
			if (l != null && l != fromLink) {
				if (link.forks > 0) {
					cc.addFork();
				}
				cc.addDirection((byte) dir);
				nextLink(cc, l, link);
				if (link.forks > 0) {
					cc.addReturn();
					link.forks--;
				}
			}
		}
	}

}
