package cs.nmsu.edu.csdemo.tools;

public class POIObject {
	int placeID; // the place id that is used in this project
	double[] locations; // the latitude and the longitude of the POIs
	float[] data; // the attributes of the POI

	String g_p_id; // google map place id
	String g_p_name;// google map showed name

	public void cleanContents() {
		this.g_p_id = "";
		this.data = new float[] { -1, -1, -1 };
		this.locations = new double[] { -1, -1 };
		this.g_p_name = "";
	}

	@Override
	public String toString() {
		return this.placeID + " " + this.g_p_id + " " + this.data[0] + " " + this.data[1] + " " + this.data[2] + " "
				+ this.locations[0] + " " + this.locations[1];
	}
}
