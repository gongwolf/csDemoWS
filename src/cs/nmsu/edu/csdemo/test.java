package cs.nmsu.edu.csdemo;

import java.util.ArrayList;

import org.neo4j.graphdb.GraphDatabaseService;
import cs.nmsu.edu.csdemo.RstarTree.Data;
import cs.nmsu.edu.csdemo.methods.ExactMethod;
import cs.nmsu.edu.csdemo.neo4jTools.connector;
import cs.nmsu.edu.csdemo.methods.*;

public class test {
	public static void main(String args[]) {
		test t = new test();
//		t.testRun("NY");
		t.improvedExactQueryById(5079, "NY");
	}

	public void testRun(String city) {
		
		Data queryD = getQueryDataPoint(city);
		ExactMethod bm5 = new ExactMethod(city);
		bm5.baseline(queryD);
	}

	public Data getQueryDataPoint(String city) {
		String home_folder = System.getProperty("user.home");
		String graph = home_folder + "/neo4j334/testdb_" + city + "/databases/graph.db";
		System.out.println("the folder of the graph database:" + graph);
		connector n = new connector(graph);
		n.startDB();
		GraphDatabaseService graphdb = n.getDBObject();
		ExactMethod bm5 = new ExactMethod(city);
		bm5.graphdb = graphdb;
		int random_place_id = bm5.getRandomNumberInRange_int(0, bm5.getNumberOfHotels() - 1);
		Data queryD = bm5.getDataById(random_place_id);
		bm5.nearestNetworkNode(queryD);
		double distance = bm5.nn_dist;
		while (distance > 0.0105) {
			random_place_id = bm5.getRandomNumberInRange_int(0, bm5.getNumberOfHotels() - 1);
			queryD = bm5.getDataById(random_place_id);
			bm5.nearestNetworkNode(queryD);
			distance = bm5.nn_dist;
		}
		System.out.println(queryD);

		n.shutdownDB();
		
		return queryD;
	}
	
	
	public ArrayList<Result> improvedExactQueryById(int queryPlaceId, String city){
		ExactMethod bm5 = new ExactMethod(city);
		Data queryD = bm5.getDataById(queryPlaceId);
		System.out.println(queryD);
		bm5.baseline(queryD);
//		for(Result r:bm5.skyPaths) {
//			System.out.println(r);
//		}
		return null;
		
	}
}
