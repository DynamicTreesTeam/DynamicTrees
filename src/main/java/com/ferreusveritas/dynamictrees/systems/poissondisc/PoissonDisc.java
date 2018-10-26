package com.ferreusveritas.dynamictrees.systems.poissondisc;

import com.ferreusveritas.dynamictrees.util.SimpleBitmap;

import net.minecraft.util.math.BlockPos;

public class PoissonDisc extends Vec2i {
	
	public int radius;
	public int arc;
	public boolean real;
	
	private static SimpleBitmap[] cbm = new SimpleBitmap[9];//Bitmaps of whole circles 
	private static SimpleBitmap[] icbm = new SimpleBitmap[9];//Bitmaps of the interiors of circles(Non-edge)
	
	static {
		int circledata[] = {0x48,0x488,0x36D1,0x248D1,0x16D919,0xDB5B19,0x7FF6B19};//Packed circle data.  3 bits per slice length. 1 element per circle.
		for(int r = 2; r <= 8; r++) {//Circles with radius 2 - 8
			SimpleBitmap whole = circleBitmapGen(r, circledata[r - 2]);//Unpack circle bitmaps
			SimpleBitmap inside = new SimpleBitmap(whole.getW(), whole.getH());//Make a bitmap the same size that will serve as the inner circle pixels(non-edge)
			
			//Generate interior circle bitmap
			for(int z = 0; z < inside.getH(); z++) {
				for(int x = 0; x < inside.getW(); x++) {
					//A pixel is considered interior(non-edge) if it is both ON and surrounded on all four sides by ON pixels;
					boolean in = whole.isPixelOn(x, z) && whole.isPixelOn(x + 1, z) && whole.isPixelOn(x - 1, z) && whole.isPixelOn(x, z + 1) && whole.isPixelOn(x, z - 1);
					inside.setPixel(x, z, in ? 1 : 0);
				}
			}
			
			cbm[r] = whole;
			icbm[r] = inside;
		}
		
		//Treat radius 0 and 1 as if they are 2
		cbm[0] = cbm[1] = cbm[2];
		icbm[0] = icbm[1] = icbm[2];
	}
	
	private static SimpleBitmap circleBitmapGen(int radius, int points) {
		int dim = radius * 2 + 1;
		int top = 0;
		int bot = dim - 1;
		int lines[] = new int[dim];
		
		while(top <= bot) {
			int slice = ((points >> (top * 3)) & 0x7) + 1;
			lines[top++] = lines[bot--] = bitRun(radius - slice, radius + 1 + slice);
		}
		
		return new SimpleBitmap(dim, dim, lines);
	}
	
	/**
	* 
	* @param start range [0, 31] inclusive
	* @param stop range [0, 31] inclusive
	* @return integer representation of a run of ON bits from start to stop
	*/
	private static int bitRun(int start, int stop) {
		if(start < stop) {
			return (int) ((0xFFFFFFFFL >>> (32 - stop)) & (0xFFFFFFFFL << start));
		} else {
			return (int) ((0xFFFFFFFFL >>> (32 - stop)) | (0xFFFFFFFFL << start));
		}
	}
	
	private static SimpleBitmap getCircleBitmap(int radius) {
		return cbm[radius];
	}
	
	private SimpleBitmap getCircleBitmap() {
		return getCircleBitmap(radius);
	}
	
	private static SimpleBitmap getCircleInteriorBitmap(int radius) {
		return icbm[radius];
	}
	
	private SimpleBitmap getCircleInteriorBitmap() {
		return getCircleInteriorBitmap(radius);
	}
		
	public PoissonDisc() {
		this(0, 0, 2);
	}
	
	public PoissonDisc(BlockPos pos, int radius) {
		this(pos.getX(), pos.getZ(), radius);
	}
	
	public PoissonDisc(int x, int z, int radius, boolean real) {
		this(x, z, radius);
		this.real = real;
	}
	
	public PoissonDisc(int x, int z, int radius) {
		set(x, z, radius);
	}
	
	public PoissonDisc(Vec2i c, int radius) {
		set(c.x, c.z, radius);
	}
	
	public PoissonDisc(PoissonDisc o) {
		set(o.x, o.z, o.radius);
		arc = o.arc;
	}
	
	public PoissonDisc set(int x, int z, int radius) {
		return set(x, z).setRadius(radius);
	}
	
	@Override
	public PoissonDisc set(int x, int z) {
		super.set(x, z);
		return this;
	}
	
