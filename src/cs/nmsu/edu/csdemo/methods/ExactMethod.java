package cs.nmsu.edu.csdemo.methods;

import org.apache.commons.cli.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;

import cs.nmsu.edu.csdemo.RstarTree.Data;
import cs.nmsu.edu.csdemo.neo4jTools.connector;
import cs.nmsu.edu.csdemo.tools.GoogleMaps;

import java.io.*;
import java.util.*;

public class ExactMethod {
	public double nn_dist; // the euclidean distance from the query hotel to the bus stop.
	public ArrayList<path> qqqq = new ArrayList<>();
	public ArrayList<Result> skyPaths = new ArrayList<>();
	public GraphDatabaseService graphdb;
	Random r = new Random(System.nanoTime());
	String treePath;
	String dataPath;
	int graph_size;
	String degree;
	String graphPath;
	long add_oper = 0;
	long check_add_oper = 0;
	long map_operation = 0;
	long checkEmpty = 0;
	long read_data = 0;
	// Todo: each hotel know the distance to the hotel than dominate it.
	HashMap<Integer, Double> dominated_checking = new HashMap<>(); //
	String home_folder = System.getProperty("user.home");
	private int hotels_num;
	private double range;
	private HashMap<Long, myNode> tmpStoreNodes = new HashMap();
	private ArrayList<Data> sNodes = new ArrayList<>();
	private ArrayList<Data> sky_hotel;
	private HashSet<Integer> finalDatas = new HashSet<>();
	private int checkedDataId = 9;
	private long add_counter; // how many times call the addtoResult function
	private long pro_add_result_counter; // how many path + hotel combination of the results are generated
	private long sky_add_result_counter; // how many results are taken the addtoskyline operation
	private Data queryD;
	private long d_list_num = 0;

	public ExactMethod(int graph_size, String degree, double range, int hotels_num) {
		this.range = range;
		this.hotels_num = hotels_num;
		r = new Random(System.nanoTime());
		this.graph_size = graph_size;
		this.degree = degree;
		this.graphPath = home_folder + "/neo4j334/testdb" + this.graph_size + "_" + this.degree + "/databases/graph.db";
		this.treePath = home_folder + "/shared_git/bConstrainSkyline/data/test_" + this.graph_size + "_" + this.degree
				+ "_" + range + "_" + hotels_num + ".rtr";
		this.dataPath = home_folder + "/shared_git/bConstrainSkyline/data/staticNode_" + this.graph_size + "_"
				+ this.degree + "_" + range + "_" + hotels_num + ".txt";
	}

	public ExactMethod(String city) {
		r = new Random(System.nanoTime());
		this.graphPath = home_folder + "/neo4j334/testdb_" + city + "_Random/databases/graph.db";
		this.treePath = home_folder + "/mydata/DemoProject/data/real_tree_" + city + ".rtr";
		this.dataPath = home_folder + "/mydata/DemoProject/data/staticNode_real_" + city + ".txt";
		this.hotels_num = getNumberOfHotels();
		System.out.println("There are " + this.hotels_num + " in the city " + city);
	}

	public ExactMethod(String city, String type) {
		r = new Random(System.nanoTime());
		this.graphPath = home_folder + "/neo4j334/testdb_" + city + "_Random/databases/graph.db";
		this.treePath = home_folder + "/mydata/DemoProject/data/real_tree_" + city + "_" + type + ".rtr";
		this.dataPath = home_folder + "/mydata/DemoProject/data/staticNode_real_" + city + "_" + type + ".txt";
		this.hotels_num = getNumberOfHotels();
		System.out.println("There are " + this.hotels_num + " " + type + " in the city " + city);
	}

	public ExactMethod(String tree, String data, String graph) {
		this.graphPath = graph;
		this.treePath = tree;
		this.dataPath = data;
	}

