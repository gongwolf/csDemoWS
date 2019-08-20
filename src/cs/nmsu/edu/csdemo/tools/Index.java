package cs.nmsu.edu.csdemo.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import cs.nmsu.edu.csdemo.RstarTree.Data;
import cs.nmsu.edu.csdemo.methods.Skyline;
import cs.nmsu.edu.csdemo.methods.constants;
import cs.nmsu.edu.csdemo.methods.myNode;
import cs.nmsu.edu.csdemo.neo4jTools.connector;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.Transaction;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class Index {
	private final String base = System.getProperty("user.home") + "/mydata/DemoProject/data/index";
	public String home_folder;
	private String dataPath;
	private String treePath;
	private String graphPath;
	private int bits_place_id;
	private String source_data_tree;
	private String neo4j_db;
	// private final int pagesize_data;
	private int pagesize_list;
	private String node_info_path;
	private long num_nodes;
	private double distance_threshold;

	public Index(String city, double distance_threshold) {
//		this.distance_threshold = 0.0105;
		this.distance_threshold = distance_threshold;
		if(distance_threshold!= -1) {
			this.home_folder = base + "/" + city + "_index_"+(int)distance_threshold+"/";
		}else {
			this.home_folder = base + "/" + city + "_index_all/";
		}
		
		this.graphPath = System.getProperty("user.home") + "/neo4j334/testdb_" + city + "_Random/databases/graph.db";
		this.treePath = System.getProperty("user.home") + "/mydata/DemoProject/data/real_tree_" + city + ".rtr";
		this.dataPath = System.getProperty("user.home") + "/mydata/DemoProject/data/staticNode_real_" + city + ".txt";

		this.source_data_tree = this.treePath;
		this.neo4j_db = this.graphPath;
		this.node_info_path = System.getProperty("user.home") + "/mydata/projectData/testGraph_real_50_Random/data/"
				+ city + "_NodeInfo.txt";
		this.num_nodes = getLineNumbers();
		this.pagesize_list = 1024;
	}

	public void buildIndex(boolean deleteBeforeBuild) {
		if (deleteBeforeBuild) {
			File dataF = new File(home_folder);
			try {
				FileUtils.deleteDirectory(dataF);
				dataF.mkdirs();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Skyline sk = new Skyline(this.source_data_tree);
		sk.allDatas(); // get all data objects from R-tree
		sk.findSkyline(); // get skyline objects among all the data objects
		System.out.println("number of data objects " + sk.allNodes.size());
		System.out.println("number of skyline data objects:" + sk.sky_hotels.size());
		writeDataToDisk(sk.allNodes);

		String header_name = this.home_folder + "/header.idx";
		String list_name = this.home_folder + "/list.idx";

		System.out.println("number of bus stops :" + this.num_nodes);

		try {
			connector n = new connector(this.neo4j_db);
			n.startDB();
			RandomAccessFile header_f = new RandomAccessFile(header_name, "rw");
			header_f.seek(0);
			RandomAccessFile list_f = new RandomAccessFile(list_name, "rw");
			list_f.seek(0);

			int page_list_number = 0;
			for (int node_id = 0; node_id <= num_nodes; node_id++) {
				if (node_id % 1000 == 0) {
					System.out.println("========================" + node_id + "=========================");
				}

				try (Transaction tx = n.graphDB.beginTx()) {
					myNode node = new myNode(node_id,n);

					ArrayList<Data> d_list;
					if (this.distance_threshold == -1) {
						d_list = new ArrayList<>(sk.sky_hotels);
					} else {
						d_list = new ArrayList<>();
						for (Data d : sk.sky_hotels) {
//							double d2 = Math.sqrt(Math.pow(node.locations[0] - d.location[0], 2)
//									+ Math.pow(node.locations[1] - d.location[1], 2));
                            double d2 = GoogleMaps.distanceInMeters(node.locations[0], node.locations[1], d.location[0], d.location[1]);
							if (d2 < this.distance_threshold) {
								d_list.add(d);
							}
						}

					}

					// if we can find the distance from the bus_stop n to the hotel d is shorter
					// than the distance to one of the skyline hotels s_d
					// It means the hotel could be a candidate hotel of the bus stop n.
					for (Data d : sk.allNodes) {
						boolean flag = true;
						// distance from node to d
//						double d2 = Math.sqrt(Math.pow(node.locations[0] - d.location[0], 2) + Math.pow(node.locations[1] - d.location[1], 2));
                        double d2 = GoogleMaps.distanceInMeters(node.locations[0], node.locations[1], d.location[0], d.location[1]);

						// the minimum distance from node n to the skyline hotels s_d who dominated d
						double min_dist = Double.MAX_VALUE;
						for (Data s_d : sk.sky_hotels) {
							// distance from node to the skyline data s_d
//							double d1 = Math.sqrt(Math.pow(node.locations[0] - s_d.location[0], 2) + Math.pow(node.locations[1] - s_d.location[1], 2));
                            double d1 = GoogleMaps.distanceInMeters(node.locations[0], node.locations[1], s_d.location[0], s_d.location[1]);

							if (checkDominated(s_d.getData(), d.getData()) && d1 < min_dist) {
								if (distance_threshold == -1) {
									min_dist = d1;
								} else {
									if (d1 < this.distance_threshold) {
										min_dist = d1;
									}
								}
							}
						}

						if (this.distance_threshold != -1) {
							if (min_dist > d2 && this.distance_threshold > d2) {
								d_list.add(d);
							}
						} else {
							if (min_dist > d2) {
								d_list.add(d);
							}
						}

					}

//                    System.out.println(d_list.size());

//                    ArrayList<Data> d_list = new ArrayList<>(sk.sky_hotels);
//                    //if we can find the distance from the bus_stop n to the hotel d is shorter than the distance to one of the skyline hotels s_d
//                    //It means the hotel could be a candidate hotel of the bus stop n.
//                    for (Data d : sk.allNodes) {
//                        for (Data s_d : sk.sky_hotels) {
////                            double d1 = GoogleMaps.distanceInMeters(node.locations[0], node.locations[1], s_d.location[0], s_d.location[1]);
////                            double d2 = GoogleMaps.distanceInMeters(node.locations[0], node.locations[1], d.location[0], d.location[1]);
//                            double d1 = Math.sqrt(Math.pow(node.locations[0] - s_d.location[0], 2) + Math.pow(node.locations[1] - s_d.location[1], 2));
//                            double d2 = Math.sqrt(Math.pow(node.locations[0] - d.location[0], 2) + Math.pow(node.locations[1] - d.location[1], 2));
//                            if (this.distance_threshold != -1) {
//                                if (d1 > d2 && this.distance_threshold > d2 && checkDominated(s_d.getData(), d.getData())) {
//                                    d_list.add(d);
//                                    break;
//                                }
//                            } else {
//                                if (d1 > d2 && checkDominated(s_d.getData(), d.getData())) {
//                                    d_list.add(d);
//                                    break;
//                                }
//                            }
//
//                        }
//                    }

					int d_size = d_list.size();

					header_f.writeInt(page_list_number); // start page of the list file
					header_f.writeInt(d_size); // the size of the list of current node

					int records = 0;
					for (Data d : d_list) {
						list_f.writeInt(d.PlaceId);
						records++;
						// if the current page stores one more and the page will full, then page number
						// ++
						if ((this.pagesize_list / 4) < (records + 1)) {
							page_list_number++;
							records=0;
						}
					}

					// fill the remainning page with -1.
					long list_end = list_f.getFilePointer();
					for (long i = list_end; i < (page_list_number + 1) * this.pagesize_list; i++) {
						list_f.writeByte(-1);
					}

					page_list_number++;
					tx.success();
					
					
					if(d_size>0) {
						System.out.println(node_id+"     "+d_size+"   "+d_size*4+"/1024="+(d_size*4/1024+1)+"pages  "+  page_list_number+"  "+records);
					}
				}
				
//				System.exit(0);

			}

			header_f.close();
			list_f.close();
			n.shutdownDB();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void writeDataToDisk(ArrayList<Data> allNodes) {
		ArrayList<Data> an = new ArrayList<>(allNodes);
		String Data_file = this.home_folder + "/data.dat";
		try {
			RandomAccessFile data_f = new RandomAccessFile(Data_file, "rw");
			data_f.seek(0);

			Collections.sort(an, new Comparator<Data>() {
				@Override
				public int compare(Data lhs, Data rhs) {
					return lhs.getPlaceId() > rhs.getPlaceId() ? 1 : (lhs.getPlaceId() < rhs.getPlaceId()) ? -1 : 0;
				}
			});

			for (int i = 0; i < an.size(); i++) {
				Data d = an.get(i);
				byte[] b_d = new byte[d.get_size()];
				d.write_to_buffer(b_d);
				data_f.write(b_d);
			}

			data_f.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean checkDominated(double[] costs, double[] estimatedCosts) {
		for (int i = 0; i < costs.length; i++) {
			if (costs[i] * (1.0) > estimatedCosts[i]) {
				return false;
			}
		}
		return true;
	}
	
	public ArrayList<Data> read_d_list_from_disk(long node_id) {

        String header_name = this.home_folder + "/header.idx";
        String list_name = this.home_folder + "/list.idx";
        String Data_file = this.home_folder + "/data.dat";
        ArrayList<Data> d_list = new ArrayList<>();

        try {

            RandomAccessFile header_f = new RandomAccessFile(header_name, "r");
            header_f.seek((node_id * 8));
            int pagenumber = header_f.readInt();
            int d_size = header_f.readInt();

            RandomAccessFile list_f = new RandomAccessFile(list_name, "r");
            list_f.seek(pagenumber * pagesize_list);


            RandomAccessFile data_f = new RandomAccessFile(Data_file, "r");


            for (int i = 0; i < d_size; i++) {
                int d_id = list_f.readInt();
                Data d = new Data(3);
                data_f.seek(d_id * d.get_size());
                byte[] b_d = new byte[d.get_size()];
                data_f.read(b_d);
                d.read_from_buffer(b_d);
                d_list.add(d);
            }

            data_f.close();
            header_f.close();
            list_f.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return d_list;
    }

	public static void main(String[] args) {
		for (String cy : constants.cityList) {
			double range = 0;
			for(int i = 0 ; i<= 30;i++) {
				if(i ==0 ) {
					range = -1; 
				}else if(i==1) {
					range = 500;
				}else {
					range+=50;
				}
				System.out.println("Builing index for city: "+cy);
				Index idx = new Index(cy, range);
				idx.buildIndex(true);
				System.out.println("Finished Index Build for city: "+cy);
				System.out.println("====================================================");
			}
			
		}

//		String cy = "NY";
//		System.out.println("Builing index for city: "+ cy);
//		Index idx = new Index(cy, -1);
//		idx.buildIndex(true);
//		System.out.println("Finished Index Build for city: "+cy);
//		System.out.println("====================================================");

	}

	public long getLineNumbers() {
//      System.out.println(node_info_path);
		long lines = 0;
		try {
			File file = new File(this.node_info_path);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				long l = Long.valueOf(line.split(" ")[0]);
				if (l > lines) {
					lines = l;
				}
			}
			fileReader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return lines;
	}

}
