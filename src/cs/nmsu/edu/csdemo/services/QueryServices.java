package cs.nmsu.edu.csdemo.services;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.neo4j.graphdb.Transaction;

import cs.nmsu.edu.csdemo.RstarTree.Data;
import cs.nmsu.edu.csdemo.method.approx.ApproxMixedIndex;
import cs.nmsu.edu.csdemo.method.approx.ApproxRangeIndex;
import cs.nmsu.edu.csdemo.methods.ExactMethod;
import cs.nmsu.edu.csdemo.methods.ExactMethodIndex;
import cs.nmsu.edu.csdemo.methods.Result;
import cs.nmsu.edu.csdemo.methods.constants;
import cs.nmsu.edu.csdemo.neo4jTools.connector;

@Path("/query")

public class QueryServices {

	String homepath = System.getProperty("user.home");
	int t_distance = 40;

	/**Group of exact methods**/
	@Path("/improvedExact/{city}/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response improvedExactQueryById(@PathParam("id") int queryPlaceId, @PathParam("city") String city) {

		if (!constants.cityList.contains(city)) {
			return null;
		}

		ExactMethod bm5 = new ExactMethod(city);
		Data queryD = bm5.getDataById(queryPlaceId);
		System.out.println(queryD);
		bm5.baseline(queryD);

		ArrayList<ResultBean> result = new ArrayList<>();
		for (Result r : bm5.skyPaths) {
			ResultBean rbean = new ResultBean(r);
			result.add(rbean);
		}

		updateBeansNodeLocationInformation(result, city);
		return Response.status(200).entity(result).header("Access-Control-Allow-Origin", "*").build();
	}
	
	@Path("/improvedExactLocation/{city}/{lat}/{lng}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response improvedExactQueryByLocation(@PathParam("city") String city, @PathParam("lat") double lat, @PathParam("lng") double lng) {

		if (!constants.cityList.contains(city)) {
			return null;
		}

		int isAIOP = isAInterestingOfPoint(city, lat, lng);
		
		if(isAIOP!=-1) {
			return improvedExactQueryById(isAIOP,city);
		}else {
			ExactMethod bm5 = new ExactMethod(city);
			bm5.baseline(lat, lng);

			ArrayList<ResultBean> result = new ArrayList<>();
			for (Result r : bm5.skyPaths) {
				ResultBean rbean = new ResultBean(r);
				result.add(rbean);
			}

			updateBeansNodeLocationInformation(result, city);
			return Response.status(200).entity(result).header("Access-Control-Allow-Origin", "*").build();
			
		}
	}
	
	@Path("/improvedExactLocationType/{city}/{type}/{lat}/{lng}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response improvedExactQueryByLocationType(@PathParam("city") String city, @PathParam("lat") double lat, @PathParam("lng") double lng, @PathParam("type") String type) {

		if (!constants.cityList.contains(city)) {
			return null;
		}

		int isAIOP = isAInterestingOfPoint(city, lat, lng);
		
		if(isAIOP!=-1) {
			System.out.println(isAIOP+"     "+city);
			return improvedExactQueryByIdType(isAIOP,city,type);
		}else {
			ExactMethod bm5 = new ExactMethod(city,type);
			bm5.baseline(lat, lng);

			ArrayList<ResultBean> result = new ArrayList<>();
			for (Result r : bm5.skyPaths) {
				ResultBean rbean = new ResultBean(r);
				result.add(rbean);
			}

			updateBeansNodeLocationInformation(result, city);
//			return result;
			return Response.status(200).entity(result).header("Access-Control-Allow-Origin", "*").build();	
		}
	}
		
