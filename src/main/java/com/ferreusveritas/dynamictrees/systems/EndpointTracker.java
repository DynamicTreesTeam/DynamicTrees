package com.ferreusveritas.dynamictrees.systems;

import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

//** This is highly experimental leaves-fall-off-in-winter stuff that will probably never be implemented */ 
public class EndpointTracker {

	public static class ChunkLocation extends Vec3i {

		public ChunkLocation(World world, int x, int z) {
			super(x, world.provider.getDimension(), z);
		}

		public int blockX() {
			return getX() << 4;
		}

		public int blockZ() {
			return getZ() << 4;
		}

	}

	public static class ChunkEntry {

		public ChunkLocation chunkLoc;
		public short[] points;
		private int numPoints;

		public ChunkEntry(ChunkLocation chunkPos) {
			chunkLoc = chunkPos;
			points = new short[32];
		}

		private int findIndex(short pos) {
			return Arrays.binarySearch(points, 0, numPoints, pos);
		}

		private void insert(short pos) {
			int elem = findIndex(pos);
			if (elem < 0) {
				if (numPoints + 1 > points.length) {
					points = Arrays.copyOf(points, points.length * 2);
				}
				elem = -elem - 1;
				System.arraycopy(points, elem, points, elem + 1, numPoints - elem);
				points[elem] = pos;
				numPoints++;
			}
		}

		private void remove(short pos) {
			int elem = findIndex(pos);
			if (elem >= 0) {
				int len = numPoints - elem - 1;
				if (len > 0) {
					System.arraycopy(points, elem + 1, points, elem, len);
				}
				numPoints--;
			}
		}

		public void addPoint(BlockPos pos) {
			insert(encode(pos));
		}

		public void remPoint(BlockPos pos) {
			remove(encode(pos));
		}

		private short encode(BlockPos pos) {
			return (short) ((pos.getX() & 15) << 16 | pos.getZ() << 8 | (pos.getY() & 15));
		}

		public MutableBlockPos decode(short input, MutableBlockPos pos) {
			return pos.setPos(chunkLoc.blockX() + (input >> 16), input & 255, chunkLoc.blockZ() + ((input >> 8) & 15));
		}

		public int size() {
			return numPoints;
		}

		public void getPoint(int index, MutableBlockPos pos) {
			if (index >= 0 && index < numPoints) {
				decode(points[index], pos);
			}
		}

		public NBTTagByteArray saveNBT() {
			ByteBuffer byteBuf = ByteBuffer.allocate(numPoints * 2);
			for (int i = 0; i < numPoints; i++) {
				byteBuf.putShort(points[i]);
			}

			return new NBTTagByteArray(byteBuf.array());
		}

		public void loadNBT(NBTTagByteArray nbtArray) {
			byte[] bytes = nbtArray.getByteArray();
			numPoints = bytes.length / 2;

			if (points.length < numPoints) {
				points = new short[numPoints];
			}

			ByteBuffer byteBuf = ByteBuffer.wrap(bytes);

			for (int i = 0; i < numPoints; i++) {
				points[i] = byteBuf.getShort();
			}
		}

	}

	private final HashMap<ChunkLocation, ChunkEntry> chunkMap;

	public EndpointTracker() {
		chunkMap = new HashMap<>();
	}

	public void addPoint(World world, BlockPos pos) {
		getChunkEntry(world, pos.getX() >> 4, pos.getZ() >> 4).addPoint(pos);
	}

	public void remPoint(World world, BlockPos pos) {
		getChunkEntry(world, pos.getX() >> 4, pos.getZ() >> 4).remPoint(pos);
	}

	public ChunkEntry getChunkEntry(World world, int x, int z) {
		return chunkMap.compute(new ChunkLocation(world, x, z), (k, v) -> (v != null) ? v : new ChunkEntry(k));
	}

}
