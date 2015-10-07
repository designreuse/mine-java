package fork;

import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang3.StringEscapeUtils;

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
	
	public static void main(String[] args){
		
		ArrayList<String> input = new ArrayList<String>();
		input.add("haha");
		input.add("hoho");
		System.out.println(arrayListToString(input));
		
	}
	
}
