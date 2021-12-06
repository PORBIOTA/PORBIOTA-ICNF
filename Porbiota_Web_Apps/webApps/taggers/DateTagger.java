package taggers;
import java.io.IOException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * Class to parse and tag dates existing in a string
 * 
 * @author Tomé Neves de Matos
 *
 */

public class DateTagger {

	/**
	 * Main method for testing or independent usage
	 * @param args String[] The string to parse and tag for dates
	 * @throws IOException
	 */
	
	public static void main(String[] args) {

		String str = dateTagger(args[0]);
		System.out.println(str);		
	}

	/**
	 * Tags a string for dates in all formats
	 * @param str String The string to parse and tag for dates
	 * @return String The string with the dates tagged
	 */
	
	public static String dateTagger (String str) {

		boolean[] toTag = new boolean[6];

		for (int i = 0; i < toTag.length; i++) {
			toTag[i]= true;
		}

		return dateTagger(str, toTag);		
	}

	/**
	 * Tags a string for dates in with the month written in roman numerals
	 * @param str String The string to parse and tag for dates
	 * @return String The string with the dates tagged
	 */
	
	public static String dateRomanTagger (String str) {

		boolean[] toTag = new boolean[6];

		toTag[0] = false;
		toTag[1] = false;
		toTag[2] = true;
		toTag[3] = true;
		toTag[4] = false;
		toTag[5] = true;

		return dateTagger(str, toTag);		
	}
	
	/**
	 * Tags a string for dates written solely with normal numerals
	 * @param str String The string to parse and tag for dates
	 * @return String The string with the dates tagged
	 */
	
	public static String dateNormalTagger (String str) {

		boolean[] toTag = new boolean[6];

		toTag[0] = true;
		toTag[1] = true;
		toTag[2] = false;
		toTag[3] = false;
		toTag[4] = true;
		toTag[5] = false;

		return dateTagger(str, toTag);		
	}


	/**
	 * Tags a string for the type of dates chosen
	 * @param str The string to parse and tag for dates
	 * @param toTag boolean[] with true or false for each of the types of dates
	 * 0 - yyyyMMdd
	 * 1 - ddMMyyyy
	 * 2 - ddROMANyyyy
	 * 3 - yyyyROMANdd
	 * 4 - xxMMzz
	 * 5 - xxROMANzz	 * 			
	 * @return String The string with the dates tagged
	 */

	public static String dateTagger (String str, boolean[] toTag) {
		if (toTag[2]) 
			//Sends with true if only roman months
			str = ddRomanyyyyTagger(str,!(toTag[0]||toTag[1]||toTag[4]));
		if (toTag[3]) 
			//Sends with true if only roman months
			str = yyyyRomanddTagger(str,!(toTag[0]||toTag[1]||toTag[4]));
		if (toTag[5]) 
			//Sends with true if only roman months
			str = xxRomanzzTagger(str,!(toTag[0]||toTag[1]||toTag[4]));
		if (toTag[0]) 
			str = yyyyMMddTagger(str);
		if (toTag[1]) 
			str = ddMMyyyyTagger(str);

		if (toTag[4]) {
			str = xxMMzzTagger(str);
			str = xxMMzzExtraTagger(str);
		}


		return str;
	}

	/**
	 * Tags a string for dates in the yyyMMdd format
	 * @param str String The string to parse and tag for dates
	 * @return String The string with the dates tagged
	 */
	
