package com.ferreusveritas.dynamictrees.worldgen;

import java.util.ArrayList;

import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.util.Circle;
import com.ferreusveritas.dynamictrees.util.Vec2i;

public class CircleHelper {

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
	public static double deltaAngle(double alpha, double beta) {
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

		PairData pd = new PairData(cA.radius, cBrad);
		int sector = pd.getSector(angle);
		Vec2i[] coordList = pd.getCoordsForSectors(sector - 2, sector + 2);
		Vec2i closestCoord = coordList[0];
		double closestAngle = Math.PI;

		for(Vec2i c: coordList) {
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

		PairData pd = new PairData(cA.radius, cBrad);
		int sector = pd.getSector(angle);

		Vec2i[] coordList = pd.getCoordsForSectors(sector - 2, sector + 2);
		Vec2i closestCoord = coordList[0];
		double closestAngle = Math.PI;
		boolean isLoose = false;
		
		for(Vec2i c: coordList) {
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

		Circle cC = new Circle(0, 0, cCrad);//This is where the solution will be stored
		Vec2i delta = new Vec2i(cB.x - cA.x, cB.z - cA.z);

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

		PairData pdAC = new PairData(cA.radius, cC.radius);
		PairData pdBC = new PairData(cB.radius, cC.radius);
		
		int posAC = pdAC.getSector(angBAC);
		int posBC = pdBC.getSector(angABC);
		
		Vec2i[] coordListAC = pdAC.getCoordsForSectors(posAC - 2, posAC + 2);
		Vec2i[] coordListBC = pdBC.getCoordsForSectors(posBC - 2, posBC + 2);

		Vec2i a = new Vec2i();
		Vec2i b = new Vec2i();
		boolean solution = false;
		
		for(int ac = 0; ac < coordListAC.length; ac++) {//We'll be using these loops to scan a 5x5 block area for solutions
			for(int bc = 0; bc < coordListBC.length; bc++) {
				a.set(coordListAC[ac]).add(cA.x, cA.z);//Add the relative A->C coordinates to the circle A's absolute coordinates
				b.set(coordListBC[bc]).add(cB.x, cB.z);//Add the relative B->C coordinates to the circle A's absolute coordinates
				if( (a.x == b.x) && (a.z == b.z) ) {//We've found a location where the new circle C is touching both circle A and circle B
					cC.set(a);//Set this as the current solution although there may be others
					solution = !(a.loose && b.loose);//The solution is not viable if both are loose
					if(!(a.loose || b.loose)) {//Neither are loose.. perfect fit
						return cC;//A perfect fit is ideally what we are looking for so we can leave with it now 
					}
				}
			}
		}
		
		
		if(!solution && ModConfigs.worldGenDebug) {
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
			System.err.println("posAC: " + posAC + "/" + pdAC.getSectors());
			System.err.println("posBC: " + posBC + "/" + pdBC.getSectors());
			
			for(int i = 0; i < 5; i++) {
				System.err.println("coordListAC[" + i + "]: " + coordListAC[i]);
			}

			for(int i = 0; i < 5; i++) {
				System.err.println("coordListBC[" + i + "]: " + coordListBC[i]);
			}
			
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
