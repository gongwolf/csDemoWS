package cs.nmsu.edu.csdemo.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class constants {
	public static final int path_dimension = 4; // 1(edu_dis)+3(road net work attrs)+3(static node attrs);

	public static HashMap<Long, Long> accessedNodes = new HashMap<>();
	public static HashMap<Long, Long> accessedEdges = new HashMap<>();
	
	public static HashSet<String> cityList = new HashSet<>(Arrays.asList("SF", "LA", "NY"));

	public static void print(double[] costs) {
		System.out.print("[");
		for (double c : costs) {
			System.out.print(c + " ");
		}
		System.out.println("]");
	}

	public static void main(String args[]) {
		double lat1 = 32.279799;
		double long1 = -106.756235;
		double lat2 = 32.282049;
		double long2 = -106.766577;
		distanceInMeters(lat1, long1, lat2, long2);
	}

	public static double distanceInMeters(double lat1, double long1, double lat2, double long2) {
		long R = 6371000;

		double d;
		// method 1
//        double r_lat1 = Math.PI / 180 * lat1;
//        double r_lat2 = Math.PI / 180 * lat2;
//        double delta_lat = Math.PI / 180 * (lat2 - lat1);
//        double delta_long = Math.PI / 180 * (long2 - long1);
//        double a = Math.sin(delta_lat / 2) * Math.sin(delta_lat / 2) + Math.cos(r_lat1) * Math.cos(r_lat2) * Math.sin(delta_long / 2) * Math.sin(delta_long / 2);
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//        d = R * c;
//        System.out.println(d);

		// method 2
//        double x = Math.PI / 180 * (long2 - long1) * Math.cos(Math.PI / 180 * (lat1 + lat2) / 2);
//        double y = Math.PI / 180 * (lat2 - lat1);
//        d = Math.sqrt(x * x + y * y) * R;
//        System.out.println(d);

		// method 3
		double r_lat1 = Math.PI / 180 * lat1;
		double r_lat2 = Math.PI / 180 * lat2;
		double delta_long = Math.PI / 180 * (long2 - long1);
		d = Math.acos(Math.sin(r_lat1) * Math.sin(r_lat2) + Math.cos(r_lat1) * Math.cos(r_lat2) * Math.cos(delta_long))
				* R;
//		System.out.println(d);
		return d;
	}

}
