package com.ferreusveritas.dynamictrees.worldgen;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.MathHelper;

import net.minecraft.util.EnumFacing;

/**
 * A storage class for {@link JoCode} objects.
 * Stores JoCodes by radius.  Can be used to call random JoCodes
 * during worldgen.
 * 
 * @author ferreusveritas
 *
 */
public class TreeCodeStore {

	ArrayList<ArrayList<JoCode>> store = new ArrayList<ArrayList<JoCode>>(7);//Radius values 2,3,4,5,6,7,8
	DynamicTree tree;
	
	public TreeCodeStore(Species tree) {
		this.tree = tree;
		for(int i = 0; i < 7; i++) {
			store.add(new ArrayList<JoCode>());
		}
	}
	
	private ArrayList<JoCode> getListForRadius(int radius) {
		radius = MathHelper.clamp(radius, 2, 8);
		return store.get(radius - 2);
	}
	
	public void addCodesFromFile(String filename) {
		try {
			Logger.getLogger(ModConstants.MODID).log(Level.CONFIG, "Loading Tree Codes for tree \"" + tree.getName() + "\" from file: " + filename);
			BufferedReader readIn = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(filename), "UTF-8"));
			String line;
			while((line = readIn.readLine()) != null) {
				if((line.length() >= 3) && (line.charAt(0) != '#')) {
					String[] split = line.split(":");
					addCode(Integer.valueOf(split[0]), split[1]);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addCode(int radius, String code) {
		JoCode joCode = new JoCode(code).setCareful(false);
		
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
		EnumFacing dirs[] = {EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST};
		ArrayList<String> arr = new ArrayList<String>() ;
		for(EnumFacing dir: dirs) {
			arr.add(joCode.rotate(dir).toString());
		}
		
		Collections.sort(arr);
		System.out.println(tree.getName() + ":" + radius + ":" + arr.get(0));
	}
	
	public JoCode getRandomCode(int radius, Random rand) {		
		ArrayList<JoCode> list = getListForRadius(radius);
		if(!list.isEmpty()) {
			return list.get(rand.nextInt(list.size()));
		}
		
		return null;
	}

}
