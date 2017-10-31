package com.ferreusveritas.dynamictrees.worldgen;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.util.EnumFacing;

public class TreeCodeStore {

	HashMap<Integer, ArrayList<JoCode>> store = new HashMap<Integer, ArrayList<JoCode>>();

	public void addCodesFromFile(DynamicTree tree, String filename) {
		try {
			Logger.getLogger(DynamicTrees.MODID).log(Level.CONFIG, "Loading Tree Codes for " + tree.getName() + " tree from file: " + filename);
			BufferedReader readIn = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(filename), "UTF-8"));
			String line;
			while((line = readIn.readLine()) != null) {
				if((line.length() >= 3) && (line.charAt(0) != '#')) {
					String[] split = line.split(":");
					addCode(tree, Integer.valueOf(split[0]), split[1]);
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

	//Lease significant 4 bits store the radius 0-15
	//The remaining more significant bits encode the tree id.
	static private int getKey(DynamicTree tree, int radius) {
		return (tree.getId() << 4) | radius;
	}

	public void addCode(DynamicTree tree, int radius, String code) {
		int key = getKey(tree, radius);
		
		ArrayList<JoCode> list;
		
		if(store.containsKey(key)) {
			list = store.get(key);
		} else {
			list = new ArrayList<JoCode>();
			store.put(key, list);
		}
		
		JoCode joCode = new JoCode(code);
		
		//Code reserved for collecting WorldGen JoCodes
		//collectWorldGenCodes(tree, radius, joCode);
		
		list.add(joCode.setCareful(false));
	}

	/**
	 * This collects a list of trees and creates 4 variations for the 4 directions and then
	 * sorts them alphanumerically.  By sorting the rotated JoCodes you can eliminate duplicates
	 * who are only different by the direction they are facing.
	 * 
	 * @param tree
	 * @param radius
	 * @param joCode
	 */
	//Code reserved for collecting WorldGen JoCodes
	@SuppressWarnings("unused")
	private void collectWorldGenCodes(DynamicTree tree, int radius, JoCode joCode) {
		EnumFacing dirs[] = {EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST};
		ArrayList<String> arr = new ArrayList<String>() ;
		for(EnumFacing dir: dirs) {
			arr.add(joCode.rotate(dir).toString());
		}
		
		Collections.sort(arr);
		System.out.println(tree.getName() + ":" + radius + ":" + arr.get(0));
	}
	
	public JoCode getRandomCode(DynamicTree tree, int radius, Random rand) {
		int key = getKey(tree, radius);
		
		if(store.containsKey(key)) {
			ArrayList<JoCode> list = store.get(key);
			return list.get(rand.nextInt(list.size()));
		}
		
		return null;
	}

}