	@Path("/improvedExact/{city}/{type}/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response improvedExactQueryByIdType(@PathParam("id") int queryPlaceId, @PathParam("city") String city,
			@PathParam("type") String type) {

		if (!constants.cityList.contains(city)) {
			return null;
		}

		if (!constants.typeList.contains(type)) {
			return null;
		}

		ExactMethod bm5 = new ExactMethod(city, type);
		Data queryD = bm5.getDataById(queryPlaceId);
		System.out.println(queryD);
		bm5.baseline(queryD);

		ArrayList<ResultBean> result = new ArrayList<>();
		for (Result r : bm5.skyPaths) {
			ResultBean rbean = new ResultBean(r);
			result.add(rbean);
		}

		updateBeansNodeLocationInformation(result, city);
		return Response.status(200).entity(result).header("Access-Control-Allow-Origin", "*").build();
	}
	
	
	/**Group of exact methods with Index**/
	@Path("/improvedExactLocationTypeIndex/{city}/{type}/{lat}/{lng}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response improvedExactQueryByLocationTypeIndex(@PathParam("city") String city, @PathParam("lat") double lat, @PathParam("lng") double lng, @PathParam("type") String type) {

		if (!constants.cityList.contains(city)) {
			return null;
		}

		int isAIOP = isAInterestingOfPoint(city, lat, lng);
		
		if(isAIOP!=-1) {
			System.out.println(isAIOP+"     "+city);
			return improvedExactQueryByIdTypeIndex(isAIOP,city,type);
		}else {
			ExactMethodIndex bm5 = new ExactMethodIndex(city,type);
			bm5.baselineWihtIndex(lat, lng);

			ArrayList<ResultBean> result = new ArrayList<>();
			for (Result r : bm5.skyPaths) {
				ResultBean rbean = new ResultBean(r);
				result.add(rbean);
			}

			updateBeansNodeLocationInformation(result, city);
//			return result;
			return Response.status(200).entity(result).header("Access-Control-Allow-Origin", "*").build();	
		}
	}

	@Path("/improvedExactIndex/{city}/{type}/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response improvedExactQueryByIdTypeIndex(@PathParam("id") int queryPlaceId, @PathParam("city") String city, @PathParam("type") String type) {

		if (!constants.cityList.contains(city)) {
			return null;
		}

		if (!constants.typeList.contains(type)) {
			return null;
		}

		ExactMethodIndex bm5 = new ExactMethodIndex(city, type);
		Data queryD = bm5.getDataById(queryPlaceId);
		System.out.println(queryD);
		bm5.baselineIndex(queryD);

		ArrayList<ResultBean> result = new ArrayList<>();
		for (Result r : bm5.skyPaths) {
			ResultBean rbean = new ResultBean(r);
			result.add(rbean);
		}

		updateBeansNodeLocationInformation(result, city);
		return Response.status(200).entity(result).header("Access-Control-Allow-Origin", "*").build();
	}
			
	@Path("/improvedExactLocationIndex/{city}/{lat}/{lng}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response improvedExactQueryByLocationIndex(@PathParam("city") String city, @PathParam("lat") double lat, @PathParam("lng") double lng) {

		if (!constants.cityList.contains(city)) {
			return null;
		}

		int isAIOP = isAInterestingOfPoint(city, lat, lng);
		
		if(isAIOP!=-1) {
			return improvedExactQueryByIdIndex(isAIOP,city);
		}else {
			ExactMethodIndex bm5 = new ExactMethodIndex(city);
			bm5.baselineWihtIndex(lat, lng);

			ArrayList<ResultBean> result = new ArrayList<>();
			for (Result r : bm5.skyPaths) {
				ResultBean rbean = new ResultBean(r);
				result.add(rbean);
			}

			updateBeansNodeLocationInformation(result, city);
			return Response.status(200).entity(result).header("Access-Control-Allow-Origin", "*").build();
			
		}
	}
	
	@Path("/improvedExactIndex/{city}/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response improvedExactQueryByIdIndex(@PathParam("id") int queryPlaceId, @PathParam("city") String city) {

		if (!constants.cityList.contains(city)) {
			return null;
		}

		ExactMethodIndex bm5 = new ExactMethodIndex(city);
		Data queryD = bm5.getDataById(queryPlaceId);
		System.out.println(queryD);
		bm5.baselineIndex(queryD);

		ArrayList<ResultBean> result = new ArrayList<>();
		for (Result r : bm5.skyPaths) {
			ResultBean rbean = new ResultBean(r);
			result.add(rbean);
		}

		updateBeansNodeLocationInformation(result, city);
//		return result;
		return Response.status(200).entity(result).header("Access-Control-Allow-Origin", "*").build();
	}

	
	/** Group of approximate Range indexed methods **/
	@Path("/approxRangeIndexed/{city}/{id}/{threshold}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response ApproxRangeIndexedQueryById(@PathParam("id") int queryPlaceId, @PathParam("city") String city,
			@PathParam("threshold") double distance_threshold) {

		if (!constants.cityList.contains(city)) {
			return null;
		}

		ApproxRangeIndex approx_range_index = new ApproxRangeIndex(city, distance_threshold);
		Data queryD = approx_range_index.getDataById(queryPlaceId);
		System.out.println(queryD);
		approx_range_index.baseline(queryD);

		ArrayList<ResultBean> result = new ArrayList<>();
		for (Result r : approx_range_index.skyPaths) {
			ResultBean rbean = new ResultBean(r);
			result.add(rbean);
		}

		updateBeansNodeLocationInformation(result, city);
		return Response.status(200).entity(result).header("Access-Control-Allow-Origin", "*").build();

	}
	
