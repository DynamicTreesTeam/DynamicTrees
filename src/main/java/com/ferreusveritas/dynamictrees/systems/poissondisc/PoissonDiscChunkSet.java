package com.ferreusveritas.dynamictrees.systems.poissondisc;

import java.util.Arrays;
import java.util.List;

/**
* <h1>Chunk Poison Disc Set</h1>
* <p>
* A class that handles a group of non-overlapping discs that exist within a single chunk.<br>
* <ul>
* <li>Each chunk disc set contains 16 4x4 block areas(tiles) that can contain either exactly one disc or nothing.</li>
* <li>Discs can have a radius of 2 to 8 blocks.</li>
* <li>Discs are not permitted to intersect with any other disc.</li>
* <li>There are 16 disc positions per chunk but it's impossible to use them all because of crowding.</li>
* </ul>
* </p>
* <p><pre><tt>
* <b>◀───Chunk───▶</b>
* -------------
* │00│01│02│03│
* -------------
* │04│05│06│07│
* -------------
* │08│09│10│11│
* -------------
* │12│13│14│15│
* -------------</tt></pre>
* </p>
* <p><pre><tt>
* Each of the 16 tiles are encoded as a single byte.  Bits are encoded thusly:
* -------------------------
* │07│06│05│04│03│02│01│00│
* -------------------------<b>
* │Ex│ Radius │  X  │  Z  │</b>
* -------------------------</tt></pre>
* <ul>
* <li><b>X:</b> The X offset of the disc center within the tile. (0-3)</li>
* <li><b>Z:</b> The Z offset of the disc center within the tile. (0-3)</li>
* <li><b>Radius:</b> The radius of the disc - 1. (0-7) Zero means no disc, any other value will have 1 added to it before use.</li>
* <li><b>Ex:</b>Extended bit.</li>
* </ul>
* </p>
*
* @author ferreusveritas
*
*/
public class PoissonDiscChunkSet {

	private byte[] discData;
	public boolean generated = false;

	public PoissonDiscChunkSet() {
		discData = new byte[16];
	}

	public PoissonDiscChunkSet(byte data[]) {
		generated = true;
		discData = data != null && data.length == 16 ? Arrays.copyOf(data, 16) : new byte[16];
	}

	public List<PoissonDisc> getDiscs(List<PoissonDisc> discs, int chunkX, int chunkZ) {
		for(int tile = 0; tile < 16; tile++) {
			byte cd = discData[tile];
			if(cd != 0) {//No data in the tile
				if((cd & 0x80) != 0) {//Extended Bit
					int flip = (cd | (cd << 1)) & 3;//0 or 3
					discs.add(unpackDiscData(tile, 0x10 ^ flip, chunkX, chunkZ));//r2 @ 0,0 or 0,3
					discs.add(unpackDiscData(tile, 0x1f ^ flip, chunkX, chunkZ));//r2 @ 3,3 or 3,0
				} else if((cd & 0x70) != 0) {//has a radius
					discs.add(unpackDiscData(tile, cd, chunkX, chunkZ));
				}
			}
		}
		return discs;
	}

	public void clearDiscs() {
		Arrays.fill(discData, (byte)0);
	}

	public List<PoissonDisc> addDiscs(List<PoissonDisc> discs) {
		clearDiscs();
		for(PoissonDisc d: discs) {
			addDisc(d);
		}
		return discs;
	}

	private static final PoissonDisc unpackDiscData(int tile, int diskData, int chunkX, int chunkZ) {
		int radius = getRadiusFromDiscData(diskData);
		int x = ((tile << 2) & 12);
		int z = (tile & 12);
		return new PoissonDisc((chunkX << 4) | x | ((diskData >> 2) & 3), (chunkZ << 4) | z | (diskData & 3), radius, true);
	}
	
	private static final int getRadiusFromDiscData(int discData) {
		return ((discData >> 4) & 7) + 1;
	}
	
	private static final byte buildDiscData(PoissonDisc c) {
		return (byte) ((((c.radius - 1) & 7) << 4) | (c.x & 3) << 2 | c.z & 3);
	}
	
	private static final int calcTileNum(PoissonDisc c) {
		return c.z & 12 | ((c.x & 12) >> 2);//Calculate which of the 16 tiles we are working in
	}
	
	public boolean addDisc(PoissonDisc d) {
		if(d.radius >=2 && d.radius <= 8) {
			int tile = calcTileNum(d); //Calculate which of the 16 tiles we are working in
			int cd = discData[tile];
			if(cd != 0) { //There's already a disc in this tile
				if((d.radius == 2) && (getRadiusFromDiscData(cd) == 2) ) { //but we are adding a radius 2 disc to a tile with an exiting radius 2 disc.. it might still be possible to slip it in.
					int oldDiscPos = cd & 15; //Get subtile position of old disc(lowest 4 bits)
					int newDiscPos = ((d.x & 3) << 2) | (d.z & 3);//Get subtile position of new disc
					switch(oldDiscPos << 4 | newDiscPos) {//Combine both subtile positions into a single value to expedite comparison
						case 0x0F://0,0 and 3,3
						case 0xF0://3,3 and 0,0
							discData[tile] = (byte) 0x80;//Extended bit
							return true;
						case 0xC3://3,0 and 0,3
						case 0x3C://0,3 and 3,0
							discData[tile] = (byte) 0x81;//Extended bit and rotate tile 90
							return true;
					}
				}
			} else { //Add a single simple disc
				discData[tile] = buildDiscData(d);
				return true;
			}
		}
		return false;
	}

	public byte[] getDiscData() {
		return discData;
	}

	public void setDiscData(byte[] discData) {
		this.discData = Arrays.copyOf(discData, 16);
	}

}
