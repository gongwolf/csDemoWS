package cs.nmsu.edu.csdemo.services;

import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.neo4j.graphdb.Transaction;

import cs.nmsu.edu.csdemo.RstarTree.Data;
import cs.nmsu.edu.csdemo.methods.ExactMethod;
import cs.nmsu.edu.csdemo.methods.Result;
import cs.nmsu.edu.csdemo.methods.constants;
import cs.nmsu.edu.csdemo.neo4jTools.connector;



@Path("/query")

public class QueryServices {
	@Path("/improvedExact/{city}/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<ResultBean> improvedExactQueryById(@PathParam("id") int queryPlaceId, @PathParam("city") String city){
		
		if(!constants.cityList.contains(city)) {
			return null;
		}
		
		ExactMethod bm5 = new ExactMethod(city);
		Data queryD = bm5.getDataById(queryPlaceId);
		System.out.println(queryD);
		bm5.baseline(queryD);
		
		ArrayList<ResultBean> result = new ArrayList<>();
		for(Result r : bm5.skyPaths) {
			ResultBean rbean = new ResultBean(r);
			result.add(rbean);
		}
		
		updateBeansNodeLocationInformation(result,city);
		return result;
	}

	private void updateBeansNodeLocationInformation(ArrayList<ResultBean> result,String city) {
		String home_folder = System.getProperty("user.home");
		String graphPath = home_folder + "/neo4j334/testdb_" + city + "_Random/databases/graph.db";
		connector n = new connector(graphPath);
		n.startDB();
		try(Transaction tx = n.graphDB.beginTx()){
			
			for(ResultBean rbean:result) {
				for(NodeBeans nbean : rbean.nodeIDs) {
					double[] locations = new double[2];
					locations[0] = (double) n.graphDB.getNodeById(nbean.getId()).getProperty("lat");
					locations[1] = (double) n.graphDB.getNodeById(nbean.getId()).getProperty("log");
					nbean.setLat(locations[0]);
					nbean.setLng(locations[1]);
				}
				
			}
			
			tx.success();
		}
		
		n.shutdownDB();
	}
}