	public PoissonDisc setRadius(int radius) {
		this.radius = net.minecraft.util.math.MathHelper.clamp(radius, 2, 8);
		return this;
	}
	
	/**
	* Test if a coordinate point is inside of a whole raster circle
	* 
	* @param x X-Axis
	* @param z Z-Axis(Y)
	* @return True if inside. False if outside
	*/
	public boolean isInside(int x, int z) {
		return getCircleBitmap().isPixelOn(x - this.x + radius, z - this.z + radius);
	}
	
	/**
	* Test if a coordinate point is inside of a raster circle's interior pixels(non-edge)
	* 
	* @param x X-Axis
	* @param z Z-Axis(Y)
	* @return True if inside. False if outside
	*/
	public boolean isInterior(int x, int z) {
		return getCircleInteriorBitmap().isPixelOn(x - this.x + radius, z - this.z + radius);
	}
	
	/**
	* Returns true if the point one of the edge pixels of the circle
	* 
	* @param x X-Axis
	* @param z Z-Axis(Y)
	* @return True if on edge. False otherwise.
	*/
	public boolean isEdge(int x, int z) {
		return isInside(x, z) && !isInterior(x, z);
		//return isInside(x, z) && ( !isInside(x + 1, z) || !isInside(x - 1, z) || !isInside(x, z + 1) || !isInside(x, z - 1) );
	}
	
	/**
	* 
	* Uses a simple bitmap to detect collisions of raster circles
	* 
	* @param other The other circle to test against
	* @return true if intersection detected, false otherwise.
	*/
	public boolean doCirclesIntersect(PoissonDisc other) {
		SimpleBitmap thisbm = getCircleBitmap();
		SimpleBitmap otherbm = other.getCircleBitmap();

		int dx = other.x - this.x;
		int dz = other.z - this.z;
		
		return thisbm.isColliding( ((thisbm.getW() - otherbm.getW()) / 2) + dx, ((thisbm.getH() - otherbm.getH()) / 2) + dz, otherbm);
	}
	
	/**
	 * Uses a simple bitmap to detect collisions of raster circles.
	 * This variation allows for edge pixels to intersect without creating a collision.
	 * 
	* @param other The other circle to test against
	* @return true if padded intersection detected, false otherwise.
	 */
	public boolean doCirclesIntersectPadding(PoissonDisc other) {
		SimpleBitmap thisbm = getCircleBitmap();
		SimpleBitmap otherbm = other.getCircleInteriorBitmap();

		int dx = other.x - this.x;
		int dz = other.z - this.z;
		
		return thisbm.isColliding( ((thisbm.getW() - otherbm.getW()) / 2) + dx, ((thisbm.getH() - otherbm.getH()) / 2) + dz, otherbm);
	}	
	
	/**
	* Mask off an arc CW in 1/32 increments. From 0 to PI * 2.  
	* 0 PI is pointing +X(East) increasing from 0 PI starts toward +Z(South) ⟳
	* 
	* @param startAngle starting angle in radians
	* @param endAngle starting angle in radians
	*/
	public void maskArc(double startAngle, double endAngle) {
		int start = Math.round(PoissonDiscMathHelper.radiansToTurns(startAngle) * 32.0f) & 31;
		int end =   Math.round(PoissonDiscMathHelper.radiansToTurns(endAngle)   * 32.0f) & 31;
		arc |= bitRun(start, end);
	}
	
	/**
	* Fill or Solve the entire circle.
	*/
	public void fillArc() {
		arc = 0xFFFFFFFF;
	}
	
	/**
	* Set the circle to a solved state
	*/
	public void setSolved() {
		fillArc();
	}
	
	/**
	* Clear arc mask so all bit angles are free.
	*/
	public void clearArc() {
		arc = 0;
	}
	
