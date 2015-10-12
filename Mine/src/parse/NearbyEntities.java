package parse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceCalculator;
import com.spatial4j.core.distance.GeodesicSphereDistCalc;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.impl.PointImpl;
import com.spatial4j.core.distance.DistanceUtils;

import cup.GeocodeResponse;
import cup.GeocodeResponseArc;
import cup.GeocodeResponseBing;
import cup.GeocodeResponseDataBC;
import cup.GeocodeResponseGeoNames;
import cup.GeocodeResponseGoogle;
import cup.GeocodeResponseNominatim;
import cup.GeocodeResponseOpenCage;
import cup.GeocodeResponsePhoton;
import fork.Utility;
import secret.Key;

public class NearbyEntities {
	
	public static final String[] GEO_LIST = {"google", "openstreetmap", "arcgis", "opencage", "bing", "databc", "photon", "geonames"};

	private static String geoQuery(String geoCoder, String address) throws UnsupportedEncodingException {
		
		String url = "";
		address = URLEncoder.encode(address, "UTF-8").replace("+", "%20");
		
		System.setProperty("jsse.enableSNIExtension", "false");
		
		switch(geoCoder){
			case "google":
				url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + address + "&key=" + Key.googleGeocode;
				break;
			case "openstreetmap":
				url = "http://nominatim.openstreetmap.org/search/" + address + "?format=json";
				break;
			case "arcgis":
				url = "https://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer/find?text=" + address + "&f=json";
				break;
			case "opencage":
				url = "https://api.opencagedata.com/geocode/v1/json?q=" + address + "&key=" + Key.openCageGeocode; //https breaks with unrecognised name header
				break;
			case "bing":
				url = "http://dev.virtualearth.net/REST/v1/Locations?query=" + address + "&key=" + Key.bingGeocode;
				break;
			case "databc":
				url = "http://apps.gov.bc.ca/pub/geocoder/addresses.geojson?addressString=" + address;
				break;
			case "photon":
				url = "http://photon.komoot.de/api?q=" + address;
				break;
			case "geonames":
				url = "http://api.geonames.org/searchJSON?q=" + address + "&username=" + Key.geoNamesUserGeoCode;
				break;
			default:
				break;
		}
		//System.out.println("GeoQuery " + geoCoder + ": " + url);
		
		URLConnection connection;
		String jsonResult = "";
		
		try {
			connection = (new URL(url)).openConnection();
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			
			while ((inputLine = in.readLine()) != null) {
			    jsonResult += inputLine;
			}
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("geoQuery(" + geoCoder + ") " + e.getMessage());
		}
		
		return jsonResult; 
	}
	
	private static GeocodeResponse geoResponseClassed(String geoCoder, String address) throws IOException {
		
		String query = address;
		//String address;
		String type = "";
		double lat = 0;
		double lng = 0;
		GeocodeResponse geoR = null;
		Gson gson = new Gson();
		
		try{
			switch(geoCoder){
				case "google":
					GeocodeResponseGoogle gResponse = gson.fromJson(geoQuery(geoCoder,address),GeocodeResponseGoogle.class);
					lat = gResponse.results[0].geometry.location.lat;
					lng = gResponse.results[0].geometry.location.lng;
					address = gResponse.results[0].formatted_address;
					break;
				case "openstreetmap":
					GeocodeResponseNominatim[] nResponse = gson.fromJson(geoQuery(geoCoder,address),GeocodeResponseNominatim[].class);
					lat = nResponse[0].lat;
					lng = nResponse[0].lng;
					address = nResponse[0].display_name;
					type = nResponse[0].type;
					break;
				case "arcgis":
					GeocodeResponseArc aResponse = gson.fromJson(geoQuery(geoCoder,address),GeocodeResponseArc.class);
					lat = aResponse.locations[0].feature.geometry.y;
					lng = aResponse.locations[0].feature.geometry.x;
					address = aResponse.locations[0].name;
					break;
				case "opencage":
					GeocodeResponseOpenCage oResponse = gson.fromJson(geoQuery(geoCoder,address),GeocodeResponseOpenCage.class);
					lat = oResponse.results[0].geometry.lat;
					lng = oResponse.results[0].geometry.lng;
					address = oResponse.results[0].formatted;
					break;
				case "bing":
					GeocodeResponseBing bResponse = gson.fromJson(geoQuery(geoCoder,address),GeocodeResponseBing.class);
					lat = bResponse.resourceSets[0].resources[0].geocodePoints[0].coordinates[0];
					lng = bResponse.resourceSets[0].resources[0].geocodePoints[0].coordinates[1];
					address = bResponse.resourceSets[0].resources[0].address.formattedAddress;
					type = bResponse.resourceSets[0].resources[0].entityType;
					break;
				case "databc":
					GeocodeResponseDataBC dResponse = gson.fromJson(geoQuery(geoCoder,address),GeocodeResponseDataBC.class);
					lat = dResponse.features[0].geometry.coordinates[1];
					lng = dResponse.features[0].geometry.coordinates[0];
					address = dResponse.features[0].properties.fullAddress;
					break;
				case "photon":
					GeocodeResponsePhoton pResponse = gson.fromJson(geoQuery(geoCoder,address),GeocodeResponsePhoton.class);
					lat = pResponse.features[0].geometry.coordinates[1];
					lng = pResponse.features[0].geometry.coordinates[0];
					address = pResponse.features[0].properties.name;
					type = pResponse.features[0].properties.osm_value;
					break;
				case "geonames":
					GeocodeResponseGeoNames gnResponse = gson.fromJson(geoQuery(geoCoder,address),GeocodeResponseGeoNames.class);
					lat = gnResponse.geonames[0].lat;
					lng = gnResponse.geonames[0].lng;
					address = gnResponse.geonames[0].name;
					type = gnResponse.geonames[0].fcodeName;
					break;
				default:
					break;
			}
			geoR = new GeocodeResponse(query, geoCoder, address, type, lat, lng);
		} catch (Exception e){
			System.out.println("geoResponseClassed(" + geoCoder + ") " + e.getMessage());
		}

		return geoR; 
	}
	
