package fork;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.lang3.StringEscapeUtils;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceCalculator;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.distance.GeodesicSphereDistCalc;
import com.spatial4j.core.shape.Point;


public class Utility {

	public static String cleanReview(String dirty){
		return StringEscapeUtils.unescapeHtml4(dirty).replaceAll("<[^>]*>", "");
	}
	
	public static String timeElapsed(Date date1, Date date2){
		long difference = (date2.getTime() - date1.getTime())/1000;
		int seconds = (int)(difference%60);
		int minutes = (int)((difference/60)%60);
		int hours = (int)((difference/3600));
		return hours + "h " + minutes + "m " + seconds + "s";
	}
	
	public static String arrayListToString(ArrayList<String> input){
		StringBuilder sb = new StringBuilder();
		for (String inputToken:input){
			sb.append(inputToken);
			sb.append(" ");
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	
	public static void exeStart(Date startDate){
		String exe = Thread.currentThread().getStackTrace()[2].getClassName();
		System.out.printf("%s initialised at %s%n", exe, startDate);
	}
	
	public static void exeComplete(Date startDate){
		String exe = Thread.currentThread().getStackTrace()[2].getClassName();
		System.out.printf("%s completed with execution time: %s", exe, timeElapsed(startDate,new Date()));
	}
	
	public static String pointToLatLng(Point p){
		return "(" + p.getY() + "," + p.getX() + ")";
	}
	
	public static Point[] boxHypo(Point p, double distance){
		DistanceCalculator dc = new GeodesicSphereDistCalc.Vincenty();
		double intended = distance;
		double hypo = Math.sqrt(2*Math.pow(intended, 2))*DistanceUtils.KM_TO_DEG;
		Point nw = dc.pointOnBearing(p, hypo, -45.0, SpatialContext.GEO, null);
		Point se = dc.pointOnBearing(p, hypo, -225.0, SpatialContext.GEO, null);
		Point[] output = {nw,se};
		
		return output;
	}
	
	public static void main(String[] args){
		
		ArrayList<String> input = new ArrayList<String>();
		input.add("haha");
		input.add("hoho");
		System.out.println(arrayListToString(input));
		
	}
	
}
