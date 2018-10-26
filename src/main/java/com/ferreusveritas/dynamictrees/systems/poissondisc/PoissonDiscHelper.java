package com.ferreusveritas.dynamictrees.systems.poissondisc;

import java.util.List;

public class PoissonDiscHelper {

	/**
	* Creates a tangential circle to cA at a random angle with radius cBrad.
	* 
	* @param cA The base circle
	* @param cBrad The radius of the created second circle
	* @param onlyTight Only returns tightly fitting circles and reject loose fits.
	* @return
	*/
	public static PoissonDisc findSecondDisc(PoissonDisc cA, int cBrad, boolean onlyTight) {
		return findSecondDisc(cA, cBrad, cA.getFreeAngle(), onlyTight);
	}

	private static final int singleSearchOrder[] = new int[] {0, 1, -1};
	
	/**
	* Creates a tangential circle to cA at a specific angle with radius cBrad
	* 
	* @param cA The base circle
	* @param cBrad The radius of the created second circle
	* @param angle The angle(in radians) to create the new circle 
 	* @param onlyTight Only returns tightly fitting circles and reject loose fits.
	* @return
	*/
	public static PoissonDisc findSecondDisc(PoissonDisc cA, int cBrad, double angle, boolean onlyTight) {

		PoissonDiscPairData pd = new PoissonDiscPairData(cA.radius, cBrad);
		int sector = pd.getSector(angle);

		if(onlyTight) {
			for(int i : singleSearchOrder) {
				Vec2i c = pd.getCoords(sector + i);
				if(c.isTight()) {//Reject loose circles when finding 2nd circle
					return (PoissonDisc) new PoissonDisc(c, cBrad).add(cA.x, cA.z);
				}
			}
		}

		return (PoissonDisc) new PoissonDisc(pd.getCoords(sector), cBrad).add(cA.x, cA.z);
	}

	private static final int pairSearchOrder[] = new int[] {34, 33, 35, 18, 50, 17, 17, 19, 49, 51, 32, 36, 2, 66, 1, 3, 65, 67, 16, 20, 48, 52};
	