	private static String yyyyMMddTagger (String str) {
		//For yyyy (M)M ((d)d) /////////////////////////////////////////////////////////////////////////////////////////
		String yyyyMMdd = "(!>?date=)\\d{4}\\s?[-/.](?<![IVXL1-9])\\d{1,2}(?![IVXL1-9])([-/.]\\d{1,2})?" ;
		Pattern pattern = Pattern.compile(yyyyMMdd);
		Matcher matcher = pattern.matcher(str);      
		String year;
		String month;
		String day;		
		String ISOdate;
		int addedChar = 0;
		while( matcher.find() ) {
			//Checks that the year makes sense
			if (Integer.parseInt(matcher.group().substring(0,4))<2050) {

				//Extract ISO date format
				//Year
				year = matcher.group().substring(0,4);
				//Month			
				if (matcher.group().length() < 7) {
					month = "0"+matcher.group().charAt(5);
				} else if (Character.isDigit(matcher.group().charAt(6))) {
					month = matcher.group().substring(5,7);
				} else {
					month = "0"+matcher.group().charAt(5);
				}

				//Day and final ISO date
				if (matcher.group().length() < 8) {
					ISOdate = year + "-" + month;					
				} else if (Character.isDigit(matcher.group().charAt(matcher.group().length()-2))) {
					day = matcher.group().substring(matcher.group().length()-2);
					ISOdate = year + "-" + month + "-" + day;
				} else {
					day = "0"+matcher.group().charAt(matcher.group().length()-1);
					ISOdate = year + "-" + month + "-" + day;
				}				

				//Checks date validity
				if (!validDate(ISOdate)) {
					continue;
				}
				//Tags the text
				str  = str.substring(0,matcher.start()+addedChar) + "<date="+ISOdate+">"+ str.substring(matcher.start()+addedChar,matcher.end()+addedChar) + "</date>"+str.substring(matcher.end()+addedChar);
				addedChar += ("<date="+ISOdate+"></date>").length();


				//finds extra days
				int previousLength = str.length();
				str = extraDayTagger(matcher, str, ISOdate, false, addedChar);
				addedChar += str.length()-previousLength;


			}
		}
		return str;
	}


	/**
	 * Tags a string for dates in the ddMMyyyy format
	 * @param str String The string to parse and tag for dates
	 * @return String The string with the dates tagged
	 */
	
	private static String ddMMyyyyTagger (String str) {
		//For ((d)d) (M)M yyyy /////////////////////////////////////////////////////////////////////////////////////////
		String ddMMyyyy = "(\\d{1,2}[-/.])?(?<![IVXL1-9])\\d{1,2}(?![IVXL1-9])[-/.]\\s?\\d{4}";               
		Pattern pattern = Pattern.compile(ddMMyyyy);
		Matcher matcher = pattern.matcher(str);      
		String year="-1";
		String month="-1";
		String day="-1";		
		String ISOdate;
		int addedChar = 0;
		while( matcher.find() ) {
			//Checks that the year makes sense
			if (Integer.parseInt(matcher.group().substring(matcher.group().length()-4))<2050) {

				//Extract ISO date format
				//Year
				year = matcher.group().substring(matcher.group().length()-4);

				//Month				
				if ((matcher.group().length()-7 > 0) && Character.isDigit(matcher.group().charAt(matcher.group().length()-7))) {
					month = matcher.group().substring(matcher.group().length()-7,matcher.group().length()-5);
				} else {
					month = "0"+matcher.group().charAt(matcher.group().length()-6);
				}

				//Day and final ISO date
				if (matcher.group().length() < 8) {
					ISOdate = year + "-" + month;					
				} else if (Character.isDigit(matcher.group().charAt(1))) {
					day = matcher.group().substring(0,2);
					ISOdate = year + "-" + month + "-" + day;
				} else {
					day = "0"+matcher.group().charAt(0);
					ISOdate = year + "-" + month + "-" + day;
				}				

				//Checks date validity
				if (!validDate(ISOdate)) {
					continue;
				}

				//finds extra days
				int previousLength = str.length();
				str = extraDayTagger(matcher, str, ISOdate, true, addedChar);
				addedChar += str.length()-previousLength;

				//Tags the text
				str  = str.substring(0,matcher.start()+addedChar) + "<date="+ISOdate+">"+ str.substring(matcher.start()+addedChar,matcher.end()+addedChar) + "</date>"+str.substring(matcher.end()+addedChar);
				addedChar += ("<date="+ISOdate+"></date>").length();

			}
		}
		return str;

	}
	
	/**
	 * Tags a string for dates in the ddMMyyyy format with the month in roman numerals
	 * @param str String The string to parse and tag for dates
	 * @return String The string with the dates tagged
	 */

