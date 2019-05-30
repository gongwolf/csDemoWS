package cs.nmsu.edu.test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/check/{var1}/{var2}")
public class hello {
	@GET
	 @Produces("text/plain")
	 public Response printFeedback(@PathParam("var1") String var1, @PathParam("var2") String var2) {
	  String result = "var1: " + var1 + " var2: " + var2;
	  return Response.status(200).entity(result).build();
	 }
}
