package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.util.MathHelper;
import com.ferreusveritas.dynamictrees.util.Vec2i;
import com.ferreusveritas.dynamictrees.util.Vec2iPCA;

public class CirclePairData {
	private Vec2i coordData[];
	private int sectors;
	
	private static Vec2i coordTable[][][] = new Vec2i[7][7][];
	
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
		int sectors = codeSize * 4;
		Vec2i[] coord = coordTable[idx1][idx2] = coordTable[idx2][idx1] = new Vec2i[sectors];

		byte curveData[] = uncompressCurve(codeSize, curveCode);
		
		for(int sector = 0; sector < sectors; sector++) {
			int modulus = Math.abs(((sector + codeSize) % (codeSize * 2) ) - codeSize);
			
			//Avoid branching by using a bit twiddle to determine the sign of the data.
			coord[sector] = new Vec2iPCA(//Use the Precomputed angle variant of Vec2i
					(-( ((sector / codeSize) + 1) & 2) + 1) * curveData[codeSize - modulus],
					(-(  (sector / codeSize)      & 2) + 1) * curveData[modulus],
					((looseMask >> Math.min(modulus - 1, 32)) & 1) == 0
				);
		}
		
	}
	
	private static byte[] uncompressCurve(int codeSize, long curveCode) {
		byte[] wave = new byte[codeSize + 2];//TODO: Determine if we need the extra 2 bytes
		
		for(int i = 0; i <= codeSize; i++) {
			wave[i + 1] = (byte) (wave[i] + ((curveCode >> i) & 1));
		}
		
		return wave;
	}
	
	public CirclePairData(int rad1, int rad2) {
		int idx1 = rad1 - 2;
		int idx2 = rad2 - 2;
		this.coordData = coordTable[idx1][idx2];
		this.sectors = coordData.length;
	}
	
	public Vec2i getCoords(int sector) {
		return new Vec2i(coordData[MathHelper.wrap(sector, sectors)]);
	}
	
	public int getSector(double actualAngle) {
		
		int sector = (int)(MathHelper.radiansToTurns(actualAngle) * sectors);
		double smallestDelta = MathHelper.deltaAngle(actualAngle, getCoords(sector).angle());
		boolean runNextDir = true;
		
		for(int dir = -1; runNextDir && dir <= 1; dir += 2) {//Search one direction and then the other
			while(true) {
				double ang = getCoords(sector + dir).angle();
				double del = MathHelper.deltaAngle(actualAngle, ang);
				if(del < smallestDelta) {
					smallestDelta = del;
					sector += dir;
					runNextDir = false;//If this direction lead to a decrease in the delta angle then the other direction can only make it larger.  
				} else {
					break;//Try the other direction
				}
			}
		}
		
		return sector;
	}
}