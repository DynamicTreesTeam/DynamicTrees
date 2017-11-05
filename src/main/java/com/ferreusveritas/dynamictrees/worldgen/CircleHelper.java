package com.ferreusveritas.dynamictrees.worldgen;

import java.util.ArrayList;

import com.ferreusveritas.dynamictrees.ConfigHandler;
import com.ferreusveritas.dynamictrees.util.Circle;
import com.ferreusveritas.dynamictrees.util.Vec2d;

public class CircleHelper {

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

	private static Vec2d[] getCoordsForPair(int rad1, int rad2, int startAngle, int stopAngle) {
		int idx1 = rad1 - 2;
		int idx2 = rad2 - 2;

		byte vsdata[] = pairdata[idx1][idx2];
		int looseMask = looseMasks[idx1][idx2];
		int codesize = vsdata.length - 2;
		
		int numAngles = stopAngle - startAngle + 1;
		Vec2d c[] = new Vec2d[numAngles];
		int coordIter = 0;
		
		for(int angleIter = startAngle; angleIter <= stopAngle; angleIter++) {
			
			//Wrap angle
			int vAngle = angleIter % (codesize * 4);
			if(vAngle < 0) {
				vAngle += (codesize * 4);
			}
				
			int modulus = Math.abs(((vAngle + codesize) % (codesize * 2) ) - codesize);

			Vec2d tc = c[coordIter++ % c.length] = new Vec2d();
			
			//Avoid branching by using a bit twiddle hack to determine the sign of the data.
			tc.x = (-( ((vAngle / codesize) + 1) & 2) + 1) * vsdata[codesize - modulus];
			tc.z = (-(  (vAngle / codesize)      & 2) + 1) * vsdata[modulus];
			tc.setLoose(((looseMask >> Math.min(modulus - 1, 32)) & 1) != 0);
		}

		return c;
	}

	private static int getNumAnglesInPair(int rad1, int rad2) {
		return (pairdata[rad1 - 2][rad2 - 2].length - 2) * 4;
	}

	public static double wrapAngle(double angle) {
		final double TwoPi = Math.PI * 2;
		angle %= TwoPi;//Wrap angle
		return angle + (angle < 0 ? TwoPi : 0);//Convert negative angle to positive
	}

	/**
	* Convert Range [0, PI * 2] to [0, 1]
	* @param angle The angle to convert [0, PI * 2] (angle will be wrapped to this range)
	* @return range [0, 1]
	*/
	public static float radiansToTurns(double angle) {
		return (float) (wrapAngle(angle) / (Math.PI * 2));
	}

	/**
	* Length (angular) of a shortest way between two angles.
	* 
	* @param alpha First angle in range [0, PI * 2] (input will be wrapped to range)
	* @param beta Second angle in range [0, PI * 2] (input will be wrapped to range)
	* @return Shorted Delta angle in range [0, PI]
	*/
	private static double deltaAngle(double alpha, double beta) {
		double phi = Math.abs(beta - alpha) % (Math.PI * 2);// This is either the distance or 360 - distance
		double distance = phi > Math.PI ? (Math.PI * 2) - phi : phi;
		return distance;
	}

	/**
	* Creates a tangential circle to cA at a random angle with radius cBrad.
	* Only returns tightly fitting circles and rejects loose fits with no overlapping
	* edges.
	* 
	* @param cA The base circle
	* @param cBrad The radius of the created second circle
	* @return
	*/
	public static Circle findSecondCircle(Circle cA, int cBrad) {

		double angle = cA.getFreeAngle();		

		int pos = (int)(radiansToTurns(angle) * getNumAnglesInPair(cA.radius, cBrad));

		Vec2d[] coordList = getCoordsForPair(cA.radius, cBrad, pos - 2, pos + 2);
		Vec2d closestCoord = coordList[0];
		double closestAngle = Math.PI;

		for(Vec2d c: coordList) {
			if(!c.isLoose()) {//Reject loose circles when finding 2nd
				double deltaAngle = deltaAngle(c.angle(), angle);
				if(deltaAngle < closestAngle) {
					closestCoord = c;
					closestAngle = deltaAngle;
				}
			}
		}

		return (Circle) new Circle(closestCoord, cBrad).add(cA.x, cA.z);
	}

