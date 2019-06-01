package cs.nmsu.edu.csdemo.services;

import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import cs.nmsu.edu.csdemo.RstarTree.Data;
import cs.nmsu.edu.csdemo.methods.ExactMethod;
import cs.nmsu.edu.csdemo.methods.Result;



@Path("/query")

public class QueryServices {
	@Path("/improvedExact/{city}/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<ResultBean> improvedExactQueryById(@PathParam("id") int queryPlaceId, @PathParam("city") String city){
		ExactMethod bm5 = new ExactMethod(city);
		Data queryD = bm5.getDataById(queryPlaceId);
		System.out.println(queryD);
		bm5.baseline(queryD);
//		for(Result r:bm5.skyPaths) {
//			System.out.println(r);
//		}
		
		ArrayList<ResultBean> result = new ArrayList<>();
		for(Result r : bm5.skyPaths) {
			ResultBean rbean = new ResultBean(r);
			result.add(rbean);
		}
		return result;
	}
}