	private static String ddRomanyyyyTagger (String str, boolean onlyRoman) {

		//For  ((d)d) RomanNumeral YYYY  /////////////////////////////////////////////////////////////////////////////////////////

		int addedChar = 0;
		String ddMMyyyyRoman;
		if (onlyRoman) {
			ddMMyyyyRoman = "(\\d{1,2}[-/.])?([1ILT]X|[1ILT]V|[1ILT][1ILT][1ILT]|[1ILT][1ILT]|[1ILT]|V[1ILT][1ILT][1ILT]|V[1ILT][1ILT]|V[1ILT]|V|X[1ILT][1ILT]|X[1ILT]|X|ix|iv|iii|ii|i|viii|vii|vi|v|xii|xi|x)[-/.]\\s?\\d{4}";
		} else {
			ddMMyyyyRoman = "(\\d{1,2}[-/.])?([1ILT]X|[1ILT]V|[1ILT][1ILT][1ILT]|[1ILT][ILT]|[ILT][1ILT]|[ILT]|V[1ILT][1ILT][1ILT]|V[1ILT][1ILT]|V[1ILT]|V|X[1ILT][1ILT]|X[1ILT]|X|ix|iv|iii|ii|i|viii|vii|vi|v|xii|xi|x)[-/.]\\s?\\d{4}";
		}
		Pattern pattern = Pattern.compile(ddMMyyyyRoman);
		Matcher matcher = pattern.matcher(str);  
		String year;
		String month;
		String day;		
		String ISOdate;
		while( matcher.find()) {
			//Checks that the year makes sense
			String[] tokens = matcher.group().split("[-/.]");

			if (tokens.length > 2) {

				month = romanToISOMonth(tokens[1]);	

				year = tokens[2];

				if (tokens[0].length()>1) {
					day = tokens[0];
				} else {
					day = "0"+tokens[0];

				}
				ISOdate= year+"-"+month+"-"+day;

			} else {				
				month = romanToISOMonth(tokens[0]);					
				year = tokens[1];
				ISOdate= year+"-"+month;
			}			

			//Checks date validity
			if (!validDate(ISOdate)) {
				continue;
			}

			//finds extra days
			int previousLength = str.length();
			str = extraDayTagger(matcher, str, ISOdate, true, addedChar);
			addedChar += str.length()-previousLength;

			//Tags the text
			str  = str.substring(0,matcher.start()+addedChar) + "<date="+ISOdate+">"+ str.substring(matcher.start()+addedChar,matcher.end()+addedChar) + "</date>"+str.substring(matcher.end()+addedChar);
			addedChar += ("<date="+ISOdate+"></date>").length();
		}


		return str;
	}

	/**
	 * Tags a string for dates in the yyyyMMdd format with the month in roman numerals
	 * @param str String The string to parse and tag for dates
	 * @return String The string with the dates tagged
	 */
	
	private static String yyyyRomanddTagger (String str, boolean onlyRoman) {
		int addedChar = 0;
		String ddMMyyyyRoman;
		if (onlyRoman) {
			ddMMyyyyRoman = "\\d{4}\\s?[-/.][?<!0-9]([1ILT]X|[1ILT]V|[1ILT][1ILT][1ILT]|[1ILT][1ILT]|[1ILT]|V[1ILT][1ILT][1ILT]|V[1ILT][1ILT]|V[1ILT]|V|X[1ILT][1ILT]|X[1ILT]|X|ix|iv|iii|ii|i|viii|vii|vi|v|xii|xi|x)[?!0-9]([-/.]\\d{1,2})?";	
		} else {
			ddMMyyyyRoman = "\\d{4}\\s?[-/.][?<!0-9]([1ILT]X|[1ILT]V|[1ILT][1ILT][1ILT]|[1ILT]I|I[1ILT]|[ILT]|V[1ILT][1ILT][1ILT]|V[1ILT][1ILT]|V[1ILT]|V|X[1ILT][1ILT]|X[1ILT]|X|ix|iv|iii|ii|i|viii|vii|vi|v|xii|xi|x)[?!0-9]([-/.]\\d{1,2})?";	
		}

		Pattern pattern = Pattern.compile(ddMMyyyyRoman);
		Matcher matcher = pattern.matcher(str);  
		String year;
		String month;
		String day;		
		String ISOdate;
		while( matcher.find() ) {
			//Checks that the year makes sense
			//Checks that the year makes sense
			String[] tokens = matcher.group().split("[-/.]");

			if (tokens.length > 2) {

				month = romanToISOMonth(tokens[1]);	

				year = tokens[0];

				if (tokens[2].length()>1) {
					day = tokens[2];
				} else {
					day = "0"+tokens[2];

				}
				ISOdate= year+"-"+month+"-"+day;

			} else {				
				month = romanToISOMonth(tokens[1]);					
				year = tokens[0];
				ISOdate= year+"-"+month;
			}		

			//Checks date validity
			if (!validDate(ISOdate)) {
				continue;
			}

			//Tags the text
			str  = str.substring(0,matcher.start()+addedChar) + "<date="+ISOdate+">"+ str.substring(matcher.start()+addedChar,matcher.end()+addedChar) + "</date>"+str.substring(matcher.end()+addedChar);
			addedChar += ("<date="+ISOdate+"></date>").length();


			//finds extra days
			int previousLength = str.length();
			str = extraDayTagger(matcher, str, ISOdate, false, addedChar);
			addedChar += str.length()-previousLength;

		}

		return str;
	}