	/**
	* Sets the arc masking for the circle depending on which 9set chunk it's in.
	* 
	* @param chunkXStart starting position on the X axis in blocks
	* @param chunkZStart starting position on the Z axis in blocks
	*/
	public void edgeMask(int chunkXStart, int chunkZStart) {
		
		int x = this.x - chunkXStart + 16;
		int z = this.z - chunkZStart + 16;
		
		//Sometimes findThirdCircle will push a circle out of the 3x3 park..  Catch it and set the circle to solved and forget about it.
		if(x < 0 || z < 0 || x >= 48 || z >= 48) {
			fillArc();
			return;
		}
		
		byte pointMap[] = {6,4,12,2,0,13,3,11,9,0,0,0,0,0,0,0};//Bitfields: [--,--,--,--,Bz,Bx,Az,Ax]
		byte points = pointMap[( (x >> 4) + (z >> 4) * 3) & 15];
		if(points == 0) {//Check for center chunk
			
			double adjs[] = {//⟳
				31.5 - x,	//East +X
				31.5 - z,	//Down +Z
				x - 15.5,	//West -X
				z - 15.5,	//North -Z
			};
			
			double r = radius + 2;//We add 2 to the radius because we can't fit the smallest circle(radius == 2) in the space anyway
			double offset = 0;//Offset in radians..  Starts pointing East +X
			
			for(int edge = 0; edge < 4; edge++) {
				double cos = adjs[edge] / r;//If the cos is < 0 then the circle is outside of the chunk(not possible since we are testing the middle chunk)
				if(cos < 1.0) {//If the cos is > 1 then the circle edge isn't overlapping the chunk edge.   
					double angle = Math.acos(cos);
					maskArc(offset - angle, offset + angle);
				}
				offset += Math.PI * 0.5;//⟳
			}
		} else {
			double angle1 = Math.atan2( 15.5 + (((points >> 1) & 1) << 4) - z, 15.5 + (((points >> 0) & 1) << 4) - x );
			double angle2 = Math.atan2( 15.5 + (((points >> 3) & 1) << 4) - z, 15.5 + (((points >> 2) & 1) << 4) - x );
			maskArc(angle1, angle2);
		}
		
	}
	
	/**
	* Detects the transition from 0 to 1 around the circle arc mask in the clockwise direction starting from 0 degrees.
	* 
	* If the arc mask is completely empty then the function defaults to a psuedorandom bit from 0 - 31
	* @return the next free bit angle for placing a circle 0 to 31
	*/
	public int getFreeBit() {
		if(arc == 0) {
			return ((x ^ z) & 31);//Pseudorandom angle
		}
		
		int pos = Integer.numberOfTrailingZeros(arc);
		
		if(pos == 0) {
			pos = Integer.numberOfTrailingZeros(~arc);
			pos += Integer.numberOfTrailingZeros(Integer.rotateRight(arc, pos));
		} // 16
		
		return ((pos - 1) & 31);
		
	}
	
	/**
	* Convert bit angle to radians
	* 
	* @param bit bit angle. a number from 0-31 that scales from 0 to PI * 2
	* @return the radian angle corresponding to the provided bit angle
	*/
	public double bitToAngle(int bit) {
		return bit / 16.0 * Math.PI;
	}
	
	/**
	* Gets the next free angle(in radians) for placing a new neighboring circle.
	* 
	* @return free angle in radians
	*/
	public double getFreeAngle() {
		return bitToAngle(getFreeBit());
	}
	
	/**
	* Test to see if the circle has any remaining free angles on its arc.
	* A circle with no free angles is considered solved in the circle packing algorithm.
	* 
	* @return True if circle has free angles(unsolved).  False if it does not(solved)
	*/
	public boolean hasFreeAngles() {
		return arc != 0xFFFFFFFF;
	}
	
	/**
	* A circle with no free angles is considered solved in the circle packing algorithm.
	* 
	* @return False if circle has free angles(unsolved).  True if it does not(solved)
	*/
	public boolean isSolved() {
		return !hasFreeAngles();
	}
	
	/**
	* Measure how deeply a circle penetrates another circle.
	* 
	* @param o Other circle
	* @return penetration depth
	*/
	public double discPenetration(PoissonDisc o) {
		Vec2i delta = new Vec2i(x - o.x, z - o.z);
		return delta.len() - (this.radius + o.radius + 1);
	}
	
	/**
	* Returns true if the circles center is inside the center chunk in a 3x3 chunk grid 
	* 
	* @param chunkXStart
	* @param chunkZStart
	* @return True if circle center is in the middle of a 3x3 grid. False otherwise.
	*/
	public boolean isInCenterChunk(int chunkXStart, int chunkZStart) {
		return x >= chunkXStart && z >= chunkZStart && x < chunkXStart + 16 && z < chunkZStart + 16;
	}
	
	@Override
	public String toString() {
		return "Circle x" + x + ", z" + z + ", r" + radius + ", " + (real ? "T" : "F") + ", " + Integer.toHexString(arc);
	}
	
}