	public void baseline(Data queryD) {
		this.queryD = queryD;
		StringBuffer sb = new StringBuffer();
		sb.append(queryD.getPlaceId() + " ");

		Skyline sky = new Skyline(treePath);

		// find the skyline hotels of the whole dataset.
		sky.findSkyline(queryD);

		this.sky_hotel = new ArrayList<>(sky.sky_hotels);
//        for (Data sddd : sky.sky_hotels) {
//            System.out.println(sddd.getPlaceId());
//        }
//        System.out.println("there are " + this.sky_hotel.size() + " skyline hotels");
//        System.out.println("-------------------------");

		long s_sum = System.currentTimeMillis();
		long index_s = 0;
		int sk_counter = 0; // the number of total candidate hotels of each bus station

		long r1 = System.currentTimeMillis();
		// Find the hotels that aren't dominated by the query point
		sky.BBS(queryD);
		long bbs_rt = System.currentTimeMillis() - r1;
		sNodes = sky.skylineStaticNodes;

		for (Data d : sNodes) {
			double[] c = new double[constants.path_dimension + 3];
			c[0] = d.distance_q;
			double[] d_attrs = d.getData();
			for (int i = 4; i < c.length; i++) {
				c[i] = d_attrs[i - 4];
			}
			Result r = new Result(queryD, d, c, null);
			addToSkyline(r);
		}
		sb.append(this.sNodes.size() + " " + this.sky_hotel.size() + " " + this.skyPaths.size() + " ");

		// find the minimum distance from query point to the skyline hotel that dominate
		// non-skyline hotel cand_d
		for (Data cand_d : sNodes) {
			double h_to_h_dist = Double.MAX_VALUE;

			if (!sky_hotel.contains(cand_d)) {
				for (Data s_h : sky_hotel) {
					if (checkDominated(s_h.getData(), cand_d.getData())) {
//                        double tmep_dist = Math.pow(s_h.location[0] - queryD.location[0], 2) + Math.pow(s_h.location[1] - queryD.location[1], 2);
//                        tmep_dist = Math.sqrt(tmep_dist);
						double tmep_dist = s_h.distance_q;
						if (tmep_dist < h_to_h_dist) {
							h_to_h_dist = tmep_dist;
						}
					}
				}
			}

			dominated_checking.put(cand_d.getPlaceId(), h_to_h_dist);
		}

		long db_time = System.currentTimeMillis();
		constants.accessedEdges.clear();
		constants.accessedNodes.clear();
		connector n = new connector(graphPath);
		n.startDB();
		this.graphdb = n.getDBObject();

		long counter = 0;
		long addResult_rt = 0;
		long expasion_rt = 0;

		try (Transaction tx = this.graphdb.beginTx()) {
			db_time = System.currentTimeMillis() - db_time;
			r1 = System.currentTimeMillis();

			long nn_rt = System.currentTimeMillis() - r1;

			long rt = System.currentTimeMillis();
			Node startNode = nearestNetworkNode(queryD);
			long numberofNodes = n.getNumberofNodes();
			while (startNode != null) {
				System.out.println(startNode.getId()+"   ----->   "+ this.tmpStoreNodes.size() + "/"+numberofNodes);
				myNode s = new myNode(queryD, startNode.getId(), -1, n);
				myNodePriorityQueue mqueue = new myNodePriorityQueue();
				mqueue.add(s);

				this.tmpStoreNodes.put(s.id, s);

				while (!mqueue.isEmpty()) {

					myNode v = mqueue.pop();
					v.inqueue = false;

					counter++;

					for (int i = 0; i < v.skyPaths.size(); i++) {
						path p = v.skyPaths.get(i);
						if (!p.expaned) {
							p.expaned = true;
							long ee = System.nanoTime();
							ArrayList<path> new_paths = p.expand(n);
							expasion_rt += (System.nanoTime() - ee);
							for (path np : new_paths) {
								if (!np.hasCycle()) {
									myNode next_n;
									if (this.tmpStoreNodes.containsKey(np.endNode)) {
										next_n = tmpStoreNodes.get(np.endNode);
									} else {
										next_n = new myNode(queryD, np.endNode, -1, n);
										this.tmpStoreNodes.put(next_n.id, next_n);
									}

									// lemma 2
									if (!(this.tmpStoreNodes.get(np.startNode).distance_q > next_n.distance_q)) {
										if (next_n.addToSkyline(np) && !next_n.inqueue) {
											mqueue.add(next_n);
											next_n.inqueue = true;
										}
									}
								}
							}
						}
					}
				}

				startNode = nearestNetworkNode(queryD);
			}

//			System.out.println("--------------------------------------------------");

			long exploration_rt = System.currentTimeMillis() - rt;
//            System.out.println("expansion finished " + exploration_rt);
			long graph_process_rt = System.currentTimeMillis() - s_sum;

			long tt_sl = 0;

//            hotels_scope = new HashMap<>();
			int addtocounter = 0;
			for (Map.Entry<Long, myNode> entry : tmpStoreNodes.entrySet()) {
//				if(addtocounter%200==0) {
//					System.out.println(addtocounter+"............................................");
//				}
//				addtocounter++;

				sk_counter += entry.getValue().skyPaths.size();
				myNode my_n = entry.getValue();

				long t_index_s = System.nanoTime();

				index_s += (System.nanoTime() - t_index_s);

				for (path p : my_n.skyPaths) {
					if (!p.rels.isEmpty()) {
						long ats = System.nanoTime();

						boolean f = addToSkylineResult(p, sNodes);

						addResult_rt += System.nanoTime() - ats;
					}
				}

			}

			// time that is used to find the candidate objects, find the nearest objects,
			sb.append(bbs_rt + "," + nn_rt + "," + exploration_rt + "," + (index_s / 1000000) + ", graph_process_rt:"
					+ graph_process_rt);
			tx.success();
		}

		long shut_db_time = System.currentTimeMillis();
		n.shutdownDB();
		shut_db_time = System.currentTimeMillis() - shut_db_time;

		s_sum = System.currentTimeMillis() - s_sum;
		sb.append("| running time(ms):" + (s_sum - db_time - shut_db_time - (index_s / 1000000)) + "|");
		sb.append(" # of final skyline results:" + this.skyPaths.size() + ", counter:" + counter + "|");
		sb.append("time used to add to skyline: " + addResult_rt / 1000000 + "(" + (this.add_oper / 1000000) + "+"
				+ (this.check_add_oper / 1000000) + "+" + (this.map_operation / 1000000) + "+"
				+ (this.checkEmpty / 1000000) + "+" + (this.read_data / 1000000) + "),");
		sb.append("time used to expasion: " + expasion_rt / 1000000 + ", ");
//        sb.append("\nadd_to_Skyline_result " + this.add_counter + "  " + this.pro_add_result_counter + "  " + this.sky_add_result_counter + " ");
//        sb.append((double) this.sky_add_result_counter / this.pro_add_result_counter);

		List<Result> sortedList = new ArrayList(this.skyPaths);
		Collections.sort(sortedList);

		HashSet<Long> final_bus_stops = new HashSet<>();

		for (Result r : sortedList) {
			this.finalDatas.add(r.end.getPlaceId());
			if (r.p != null) {
				for (Long nn : r.p.nodes) {
					final_bus_stops.add(nn);
				}
			}
		}

		sb.append("size of the final hotels:" + finalDatas.size() + " " + this.skyPaths.size() + "  " + add_counter
				+ " ");

		int visited_bus_stop = this.tmpStoreNodes.size();
		int bus_stop_in_result = final_bus_stops.size();

		sb.append("  " + visited_bus_stop + "," + bus_stop_in_result + ","
				+ (double) bus_stop_in_result / visited_bus_stop + "   " + this.sky_add_result_counter);

		sb.append(" " + sNodes.size() + " " + sk_counter);

		System.out.println(sb.toString());

	}

