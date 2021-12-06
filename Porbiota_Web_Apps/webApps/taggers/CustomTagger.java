package taggers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Class to tag a .txt with custom tags
 * @author Tomé Neves de Matos
 *
 */
public class CustomTagger {

	/** Regex of the text to tag */
	final String REGEX;
	
	/** Name to tag */
	final String DWCFIELD;
	
	/**
	 * Constructor for the customTagger class
	 * @param DwCField String Name to tag
	 * @param regex String Regex of the text to tag
	 */

	public CustomTagger(String DwCField, String regex) {
		REGEX = regex;
		DWCFIELD = DwCField;
			
	}
	
	/**
	 * Returns the given string tagged with the appropriate term
	 * @param str String The text to tag
	 * @return String The tagged text
	 */

	public String tag(String str) {
		Pattern pattern = Pattern.compile(REGEX);
		Matcher matcher = pattern.matcher(str);      
		int addedChar = 0;
		while( matcher.find() ) {
			//Tags the text
			str  = str.substring(0,matcher.start()+addedChar) + "<"+DWCFIELD+">"+ str.substring(matcher.start()+addedChar,matcher.end()+addedChar) + "</"+DWCFIELD+">"+str.substring(matcher.end()+addedChar);
			addedChar += ("<"+DWCFIELD+"></"+DWCFIELD+">").length();
		}		
		return str;
	}
}