	/**
	 * Tags a string for dates in the yyMMdd and ddMMyy format
	 * @param str String The string to parse and tag for dates
	 * @return String The string with the dates tagged
	 */
	
	private static String xxMMzzTagger (String str) {
		//For ddMMyy or yyMMdd /////////////////////////////////////////////////////////////////////////////////////////
		String xxMMzz = "(?<![IVX][.-/])(?<!(\\se\\s))(?<!\\d{1,2}[-/.])(?<!\\d{1,2})\\d{1,2}[-/.]\\d{1,2}([-/.]\\d{1,2})?(?!\\d{1,2})(?!([-/.>]\\d{1,2}))(?!(\\se\\s))(?![.-/][IVX])" ;
		Pattern pattern = Pattern.compile(xxMMzz);
		Matcher matcher = pattern.matcher(str);      
		String year;
		String month;
		String day;		
		String ISOdate;
		int addedChar = 0;
		while( matcher.find() ) {

			String[] tokens = matcher.group().split("[-/.]");

			if (tokens.length > 2) {
				//Month		
				month = completeMonth(tokens[1]);	

				if (tokens[0].length()>1) { //xx					
					if (tokens[2].length()>1) { // zz
						if (Integer.parseInt(tokens[0])>31)  {
							year = completeYear(tokens[0]);	
							day = tokens[2];
						} else {
							year = completeYear(tokens[2]);	
							day = tokens[0];
						}
					}else { //z
						year = completeYear(tokens[0]);	
						day = "0"+tokens[2];		
					}
				} else { //x
					year = completeYear(tokens[2]);	
					day = "0"+tokens[0];		
				}
				ISOdate= year+"-"+month+"-"+day;

			} else {
				if (tokens[0].length()>1) { //xx					
					if (tokens[1].length()>1) { // zz
						if (Integer.parseInt(tokens[0])>12)  {
							year = completeYear(tokens[0]);	
							month = completeMonth(tokens[1]);
						} else {
							year = completeYear(tokens[1]);	
							month = completeMonth(tokens[0]);
						}
					}else { //z
						year = completeYear(tokens[0]);	
						month = completeMonth(tokens[1]);		
					}
				} else {
					if (tokens[1].length()>1) { // zz
						year = completeYear(tokens[1]);	
						month = completeMonth(tokens[0]);
					} else {
						continue;
					}

				}
				ISOdate= year+"-"+month;

			}

			//Checks date validity
			if (!validDate(ISOdate)) {
				continue;
			}

			//Tags the text
			str  = str.substring(0,matcher.start()+addedChar) + "<date="+ISOdate+">"+ str.substring(matcher.start()+addedChar,matcher.end()+addedChar) + "</date>"+str.substring(matcher.end()+addedChar);
			addedChar += ("<date="+ISOdate+"></date>").length();	

		}

		return str;
	}

	/**
	 * Tags a string for dates extra dates (i.e., dd-ddMMyy format)in the ddMMyy or yyMMdd format.
	 * @param str String The string to parse and tag for dates
	 * @return String The string with the dates tagged
	 */
	
