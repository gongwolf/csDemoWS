package cs.nmsu.edu.csdemo.neo4jTools;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;

import cs.nmsu.edu.csdemo.methods.constants;

import java.util.Iterator;
import java.util.Random;

public class indexTesting {
	long nodetime, reltime, getCost;

	public static void main(String args[]) {
		indexTesting it = new indexTesting();
//        it.test();
		for (int i = 0; i < 10; i++) {
			it.runningTimeTesting(323);
//            it.runningTimeTesting(326);
			System.out.println("======");
		}
//        it.getAllNode();
	}

	private void runningTimeTesting(int version) {
		int[] Gsize = new int[] { 2000, 4000, 6000, 8000, 10000, 10001, 20000, 30000, 40000 };
		for (int s : Gsize) {
			String dbpath = "";

			if (s == 10001) {
				dbpath = constants.home_folder + "/neo4j" + version + "/testdb10000_idx/databases/graph.db";
			} else {
				dbpath = constants.home_folder + "/neo4j" + version + "/testdb" + s + "/databases/graph.db";
			}

//            System.out.println(dbpath);

			connector n = new connector(dbpath);
			n.startDB();
			GraphDatabaseService graphDB = n.getDBObject();
			long query_num = 1000;
			long runningTime_s = System.currentTimeMillis();
			RandomGetInformation(s, graphDB, query_num);
			long runningtime = System.currentTimeMillis() - runningTime_s;
//            System.out.println("Ruuning time in "+s+" size graph get random 1000 node and it's edges information, used "+runningtime+" ms");
			System.out.println(s + " : " + runningtime + "," + (this.nodetime / 1000000.0) + ","
					+ this.reltime / 1000000.0 + "," + this.getCost / 1000000.0);
			n.shutdownDB();
			this.nodetime = this.reltime = this.getCost = 0;
//            System.out.println(getAllNode(dbpath));
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
		}
	}

	private void RandomGetInformation(int size, GraphDatabaseService graphDB, long query_num) {
		try (Transaction tx = graphDB.beginTx()) {
			for (int i = 0; i < query_num; i++) {
				String nodeID = String.valueOf(getRandomNumberInRange(0, size - 2));
				long r1 = System.nanoTime();
//                Node node = graphDB.findNode(BNode.BusNode, "Id", nodeID);
				Node node = graphDB.getNodeById(Long.parseLong(nodeID));
				this.nodetime += System.nanoTime() - r1;
				r1 = System.nanoTime();
				Iterable<Relationship> rels = node.getRelationships(Line.Linked, Direction.BOTH);
				Iterator<Relationship> rel_Iter = rels.iterator();
				this.reltime += System.nanoTime() - r1;

				while (rel_Iter.hasNext()) {
					r1 = System.nanoTime();
					Relationship rel = rel_Iter.next();
					Node nextNode = rel.getStartNode();
					this.reltime += System.nanoTime() - r1;

					r1 = System.nanoTime();
					Double cost = Double.parseDouble(rel.getProperty("MetersDistance").toString());
					this.getCost += System.nanoTime() - r1;
				}

				if (i % 5000 == 0)
					tx.success();
			}
			tx.success();
		}
	}

	private void test() {
		connector n = new connector(constants.home_folder + "/neo4j323/testdb10000/databases/graph.db");
		n.startDB();
		GraphDatabaseService graphDB = n.getDBObject();
		try (Transaction tx = graphDB.beginTx()) {

			IndexManager index = graphDB.index();
			boolean f1 = index.existsForNodes("Name");
			System.out.println(f1);
			Index<Node> names = index.forNodes("Name");
			boolean f2 = index.existsForNodes("Name");
			System.out.println(f2);
			names.delete();
			tx.success();
		}
		n.shutdownDB();
	}

	private int getRandomNumberInRange(int min, int max) {

		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}

		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}

	public int getAllNode(String dbpath) {
		int nodeNum = 0;
		connector n = new connector(dbpath);
//        System.out.println(dbpath);
		n.startDB();
		GraphDatabaseService graphDB = n.getDBObject();
		try (Transaction tx = graphDB.beginTx()) {
			ResourceIterator<Node> itor = graphDB.findNodes(BNode.BusNode);
			while (itor.hasNext()) {
//                System.out.println("1111");
				Node node = itor.next();
				nodeNum++;
				String str_id = (String) node.getProperty("name");
				String embed_id = String.valueOf(node.getId());
				if (!str_id.equals(embed_id)) {
					System.out.println(str_id);
				}
			}
		}
		n.shutdownDB();
		return nodeNum;
	}

}