	public void baseline(double lat, double lng) {
		StringBuffer sb = new StringBuffer();
		sb.append("[" + lat + "," + lng + "]" + " ");

		Skyline sky = new Skyline(treePath);

		// find the skyline hotels of the whole dataset.
		sky.findSkyline(lat, lng);

		this.sky_hotel = new ArrayList<>(sky.sky_hotels);

		long s_sum = System.currentTimeMillis();
		long index_s = 0;
		int sk_counter = 0; // the number of total candidate hotels of each bus station

		long r1 = System.currentTimeMillis();
//		 Find the hotels that aren't dominated by the query point
		sky.allDatas(lat, lng);
		long bbs_rt = System.currentTimeMillis() - r1;
		sNodes = sky.allNodes;

		for (Data d : sNodes) {
			double[] c = new double[constants.path_dimension + 3];
			c[0] = d.distance_q;
			double[] d_attrs = d.getData();
			for (int i = 4; i < c.length; i++) {
				c[i] = d_attrs[i - 4];
			}
			Result r = new Result(lat, lng, d, c, null);
			addToSkyline(r);
		}
		sb.append(this.sNodes.size() + " " + this.sky_hotel.size() + " #ofSkyline:" + this.skyPaths.size() + " ");
		// find the minimum distance from query point to the skyline hotel that dominate
		// non-skyline hotel cand_d
		for (Data cand_d : sNodes) {
			double h_to_h_dist = Double.MAX_VALUE;

			if (!sky_hotel.contains(cand_d)) {
				for (Data s_h : sky_hotel) {
					if (checkDominated(s_h.getData(), cand_d.getData())) {
						double tmep_dist = s_h.distance_q;
						if (tmep_dist < h_to_h_dist) {
							h_to_h_dist = tmep_dist;
						}
					}
				}
			}
			dominated_checking.put(cand_d.getPlaceId(), h_to_h_dist);
		}

		long db_time = System.currentTimeMillis();
		constants.accessedEdges.clear();
		constants.accessedNodes.clear();
		connector n = new connector(graphPath);
		n.startDB();
		this.graphdb = n.getDBObject();

		long counter = 0;
		long addResult_rt = 0;
		long expasion_rt = 0;

		long iteration_rt = System.nanoTime();

		try (Transaction tx = this.graphdb.beginTx()) {
			db_time = System.currentTimeMillis() - db_time;
			r1 = System.currentTimeMillis();

			long nn_rt = System.currentTimeMillis() - r1;

			long rt = System.currentTimeMillis();
			Node startNode = nearestNetworkNode(lat, lng);
			long numberofNodes = n.getNumberofNodes();

			while (startNode != null) {

				long last_iter_rt = System.nanoTime() - iteration_rt;
				iteration_rt = System.nanoTime();
//				System.out.println(startNode.getId()+"   ----->   "+ this.tmpStoreNodes.size() + "/"+numberofNodes+"   Last Iteration Runing time:"+(last_iter_rt/1000000)+"ms");
				myNode s = new myNode(lat, lng, startNode.getId(), -1, n);

				myNodePriorityQueue mqueue = new myNodePriorityQueue();
				mqueue.add(s);
				this.tmpStoreNodes.put(s.id, s);

				while (!mqueue.isEmpty()) {
					myNode v = mqueue.pop();
					v.inqueue = false;
					counter++;

					for (int i = 0; i < v.skyPaths.size(); i++) {
						path p = v.skyPaths.get(i);
						if (!p.expaned) {
							p.expaned = true;
							long ee = System.nanoTime();
							ArrayList<path> new_paths = p.expand(n);
							expasion_rt += (System.nanoTime() - ee);
							for (path np : new_paths) {
								if (!np.hasCycle()) {
									myNode next_n;
									if (this.tmpStoreNodes.containsKey(np.endNode)) {
										next_n = tmpStoreNodes.get(np.endNode);
									} else {
										next_n = new myNode(lat, lng, np.endNode, -1, n);
										this.tmpStoreNodes.put(next_n.id, next_n);
									}

									// lemma 2
									if (!(this.tmpStoreNodes.get(np.startNode).distance_q > next_n.distance_q)) {
										if (next_n.addToSkyline(np) && !next_n.inqueue) {
											mqueue.add(next_n);
											next_n.inqueue = true;
										}
									}
								}
							}
						}
					}
				}

				startNode = nearestNetworkNode(lat, lng);
			}

//			System.out.println("------------------------------------------------------");

			long exploration_rt = System.currentTimeMillis() - rt;
//            System.out.println("expansion finished " + exploration_rt);

			long graph_process_rt = System.currentTimeMillis() - r1;

			long tt_sl = 0;

//            hotels_scope = new HashMap<>();
			int addtocounter = 0;
			for (Map.Entry<Long, myNode> entry : tmpStoreNodes.entrySet()) {
//				if(addtocounter%200==0) {
//				}

				long one_iter_rt = System.currentTimeMillis();
				addtocounter++;
				sk_counter += entry.getValue().skyPaths.size();
				myNode my_n = entry.getValue();

				long t_index_s = System.nanoTime();

				index_s += (System.nanoTime() - t_index_s);

				for (path p : my_n.skyPaths) {
//                    if (!p.rels.isEmpty()) {
					long ats = System.nanoTime();

					boolean f = addToSkylineResultByLocation(lat, lng, p, sNodes);

					addResult_rt += System.nanoTime() - ats;
//                    }
				}

				one_iter_rt = System.currentTimeMillis() - one_iter_rt;
//				System.out.println("size of skyline of Node "+ entry.getKey()+" is "+ entry.getValue().skyPaths.size()+" used "+ one_iter_rt+ "ms #### "+ sNodes.size() + " ####"+addtocounter+"............................................");
				one_iter_rt = System.currentTimeMillis();
			}

			// time that is used to find the candidate objects, find the nearest objects,
			sb.append(bbs_rt + "," + nn_rt + "," + exploration_rt + "," + (index_s / 1000000) + ", graph_process_rt:"
					+ graph_process_rt);
			tx.success();
		}

		long shut_db_time = System.currentTimeMillis();
		n.shutdownDB();
		shut_db_time = System.currentTimeMillis() - shut_db_time;

		s_sum = System.currentTimeMillis() - s_sum;
		sb.append("| running time(ms):" + (s_sum - db_time - shut_db_time - (index_s / 1000000)) + "|");
		sb.append(" # of final skyline results:" + this.skyPaths.size() + ", counter:" + counter + "|");
		sb.append("time used to add to skyline: " + addResult_rt / 1000000 + "(" + (this.add_oper / 1000000) + "+"
				+ (this.check_add_oper / 1000000) + "+" + (this.map_operation / 1000000) + "+"
				+ (this.checkEmpty / 1000000) + "+" + (this.read_data / 1000000) + "),");
		sb.append("time used to expasion: " + expasion_rt / 1000000 + ", ");
//        sb.append("\nadd_to_Skyline_result " + this.add_counter + "  " + this.pro_add_result_counter + "  " + this.sky_add_result_counter + " ");
//        sb.append((double) this.sky_add_result_counter / this.pro_add_result_counter);

		List<Result> sortedList = new ArrayList(this.skyPaths);
		Collections.sort(sortedList);

		HashSet<Long> final_bus_stops = new HashSet<>();

		for (Result r : sortedList) {
			this.finalDatas.add(r.end.getPlaceId());
			if (r.p != null) {
				for (Long nn : r.p.nodes) {
					final_bus_stops.add(nn);
				}
			}
		}

		sb.append("size of the final hotels:" + finalDatas.size() + " " + this.skyPaths.size() + "  " + add_counter
				+ " ");

		int visited_bus_stop = this.tmpStoreNodes.size();
		int bus_stop_in_result = final_bus_stops.size();

		sb.append("  " + visited_bus_stop + "," + bus_stop_in_result + ","
				+ (double) bus_stop_in_result / visited_bus_stop + "   " + this.sky_add_result_counter);

		sb.append(" " + sNodes.size() + " " + sk_counter);

		System.out.println(sb.toString());
//		System.out.println("Exact Location Method Finished");

	}

