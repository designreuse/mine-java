package cup;

public class GeocodeResponseArc {
	
	public location[] locations;

	public class location{
		public String name;
		public feature feature;
	}
	
	public class feature{
		public geometry geometry;
		public attributes attributes;
	}
	
	public class geometry{
		public double x;
		public double y;
	}
	
	public class attributes{
		public int Score;
		public String Addr_Type;
	}
	
}

