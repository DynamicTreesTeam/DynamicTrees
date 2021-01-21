package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A storage class for {@link JoCode} objects.
 * Stores JoCodes by radius.  Can be used to call random JoCodes
 * during worldgen.
 * 
 * @author ferreusveritas
 *
 */
public class JoCodeStore {

	ArrayList<ArrayList<JoCode>> store = new ArrayList<ArrayList<JoCode>>(7);//Radius values 2,3,4,5,6,7,8
	Species species;
	
	public JoCodeStore(Species tree) {
		this.species = tree;
		for(int i = 0; i < 7; i++) {
			store.add(new ArrayList<JoCode>());
		}
	}
	
	protected ArrayList<JoCode> getListForRadius(int radius) {
		radius = MathHelper.clamp(radius, 2, 8);
		return store.get(radius - 2);
	}
	
	public void addCodesFromFile(Species species, String filename) {
		try {
			Logger.getLogger(DynamicTrees.MODID).log(Level.CONFIG, "Loading Tree Codes for species \"" + species + "\" from file: " + filename);
			InputStream stream = getClass().getClassLoader().getResourceAsStream(filename);
			if(stream != null) {
				InputStreamReader streamReader = new InputStreamReader(stream, "UTF-8");
				BufferedReader readIn = new BufferedReader(streamReader);
				String line;
				while((line = readIn.readLine()) != null) {
					if((line.length() >= 3) && (line.charAt(0) != '#')) {
						String[] split = line.split(":");
						addCode(species, Integer.valueOf(split[0]), split[1]);
					}
				}
			} else {
				throw(new FileNotFoundException(filename));
			}
			
		} catch (FileNotFoundException e) {
			DynamicTrees.getLogger().warn("No JoCode file found for species \"" + species + "\" at location: " + filename);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addCode(Species species, int radius, String code) {
		JoCode joCode = species.getJoCode(code).setCareful(false);
		
		//Code reserved for collecting WorldGen JoCodes
		//collectWorldGenCodes(tree, radius, joCode);

		getListForRadius(radius).add(joCode);
	}

	/**
	 * This collects a list of trees and creates 4 variations for the 4 directions and then
	 * sorts them alphanumerically.  By sorting the rotated JoCodes you can eliminate duplicates
	 * who are only different by the direction they are facing.
	 * 
	 * @param radius
	 * @param joCode
	 */
	//Code reserved for collecting WorldGen JoCodes
	@SuppressWarnings("unused")
	private void collectWorldGenCodes(int radius, JoCode joCode) {
		Direction dirs[] = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
		ArrayList<String> arr = new ArrayList<String>() ;
		for(Direction dir: dirs) {
			arr.add(joCode.rotate(dir).toString());
		}
		
		Collections.sort(arr);
		System.out.println(species + ":" + radius + ":" + arr.get(0));
	}
	
	public JoCode getRandomCode(int radius, Random rand) {
		ArrayList<JoCode> list = getListForRadius(radius);
		if(!list.isEmpty()) {
			return list.get(rand.nextInt(list.size()));
		}
		
		return null;
	}

}
