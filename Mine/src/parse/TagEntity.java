package parse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import com.google.gson.Gson;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.impl.PointImpl;

import cup.VenuesResponseFourSquared;
import secret.Key;

public class TagEntity {

	public static void main(String[] args) throws MalformedURLException, IOException {
		// TODO Auto-generated method stub
		
		Calendar version = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		version.add(Calendar.DATE, -2);
		SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMdd");
		
		String testProp = "The Montague on The Gardens";
		Point testPoint = new PointImpl(-0.11747763699958114,51.500725737000494,SpatialContext.GEO);
		
		String testEnt = "Jorge";
		testEnt = URLEncoder.encode(testEnt, "UTF-8").replace("+", "%20");
		
		String url = "https://api.foursquare.com/v2/venues/explore?"
				+ "ll=" + testPoint.getY() + "," + testPoint.getX()
				+ "&query=" + testEnt
				+ "&v=" + sdf.format(version.getTime())
				+ "&client_id=" + Key.FOUR_SQUARE_ID
				+ "&client_secret=" + Key.FOUR_SQUARE_SECRET;
		
		System.out.println(url);
		
		URLConnection connection;
		String jsonResult = "";
		Gson gson = new Gson();
		
		connection = (new URL(url)).openConnection();
		connection.setConnectTimeout(5000);
		connection.setReadTimeout(5000);
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		
		while ((inputLine = in.readLine()) != null) {
		    jsonResult += inputLine;
		}
		in.close();
		
		VenuesResponseFourSquared vResponse = gson.fromJson(jsonResult, VenuesResponseFourSquared.class);
		System.out.println(jsonResult);
		System.out.println(vResponse.response.groups[0].items[0].venue.name);
		System.out.println(vResponse.response.groups[0].items[0].venue.categories[0].name);
		System.out.println(vResponse.response.groups[0].items[0].flags);
		//System.out.println(vResponse.response.headerFullLocation);
			
		

	}

}
