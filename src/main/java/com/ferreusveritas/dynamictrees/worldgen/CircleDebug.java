package com.ferreusveritas.dynamictrees.worldgen;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;

import com.ferreusveritas.dynamictrees.util.Circle;

import net.minecraft.world.gen.NoiseGeneratorPerlin;

public class CircleDebug {

	public static int scale = 8;

	public static void outputCirclesToPng(ArrayList<Circle> circles, int chunkX, int chunkZ, String name) {
		int width = 48 * scale;
		int height = 48 * scale;

		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		Color lightGrey = new Color(186, 189, 182);
		Color darkGrey = new Color(136, 138, 133);

		for(int gz = 0; gz < 3; gz++) {
			for(int gx = 0; gx < 3; gx++) {
				drawRect(img, gx * 16 * scale, gz * 16 * scale, 16 * scale, 16 * scale, ((gz * 3 + gx) % 2) == 0 ? lightGrey : darkGrey);
			}
		}

		for(Circle c: circles) {
			drawCircle(img, c, (chunkX - 1) * 16, (chunkZ - 1) * 16);
		}

		if(name.isEmpty()) {
			name = "unresolved-" + chunkX + ":" + chunkZ;
		}

		//System.out.println("Writing:" + name + ".png");

		try {
			ImageIO.write(img, "png", new File("./unsolved/" + name + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void drawCircle(BufferedImage image, Circle circle, int xOffset, int zOffset) {
		Color green = new Color(115, 210, 22, circle.real ? 192 : 64);
		Color red = new Color(204, 0, 0, circle.real ? 192 : 64);
		Color col = circle.hasFreeAngles() ? red : green;

		int startX = circle.x - circle.radius;
		int stopX = circle.x + circle.radius;
		int startZ = circle.z - circle.radius;
		int stopZ = circle.z + circle.radius;

		for(int z = startZ; z <= stopZ; z++) {
			for(int x = startX; x <= stopX; x++) {
				if(circle.isInside(x, z)) {
					drawRect(image, (x - xOffset) * scale, (z - zOffset) * scale, scale, scale, col);
					//safeSetRGB(image, x - xOffset, z - zOffset, col);
				}
			}
		}

		//Draw arc segments
		double radius = circle.radius + 0.5f;

		for(int i = 0; i < 32; i++) {
			boolean isOn = (circle.arc & (1 << i)) != 0;
			double x1 = circle.x + 0.5 + Math.cos(Math.PI * 2 * i / 32.0) * radius;
			double z1 = circle.z + 0.5 + Math.sin(Math.PI * 2 * i / 32.0) * radius;
			double x2 = circle.x + 0.5 + Math.cos(Math.PI * 2 * (i + 1)/ 32.0) * radius;
			double z2 = circle.z + 0.5 + Math.sin(Math.PI * 2 * (i + 1) / 32.0) * radius;
			drawLine(image, (int)((x1 - xOffset) * scale), (int)((z1 - zOffset) * scale), (int)((x2 - xOffset) * scale), (int)((z2 - zOffset) * scale), (((i & 1) == 0) ? (isOn ? Color.BLACK : Color.PINK)  : (isOn ? Color.DARK_GRAY : Color.CYAN)));
		}
	}

	public static void drawLine(BufferedImage image, int x1, int z1, int x2, int z2, Color color) {
		int w = x2 - x1;
		int h = z2 - z1;
		int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;
		if (w<0) dx1 = -1 ; else if (w>0) dx1 = 1;
		if (h<0) dy1 = -1 ; else if (h>0) dy1 = 1;
		if (w<0) dx2 = -1 ; else if (w>0) dx2 = 1;
		int longest = Math.abs(w);
		int shortest = Math.abs(h);
		if (!(longest>shortest)) {
			longest = Math.abs(h);
			shortest = Math.abs(w);
			if (h<0) dy2 = -1 ; else if (h>0) dy2 = 1;
			dx2 = 0;
		}
		int numerator = longest >> 1;
			for (int i=0;i<=longest;i++) {
				safeSetRGB(image, x1,z1,color);
				numerator += shortest;
				if (!(numerator<longest)) {
					numerator -= longest;
					x1 += dx1;
					z1 += dy1;
				} else {
					x1 += dx2;
					z1 += dy2;
				}
			}
	}

	public static void drawRect(BufferedImage image, int x, int z, int w, int h, Color color) {
		for(int zi = 0; zi < h; zi++) {
			for(int xi = 0; xi < w; xi++) {
				safeSetRGB(image, x + xi, z + zi, color);
			}
		}
	}

	public static void safeSetRGB(BufferedImage image, int x, int z, Color color) {
		if(x >= 0 && z >= 0 && x < image.getWidth() && z < image.getHeight()){
			color.getAlpha();
			Color dst = new Color(image.getRGB(x, z));

			float dr = dst.getRed() / 255f;
			float dg = dst.getGreen() / 255f;
			float db = dst.getBlue() / 255f;			
			float sr = color.getRed() / 255f;
			float sg = color.getGreen() / 255f;
			float sb = color.getBlue() / 255f;
			float sa = color.getAlpha() / 255f;

			//Simple Alpha blending
			image.setRGB(x, z, new Color(sr * sa + dr * (1f - sa), sg * sa + dg * (1f - sa), sb * sa + db * (1f - sa)).getRGB());
		}
	}

	public static void initNoisetest() {

		int width = 128;
		int height = 128;

		NoiseGeneratorPerlin noiseGenerator = new NoiseGeneratorPerlin(new Random(2), 1);

		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		for(int oct = 0; oct < 7; oct++) {
			System.out.println("Noise" + oct);

			for(int i = 0; i < width; i++) {
				for(int j = 0; j < height; j++) {
										
					float noise = (float) ((noiseGenerator.func_151601_a(i / 64.0, j / 64.0) + 1D) / 2.0D);

					switch(oct){
					case 6:	noise += (float) ((noiseGenerator.func_151601_a(i / 1.0, j / 1.0) + 1D) / 2.0D) / 64;
					case 5:	noise += (float) ((noiseGenerator.func_151601_a(i / 2.0, j / 2.0) + 1D) / 2.0D) / 32;
					case 4:	noise += (float) ((noiseGenerator.func_151601_a(i / 4.0, j / 4.0) + 1D) / 2.0D) / 16;
					case 3:	noise += (float) ((noiseGenerator.func_151601_a(i / 8.0, j / 8.0) + 1D) / 2.0D) / 8;
					case 2:	noise += (float) ((noiseGenerator.func_151601_a(i / 16.0, j / 16.0) + 1D) / 2.0D) / 4;
					case 1: noise += (float) ((noiseGenerator.func_151601_a(i / 32.0, j / 32.0) + 1D) / 2.0D) / 2;
					}

					noise /= 2;
					int value = (int) (255 * noise);

					img.setRGB(i, j, new Color(value, value, value).getRGB());
				}
			}
			
			try {
				ImageIO.write(img, "png", new File("./" + "noise" + oct + ".png"));
			} catch(IOException e) {
				e.printStackTrace();
			}
			
		}

	}

	////////////////////////////////////////////////////////////

	public static void initCircleTests(){

		ArrayList<Circle> circles = new ArrayList<Circle>();
		/*
		Circle c1 = new Circle(8, 8, 8);//Right in the middle
		c1.real = true;
		circles.add(c1);

		Circle c2 = CircleHelper.findSecondCircle(c1, 7);
		c1.real = true;
		circles.add(c2);

		Circle c3 = CircleHelper.findThirdCircle(c1, c2, 4);

		if(c3 != null) {
			circles.add(c3);

			Circle c4 = CircleHelper.findThirdCircle(c1, c3, 6);
			if(c4 != null) {
				circles.add(c4);
			}
	
			c4 = CircleHelper.findThirdCircle(c3, c2, 2);
			if(c4 != null) {
				circles.add(c4);
			}

		}

		for(Circle cOut: circles){
			for(Circle cIn: circles){
				CircleHelper.maskCircles(cOut, cIn);
			}
		}

		outputCirclesToPng(circles, 0, 0, "masking");

		int xOffset = -48 * 16;
		int zOffset = 44 * 16;

		for(int r = 2; r <= 8; r++) {

		//Test 1 (nonarant test)
		circles.clear();
		circles.add(new Circle(-8 + xOffset, -8 + zOffset, r));
		circles.add(new Circle( 8 + xOffset, -8 + zOffset, r));
		circles.add(new Circle(24 + xOffset, -8 + zOffset, r));
		circles.add(new Circle(-8 + xOffset, 8 + zOffset, r));
		circles.add(new Circle( 8 + xOffset, 8 + zOffset, r));
		circles.add(new Circle(24 + xOffset, 8 + zOffset, r));
		circles.add(new Circle(-8 + xOffset, 24 + zOffset, r));
		circles.add(new Circle( 8 + xOffset, 24 + zOffset, r));
		circles.add(new Circle(24 + xOffset, 24 + zOffset, r));
		for(Circle c: circles){c.edgeMask(xOffset, zOffset);}
		outputCirclesToPng(circles, xOffset >> 4, zOffset >> 4, "test 1r" + r);

		//Test 2 (Outside Edge Test)
		circles.clear();
		circles.add(new Circle(-3 + xOffset, 8 + zOffset, r));
		circles.add(new Circle( 18 + xOffset, 8 + zOffset, r));
		circles.add(new Circle(8 + xOffset, -3 + zOffset, r));
		circles.add(new Circle(8 + xOffset, 18 + zOffset, r));
		for(Circle c: circles){c.edgeMask(xOffset, zOffset);}
		outputCirclesToPng(circles, xOffset >> 4, zOffset >> 4, "test 2r" + r);

		//Test 3 (Inside Edge Test)
		circles.clear();
		circles.add(new Circle(4 + xOffset, 8 + zOffset, r));
		circles.add(new Circle(11 + xOffset, 8 + zOffset, r));
		circles.add(new Circle(8 + xOffset, 4 + zOffset, r));
		circles.add(new Circle(8 + xOffset, 11 + zOffset, r));
		for(Circle c: circles){c.edgeMask(xOffset, zOffset);}
		outputCirclesToPng(circles, xOffset >> 4, zOffset >> 4, "test 3r" + r);

		//Test 4 (Inside Corner Test)
		circles.clear();
		circles.add(new Circle(4 + xOffset, 4 + zOffset, r));
		circles.add(new Circle(11 + xOffset, 4 + zOffset, r));
		circles.add(new Circle(4 + xOffset, 11 + zOffset, r));
		circles.add(new Circle(11 + xOffset, 11 + zOffset, r));
		for(Circle c: circles){c.edgeMask(xOffset, zOffset);}
		outputCirclesToPng(circles, xOffset >> 4, zOffset >> 4, "test 4r" + r);

		//Test 5 (Outside Corner Test)
		circles.clear();
		circles.add(new Circle(-3 + xOffset, -3 + zOffset, r));
		circles.add(new Circle(-3 + xOffset, 18 + zOffset, r));
		circles.add(new Circle(18 + xOffset, -3 + zOffset, r));
		circles.add(new Circle(18 + xOffset, 18 + zOffset, r));
		for(Circle c: circles){c.edgeMask(xOffset, zOffset);}
		outputCirclesToPng(circles, xOffset >> 4, zOffset >> 4, "test 5r" + r);

		//Test 6
		circles.clear();
		circles.add(new Circle(16 + xOffset, 16 + zOffset, r));
		circles.add(new Circle(-1 + xOffset, 16 + zOffset, r));
		circles.add(new Circle(16 + xOffset, -1 + zOffset, r));
		circles.add(new Circle(-1 + xOffset, -1 + zOffset, r));
		for(Circle c: circles){c.edgeMask(xOffset, zOffset);}
		outputCirclesToPng(circles, xOffset >> 4, zOffset >> 4, "test 6r" + r);

		//Test 7
		circles.clear();
		circles.add(new Circle(15 + xOffset, 15 + zOffset, r));
		circles.add(new Circle(0 + xOffset, 15 + zOffset, r));
		circles.add(new Circle(15 + xOffset, 0 + zOffset, r));
		circles.add(new Circle(0 + xOffset, 0 + zOffset, r));
		for(Circle c: circles){c.edgeMask(xOffset, zOffset);}
		outputCirclesToPng(circles, xOffset >> 4, zOffset >> 4, "test 7r" + r);

		}
*/
	}

////////////////////////////////////////////////////////////

	public static void saveImage(BufferedImage img, String directory, String name) throws IOException {
		ImageIO.write(img, "png", new File(directory + name + ".png"));
	}

	public static void setAlpha(BufferedImage img, int alpha) {
		for(int i = 0; i < img.getWidth(); i++) {
			for(int j = 0; j < img.getHeight(); j++) {
				Color currentColor = new Color(img.getRGB(i, j));
				Color newColor = new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), alpha);

				img.setRGB(i, j, newColor.getRGB());
			}
		}
	}
}