	@Path("/approxRangeIndexedLocation/{city}/{lat}/{lng}/{threshold}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response ApproxRangeIndexedQueryByLocation(@PathParam("city") String city, @PathParam("lat") double lat, @PathParam("lng") double lng, @PathParam("threshold") double distance_threshold) {

		if (!constants.cityList.contains(city)) {
			return null;
		}
		
		
		int isAIOP = isAInterestingOfPoint(city, lat, lng);
		
		if(isAIOP!=-1) {
			return ApproxRangeIndexedQueryById(isAIOP,city,distance_threshold);
		}else {
			ApproxRangeIndex approx_range_index = new ApproxRangeIndex(city, distance_threshold);
			approx_range_index.baseline(lat,lng); 

			ArrayList<ResultBean> result = new ArrayList<>();
			for (Result r : approx_range_index.skyPaths) {
				ResultBean rbean = new ResultBean(r);
				result.add(rbean);
			}

			updateBeansNodeLocationInformation(result, city);
			return Response.status(200).entity(result).header("Access-Control-Allow-Origin", "*").build();
		}
	}
	
	@Path("/approxRangeIndexedLocationType/{city}/{type}/{lat}/{lng}/{threshold}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response ApproxRangeIndexedQueryByLocationType(@PathParam("city") String city, @PathParam("lat") double lat, @PathParam("lng") double lng,
			@PathParam("threshold") double distance_threshold, @PathParam("type") String type) {

		if (!constants.cityList.contains(city)) {
			return null;
		}
		
		
		int isAIOP = isAInterestingOfPoint(city, lat, lng);
		
		if(isAIOP!=-1) {
			return ApproxRangeIndexedQueryByIdType(isAIOP,city,distance_threshold,type);
		}else {
			ApproxRangeIndex approx_range_index = new ApproxRangeIndex(city, distance_threshold,type);
			approx_range_index.baseline(lat,lng); 
			
			ArrayList<ResultBean> result = new ArrayList<>();
			for (Result r : approx_range_index.skyPaths) {
				ResultBean rbean = new ResultBean(r);
				result.add(rbean);
			}

			updateBeansNodeLocationInformation(result, city);
			return Response.status(200).entity(result).header("Access-Control-Allow-Origin", "*").build();
		}
	}

	@Path("/approxRangeIndexed/{city}/{type}/{id}/{threshold}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response ApproxRangeIndexedQueryByIdType(@PathParam("id") int queryPlaceId, @PathParam("city") String city, @PathParam("threshold") double distance_threshold, @PathParam("type") String type) {

		if (!constants.cityList.contains(city)) {
			return null;
		}

		ApproxRangeIndex approx_range_index = new ApproxRangeIndex(city, distance_threshold, type);
		Data queryD = approx_range_index.getDataById(queryPlaceId);
		System.out.println(queryD);
		approx_range_index.baseline(queryD);

		ArrayList<ResultBean> result = new ArrayList<>();
		for (Result r : approx_range_index.skyPaths) {
			ResultBean rbean = new ResultBean(r);
			result.add(rbean);
		}

		updateBeansNodeLocationInformation(result, city);
		return Response.status(200).entity(result).header("Access-Control-Allow-Origin", "*").build();

	}

	
	/** Group of approximate mixed indexed methods **/
	
	@Path("/approxMixedIndexed/{city}/{id}/{threshold}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response ApproxMixedIndexedQueryById(@PathParam("id") int queryPlaceId, @PathParam("city") String city, @PathParam("threshold") double distance_threshold) {

		if (!constants.cityList.contains(city)) {
			return null;
		}

		ApproxMixedIndex approx_mixed_index = new ApproxMixedIndex(city, distance_threshold);
		Data queryD = approx_mixed_index.getDataById(queryPlaceId);
		System.out.println(queryD);
		approx_mixed_index.baseline(queryD);

		ArrayList<ResultBean> result = new ArrayList<>();
		for (Result r : approx_mixed_index.skyPaths) {
			ResultBean rbean = new ResultBean(r);
			result.add(rbean);
		}

		updateBeansNodeLocationInformation(result, city);
//		return result;
		return Response.status(200).entity(result).header("Access-Control-Allow-Origin", "*").build();

	}
	
