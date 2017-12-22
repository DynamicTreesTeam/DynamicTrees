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
* │Ex│ Radius │  X  │  Z  │</b>
* └──┴────────┴─────┴─────┘</tt></pre>
* <ul>
* <li><b>X:</b> The X offset of the circle center within the tile. (0-3)</li>
* <li><b>Z:</b> The Z offset of the circle center within the tile. (0-3)</li>
* <li><b>Radius:</b> The radius of the circle - 1. (0-7) Zero means no circle, any other value will have 1 added to it before use.</li>
* <li><b>Ex:</b>Extended bit.</li>
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
		for(int tile = 0; tile < 16; tile++) {
			byte cd = circleData[tile];
			if(cd != 0) {//No data in the tile
				if((cd & 0x80) != 0) {//Extended Bit
					int flip = (cd | (cd << 1)) & 3;//0 or 3
					circles.add(unpackCircleData(tile, 0x10 ^ flip, chunkX, chunkZ));//r2 @ 0,0 or 0,3
					circles.add(unpackCircleData(tile, 0x1f ^ flip, chunkX, chunkZ));//r2 @ 3,3 or 3,0
				} else if((cd & 0x70) != 0) {//has a radius
					circles.add(unpackCircleData(tile, cd, chunkX, chunkZ));
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

	private static final Circle unpackCircleData(int tile, int circleData, int chunkX, int chunkZ) {
		int radius = getRadiusFromCircleData(circleData);
		int x = ((tile << 2) & 12);
		int z = (tile & 12);
		return new Circle((chunkX << 4) | x | ((circleData >> 2) & 3), (chunkZ << 4) | z | (circleData & 3), radius, true);
	}
	
	private static final int getRadiusFromCircleData(int circleData) {
		return ((circleData >> 4) & 7) + 1;
	}
	
	private static final byte buildCircleData(Circle c) {
		return (byte) ((((c.radius - 1) & 7) << 4) | (c.x & 3) << 2 | c.z & 3);
	}
	
	private static final int calcTileNum(Circle c) {
		return c.z & 12 | ((c.x & 12) >> 2);//Calculate which of the 16 tiles we are working in
	}
	
	boolean addCircle(Circle c) {
		if(c.radius >=2 && c.radius <= 8) {
			int tile = calcTileNum(c); //Calculate which of the 16 tiles we are working in
			int cd = circleData[tile];
			if(cd != 0) { //There's already a circle in this tile
				if((c.radius == 2) && (getRadiusFromCircleData(cd) == 2) ) { //but we are adding a radius 2 circle to a tile with an exiting radius 2 circle.. it might still be possible to slip it in.
					int oldCirclePos = cd & 15; //Get subtile position of old circle(lowest 4 bits)
					int newCirclePos = ((c.x & 3) << 2) | (c.z & 3);//Get subtile position of new circle
					switch(oldCirclePos << 4 | newCirclePos) {//Combine both subtile positions into a single value to expedite comparison
						case 0x0F://0,0 and 3,3
						case 0xF0://3,3 and 0,0
							circleData[tile] = (byte) 0x80;//Extended bit
							return true;
						case 0xC3://3,0 and 0,3
						case 0x3C://0,3 and 3,0
							circleData[tile] = (byte) 0x81;//Extended bit and rotate tile 90
							return true;
					}
				}
			} else { //Add a single simple circle
				circleData[tile] = buildCircleData(c);
				return true;
			}
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
