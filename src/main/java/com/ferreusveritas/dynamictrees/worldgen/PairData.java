package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.util.Vec2i;

public class PairData {
	private byte vsdata[];
	private int looseMask;
	private int codesize;
	private int sectors;
	
	private static int looseMasks[][] = new int[7][7];
	private static byte pairdata[][][] = new byte[7][7][];

	static { //Yuh.. magic.
		createPairData(8, 8, 32, 0x049556DF, 0x04955490);
		createPairData(8, 7, 30, 0x012556DF, 0x01255480);
		createPairData(8, 6, 28, 0x004956DF, 0x00495490);
		createPairData(8, 5, 26, 0x0012AADF, 0x0012AA90);
		createPairData(8, 4, 24, 0x0004AADF, 0x0004AA90);
		createPairData(8, 3, 22, 0x0002556F, 0x00025548);
		createPairData(8, 2, 20, 0x0000956F, 0x00009548);
		createPairData(7, 7, 28, 0x004956DF, 0x00495490);
		createPairData(7, 6, 26, 0x001256DF, 0x00125490);
		createPairData(7, 5, 24, 0x0004AADF, 0x0004AA90);
		createPairData(7, 4, 22, 0x00012ADF, 0x00012A90);
		createPairData(7, 3, 20, 0x0000956F, 0x00009548);
		createPairData(7, 2, 18, 0x0000256F, 0x00002548);
		createPairData(6, 6, 24, 0x000496DF, 0x00049490);
		createPairData(6, 5, 22, 0x00012ADF, 0x00012A90);
		createPairData(6, 4, 20, 0x00004ADF, 0x00004A90);
		createPairData(6, 3, 18, 0x0000256F, 0x00002548);
		createPairData(6, 2, 16, 0x0000096F, 0x00000948);
		createPairData(5, 5, 20, 0x0000555F, 0x00005550);
		createPairData(5, 4, 18, 0x0000155F, 0x00001550);
		createPairData(5, 3, 16, 0x00000AAF, 0x00000AA8);
		createPairData(5, 2, 14, 0x000002AF, 0x000002A8);
		createPairData(4, 4, 16, 0x0000055F, 0x00000550);
		createPairData(4, 3, 14, 0x000002AF, 0x000002A8);
		createPairData(4, 2, 12, 0x000000AF, 0x000000A8);
		createPairData(3, 3, 12, 0x00000157, 0x00000154);
		createPairData(3, 2, 10, 0x00000057, 0x00000054);
		createPairData(2, 2,  8, 0x00000017, 0x00000014);
	}
	
	private static void createPairData(int rad1, int rad2, int codeSize, int curveCode, int looseMask) {
		int idx1 = rad1 - 2;
		int idx2 = rad2 - 2;
		pairdata[idx1][idx2] = pairdata[idx2][idx1] = uncompressCurve(codeSize, curveCode);
		looseMasks[idx1][idx2] = looseMasks[idx2][idx1] = looseMask;
	}

	private static byte[] uncompressCurve(int codeSize, long curveCode) {
		byte[] wave = new byte[codeSize + 2];

		for(int i = 0; i <= codeSize; i++) {
			wave[i + 1] = (byte) (wave[i] + ((curveCode >> i) & 1));
		}

		return wave;
	}
	
	public int getSectors() {
		return sectors;
	}
	
	public PairData(int rad1, int rad2) {
		int idx1 = rad1 - 2;
		int idx2 = rad2 - 2;
		this.vsdata = pairdata[idx1][idx2];
		this.looseMask = looseMasks[idx1][idx2];
		this.codesize = vsdata.length - 2;
		this.sectors = (vsdata.length - 2) * 4;
	}
	
	public Vec2i getCoords(int sector) {
		//Wrap angle
		int vAngle = sector % (codesize * 4);
		if(vAngle < 0) {
			vAngle += (codesize * 4);
		}
			
		int modulus = Math.abs(((vAngle + codesize) % (codesize * 2) ) - codesize);

		Vec2i tc = new Vec2i();
		
		//Avoid branching by using a bit twiddle hack to determine the sign of the data.
		tc.x = (-( ((vAngle / codesize) + 1) & 2) + 1) * vsdata[codesize - modulus];
		tc.z = (-(  (vAngle / codesize)      & 2) + 1) * vsdata[modulus];
		tc.setLoose(((looseMask >> Math.min(modulus - 1, 32)) & 1) != 0);
		
		return tc;
	}
	
	public Vec2i[] getCoordsForSectors(int startSector, int stopSector) {
		
		int numSectors = stopSector - startSector + 1;
		Vec2i c[] = new Vec2i[numSectors];
		int coordIter = 0;
		
		for(int sectorIter = startSector; sectorIter <= stopSector; sectorIter++) {
			c[coordIter++] = getCoords(sectorIter);
		}

		return c;
	}
	
	public int getSector(double actualAngle) {
		
		int sector = (int)(CircleHelper.radiansToTurns(actualAngle) * sectors);
		double smallestDelta = CircleHelper.deltaAngle(actualAngle, getCoords(sector).angle());
		
		for(int dir = -1; dir <= 1; dir += 2) {
			while(true) {
				double ang = getCoords(sector + dir).angle();
				double del = CircleHelper.deltaAngle(actualAngle, ang);
				if(del < smallestDelta) {
					smallestDelta = del;
					sector += dir;
				} else {
					break;
				}
			}
		}
		
		return sector;
	}
}