	@Path("/approxMixedIndexed/{city}/{type}/{id}/{threshold}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response ApproxMixedIndexedQueryByIdType(@PathParam("id") int queryPlaceId, @PathParam("city") String city,
			@PathParam("threshold") double distance_threshold, @PathParam("type") String type) {

		if (!constants.cityList.contains(city)) {
			return null;
		}

		ApproxMixedIndex approx_mixed_index = new ApproxMixedIndex(city, distance_threshold, type);
		Data queryD = approx_mixed_index.getDataById(queryPlaceId);
		System.out.println(queryD);
		approx_mixed_index.baseline(queryD);

		ArrayList<ResultBean> result = new ArrayList<>();
		for (Result r : approx_mixed_index.skyPaths) {
			ResultBean rbean = new ResultBean(r);
			result.add(rbean);
		}

		updateBeansNodeLocationInformation(result, city);
		return Response.status(200).entity(result).header("Access-Control-Allow-Origin", "*").build();

	}

	@Path("/approxMixedIndexedLocation/{city}/{lat}/{lng}/{threshold}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response ApproxMixedIndexedQueryByLocation(@PathParam("city") String city, @PathParam("lat") double lat, @PathParam("lng") double lng, @PathParam("threshold") double distance_threshold) {

		if (!constants.cityList.contains(city)) {
			return null;
		}
		
		int isAIOP = isAInterestingOfPoint(city, lat, lng);
		
		if(isAIOP!=-1) {
			return ApproxMixedIndexedQueryById(isAIOP,city,distance_threshold);
		}else {
			ApproxMixedIndex approx_mixed_index = new ApproxMixedIndex(city, distance_threshold);
			approx_mixed_index.baseline(lat, lng);

			ArrayList<ResultBean> result = new ArrayList<>();
			for (Result r : approx_mixed_index.skyPaths) {
				ResultBean rbean = new ResultBean(r);
				result.add(rbean);
			}

			updateBeansNodeLocationInformation(result, city);
//			return result;
			return Response.status(200).entity(result).header("Access-Control-Allow-Origin", "*").build();
		}
	}
	
	@Path("/approxMixedIndexedLocationType/{city}/{type}/{lat}/{lng}/{threshold}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response ApproxMixedIndexedQueryByLocationType(@PathParam("city") String city, @PathParam("lat") double lat, @PathParam("lng") double lng, @PathParam("threshold") double distance_threshold, @PathParam("type") String type) {

		if (!constants.cityList.contains(city)) {
			return null;
		}
		
		
		int isAIOP = isAInterestingOfPoint(city, lat, lng);
		
		if(isAIOP!=-1) {
			return ApproxMixedIndexedQueryByIdType(isAIOP,city,distance_threshold,type);
		}else {
			ApproxMixedIndex approx_range_index = new ApproxMixedIndex(city, distance_threshold,type);
			approx_range_index.baseline(lat,lng); 

			ArrayList<ResultBean> result = new ArrayList<>();
			for (Result r : approx_range_index.skyPaths) {
				ResultBean rbean = new ResultBean(r);
				result.add(rbean);
			}

			updateBeansNodeLocationInformation(result, city);
			return Response.status(200).entity(result).header("Access-Control-Allow-Origin", "*").build();
		}
	}

	
	/**Auxiliary functions**/
	// get location information of each bus top and update the result
	private void updateBeansNodeLocationInformation(ArrayList<ResultBean> result, String city) {
		String home_folder = System.getProperty("user.home");
		String graphPath = home_folder + "/neo4j334/testdb_" + city + "_Random/databases/graph.db";
		connector n = new connector(graphPath);
		n.startDB();
		try (Transaction tx = n.graphDB.beginTx()) {
			for (ResultBean rbean : result) {
				for (NodeBeans nbean : rbean.nodeIDs) {
					double[] locations = new double[2];
					locations[0] = (double) n.graphDB.getNodeById(nbean.getId()).getProperty("lat");
					locations[1] = (double) n.graphDB.getNodeById(nbean.getId()).getProperty("log");
					nbean.setLat(locations[0]);
					nbean.setLng(locations[1]);
				}
				rbean.end_name = getLocationNameByID(rbean.end, city);
			}
			tx.success();
		}

		n.shutdownDB();
	}

