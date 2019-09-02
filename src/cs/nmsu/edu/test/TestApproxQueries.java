package cs.nmsu.edu.test;

import java.util.Random;

import cs.nmsu.edu.csdemo.RstarTree.Data;
import cs.nmsu.edu.csdemo.method.approx.ApproxRangeIndex;
import cs.nmsu.edu.csdemo.method.approx.ApproxRangeKlengthIndex;

public class TestApproxQueries {
	public static void main(String args[]) {
		TestApproxQueries t = new TestApproxQueries();
		t.ApproxRangeTestCase1();
	}

	public void ApproxRangeTestCase1() {
		String city="LA";
		int queryPlaceId=1233;
		int threshold = 1950;
		
//		ApproxRangeIndex apx_range_index = new ApproxRangeIndex(city,threshold);	
//		Data queryD = apx_range_index.getDataById(queryPlaceId);
//		System.out.println(queryD);
//		apx_range_index.baseline(queryD);
//		apx_range_index.clearTempResult();
		
		System.out.println("==========================================================");
		ApproxRangeKlengthIndex(city, threshold, 10, queryPlaceId);
		System.out.println("==========================================================");
		ApproxRangeKlengthIndex(city, threshold, 20, queryPlaceId);
		System.out.println("==========================================================");
		ApproxRangeKlengthIndex(city, threshold, 30, queryPlaceId);
		System.out.println("==========================================================");
		ApproxRangeKlengthIndex(city, threshold, 40, queryPlaceId);
		System.out.println("==========================================================");
		ApproxRangeKlengthIndex(city, threshold, 50, queryPlaceId);
		System.out.println("==========================================================");
		ApproxRangeKlengthIndex(city, threshold, 60, queryPlaceId);
		System.out.println("==========================================================");
//		ApproxRangeKlengthIndex(city, threshold, 30000, queryPlaceId);
//		System.out.println("==========================================================");
	}
	
	
	public void ApproxRangeKlengthIndex(String city, int threshold, int k, int queryPlaceId) {
		ApproxRangeKlengthIndex apx_range_k_index = new ApproxRangeKlengthIndex(city,threshold);
		Data queryD = apx_range_k_index.getDataById(queryPlaceId);
		apx_range_k_index.baseline(queryD, k);
		apx_range_k_index.clearTempResult();
	}
	
	
	public int getRandomNumberInRange_int(int min, int max) {

		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}

		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}


}
