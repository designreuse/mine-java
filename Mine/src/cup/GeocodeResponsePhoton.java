package cup;

public class GeocodeResponsePhoton {
	
	public feature[] features;

	public class feature{
		public geometry geometry;
		public properties properties;
	}
	
	public class geometry{
		public double[] coordinates;
	}
	
	public class properties{
		public String osm_key;
		public String osm_value;
		public String name;
	}
	
}