	private static String xxMMzzExtraTagger (String str) {
		//For ddMMyy or yyMMdd /////////////////////////////////////////////////////////////////////////////////////////
		String xxMMzz = "\\d{1,2}([-/.]|(\\se\\s))\\d{1,2}[-/.]\\d{1,2}([-/.]|(\\\\se\\\\s))\\d{1,2}" ;
		Pattern pattern = Pattern.compile(xxMMzz);
		Matcher matcher = pattern.matcher(str);      
		String year = "XX";
		String month = "XX";
		String day = "XX";		
		String ISOdate;
		boolean extraAtEnd;
		int eLocation;
		int addedChar = 0;
		String extra;
		while( matcher.find() ) {
			extraAtEnd = false;
			//checks if " e " exists			
			if( (eLocation = matcher.group().indexOf(" e ")) != -1) {
				//if " e " exists, check its position
				if (eLocation >= 4) { // extra/day/month/year
					extraAtEnd = true;
				}

			} else { //no " e "			 

				//Gets the first non digit
				int firstNonDigit = 0;
				while(Character.isDigit(matcher.group().charAt(firstNonDigit))) {
					firstNonDigit++;
				}

				//Gets the second non digit
				int secondNonDigit = firstNonDigit+1;			
				while(Character.isDigit(matcher.group().charAt(secondNonDigit))) {
					secondNonDigit++;
				}

				//Gets the last non digit
				int lastNonDigit = matcher.group().length()-1;			
				while(Character.isDigit(matcher.group().charAt(lastNonDigit))) {
					lastNonDigit--;
				}

				//If the second and last don't match, the format is year/month/day/extra
				if (!(((Character) matcher.group().charAt(lastNonDigit)).equals(matcher.group().charAt(secondNonDigit)))) {
					extraAtEnd = true;
				}
			}


			if (extraAtEnd) {
				//Extract ISO date format
				year = completeYear(matcher.group().substring(0,2));	

				//Month			
				month = completeMonth(matcher.group().substring(3,5));

				//Day
				if (Character.isDigit(matcher.group().charAt(7))) {
					day = matcher.group().substring(6,8);
				} else {
					day = "0" + matcher.group().charAt(6);
				}


				ISOdate = year + "-" + month + "-" + day;

				//Checks date validity
				if (!validDate(ISOdate)) {
					continue;
				}

				//Tags the text

				//Necessary to find first non-extra digit
				int z = 2;;
				while (!Character.isDigit(matcher.group().charAt(matcher.group().length()-z-1))) {
					z++;
				}

				//Tags the normal date
				str  = str.substring(0,matcher.start()+addedChar) + "<date="+ISOdate+">"+ str.substring(matcher.start()+addedChar,matcher.end()+addedChar-z) + "</date>"+str.substring(matcher.end()+addedChar-z);
				addedChar += ("<date="+ISOdate+"></date>").length();	

				if (Character.isDigit(matcher.group().charAt(matcher.group().length()-2))) {
					extra = matcher.group().substring(matcher.group().length()-2);
				} else {
					extra =  "0" + matcher.group().charAt(matcher.group().length()-1);
				}

				//Tags extra date
				str  = str.substring(0,matcher.end()+addedChar-z) + "<date="+ ISOdate.substring(0,ISOdate.length()-2)+extra+">"+ str.substring(matcher.end()+addedChar-z,matcher.end()+addedChar) + "</date>"+str.substring(matcher.end()+addedChar);
				addedChar += ("<date="+ISOdate.substring(0,ISOdate.length()-2)+extra+"></date>").length();	


			} else {

				//Extract ISO date format
				year = completeYear(matcher.group().substring(matcher.group().length()-2));	

				//Month					
				month = completeMonth(matcher.group().substring(matcher.group().length()-5,matcher.group().length()-3));

				//Day
				if (Character.isDigit(matcher.group().charAt(eLocation+4))) {
					day = matcher.group().substring(eLocation+3,eLocation+5);
				} else {
					day = "0" + matcher.group().charAt(eLocation+3);
				}		


				ISOdate = year + "-" + month + "-" + day;

				//Checks date validity
				if (!validDate(ISOdate)) {
					continue;
				}

				//Tags the text

				//Necessary to find first non-extra digit
				int z = 2;
				while (!Character.isDigit(matcher.group().charAt(z))) {
					z++;
				}

				if (Character.isDigit(matcher.group().charAt(1))) {
					extra = matcher.group().substring(0,2);
				} else {
					extra =  "0" + matcher.group().charAt(0);
				}

				//Tags extra date
				str  = str.substring(0,matcher.start()+addedChar) + "<date="+ ISOdate.substring(0,ISOdate.length()-2)+extra+">"+ str.substring(matcher.start()+addedChar,matcher.start()+addedChar+z) + "</date>"+str.substring(matcher.start()+addedChar+z);
				addedChar += ("<date="+ISOdate.substring(0,ISOdate.length()-2)+extra+"></date>").length();	

				//Tags normal date
				str  = str.substring(0,matcher.start()+addedChar+z) + "<date="+ISOdate+">"+ str.substring(matcher.start()+addedChar+z,matcher.end()+addedChar) + "</date>"+str.substring(matcher.end()+addedChar);
				addedChar += ("<date="+ISOdate+"></date>").length();	

			}


		}

		return str;
	}