	private boolean addToSkylineResult(path np, ArrayList<Data> d_list) {

//    private boolean addToSkylineResult(path np, Data d) {
		this.add_counter++;
		long r2a = System.nanoTime();

//		if (np.rels.isEmpty()) {
//			return false;
//		}
		if (np.isDummyPath()) {
			return false;
		}

		this.checkEmpty += System.nanoTime() - r2a;

		long rr = System.nanoTime();
		myNode my_endNode = this.tmpStoreNodes.get(np.endNode);
		this.map_operation += System.nanoTime() - rr;

		long dsad = System.nanoTime();
		long d1 = 0, d2 = 0;
		boolean flag = false;

		for (Data d : d_list) {
			if (!this.dominated_checking.containsKey(d.getPlaceId()) || d.getPlaceId() == queryD.getPlaceId()) {
				continue;
			}

			this.pro_add_result_counter++;
			long rrr = System.nanoTime();

			if (d.getPlaceId() == queryD.getPlaceId()) {
				continue;
			}

			double[] final_costs = new double[np.costs.length + 3];
			System.arraycopy(np.costs, 0, final_costs, 0, np.costs.length);
//			double end_distance = Math.sqrt(Math.pow(my_endNode.locations[0] - d.location[0], 2)
//					+ Math.pow(my_endNode.locations[1] - d.location[1], 2));

//            d.distance_q = Math.sqrt(Math.pow(d.location[0] - queryD.location[0], 2) + Math.pow(d.location[1] - queryD.location[1], 2));

			double end_distance = GoogleMaps.distanceInMeters(my_endNode.locations[0], my_endNode.locations[1],
					d.location[0], d.location[1]);

			final_costs[0] += end_distance;
			// lemma3
			// double d3 = Math.sqrt(Math.pow(d.location[0] - queryD.location[0], 2) +
			// Math.pow(d.location[1] - queryD.location[1], 2));

			if (final_costs[0] < d.distance_q && final_costs[0] < this.dominated_checking.get(d.getPlaceId())) {

				double[] d_attrs = d.getData();
				for (int i = 4; i < final_costs.length; i++) {
					final_costs[i] = d_attrs[i - 4];
				}

				Result r = new Result(this.queryD, d, final_costs, np);

				this.check_add_oper += System.nanoTime() - rrr;
				d1 += System.nanoTime() - rrr;
				long rrrr = System.nanoTime();
				this.sky_add_result_counter++;
				boolean t = addToSkyline(r);

				this.add_oper += System.nanoTime() - rrrr;
				d2 += System.nanoTime() - rrrr;

				if (!flag && t) {
					flag = true;
				}
			}
		}

		this.read_data += (System.nanoTime() - d1 - d2 - dsad);
		return flag;
	}

