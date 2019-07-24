package cs.nmsu.edu.csdemo.tools;

import com.google.maps.GeoApiContext;

public class GoogleMaps {
	private GeoApiContext context;

	public GoogleMaps() {
		this.context = new GeoApiContext.Builder().apiKey("AIzaSyA8M13Xzf7XZH9hV3_E2L1FsA9ZcCqfYS0").build();
	}

	public static void main(String args[]) {

		GoogleMaps g = new GoogleMaps();

		double lat1 = 32.279799;
		double long1 = -106.756235;
//		double lat2 = lat1;
//		double long2 = long1;
		double lat2 = 32.282049;
		double long2 = -106.766577;

		double distance_meter = g.distanceInMeters(lat1, long1, lat2, long2);
		double distance_euclidean = Math.sqrt(Math.pow(lat1 - lat2, 2) + Math.pow(long1 - long2, 2));

//		while (1003 != (int) distance_meter) {
//			lat2 += 0.000001;
//			distance_meter = g.distanceInMeters(lat1, long1, lat2, long2);
//			distance_euclidean = Math.sqrt(Math.pow(lat1 - lat2, 2) + Math.pow(long1 - long2, 2));
//		}

		System.out.println(distance_meter + " ~~~~~~ " + distance_euclidean);
		System.out.println("[" + lat1 + "," + long1 + "] , [" + lat2 + "," + long2 + "]");

	}

	public static double distanceInMeters(double lat1, double long1, double lat2, double long2) {
		long R = 6371000;
		double d;

//		double r_lat1 = Math.PI / 180 * lat1;
//		double r_lat2 = Math.PI / 180 * lat2;
//        double delta_lat = Math.PI / 180 * (lat2 - lat1);
//		double delta_long = Math.PI / 180 * (long2 - long1);
//        double a = Math.sin(delta_lat / 2) * Math.sin(delta_lat / 2) + Math.cos(r_lat1) * Math.cos(r_lat2) * Math.sin(delta_long / 2) * Math.sin(delta_long / 2);
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//        d = R * c;
//        System.out.println(d);
        double x = Math.PI / 180 * (long2 - long1) * Math.cos(Math.PI / 180 * (lat1 + lat2) / 2);
        double y = Math.PI / 180 * (lat2 - lat1);
        d = Math.sqrt(x * x + y * y) * R;
//        System.out.println(d);
//		d = Math.acos(Math.sin(r_lat1) * Math.sin(r_lat2) + Math.cos(r_lat1) * Math.cos(r_lat2) * Math.cos(delta_long))* R;
//        System.out.println(d);
		return d;
	}
}
