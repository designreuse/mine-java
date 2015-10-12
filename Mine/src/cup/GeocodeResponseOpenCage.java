package cup;

public class GeocodeResponseOpenCage {
	
	public result[] results;

	public class result{
		public String formatted;
		public geometry geometry;
	}
	
	public class geometry{
		public double lat;
		public double lng;
	}
	
}

