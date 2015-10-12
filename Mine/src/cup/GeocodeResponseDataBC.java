package cup;

public class GeocodeResponseDataBC {
	
	public feature[] features;

	public class feature{
		public geometry geometry;
		public properties properties;
	}
	
	public class geometry{
		public double[] coordinates;
	}
	
	public class properties{
		public String fullAddress;
	}
	
}

