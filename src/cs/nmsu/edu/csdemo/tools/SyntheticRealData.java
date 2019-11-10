package cs.nmsu.edu.csdemo.tools;

import java.io.*;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Random;

import cs.nmsu.edu.csdemo.RstarTree.Constants;
import cs.nmsu.edu.csdemo.RstarTree.Data;
import cs.nmsu.edu.csdemo.RstarTree.Node;
import cs.nmsu.edu.csdemo.RstarTree.RTree;
import cs.nmsu.edu.csdemo.methods.constants;

public class SyntheticRealData {
	int dimension;
	Random r = new Random(System.nanoTime());
	String path_base = constants.home_folder+"/mydata/DemoProject/data/"; // the folder that stores the bus and points of
																	// interest data
	String poi_data = path_base + "IOP_data";
	String tree_path = constants.home_folder+"/mydata/DemoProject/data/real_tree.rtr"; // the place of the Rtree file
	String[] cities = new String[] { "New York", "San Francisco", "Los Angeles" };
	String[] abbr_cities = new String[] { "NY", "SF", "LA" };
	String[] p_types = new String[] { "food", "lodging", "restaurant" };
	HashSet<String> list = new HashSet<>();
	int max_id = 0;
	private DecimalFormat df2 = new DecimalFormat(".##");

	public SyntheticRealData(int dimension) {
		this.dimension = dimension;
	}

	public static void main(String args[]) {
		int dimension = 3;

		if (args.length == 1) {
			dimension = Integer.parseInt(args[0]);
		}

		SyntheticRealData sd = new SyntheticRealData(dimension);
		sd.readPOIsData();
	}

	private void readPOIsData() {

		File fp = new File(tree_path);

		if (fp.exists()) {
			fp.delete();
		}

		FileWriter fw = null;
		BufferedWriter bw = null;

		long sum_counter = 0;
		for (int c = 0; c < this.cities.length; c++) {
			String city = this.cities[c];
			String abbr_city = this.abbr_cities[c];
			this.max_id = 0;
			long counter = 0;

			File file = new File(constants.home_folder+"/mydata/DemoProject/data/staticNode_real_" + abbr_city + ".txt"); // store
																													// the
			// hotel information
			String t_path = constants.home_folder+"/mydata/DemoProject/data/real_tree_" + abbr_city + ".rtr";
			RTree rt = new RTree(t_path, Constants.BLOCKLENGTH, Constants.CACHESIZE, dimension);

			if (file.exists()) {
				file.delete();
			}

			for (String type : p_types) {
				String path = this.poi_data + "/outfilename_" + type + "_" + city;

				try {

					fw = new FileWriter(file.getAbsoluteFile(), true);
					bw = new BufferedWriter(fw);

					File f = new File(path);
					BufferedReader b = new BufferedReader(new FileReader(f));
					String line = "";

					POIObject poi_obj = new POIObject();

					poi_obj.placeID = this.max_id;
					poi_obj.data = new float[] { -1, -1, -1 };
					poi_obj.g_p_id = "";
					poi_obj.locations = new double[] { -1, -1 };
					poi_obj.g_p_name = "";

					while (((line = b.readLine()) != null)) {

						counter++;

						if (line.startsWith(
								"=======================================================================")) {
							if (!this.list.contains(poi_obj.g_p_id) && poi_obj.locations[0] != -1
									&& poi_obj.locations[1] != -1) {
								this.max_id++;
								this.list.add(poi_obj.g_p_id);

								Data d = new Data(dimension);
								d.setPlaceId(poi_obj.placeID);
								d.setLocation(poi_obj.locations);

								for (int i = 0; i < poi_obj.data.length; i++) {
									// if there is no-value, generate a random value follow the Gaussian
									// distribution.
									if (poi_obj.data[i] == -1) {
										poi_obj.data[i] = getGaussian(2.5, 5 / 6);
									}

									poi_obj.data[i] = Float.parseFloat(df2.format(poi_obj.data[i]));
								}

								d.setData(poi_obj.data);
								bw.write(poi_obj.placeID + "," + poi_obj.locations[0] + "," + poi_obj.locations[1] + ","
										+ poi_obj.data[0] + "," + poi_obj.data[1] + "," + poi_obj.data[2] + ","
										+ poi_obj.g_p_id +","+poi_obj.g_p_name+ "\n");
								System.out.println(
										poi_obj.placeID + "," + poi_obj.locations[0] + "," + poi_obj.locations[1] + ","
												+ poi_obj.data[0] + "," + poi_obj.data[1] + "," + poi_obj.data[2]);
								rt.insert(d);
							}

							poi_obj.cleanContents();
							poi_obj.placeID = this.max_id;
						} else if (line.startsWith("placeId")) {
							poi_obj.g_p_id = line.split(":")[1].trim();
						} else if (line.startsWith("rating:")) {
							poi_obj.data[0] = 5 - Float.valueOf(line.split(":")[1].trim());
						} else if (line.startsWith("pricelevel:")) {
							poi_obj.data[1] = Float.valueOf(line.split(":")[1].trim());
						} else if (line.startsWith("[")) {
							poi_obj.data[2] = 10 - line.split(",").length;
						} else if (line.startsWith("   locations:")) {
							poi_obj.locations[0] = Double.parseDouble(line.split(":")[1].trim().split(",")[0]);
							poi_obj.locations[1] = Double.parseDouble(line.split(":")[1].trim().split(",")[1]);
						} else if (line.startsWith("name:")) {
							poi_obj.g_p_name = line.split(":")[1].trim();
						}

					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {

					try {

						if (bw != null)
							bw.close();

						if (fw != null)
							fw.close();
//                System.out.println("Done!! See MCP_Results.csv for MCP of each cow for each day.");

					} catch (IOException ex) {

						ex.printStackTrace();

					}
				}
			}
			rt.delete(); // write tree to disk
			sum_counter += counter;
		}

		System.out.println(sum_counter);
		System.out.println(this.list.size());

	}

	public void testStaticRTree() {
		RTree rt = new RTree(this.tree_path, Constants.CACHESIZE);
		System.out.println((((Node) rt.root_ptr).get_num_of_data()));
	}

	private float getGaussian(double mean, double sd) {
		double value = r.nextGaussian() * sd + mean;

		while (value < 0 || value > 5) {
			value = r.nextGaussian() * sd + mean;
		}

		return (float) value;
	}
}
