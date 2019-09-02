package cs.nmsu.edu.csdemo.tools;

public class QueryParameters {
	
	public String city="";
	public int num_bus_stop=-1;
	public String type="";
	
	
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public int getNum_bus_stop() {
		return num_bus_stop;
	}
	public void setNum_bus_stop(int num_bus_stop) {
		if(num_bus_stop==0) {
			this.num_bus_stop = -1;
		}else {
			this.num_bus_stop = num_bus_stop;
		}
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	@Override
	public String toString() {
		return "QueryParameters [city=" + city + ", num_bus_stop=" + num_bus_stop + ", type=" + type + "]";
	}
	
	

}