	private boolean addToSkylineResultByLocation(double lat, double lng, path np, ArrayList<Data> d_list) {
		this.add_counter++;
		long r2a = System.nanoTime();

//		if (np.rels.isEmpty()) {
//			return false;
//		}

		if (np.isDummyPath()) {
			return false;
		}

		this.checkEmpty += System.nanoTime() - r2a;

		long rr = System.nanoTime();
		myNode my_endNode = this.tmpStoreNodes.get(np.endNode);
		this.map_operation += System.nanoTime() - rr;

		long dsad = System.nanoTime();
		long d1 = 0, d2 = 0;
		boolean flag = false;

		for (Data d : d_list) {
			if (!this.dominated_checking.containsKey(d.getPlaceId())) {
				continue;
			}

			this.pro_add_result_counter++;
			long rrr = System.nanoTime();

			double[] final_costs = new double[np.costs.length + 3];
			System.arraycopy(np.costs, 0, final_costs, 0, np.costs.length);
//			double end_distance = Math.sqrt(Math.pow(my_endNode.locations[0] - d.location[0], 2)
//						+ Math.pow(my_endNode.locations[1] - d.location[1], 2));

//	        d.distance_q = Math.sqrt(Math.pow(d.location[0] - queryD.location[0], 2) + Math.pow(d.location[1] - queryD.location[1], 2));

			double end_distance = GoogleMaps.distanceInMeters(my_endNode.locations[0], my_endNode.locations[1],
					d.location[0], d.location[1]);

			final_costs[0] += end_distance;
			// lemma3
			// double d3 = Math.sqrt(Math.pow(d.location[0] - queryD.location[0], 2) +
			// Math.pow(d.location[1] - queryD.location[1], 2));

			if (final_costs[0] < d.distance_q && final_costs[0] < this.dominated_checking.get(d.getPlaceId())) {

				double[] d_attrs = d.getData();
				for (int i = 4; i < final_costs.length; i++) {
					final_costs[i] = d_attrs[i - 4];
				}

				Result r = new Result(lat, lng, d, final_costs, np);

				this.check_add_oper += System.nanoTime() - rrr;
				d1 += System.nanoTime() - rrr;
				long rrrr = System.nanoTime();
				this.sky_add_result_counter++;
				boolean t = addToSkyline(r);

				this.add_oper += System.nanoTime() - rrrr;
				d2 += System.nanoTime() - rrrr;

				if (!flag && t) {
					flag = true;
				}
			}
		}

		this.read_data += (System.nanoTime() - d1 - d2 - dsad);
		return flag;
	}