	/**
	* Creates a tangential circle to cA at a specific angle with radius cBrad
	* 
	* @param cA The base circle
	* @param cBrad The radius of the created second circle
	* @return
	*/
	public static Circle findSecondCircle(Circle cA, int cBrad, double angle) {

		angle = Math.toRadians(angle);
		
		int pos = (int)(radiansToTurns(angle) * getNumAnglesInPair(cA.radius, cBrad));

		Vec2d[] coordList = getCoordsForPair(cA.radius, cBrad, pos - 2, pos + 2);
		Vec2d closestCoord = coordList[0];
		double closestAngle = Math.PI;
		boolean isLoose = false;
		
		for(Vec2d c: coordList) {
			double deltaAngle = deltaAngle(c.angle(), angle);
			if(deltaAngle < closestAngle) {
				closestCoord = c;
				closestAngle = deltaAngle;
				isLoose = c.isLoose();
			}
		}

		Circle result = (Circle) new Circle(closestCoord, cBrad).add(cA.x, cA.z);
		result.loose = isLoose;
		
		return result;
	}

	
	/**
	* Finds a circle that is tangential to both circle cA and circle cB of radius cCrad.
	* Prefers a tight fit for both circles if possible.  Otherwise fits tightly with
	* circle cA.  If the fit is loose for both circles the result is rejected.
	* 
	* @param cA
	* @param cB
	* @param cCrad
	* @return
	*/
	public static Circle findThirdCircle(Circle cA, Circle cB, int cCrad) {
		if(cA == null || cB == null || cCrad < 2 || cCrad > 8) {
			System.err.println("3rd circle condition: Radius out of bounds or null circles");
			return null;
		}

		Circle cC = new Circle(0, 0, cCrad);
		Vec2d delta = new Vec2d(cB.x - cA.x, cB.z - cA.z);

		//Calculate lengths of the sides of triangle ABC whose corners are the centers of the 3 circles
		double lenAB = delta.len();
		int lenAC = cA.radius + cC.radius;
		int lenBC = cB.radius + cC.radius;

		//Use law of cosines to determine missing angles of triangle ABC
		double angA = Math.acos((lenAC * lenAC + lenAB * lenAB - lenBC * lenBC) / (2 * lenAC * lenAB));
		double angB = Math.acos((lenBC * lenBC + lenAB * lenAB - lenAC * lenAC) / (2 * lenBC * lenAB));

		double angAB = delta.angle();

		double angBAC = wrapAngle(angAB - angA);
		double angABC = wrapAngle(-Math.PI + angAB + angB);

		int posAC = (int)(radiansToTurns(angBAC) * getNumAnglesInPair(cA.radius, cC.radius));
		int posBC = (int)(radiansToTurns(angABC) * getNumAnglesInPair(cB.radius, cC.radius));

		Vec2d[] coordListAC = getCoordsForPair(cA.radius, cC.radius, posAC - 2, posAC + 2);
		Vec2d[] coordListBC = getCoordsForPair(cB.radius, cC.radius, posBC - 2, posBC + 2);

		Vec2d a = new Vec2d();
		Vec2d b = new Vec2d();
		boolean solution = false;
		
		for(int ac = 0; ac < coordListAC.length; ac++) {
			for(int bc = 0; bc < coordListBC.length; bc++) {
				a.set(coordListAC[ac]).add(cA.x, cA.z);
				b.set(coordListBC[bc]).add(cB.x, cB.z);
				if( (a.x == b.x) && (a.z == b.z) ) {
					cC.set(a);
					solution = !(a.loose && b.loose);
					if(!(a.loose || b.loose)) {//Neither are loose.. perfect fit
						return cC;
					}
				}
			}
		}

		
		if(!solution && ConfigHandler.worldGenDebug) {
			ArrayList<Circle> circles = new ArrayList<Circle>();
			Circle cAtemp = new Circle(cA);
			Circle cBtemp = new Circle(cB);
			
			if(cAtemp.x < cBtemp.x) {
				cBtemp.x -= cAtemp.x;
				cAtemp.x = 0;
			} else {
				cAtemp.x -= cBtemp.x;
				cBtemp.x = 0;
			}

			if(cAtemp.z < cBtemp.z) {
				cBtemp.z -= cAtemp.z;
				cAtemp.z = 0;
			} else {
				cAtemp.z -= cBtemp.z;
				cBtemp.z = 0;
			}
			
			circles.add(cAtemp);
			circles.add(cBtemp);
			circles.add(new Circle(24, 24, cCrad));
			CircleDebug.outputCirclesToPng(circles, 0, 0, "NSF:" + System.currentTimeMillis());
			System.err.println("3rd circle condition: No solution found");
			System.err.println("CircleA:" + cA);
			System.err.println("CircleB:" + cB);
			System.err.println("RadiusC:" + cCrad);
		}
		
		
		return solution ? cC : null;
	}
	
	public static void maskCircles(Circle c1, Circle c2) {
		maskCircles(c1, c2, false);
	}

	public static void maskCircles(Circle c1, Circle c2, boolean force) {

		if(c1 == c2) {
			return;
		}

		Vec2d delta = new Vec2d(c2.x - c1.x, c2.z - c1.z);
		double angle = delta.angle();
		double dist = delta.len();

		if(force || c2.isInside(c1.x + (int)(delta.x * (c1.radius + 2) / dist), c1.z + (int)(delta.z * (c1.radius + 2) / dist))) {//If this is true then the circles c1 & c2 are adjacent(enough)
			if(c1.hasFreeAngles()) {
				double ang = Math.asin((c2.radius + 1.5) / dist);
				c1.maskArc(angle - ang, angle + ang);
			}
			if(c2.hasFreeAngles()) {
				double ang = Math.asin((c1.radius + 1.5) / dist);
				c2.maskArc(angle - ang + Math.PI, angle + ang + Math.PI);
			}
		}
	}

	public static void solveCircles(ArrayList<Circle> unsolved, ArrayList<Circle> allCircles) {
		//Mask out circles against one another
		for(Circle u: unsolved) {
			for(Circle c: allCircles) {
				CircleHelper.maskCircles(u, c);
			}
		}
	}

	/**
	* Gather the unsolved circles into a list.  Eliminate solved unreal circles.
	* @param unsolved
	* @param allCircles
	* @return
	*/
	public static ArrayList<Circle> gatherUnsolved(ArrayList<Circle> unsolved, ArrayList<Circle> allCircles) {

		unsolved.clear();//Prep the list for recreation

		for(int ci = 0; ci < allCircles.size(); ci++) {
			Circle c = allCircles.get(ci);
			if(c.hasFreeAngles()) {
				unsolved.add(c);
			}
		}

		return unsolved;
	}

	//Delete the circle. The order of the circles is unimportant
	public static void fastRemove(ArrayList<Circle> circles, int index) {
		Circle c = circles.remove(circles.size() - 1);//Pop the last element off
		if(index < circles.size()) {
			circles.set(index, c);//Place the popped element over the one we are deleting.
		}
	}

}