	public static GeocodeResponse bestGeo(String query) throws InterruptedException{
				
		System.out.println("\ntestEntity: " + query);
		ArrayList<Thread> tList = new ArrayList<Thread>();
		ArrayList<GeocodeResponse> rList = new ArrayList<GeocodeResponse>();
		DistanceCalculator dc = new GeodesicSphereDistCalc.Vincenty();
		for (String g:GEO_LIST){
			Thread t = new Thread(new Runnable() {
			    public void run() {
			        //Do whatever
			    	try {
			    		GeocodeResponse r = geoResponseClassed(g,query);
						if(r!=null){
							rList.add(r);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.out.println(e.getMessage());
					}
			    }
			});
			tList.add(t);
			t.start();
			
		}
		for(Thread t:tList){
			t.join();
		}
		ArrayList<GeocodeResponse> successResponsesArranged = new ArrayList<GeocodeResponse>();
		for(GeocodeResponse r:rList){
			double combinedDistance = 0;
			for(GeocodeResponse r2:rList){
				combinedDistance += dc.distance(r.point,r2.point);
			}
			r.combinedDistance = combinedDistance;
			if(successResponsesArranged.isEmpty()){
				successResponsesArranged.add(r);
			} else {
				for(int i=0;i<successResponsesArranged.size();i++){
					GeocodeResponse compare = successResponsesArranged.get(i);
					if(r.combinedDistance<compare.combinedDistance){
						successResponsesArranged.add(i, r);
						//System.out.println(r.geoCoder + " VS " + compare.geoCoder);
						break;
					} else if (i==successResponsesArranged.size()-1){
						successResponsesArranged.add(r);
						break;
					}
				}
			}
		}
		for(GeocodeResponse r:successResponsesArranged){
			//System.out.println(r.toString());
		}
		
		return successResponsesArranged.get(0);
		
	}
	
	public static void main(String[] args) throws JsonSyntaxException, UnsupportedEncodingException, IOException, InterruptedException {
		// TODO Auto-generated method stub
		
		Gson gson = new Gson();
		Date startDate = new Date();
        
		Utility.exeStart(startDate);
		
		ArrayList<String> testEnt = new ArrayList<String>();
		//testEnt.add("The Kensington Hotel");
		//testEnt.add("London House Hotel");
		//testEnt.add("Royal Horseguards Hotel");
		//testEnt.add("Buckingham Palace");
		//testEnt.add("London Eye");
		//testEnt.add("Russell square");
		//testEnt.add("Big Ben");
		//testEnt.add("Houses of Parliament");
		testEnt.add("Singapore Management University Singapore");
		//testEnt.add("Singapore Padang");
		//testEnt.add("Parkway Parade Singapore");
		testEnt.add("503 Tampines Central Singapore");
		testEnt.add("YMCA Singapore");
		
		Point SMU = new PointImpl(103.850013919008,1.29655655,SpatialContext.GEO);
		DistanceCalculator dc = new GeodesicSphereDistCalc.Vincenty();
		
		for (String a:testEnt){
			GeocodeResponse g =  bestGeo(a);
			System.out.println("BEST: " + g.toString());
			System.out.println("Distance from SMU: " + dc.distance(SMU, g.point)*DistanceUtils.DEG_TO_KM);
			
			Point[] box = Utility.boxHypo(g.point,1.0);
			System.out.println("NW-SW Bound: " + Utility.pointToLatLng(box[0]) + ", " + Utility.pointToLatLng(box[1]));
			//System.out.println(dc.distance(g.point,box[0])*DistanceUtils.DEG_TO_KM);
			System.out.println("Within 4km of SMU? " + dc.within(g.point, SMU.getX(), SMU.getY(), 4*DistanceUtils.KM_TO_DEG));

		}
		
		
		//Utility.exeComplete(startDate);
		
	}

}
