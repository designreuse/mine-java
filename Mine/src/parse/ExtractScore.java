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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import cup.Entity;
import cup.Property;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import fork.StringSimilarity;
import fork.Utility;

public class ExtractScore {

	public static final double FULL_UPPERCASE_SCORE = 10.0;
	public static final double STRING_SIM_THRESHOLD = 0.8;
	public static final double FREQ_REVIEW_THRESHOLD = 0.25;	//Must be capitalised x number of times -- writeEntityScore>property.reviews.size()*FREQ_REVIEW_THRESHOLD
	public static final double SCORE_FREQ_THRESHOLD = 6.0;	//Proportion of capitalisation: writeEntityScore/writeEntityFreq>SCORE_FREQ_THRESHOLD
	
	public static void main(String[] args) throws JsonSyntaxException, IOException {
		// TODO Auto-generated method stub
		Gson gson = new Gson();
		BufferedReader br = null;
		File file = new File("data/raw.json");
		Date startDate = new Date();
		int currentPropertyCount = 0;
        
        System.out.println("Raw() intialised at " + startDate);
		
		br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		Property[] propertyList = gson.fromJson(br.readLine(), Property[].class);
		br.close();
		
		MaxentTagger tagger = new MaxentTagger("taggers/english-left3words-distsim.tagger");
		
		for(Property property:propertyList){
			currentPropertyCount++;
			System.out.printf("Analysing %d/%d property %s with %d reviews...%n",currentPropertyCount,propertyList.length,property.name,property.reviews.size());
			
			TreeMap<String,Integer> propertyEntityFreq = new TreeMap<String,Integer>(String.CASE_INSENSITIVE_ORDER);
			TreeMap<String,Double> propertyEntityScore = new TreeMap<String,Double>(String.CASE_INSENSITIVE_ORDER);
			
			for(String review:property.reviews){
				String taggedReview = tagger.tagString(review) + "END_END"; //so we loop one more time to flush phrase
				//System.out.println("taggedReview: " + taggedReview);
				String[] taggedReviewTokens = taggedReview.split(" ");
				String previousTag = "";
				String previous2Tag = "";
				String currentPhrase = "";
				TreeMap<String,Double> reviewEntityScore = new TreeMap<String,Double>(String.CASE_INSENSITIVE_ORDER);
				ArrayList<String> currentPhraseArray = new ArrayList<String>();
				
				for (String taggedReviewToken:taggedReviewTokens){
					//System.out.println("taggedReviewToken: " + taggedReviewToken);
					String[] currentWordTag =  taggedReviewToken.split("_");
					String currentWord = currentWordTag[0];
					String currentTag = currentWordTag[1];
					//System.out.print("currentWord: " + currentWord);
					//System.out.println(", currentTag: " + currentTag);
					
					//considering nouns or noun phrases
					if(currentTag.startsWith("NN") || (previousTag.startsWith("NN")&&currentTag.startsWith("IN"))){
						currentPhraseArray.add(currentWord);
					} else if (!currentPhraseArray.isEmpty()) {
						
						if (previousTag.startsWith("IN") && previous2Tag.startsWith("NN")){
							currentPhraseArray.clear();
							previous2Tag = previousTag;
							previousTag = currentTag;
        					continue;
        				}
						
						//scoring based on Capitalisation
						currentPhrase = Utility.arrayListToString(currentPhraseArray);
						//System.out.println("currentPhrase: " + currentPhrase);
						
						int upperCaseCount = 0;
    					for (String s:currentPhraseArray){
    						if(!s.toLowerCase().equals(s)){
    							upperCaseCount++;
    						}
    					}
    					//System.out.println("upperCaseCount: " + upperCaseCount);
    					//double currentScore = (upperCaseCount!=0?(FULL_UPPERCASE_SCORE*upperCaseCount/currentPhraseArray.size()):FULL_UPPERCASE_SCORE/2);
    					//System.out.println("currentScore: " + currentScore);
    					double currentScore = 1.0;
    					if (upperCaseCount==currentPhraseArray.size()){
    						currentScore = FULL_UPPERCASE_SCORE;
    					} else if (upperCaseCount>0){
    						currentScore = (FULL_UPPERCASE_SCORE*upperCaseCount/currentPhraseArray.size())+1;
    					}
    					
						if(reviewEntityScore.containsKey(currentPhrase)){
							Double retrievedScore = reviewEntityScore.get(currentPhrase);
							Double higherScore = Math.max(retrievedScore, currentScore);
							if(higherScore==currentScore){
								//within a review, score only once for a repeated phrase AND store most-capitalised version
								reviewEntityScore.remove(currentPhrase);
								reviewEntityScore.put(currentPhrase,currentScore);
							}
						} else {
							reviewEntityScore.put(currentPhrase,currentScore);
						}
						currentPhraseArray.clear();
					}
					previous2Tag = previousTag;
					previousTag = currentTag;
				}
				
				//different capitalisation variations will be tolerated until all reviews totalled up
				//System.out.println("reviewEntityScore.keySet().size(): " + reviewEntityScore.keySet().size());
				for(String entity:reviewEntityScore.keySet()){
					if(propertyEntityFreq.containsKey(entity)){
						propertyEntityFreq.put(entity, propertyEntityFreq.get(entity)+1);
						propertyEntityScore.put(entity, propertyEntityScore.get(entity)+reviewEntityScore.get(entity));
					} else {
						propertyEntityFreq.put(entity, 1);
						propertyEntityScore.put(entity, reviewEntityScore.get(entity));
					}
				}
				//System.out.println("propertyEntityScore: " + propertyEntityScore.size());
				
			}
			
			//combining misspellings
			for(String entityName:propertyEntityFreq.keySet()){
				int writeEntityFreq = propertyEntityFreq.get(entityName);
				double writeEntityScore = propertyEntityScore.get(entityName);
				
				//repeated threshold condition, inverted
    			if(!(propertyEntityFreq.get(entityName)>0 && writeEntityScore>=property.reviews.size()*FREQ_REVIEW_THRESHOLD && 1.0*writeEntityScore/writeEntityFreq>=SCORE_FREQ_THRESHOLD)){
    				continue;
    			}
				
				for(String entityName2:propertyEntityFreq.keySet()){
					
					//weakness here is a snowball -- if there's a 3, 4, 5; the middle will prevail.
    				if(propertyEntityFreq.get(entityName)==0 || propertyEntityFreq.get(entityName2)==0 || entityName.equalsIgnoreCase(entityName2)){
        				continue;
        			}
    				
    				if(StringSimilarity.similarity(entityName,entityName2)>=STRING_SIM_THRESHOLD){
    					System.out.print(entityName + " VS " + entityName2 + ": ");
    					int entityFreq = propertyEntityFreq.get(entityName);
    					int entityFreq2 = propertyEntityFreq.get(entityName2);
    					int entityFreqSum = entityFreq + entityFreq2;
    					double entityFreqScore = propertyEntityScore.get(entityName);
    					double entityFreqScore2 = propertyEntityScore.get(entityName2);
    					double entityFreqScoreSum = entityFreqScore + entityFreqScore2;
    					
        				if(entityFreq>=entityFreq2){
        					System.out.println(entityName + " WINS");
        					propertyEntityFreq.put(entityName, entityFreqSum);
        					propertyEntityScore.put(entityName, entityFreqScoreSum);
        					propertyEntityFreq.put(entityName2,0);
        					propertyEntityScore.put(entityName2,0.0);
        				} else {
        					System.out.println(entityName2 + " WINS");
        					propertyEntityFreq.put(entityName2, entityFreqSum);
        					propertyEntityScore.put(entityName2, entityFreqScoreSum);
        					propertyEntityFreq.put(entityName,0);
        					propertyEntityScore.put(entityName,0.0);
        				}
        			}
					
				}
			}
			
			//storing to POJO
			property.entityList = new ArrayList<Entity>();
			
			for(String entity:propertyEntityFreq.keySet()){
				int writeEntityFreq = propertyEntityFreq.get(entity);
				double writeEntityScore = propertyEntityScore.get(entity);
				//System.out.println(writeEntityFreq + " " + property.reviews.size()*FREQ_REVIEW_THRESHOLD  + " " +  1.0*writeEntityScore/writeEntityFreq + " " + SCORE_FREQ_THRESHOLD);
				//if(true){
				if(writeEntityScore>=property.reviews.size()*FREQ_REVIEW_THRESHOLD && 1.0*writeEntityScore/writeEntityFreq>=SCORE_FREQ_THRESHOLD){
					property.entityList.add(new Entity(entity,writeEntityFreq,writeEntityScore));
				}
			}
			
			System.out.println("Score needed: " + property.reviews.size()*FREQ_REVIEW_THRESHOLD  + ", Score/Freq needed: " + SCORE_FREQ_THRESHOLD);
			
			property.reviews = null;
			
			Date propertyEndDate = new Date();
			System.out.printf("Property %s has %d entities found, time elapsed: %s%n",property.name,property.entityList.size(),Utility.timeElapsed(startDate, propertyEndDate));
		}
		
		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data/extract_pretty.json"), "UTF-8"));
        gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        writer.write(gson.toJson(propertyList));
    	writer.close();
		
		System.out.println("ExtractScore() complete.");
	}

}