	public Data generateQueryData() {
		Data d = new Data(3);
		d.setPlaceId(9999999);
		float latitude = randomFloatInRange(0f, 180f);
		float longitude = randomFloatInRange(0f, 180f);
		d.setLocation(new double[] { latitude, longitude });

		float priceLevel = randomFloatInRange(0f, 5f);
		float Rating = randomFloatInRange(0f, 5f);
		float other = randomFloatInRange(0f, 5f);
		d.setData(new float[] { priceLevel, Rating, other });
		return d;
	}

	public float randomFloatInRange(float min, float max) {
		float random = min + r.nextFloat() * (max - min);
		return random;
	}

	public Node nearestNetworkNode(Data queryD) {
		Node nn_node = null;
		double distz = Float.MAX_VALUE;
		try (Transaction tx = this.graphdb.beginTx()) {
			ResourceIterable<Node> iter = this.graphdb.getAllNodes();
			for (Node n : iter) {
				double lat = (double) n.getProperty("lat");
				double log = (double) n.getProperty("log");

				double temp_distz = (Math.pow(lat - queryD.location[0], 2) + Math.pow(log - queryD.location[1], 2));
				if (distz > temp_distz && !this.tmpStoreNodes.containsKey(n.getId())) {
					nn_node = n;
					distz = temp_distz;
					this.nn_dist = distz;
				}
			}
			tx.success();
		}

		this.nn_dist = distz;
		return nn_node;
	}