	/**
	* Finds a circle that is tangential to both circle cA and circle cB of radius cCrad.
	* Prefers a tight fit for both circles if possible.  Otherwise fits tightly with
	* either circle cA.  If all else fails return a fit loose to both circles.
	* 
	* @param cA
	* @param cB
	* @param cCrad
	* @return
	*/
	public static PoissonDisc findThirdDisc(PoissonDisc cA, PoissonDisc cB, int cCrad) {
		if(cA == null || cB == null || cCrad < 2 || cCrad > 8) {
			System.err.println("3rd circle condition: Radius out of bounds or null circles");
			return null;
		}

		Vec2i delta = new Vec2i(cB.x - cA.x, cB.z - cA.z);

		//Calculate lengths of the sides of triangle ABC whose corners are the centers of the 3 circles
		double lenAB = delta.len();
		int lenAC = cA.radius + cCrad;
		int lenBC = cB.radius + cCrad;

		//Use law of cosines to determine missing angles of triangle ABC
		double angA = Math.acos((lenAC * lenAC + lenAB * lenAB - lenBC * lenBC) / (2 * lenAC * lenAB));
		double angB = Math.acos((lenBC * lenBC + lenAB * lenAB - lenAC * lenAC) / (2 * lenBC * lenAB));

		//Get relative angle relationships for the 3 circles
		double angAB = delta.angle();
		double angBAC = PoissonDiscMathHelper.wrapAngle(angAB - angA);
		double angABC = PoissonDiscMathHelper.wrapAngle(-Math.PI + angAB + angB);

		PoissonDiscPairData pdAC = new PoissonDiscPairData(cA.radius, cCrad);
		PoissonDiscPairData pdBC = new PoissonDiscPairData(cB.radius, cCrad);
		
		//The closest sectors for the given angles
		int sectorAC = pdAC.getSector(angBAC);
		int sectorBC = pdBC.getSector(angABC);

		//Cache of possible circle candidate coordinates
		Vec2i aCoords[] = new Vec2i[5];
		Vec2i bCoords[] = new Vec2i[5];
		
		//Possible result holders
		Vec2i halftight = null;
		Vec2i loose = null;
		
		for(int i : pairSearchOrder) {
			int aDeltaSector = i >> 4;
			int bDeltaSector = i & 15;
						
			if(aCoords[aDeltaSector] == null) {
				//Add the relative A->C coordinates to the circle A's absolute coordinates
				aCoords[aDeltaSector] = pdAC.getCoords(sectorAC + aDeltaSector - 2).add(cA.x, cA.z);
			}

			if(bCoords[bDeltaSector] == null) {
				//Add the relative B->C coordinates to the circle B's absolute coordinates
				bCoords[bDeltaSector] = pdBC.getCoords(sectorBC + bDeltaSector - 2).add(cB.x, cB.z);
			}
			
			Vec2i a = aCoords[aDeltaSector];
			Vec2i b = bCoords[bDeltaSector];
			
			if( (a.x == b.x) && (a.z == b.z) ) {//We've found a location where the new circle C is touching both circle A and circle B
				if(a.tight && b.tight) {//both are tight(AND).. perfect fit
					return new PoissonDisc(a, cCrad);//A perfect fit is ideally what we are looking for so we can leave with it now
				}
				else
				if(halftight == null) {
					if(a.tight || b.tight) {//one is tight(OR)
						halftight = new Vec2i(a);
					}
					else
					if(loose == null) {//neither are tight(NOR)
						loose = new Vec2i(a);
					}
				}
			}
		}

		if(halftight != null) {
			return new PoissonDisc(halftight, cCrad);
		}
		if(loose != null) {
			return new PoissonDisc(loose, cCrad);
		}
		
		//If we've gotten this far the only possibility is that the circles are too far apart to make a new circle
		//that is tangential to both. So we will simply create a tangent circle pointing at the circle that is too far away.
		return findSecondDisc(cA, cCrad, angAB, true);
		
		/*if(ModConfigs.worldGenDebug) {
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
			if(ModConfigs.poissonDiscImageWrite) {
				CircleDebug.outputCirclesToPng(circles, 0, 0, "NSF:" + System.currentTimeMillis());
			}
			System.err.println("------------------------------------");
			System.err.println("lenAB: " + lenAB);
			System.err.println("angAB: " + Math.toDegrees(angAB));
			System.err.println("angBAC: " + Math.toDegrees(angBAC));
			System.err.println("angABC: " + Math.toDegrees(angABC));
			System.err.println("posAC: " + sectorAC);
			System.err.println("posBC: " + sectorBC);
			System.err.println("3rd circle condition: No solution found");
			System.err.println("CircleA:" + cA);
			System.err.println("CircleB:" + cB);
			System.err.println("RadiusC:" + cCrad);
		}
		
		return null;*/
	}
	
	public static void maskDiscs(PoissonDisc c1, PoissonDisc c2) {
		maskDiscs(c1, c2, false);
	}

	public static void maskDiscs(PoissonDisc c1, PoissonDisc c2, boolean force) {

		if(c1 == c2) {
			return;
		}

		Vec2i delta = new Vec2i(c2.x - c1.x, c2.z - c1.z);
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

	public static void solveDiscs(List<PoissonDisc> unsolved, List<PoissonDisc> allDiscs) {
		//Mask out circles against one another
		for(PoissonDisc u: unsolved) {
			for(PoissonDisc c: allDiscs) {
				PoissonDiscHelper.maskDiscs(u, c);
			}
		}
	}

	/**
	* Gather the unsolved circles into a list.  Eliminate solved unreal circles.
	* @param unsolved
	* @param allDiscs
	* @return
	*/
	public static List<PoissonDisc> gatherUnsolved(List<PoissonDisc> unsolved, List<PoissonDisc> allDiscs) {

		unsolved.clear();//Prep the list for recreation

		for(int ci = 0; ci < allDiscs.size(); ci++) {
			PoissonDisc c = allDiscs.get(ci);
			if(c.hasFreeAngles()) {
				unsolved.add(c);
			}
		}

		return unsolved;
	}

	//Delete the circle. The order of the circles is unimportant
	public static void fastRemove(List<PoissonDisc> discs, int index) {
		PoissonDisc c = discs.remove(discs.size() - 1);//Pop the last element off
		if(index < discs.size()) {
			discs.set(index, c);//Place the popped element over the one we are deleting.
		}
	}

}
