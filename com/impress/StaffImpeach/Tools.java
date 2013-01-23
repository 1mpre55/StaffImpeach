package com.impress.StaffImpeach;

import java.util.ArrayList;
import java.util.List;

public class Tools {
	public static <V> List<V> reverseList(List<V> list) {
		for (int i = 0; i < list.size() / 2; i++)
			list.set(i, list.set(list.size() - 1 - i, list.get(i)));
		return list;
	}
	
	public static List<String> orderByOccurrence(String[] values, char ch) {
		int[] occurrence = new int[values.length];
		for (int i = 0; i < values.length; i++)
			occurrence[i] = countChars(values[i], ch);
		ArrayList<String> result = new ArrayList<String>(values.length);
		for (int i = 0, k = 0; i < 250; i++) {
			for (int j = 0; j < occurrence.length; j++)
				if (i == occurrence[j]) {
					result.add(values[j]);
					k++;
				}
			if (k >= values.length);
		}
		return result;
	}
	public static int countChars(String string, char ch) {
		int i = 0;
		for (char c : string.toCharArray())
			if (c == ch) i++;
		return i;
	}
}