	private String getLocationNameByID(long id, String city) {
		
		String name = null;
		
		IOPobject iobj = new IOPobject();
		String home_folder = System.getProperty("user.home");
		String dataPath = home_folder + "/mydata/DemoProject/data/staticNode_real_" + city + ".txt";

		BufferedReader br = null;
		int linenumber = 0;

		try {
			br = new BufferedReader(new FileReader(dataPath));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (linenumber == id) {
//                    System.out.println(line);
					String[] infos = line.split(",");
					Double lat = Double.parseDouble(infos[1]);
					Double log = Double.parseDouble(infos[2]);

					Float c1 = Float.parseFloat(infos[3]);
					Float c2 = Float.parseFloat(infos[4]);
					Float c3 = Float.parseFloat(infos[5]);

					iobj.setPlaceID((int) id);
					iobj.setLocations(new double[] { lat, log });
					iobj.setData(new float[] { c1, c2, c3 });

					iobj.setG_p_id(infos[6]);
					iobj.setG_p_name(infos[7]);
					
					name = iobj.g_p_name;
					
					break;
				} else {
					linenumber++;
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Can not open the file, please check it. ");
		}
		
		return name;
	}

	// check whether the given location is a IOP in the data set.
	private int isAInterestingOfPoint(String city, double lat, double lng) {
		int isIOP=-1;
		double min_distance = Double.MAX_VALUE;
		String datapath = this.homepath + "/mydata/DemoProject/data/staticNode_real_" + city + ".txt";

		double[] targetLocations = new double[2];
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(datapath));
			String line = reader.readLine();
			while (line != null) {
				double lat2 = Double.parseDouble(line.split(",")[1]);
				double lng2 = Double.parseDouble(line.split(",")[2]);
				double distance = constants.distanceInMeters(lat,lng,lat2,lng2);
				if(distance < min_distance) {
					min_distance = distance;
					if(distance < t_distance) {
						isIOP = Integer.parseInt(line.split(",")[0]);
						targetLocations[0] = lat2;
						targetLocations[1] = lng2;
					}
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(isIOP+" -->> distance to query location is "+(long)min_distance+"m   "+lat+" "+lng+"     "+targetLocations[0]+" "+targetLocations[1]);
//		constants.distanceInMeters(lat,lng,targetLocations[0],targetLocations[1]);
		return isIOP;
	}
	
	/**Testing main function**/
	public static void main(String args[]) {
		QueryServices qs = new QueryServices();
//		qs.improvedExactQueryByLocationIndex("SF", 37.7784685, -122.3943108);
//		qs.improvedExactQueryByLocation("NY", 40.9062324,-73.90143);
//		System.out.println("======================================================");
//		qs.improvedExactQueryById(5079, "NY");
		qs.improvedExactQueryByIdIndex(5079, "NY");
//		System.out.println("======================================================");
//		qs.improvedExactQueryByLocationType("NY", 40.9062324,-73.90143,"food");
//		qs.improvedExactQueryByLocationTypeIndex("NY", 40.9062324,-73.90143,"food");
//		System.out.println("======================================================");
//		qs.improvedExactQueryByIdType(5079, "NY","food");
//		qs.improvedExactQueryByIdTypeIndex(1233, "LA","food");
//
//		
//		System.out.println("======================================================");
//		qs.ApproxRangeIndexedQueryByLocation("NY", 40.9062324,-73.90143,850);
//		System.out.println("======================================================");
//		qs.ApproxRangeIndexedQueryById(5079, "NY",850);
//		System.out.println("======================================================");
//		qs.ApproxRangeIndexedQueryByLocationType("NY", 40.9062324,-73.90143,850,"food");
//		System.out.println("======================================================");
//		qs.ApproxRangeIndexedQueryByIdType(5079, "NY",850,"food");
////		
////		
//		System.out.println("======================================================");
//		qs.ApproxMixedIndexedQueryByLocation("SF", 37.7784685, -122.3943108 , 750);
//		System.out.println("======================================================");
//		qs.ApproxMixedIndexedQueryById(5079, "NY",850);
//		System.out.println("======================================================");
//		qs.ApproxMixedIndexedQueryByLocationType("NY", 40.9062324,-73.90143,850,"food");
//		System.out.println("======================================================");
//		qs.ApproxMixedIndexedQueryByIdType(5079, "NY",850,"food");
	}
}