	/**
	 * Tags a string for dates in the yyMMdd and ddMMyy format with the month in roman numerals
	 * @param str String The string to parse and tag for dates
	 * @return String The string with the dates tagged
	 */
	
	private static String xxRomanzzTagger (String str, boolean onlyRoman) {
		//For ddMMyy or yyMMdd /////////////////////////////////////////////////////////////////////////////////////////
		String xxMMzz;
		if (onlyRoman) {
			xxMMzz = "(?<!\\d{1,2})(?<!\\d{4}[./-])"
					+ "((\\d{1,2}[-/.](?<![IXVLiTxv1])([1ILT]X|[1ILT]V|[1ILT][1ILT][1ILT]|[1ILT][1ILT]|[1ILT]|V[1ILT][1ILT][1ILT]|V[1ILT][1ILT]|V[1ILT]|V|X[1ILT][1ILT]|X[1ILT]|X|ix|iv|iii|ii|i|viii|vii|vi|v|xii|xi|x)(?![IXVLiTxv1])([-/.]\\d{1,2})?)|"
					+ "((\\d{1,2}[-/.])?(?<![IXVLTixv1])([1ILT]X|[1ILT]V|[1ILT][1ILT][1ILT]|[1ILT][1ILT]|[1ILT]|V[1ILT][1ILT][1ILT]|V[1ILT][1ILT]|V[1ILT]|V|X[1ILT][1ILT]|X[1ILT]|X|ix|iv|iii|ii|i|viii|vii|vi|v|xii|xi|x)(?![IXVLiTxv1])[-/.]\\d{1,2}))"
					+ "(?!\\d{1,2})(?!\\s?[./-]\\s?\\d{4})" ;
		} else {
			xxMMzz = "(?<!\\d{1,2})(?<!\\d{4}[./-])"
					+ "((\\d{1,2}[-/.](?<![IXVLiTxv0-9])([1ILT]X|[1ILT]V|[1ILT][1ILT][1ILT]|[1ILT][ILT]|[ILT][1ILT]|[ILT]|V[1ILT][1ILT][1ILT]|V[1ILT][1ILT]|V[1ILT]|V|X[1ILT][1ILT]|X[1ILT]|X|ix|iv|iii|ii|i|viii|vii|vi|v|xii|xi|x)(?![IXVLiTxv0-9])([-/.]\\d{1,2})?)|"
					+ "((\\d{1,2}[-/.])?(?<![IXVLTixv0-9])([1ILT]X|[1ILT]V|[1ILT][1ILT][1ILT]|[1ILT][ILT]|[ILT][1ILT]|[ILT]|V[1ILT][1ILT][1ILT]|V[1ILT][1ILT]|V[1ILT]|V|X[1ILT][1ILT]|X[1ILT]|X|ix|iv|iii|ii|i|viii|vii|vi|v|xii|xi|x)(?![IXVLiTxv0-9])[-/.]\\d{1,2}))"
					+ "(?!\\d{1,2})(?![./-]\\s?\\d{4})" ;
		}

		Pattern pattern = Pattern.compile(xxMMzz);
		Matcher matcher = pattern.matcher(str);      
		String year;
		String month;
		String day;		
		String ISOdate;
		int addedChar = 0;
		boolean extraAtEnd;
		while( matcher.find() ) {
			extraAtEnd = false;
			//Checks that the last two characters are a digit
			String[] tokens = matcher.group().split("[-/.]");

			if (tokens.length > 2) {
				//Month		
				month = romanToISOMonth(tokens[1]);	

				if (tokens[0].length()>1) { //xx					
					if (tokens[2].length()>1) { // zz
						if (Integer.parseInt(tokens[0])>31)  {
							year = completeYear(tokens[0]);	
							day = tokens[2];
							extraAtEnd = true;
						} else {
							year = completeYear(tokens[2]);	
							day = tokens[0];
						}
					}else { //z
						year = completeYear(tokens[0]);	
						day = "0"+tokens[2];		
						extraAtEnd = true;
					}
				} else { //x
					year = completeYear(tokens[2]);	
					day = "0"+tokens[0];		
				}
				ISOdate= year+"-"+month+"-"+day;

			} else { //only two tokens
				if (Character.isDigit(tokens[0].charAt(0))) { //yyM		
					if (tokens[0].length()>1) {
						year = 	completeYear(tokens[0]);	
						month = romanToISOMonth(tokens[1]);	
					} else {
						continue;
					}

				} else { //Myy
					if (tokens[1].length()>1) {
						year = 	completeYear(tokens[1]);	
						month = romanToISOMonth(tokens[0]);	
					} else {
						continue;
					}
				}
				ISOdate= year+"-"+month;
			}

			//Checks date validity
			if (!validDate(ISOdate)) {
				continue;
			}

			if (extraAtEnd) {
				//Tags the text
				str  = str.substring(0,matcher.start()+addedChar) + "<date="+ISOdate+">"+ str.substring(matcher.start()+addedChar,matcher.end()+addedChar) + "</date>"+str.substring(matcher.end()+addedChar);
				addedChar += ("<date="+ISOdate+"></date>").length();	


				//finds extra days
				int previousLength = str.length();
				str = extraDayTagger(matcher, str, ISOdate, false, addedChar);
				addedChar += str.length()-previousLength;
			} else {

				//finds extra days
				int previousLength = str.length();
				str = extraDayTagger(matcher, str, ISOdate, true, addedChar);
				addedChar += str.length()-previousLength;

				//Tags the text
				str  = str.substring(0,matcher.start()+addedChar) + "<date="+ISOdate+">"+ str.substring(matcher.start()+addedChar,matcher.end()+addedChar) + "</date>"+str.substring(matcher.end()+addedChar);
				addedChar += ("<date="+ISOdate+"></date>").length();	
			}

		}

		return str;
	}

