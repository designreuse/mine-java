package cup;

public class GeocodeResponseBing {
	
	public resourceSet[] resourceSets;

	public class resourceSet{
		public resource[] resources;
	}
	
	public class resource{
		public address address;
		public String entityType;
		public geocodePoint[] geocodePoints;
	}
	
	public class address{
		public String formattedAddress;
	}
	
	public class geocodePoint{
		public String formattedAddress;
		public double[] coordinates;
	}
	
}

