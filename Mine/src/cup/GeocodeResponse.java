package cup;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.impl.PointImpl;

public class GeocodeResponse {
	
	public String query;
	public String geoCoder;
	public String address;
	public String type;
	public double lat;
	public double lng;
	public Point point;
	public double combinedDistance;
	
	public GeocodeResponse(String query, String geoCoder, String address, String type, double lat, double lng) {
		this.query = query;
		this.geoCoder = geoCoder;
		this.address = address;
		this.type = type;
		this.lat = lat;
		this.lng = lng;
		this.point = new PointImpl(lng,lat,SpatialContext.GEO);
	}
	
	public String toString(){
		return geoCoder + "(" + query + "," + combinedDistance + ")" + " - (" + lat + "," + lng + ") [" + type + "] " + address;
		
	}

}