	/**
	 * Converts yy to yyyy if needed
	 * @param partialYear String A year in yy or yyyy format
	 * @return String yyyy formatted year
	 */
	
	private static String completeYear(String partialYear) {
		String fullyear ="";
		int currentYear = Integer.parseInt((Integer.toString(Calendar.getInstance().get(Calendar.YEAR)).substring(2,4))); 
		if(Integer.parseInt(partialYear) > currentYear) {
			fullyear = "19"+partialYear;
		} else {
			fullyear = "20"+partialYear;
		}		
		return fullyear;
	}

	/**
	 * Converts M to MM if needed
	 * @param partialYear String A month in M or MM format
	 * @return String MM formatted month
	 */
	
	private static String completeMonth (String partialMonth) {
		//Month			
		if (Character.isDigit(partialMonth.charAt(0))) {
			return partialMonth;
		} else {
			return ("0"+partialMonth.charAt(1));
		}
	}

	
	/**
	 * Tags a string for dates extra dates (i.e., dd-ddMMyy format)in the ddMMyy or yyMMdd format.
	 * @param str String The string to parse and tag for dates
	 * @return String The string with the dates tagged
	 */
	
	/**
	 * Tags a string for dates extra dates (i.e., dd-ddMMyyyy format)in the ddMMyyyy or yyyyMMdd format.
	 * @param matcher Matcher The matcher that found the original date
	 * @param str String The string being parsed and tagged for dates
	 * @param ISOdate String The original ISO date
	 * @param before boolean true if ddMMyyy, false if yyyyMMdd
	 * @param addedChar int The current offset in reading the string due to previously placed tags
	 * @return String The string with the dates tagged
	 */
	
