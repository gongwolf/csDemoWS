package cs.nmsu.edu.csdemo.services;

import java.util.ArrayList;

import cs.nmsu.edu.csdemo.RstarTree.Data;
import cs.nmsu.edu.csdemo.methods.Result;
import cs.nmsu.edu.csdemo.methods.constants;
import cs.nmsu.edu.csdemo.methods.path;

public class ResultBean {
	public long start, end;
	public double[] start_location;
	public double[] end_location;
	public ArrayList<NodeBeans> nodeIDs;
	public ArrayList<Long> relsIDs;
	public double[] costs = new double[constants.path_dimension + 3];

	public ResultBean() {
		this.start = this.end = -1;
		this.nodeIDs = new ArrayList<>();
		this.nodeIDs = new ArrayList<>();
		this.start_location = new double[2];
		this.end_location = new double[2];

	}

	public ResultBean(Result r) {
		this.start = r.start.getPlaceId();
		this.end = r.end.getPlaceId();

		this.start_location = new double[2];
		this.end_location = new double[2];
		this.start_location = r.start.location;
		this.end_location = r.end.location;

		this.nodeIDs = new ArrayList<NodeBeans>();
		this.relsIDs = new ArrayList<Long>();

		if (r.p != null) {
			this.relsIDs.addAll(r.p.rels);
			
			for (long nid : r.p.nodes) {
				NodeBeans nbean = new NodeBeans();
				nbean.setId(nid);
				this.nodeIDs.add(nbean);
			}
		}

		this.costs = r.costs;
	}

}
