package parse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.impl.PointImpl;

import cup.Entity;
import cup.EntityTagged;
import cup.GeocodeResponse;
import cup.Property;
import cup.VenuesResponseFourSquared;
import fork.Utility;
import secret.Key;

public class AddGeo {
	
	public static VenuesResponseFourSquared geoFS(String entity, double lat, double lng) throws MalformedURLException, IOException{
		
		String fAdd = URLEncoder.encode(entity, "UTF-8").replace("+", "%20");
		Calendar version = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		version.add(Calendar.DATE, -2);
		SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMdd");
		
		String url = "https://api.foursquare.com/v2/venues/explore?"
				+ "ll=" + lat + "," + lng
				+ "&query=" + fAdd
				+ "&radius=" + 100000
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
		System.out.println("geoFS(): [" + entity + "] " + vResponse.response.groups[0].items[0].venue.location.address);
		return vResponse;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		File file = new File("data/extract_pretty.json");
        BufferedReader br = null;
        Date startDate = new Date();
        String json = "";
        String inputLine;
        Gson gson = new Gson();
        
        Utility.exeStart(startDate);
        
        br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		while ((inputLine = br.readLine()) != null) {
			json += inputLine;
		}
		br.close();
		
		String region = "London";
		
		GeocodeResponse g =  NearbyEntities.bestGeo(region);
		System.out.println("BEST: " + g.toString());
		Point regionPt = new PointImpl(g.lng,g.lat,SpatialContext.GEO);
		
		Property[] propertyList = gson.fromJson(json, Property[].class);
		
		for(Property p:propertyList){
			System.out.println("Processing " + p.name);
			VenuesResponseFourSquared pVRes = geoFS(p.name, g.lat, g.lng);
			p.name_formatted = pVRes.response.groups[0].items[0].venue.name;
			double pLat = pVRes.response.groups[0].items[0].venue.location.lat;
			double pLng = pVRes.response.groups[0].items[0].venue.location.lng;
			p.latlng = new double[]{pLat,pLng};
			p.address = Arrays.toString(pVRes.response.groups[0].items[0].venue.location.formattedAddress);
			p.type = pVRes.response.groups[0].items[0].venue.categories[0].name;
			p.tip = pVRes.response.groups[0].items[0].tips[0].text;
			
			HashMap<String,EntityTagged> tagged = new HashMap<String,EntityTagged>();
			p.entitiesUntagged = new ArrayList<Entity>();
			p.entitiesTagged = new ArrayList<EntityTagged>();
			
			for (Entity e:p.entityList){
				System.out.println("Processing " + p.name + " - " + e.name);
				
				//4squared can't detect places, but geonames is good at it
				EntityTagged isRegion = NearbyEntities.detectAreaGN(e.name, regionPt);
				if (isRegion!=null){
					if(!tagged.containsKey(isRegion.name)){
						isRegion.reviewEntities = new ArrayList<Entity>();
						isRegion.reviewEntities.add(e);
						
						p.entitiesTagged.add(isRegion);
						tagged.put(isRegion.address, isRegion);
					} else {
						EntityTagged newET = tagged.get(isRegion.name);
						newET.reviewEntities.add(e);
					}
					continue;
				}
				
				VenuesResponseFourSquared eVRes = geoFS(e.name, pLat, pLng);
				String matchType = "";
				String eVId = "";
				try{
					eVId = eVRes.response.groups[0].items[0].venue.id;
					matchType = eVRes.response.groups[0].items[0].flags.exactMatch;
				} catch (NullPointerException error){
					System.out.println("epLoop with " + e.name + " " + error.getMessage());
					p.entitiesUntagged.add(e);
					continue;
				}
				//System.out.println("MatchType: " + matchType);
				if(matchType == null || !matchType.contentEquals("true")){
					p.entitiesUntagged.add(e);
				} else {
					if(!tagged.containsKey(eVId)){
						EntityTagged newET = new EntityTagged();
						newET.name = eVRes.response.groups[0].items[0].venue.name;
						double eLat = eVRes.response.groups[0].items[0].venue.location.lat;
						double eLng = eVRes.response.groups[0].items[0].venue.location.lng;
						newET.latlng = new double[]{eLat,eLng};
						newET.address = Arrays.toString(eVRes.response.groups[0].items[0].venue.location.formattedAddress);
						newET.type = eVRes.response.groups[0].items[0].venue.categories[0].name;
						newET.tip = eVRes.response.groups[0].items[0].tips[0].text;
						newET.reviewEntities = new ArrayList<Entity>();
						newET.reviewEntities.add(e);
						
						p.entitiesTagged.add(newET);
						tagged.put(eVId, newET);
					} else {
						EntityTagged newET = tagged.get(eVId);
						newET.reviewEntities.add(e);
					}
				}
				p.entityList = null;
			}
			
		}
		
		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data/geo_pretty.json"), "UTF-8"));
        gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        writer.write(gson.toJson(propertyList));
    	writer.close();
		
    	Utility.exeComplete(startDate);
	}
}
