package com.ferreusveritas.dynamictrees.worldgen;

import java.util.ArrayList;
import java.util.Arrays;

import com.ferreusveritas.dynamictrees.util.Circle;

/**
* <h1>Chunk Circle Set</h1>
* <p>
* A class that handles a group of non-overlapping circles that exist within a single chunk.<br>
* <ul>
* <li>Each chunk circle set contains 16 4x4 block areas(tiles) that can contain either exactly one circle or nothing.</li>
* <li>Circles can have a radius of 2 to 8 blocks.</li>
* <li>Circles are not permitted to intersect with any other circle.</li>
* <li>There are 16 circle positions per chunk but it's impossible to use them all because of crowding.</li>
* </ul>
* </p>
* <p><pre><tt>
* <b>◀───Chunk───▶</b>
* ┌──┬──┬──┬──┐
* │00│01│02│03│
* ├──┼──┼──┼──┤
* │04│05│06│07│
* ├──┼──┼──┼──┤
* │08│09│10│11│
* ├──┼──┼──┼──┤
* │12│13│14│15│
* └──┴──┴──┴──┘</tt></pre>
* </p>
* <p><pre><tt>
* Each of the 16 tiles are encoded as a single byte.  Bits are encoded thusly:
* ┌──┬──┬──┬──┬──┬──┬──┬──┐
* │07│06│05│04│03│02│01│00│
* ├──┼──┴──┴──┼──┴──┼──┴──┤<b>
* │Rs│ Radius │  X  │  Z  │</b>
* └──┴────────┴─────┴─────┘</tt></pre>
* <ul>
* <li><b>X:</b> The X offset of the circle center within the tile. (0-3)</li>
* <li><b>Z:</b> The Z offset of the circle center within the tile. (0-3)</li>
* <li><b>Radius:</b> The radius of the circle - 1. (0-7) Zero means no circle, any other value will have 1 added to it before use.</li>
* <li><b>Rs:</b>Reserved bit.</li>
* </ul>
* </p>
*
* @author ferreusveritas
*
*/
public class ChunkCircleSet {

	private byte[] circleData;
	boolean generated = false;

	ChunkCircleSet() {
		circleData = new byte[16];
	}

	ChunkCircleSet(byte data[]) {
		generated = true;
		circleData = data != null && data.length == 16 ? Arrays.copyOf(data, 16) : new byte[16];
	}

	public ArrayList<Circle> getCircles(ArrayList<Circle> circles, int chunkX, int chunkZ) {
		for(int i = 0; i < 16; i++) {
			if(circleData[i] != 0) {
				int radius = (circleData[i] >> 4) & 7;//0-7
				if(radius > 0) {
					radius++;
					int x = ((i << 2) & 12) | ((circleData[i] >> 2) & 3);
					int z = (i & 12) | (circleData[i] & 3);
					Circle c = new Circle((chunkX << 4) | x, (chunkZ << 4) | z, radius);
					c.real = true;//All circles loaded from a chunk circle set are real
					circles.add(c);
				}
			}
		}
		return circles;
	}

	public void clearCircles() {
		Arrays.fill(circleData, (byte)0);
	}

	public ArrayList<Circle> addCircles(ArrayList<Circle> circles) {
		clearCircles();
		for(Circle c: circles) {
			addCircle(c);
		}
		return circles;
	}

	boolean addCircle(Circle c) {
		if(c.radius >=2 && c.radius <= 8) {
			int x = c.x & 15;
			int z = c.z & 15;
			int tile = z & 12 | ((x & 12) >> 2); 
			circleData[tile] = (byte) ((((c.radius - 1) & 7) << 4) | (x & 3) << 2 | z & 3);
			return true;
		}
		return false;
	}

	public byte[] getCircleData() {
		return circleData;
	}

	public void setCircleData(byte[] circleData) {
		this.circleData = Arrays.copyOf(circleData, 16);
	}

}
