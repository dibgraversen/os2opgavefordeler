package dk.os2opgavefordeler.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for distribution rule filtering
 */
public class FilterHelper {

	// this helper class should not be instantiated
	private FilterHelper() {}

	public static List<Integer> stringAsIntRangeList(String s) {
		List<Integer> res = new ArrayList<>();

		String[] daysArray = s.split(",");

		for (String d: daysArray) {
			if (d.contains("-")) { // range
				int begin = Integer.parseInt(d.split("-")[0]);
				int end = Integer.parseInt(d.split("-")[1]);
				if (end < begin) {
					int tmp = begin;
					begin = end;
					end = tmp;
				}
				for (int i = begin; i <= end; i++) {
					res.add(i);
				}
			}
			else {
				res.add(Integer.parseInt(d));
			}
		}

		return res;
	}

}
