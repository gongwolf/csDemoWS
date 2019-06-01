package cs.nmsu.edu.csdemo.services;

import cs.nmsu.edu.csdemo.RstarTree.Data;
import cs.nmsu.edu.csdemo.methods.Result;
import cs.nmsu.edu.csdemo.methods.constants;
import cs.nmsu.edu.csdemo.methods.path;

public class ResultBean {
	public long start, end;
//	private double[] start_location;
//	private double[] end_location;
	public path p;
	public double[] costs = new double[constants.path_dimension + 3];
	public double score = 0.0;

	public ResultBean() {
		this.start = this.end = -1;
		this.p = null;
	}

	public ResultBean(Result r) {
//		this.start = r.start.getPlaceId();
//		this.end = r.end.getPlaceId();
		this.start = r.start.getPlaceId();
		this.end = r.end.getPlaceId();
		this.p = r.p;
		this.costs = r.costs;
		this.score = r.score;
	}
}
