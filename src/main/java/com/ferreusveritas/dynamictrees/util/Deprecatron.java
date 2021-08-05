package com.ferreusveritas.dynamictrees.util;

import java.util.HashMap;
import java.util.Map;

public class Deprecatron {

	public static Map<String, Integer> notificationMap = new HashMap<>();

	public static void Complain(String issue, String msg, int spamCount) {
		int count = notificationMap.computeIfAbsent(issue, i -> 0);
		if (count == 0) { //Print out a stack trace one time so we can see who the culprit is in the logs
			new Exception().printStackTrace();
		}
		if (count < spamCount || spamCount == 0) {
			notificationMap.put(issue, count + 1);
			System.err.println(msg);
		}
	}

	public static void Complain(String issue, String msg) {
		Complain(issue, msg, 200); //Print out no more than 200 messages.  Not so many that it would wreck a server log file.
	}

}
