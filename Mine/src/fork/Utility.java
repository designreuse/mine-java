package fork;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.lang3.StringEscapeUtils;

import cup.Domain;

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
	
	public static String cleanRedditHtml(String input, HashMap<String, Domain> domainCount){
		String output = input.replace("\\n", " ")//.replace("^", " ")
				.replace("&#39;", "'").replace("&quot;", "\"").replace("&nbsp;", " ")
				.replaceAll("<[^:>]*>", "").replace("<a href=\\\"", " ").replace("\\\">", " ");
		
		//remove duplicate links after removing href tags
		
		String[] outputHref = output.split("[\\s]+");
		String output2 = "";
		
		for(int i=0;i<outputHref.length;i++){
			if(outputHref[i].contains("http")){
				if(i+1<outputHref.length && outputHref[i].contentEquals(outputHref[(i+1)])){
					//System.out.println(outputHref[i]);
					continue;
				}
				//notes domain only when not duplicated due to reddit auto-linking causing a duplicate
				collectDomainsFromSelftextLink(outputHref[i], domainCount);
			}
			
			output2 += outputHref[i] + (i+1<outputHref.length?" ":"");
			
		}
		
		return output2;
	}
	
	public static void collectDomainsFromSelftextLink(String link, HashMap<String, Domain> linkStore){
		String[] slash = link.split("/");
		String[] dot = slash[2].split("\\.");
		String domain =  dot[dot.length-2] + "." + dot[dot.length-1];
		//System.out.println(link + " - " + domain);
		if(linkStore.containsKey(domain)){
			linkStore.get(domain).links.add(link);
		} else {
			linkStore.put(domain, new Domain(domain,link));
		}		
	}
	
	public static void sumDomains(HashMap<String, Domain> linkStore){
		for(String s:linkStore.keySet()){
			Domain d = linkStore.get(s);
			d.count = d.links.size();
		}
	}
	
	public static void main(String[] args){
		
		ArrayList<String> input = new ArrayList<String>();
		input.add("haha");
		input.add("hoho");
		System.out.println(arrayListToString(input));
		
	}
	
}