	public Node nearestNetworkNode(double q_lat, double q_lng) {
		Node nn_node = null;
		double distz = Float.MAX_VALUE;
		try (Transaction tx = this.graphdb.beginTx()) {
			ResourceIterable<Node> iter = this.graphdb.getAllNodes();
			for (Node n : iter) {
				double lat = (double) n.getProperty("lat");
				double log = (double) n.getProperty("log");

				double temp_distz = (Math.pow(lat - q_lat, 2) + Math.pow(log - q_lng, 2));
				if (distz > temp_distz && !this.tmpStoreNodes.containsKey(n.getId())) {
					nn_node = n;
					distz = temp_distz;
					this.nn_dist = distz;
				}
			}
			tx.success();
		}

		this.nn_dist = distz;
//        nn_dist = (int) Math.ceil(distz);
		return nn_node;
	}

	public boolean addToSkyline(Result r) {
		int i = 0;
//        if (r.end.getPlaceId() == checkedDataId) {
//            System.out.println(r);
//        }
		if (skyPaths.isEmpty()) {
			this.skyPaths.add(r);
		} else {
			boolean can_insert_np = true;
			for (; i < skyPaths.size();) {
				if (checkDominated(skyPaths.get(i).costs, r.costs)) {
					can_insert_np = false;
					break;
				} else {
					if (checkDominated(r.costs, skyPaths.get(i).costs)) {
						this.skyPaths.remove(i);
					} else {
						i++;
					}
				}
			}

			if (can_insert_np) {
				this.skyPaths.add(r);
				return true;
			}
		}

		return false;
	}

	private boolean checkDominated(double[] costs, double[] estimatedCosts) {
		for (int i = 0; i < costs.length; i++) {
			if (costs[i] * (1.0) > estimatedCosts[i]) {
				return false;
			}
		}
		return true;
	}

	public Data getDataById(int placeId) {
		BufferedReader br = null;
		int linenumber = 0;

		Data queryD = new Data(3);

		try {
			br = new BufferedReader(new FileReader(this.dataPath));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (linenumber == placeId) {
//                    System.out.println(line);
					String[] infos = line.split(",");
					Double lat = Double.parseDouble(infos[1]);
					Double log = Double.parseDouble(infos[2]);

					Float c1 = Float.parseFloat(infos[3]);
					Float c2 = Float.parseFloat(infos[4]);
					Float c3 = Float.parseFloat(infos[5]);

					queryD.setPlaceId(placeId);
					queryD.setLocation(new double[] { lat, log });
					queryD.setData(new float[] { c1, c2, c3 });
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
		return queryD;
	}

	public int getRandomNumberInRange_int(int min, int max) {

		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}

		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}

	public int getNumberOfHotels() {
		int result = 0;
		File f = new File(this.dataPath);
		BufferedReader b = null;
		try {
			b = new BufferedReader(new FileReader(f));
			String readLine = "";

			while (((readLine = b.readLine()) != null)) {
				result++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;

	}

}
