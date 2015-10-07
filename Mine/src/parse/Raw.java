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
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cup.Property;
import fork.Utility;

public class Raw {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		File file = new File("data/2015-09-10-16-23-39outfile.txt");
        int scannerCount = 0;
        BufferedReader br = null;
        ArrayList<Property> propertyList = new ArrayList<Property>();
        Date startDate = new Date();
        
        System.out.println("Raw() intialised at " + startDate);
        
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String nextLine = null;
            String currentPropertyString = null;
            Property currentProperty = null;
            
            br.readLine(); //skipping header row
            scannerCount++;
            
            while ((nextLine = br.readLine()) != null) {
            	scannerCount++;
            	
            	String[] nextLineTokens = nextLine.split("\t");
            	currentPropertyString = Utility.cleanReview(nextLineTokens[0]);
            	
            	if(currentProperty==null){
            		currentProperty = new Property(currentPropertyString);
            		currentProperty.reviews = new ArrayList<String>();
            	} else if (!currentPropertyString.contentEquals(currentProperty.name)){
            		propertyList.add(currentProperty);
            		currentProperty = new Property(currentPropertyString);
            		currentProperty.reviews = new ArrayList<String>();
            	} else {
            		String dirty = nextLineTokens[1];
            		//System.out.println(dirty);
            		currentProperty.reviews.add(Utility.cleanReview(dirty));
            	}
            }
            propertyList.add(currentProperty);
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
        	br.close();
            System.out.printf("Scanner read is %d, Property count at %d%n",scannerCount,propertyList.size());
        }
        
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data/raw_pretty.json"), "UTF-8"));
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        writer.write(gson.toJson(propertyList));
    	writer.close();
    	
    	writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data/raw.json"), "UTF-8"));
        gson = new GsonBuilder().disableHtmlEscaping().create();
        writer.write(gson.toJson(propertyList));
    	writer.close();
    	
    	Date endDate = new Date();
    	System.out.printf("Raw() ended at %s, time elapsed: %s",endDate,Utility.timeElapsed(startDate,endDate));
	}

}
