package cup;

public class VenuesResponseFourSquared {

	public response response;
	
	public class response {
		public String headerFullLocation;
		public int suggestedRadius;
		public int totalResults;
		public group[] groups;
	}
	
	public class group {
		public item[] items;
	}
	
	public class item {
		public venue venue;
		public flags flags;
		public tip[] tips;
	}
	
	public class venue {
		public String id;
		public String name;
		public category[] categories;
		public location location;
	}
	
	public class category {
		public String name;
	}
	
	public class flags {
		public String exactMatch;
	}
	
	public class tip {
		public String text;
	}
	
	public class location {
		public String address;
		public String[] formattedAddress;
		public double lat;
		public double lng;
	}
	
}