	private static String extraDayTagger(Matcher matcher, String str, String ISOdate, boolean before, int addedChar ) {
		if (before) {
			//finds extra days
			String extraDay = "\\d{1,2}([-/]|(\\se\\s))";     
			Pattern patternExtra = Pattern.compile(extraDay);
			int seqStart = matcher.start()-5;				
			//Corrects for dates that start the sentence
			if (seqStart < 0) {
				seqStart = 0;
			}				
			Matcher matcherExtra = patternExtra.matcher(str.subSequence(seqStart+addedChar, matcher.start()+addedChar));  
			String extra;			
			while( matcherExtra.find() ) {
				if (Character.isDigit(matcherExtra.group().charAt(1))) {
					extra = matcherExtra.group().substring(0,2);
				} else {
					extra =  "0" + matcherExtra.group().charAt(0);
				}

				//Tags the text
				int startInd =seqStart+matcherExtra.start()+addedChar;
				int endInd =seqStart+matcherExtra.end()+addedChar;
				return  str.substring(0,startInd) + "<date="+ISOdate.substring(0,ISOdate.length()-2)+extra+">"+ str.substring(startInd,endInd) + "</date>"+str.substring(endInd);				
			}

		} else {
			String extraDay = "([-/]|(\\se\\s))\\d{1,2}";     
			Pattern patternExtra = Pattern.compile(extraDay);
			int seqEnd = matcher.end()+5+addedChar;
			if (seqEnd> str.length()) {
				seqEnd = str.length();
			}
			Matcher matcherExtra = patternExtra.matcher(str.subSequence(matcher.end()+addedChar, seqEnd));  
			String extra;			
			while( matcherExtra.find() ) {
				if (Character.isDigit(matcherExtra.group().charAt(matcherExtra.group().length()-2))) {
					extra = matcherExtra.group().substring(matcherExtra.group().length()-2);
				} else {
					extra =  "0" + matcherExtra.group().charAt(matcherExtra.group().length()-1);
				}

				//Tags the text
				int startInd =matcher.end()+matcherExtra.start()+addedChar;
				int endInd =matcher.end()+matcherExtra.end()+addedChar;
				return str.substring(0,startInd) + "<date="+ISOdate.substring(0,ISOdate.length()-2)+extra+">"+ str.substring(startInd,endInd) + "</date>"+str.substring(endInd);
			}
		}

		return str;
	}

	/**
	 * Receives a month in roman numerals and returns it in ISO format
	 * @param romanMonth Month in roman numerals 
	 * @return String ISO month 
	 */
	private static String romanToISOMonth(String romanMonth) {
		romanMonth = romanMonth.toUpperCase();
		switch(romanMonth) {
		case "I":
			return "01";
		case "II":
			return "02";
		case "III":
			return "03";
		case "IV":
			return "04";
		case "V":
			return "05";
		case "VI":
			return "06";
		case "VII":
			return "07";
		case "VIII":
			return "08";
		case "IX":
			return "09";
		case "X":
			return "10";
		case "XI":
			return "11";
		case "XII":
			return "12";		
		default:
			//Deals with not so normal roman numerals
			if (romanMonth.matches("[1ILT]X")) {
				return "09";
			} else if (romanMonth.matches("[1ILT]V")) {
				return "04";
			}else if (romanMonth.matches("[1ILT][1ILT][1ILT]")) {
				return "03";
			}else if (romanMonth.matches("[1ILT][1ILT]")) {
				return "02";
			}else if (romanMonth.matches("V[1ILT][1ILT][1ILT]")) {
				return "08";
			}else if (romanMonth.matches("V[1ILT][1ILT]")) {
				return "07";
			}else if (romanMonth.matches("V[1ILT]")) {
				return "06";
			}else if (romanMonth.matches("X[1ILT][1ILT]")) {
				return "12";
			}else if (romanMonth.matches("X[1ILT]")) {
				return "11";
			}else if (romanMonth.matches("[1ILT]")) {
				return "01";
			}

			return "XX";			
		}		
	}
	
	/**
	 * Checks the validity of a date
	 * @param ISODate String yyyy-MM-dd format
	 * @return boolean Returns true if the date is valid and false if not
	 */

	private static boolean validDate(String ISODate) {
		try {
			String[] dates = ISODate.trim().split("-");
			if (Integer.parseInt(dates[0])> Calendar.getInstance().get(Calendar.YEAR) ||Integer.parseInt(dates[0])<1500)
				return false;
			if (Integer.parseInt(dates[1])>12 || Integer.parseInt(dates[1])<1)
				return false;
			if (dates.length < 3) 
				return true;		
			if (Integer.parseInt(dates[2])>31 || Integer.parseInt(dates[2])<1)
				return false;
			return true;
		} catch (NumberFormatException e) {
			return false;
		}

	}

}
