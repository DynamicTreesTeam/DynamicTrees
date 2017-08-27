package com.ferreusveritas.dynamictrees.worldgen;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.TreeRegistry;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

public class TreeCodeStore {

	HashMap<Integer, ArrayList<JoCode>> store = new HashMap<Integer, ArrayList<JoCode>>();

	public TreeCodeStore() {
		loadCodesFromFile();
	}

	public void loadCodesFromFile() {
		for(DynamicTree tree: TreeRegistry.getTrees()) {
			loadCodesFromFile(tree, "assets/" + DynamicTrees.MODID + "/trees/"+ tree.getName() + ".txt");
		}
	}

	public void loadCodesFromFile(DynamicTree tree, String filename) {
		try {
			//System.out.println("Loading Tree Codes for " + tree.getName() + " tree from file: " + filename);
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
	//The remaining more siginificant bits encode the tree id.
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

		/*
		//Code reserved for collecting WorldGen JoCodes
		ForgeDirection dirs[] = {ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.WEST, ForgeDirection.EAST};
		ArrayList<String> arr = new ArrayList<String>() ;
		for(ForgeDirection dir: dirs) {
			joCode.rotate(dir);
			arr.add(joCode.toString());
		}

		Collections.sort(arr);
		System.out.print(tree.getName() + ":" + radius + ":");
		for(String s: arr) {
			System.out.print(s + ":");
		}
		System.out.println();
		*/

		list.add(joCode.setCareful(false));
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
