package cup;
import java.util.ArrayList;
import java.util.TreeMap;

public class Property {
	
	public String name;
	public ArrayList<Entity> entityList;
	public ArrayList<String> reviews;
	
	//processing only
	//public TreeMap<String,Integer> entityScore;
	//public TreeMap<String,Integer> entityFreq;
	
	public Property(String name) {
		// TODO Auto-generated constructor stub
		this.name = name;
	}

}
