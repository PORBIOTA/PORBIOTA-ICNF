package taggers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * Class to convert a tagged .txt file into a .csv table
 * 
 * @author Tomé Neves de Matos
 *
 */

public class TagToCsv {

	/** line currently being checked for tags*/
	private String line;

	/** Current verbatimScientificName to print */
	private String verbatimScientificName;

	/** Current scientificName to print */
	private String scientificName;

	/** Current location to print */
	private String location;
	
	/** Current location latitude to print */
	private String locationLat;
	
	/** Current location longitude to print */
	private String locationLon;

	/** Current verbatimDate to print */
	private String verbatimDate;

	/** Current date to print */
	private String date;

	/** File writer */
	private BufferedWriter writer;

	/** The finishTerm index of the last gotten term */
	private int lastTermIndex;

	/**	 Array with the terms to expect to end a line, in order from most important to secondary terms */	 
	private String[] finishTerm;

	/** Array with the extra terms besides scientificName, location and date */
	private String[] DwCTerms;

	/** Array with the extra terms' content */
	private String[] extraContent;

	public static void main(String[] args) throws IOException {
		TagToCsv csvWriter = new TagToCsv();
		/*** INPUT ***/
		String finishingTermOrder = "associatedTaxa,measurementValue,individualCount,date";	
		String preSpecies = "\\d{1,2}";
		int preSpeciesNtoLook = 6;
		String firstTerm = "taxonomicName";
		String input = "G:\\Porbiota\\PDFs para OCR\\OCR Testes\\BSPEN_1995_04_30_OCR_tagged2.txt";
		String output = "G:\\Porbiota\\PDFs para OCR\\OCR Testes\\BSPEN_1995_04_30_OCR_tagged2.csv";
		boolean separateDates = false;
		/********/	
		csvWriter.extractCsv(input,output,finishingTermOrder,firstTerm,preSpecies,preSpeciesNtoLook,separateDates);
	}

	public TagToCsv() {
		
	}

	
	/**
	 * Method to write a csv file with the information extracted from the CSV
	 * 
	 * @param input String Input file path
	 * @param output String Output file path 
	 * @param finishingTermOrder String List of terms that might end a line in order from most important to least, sepparated by commas
	 * @param firstTerm String The DwC term that initializes a list
	 * @param preSpecies int Regex to match before the firstTerm 
	 * @param preSpeciesNtoLook int Number of characters back to look before the the firstTerm to check the preSpecies regex
	 * @param separateDates boolean True to keep combined dates separate, false not to
	 * @throws IOException
	 */
	public void extractCsv(String input, String output, String finishingTermOrder, String firstTerm, String preSpecies, int preSpeciesNtoLook, boolean separateDates) throws IOException {

		BufferedReader reader = Files.newBufferedReader(Paths.get(input), StandardCharsets.UTF_8);
		writer = Files.newBufferedWriter(Paths.get(output), StandardCharsets.UTF_8);

		preSpecies = ".*"+preSpecies+".*";
		int preSpeciesLoc; 

		String header = "verbatimScientificName,scientificName,verbatimDate,date,location,decimalLatitude,decimalLongitude";


		//Search for custom additional tags
		Set<String> DwCTermsSet = new HashSet<String>();

		//reads the entire file
		String taggedText = "";
		String currentLine = "";
		while ((currentLine = reader.readLine())!=null) {
			taggedText += currentLine + "\n";
		}

		//Pattern to match all tags
		Pattern pattern = Pattern.compile("(?<=</)[a-zA-Z]+(?=>)");
		Matcher matcher = pattern.matcher(taggedText);      
		while( matcher.find() ) { //For each matched tag
			DwCTermsSet.add(matcher.group());
		}		

		//Stores the unique terms in an array
		Object[] DwCTermsWithExtra = DwCTermsSet.toArray();

		//Creates an array without the 3 basic terms
		DwCTerms = new String[DwCTermsWithExtra.length-3];		
		int nTerms = 0;
		for (int a = 0; a < DwCTermsWithExtra.length; a++) {
			if (!(DwCTermsWithExtra[a].toString().contentEquals("taxonomicName") 
					|| DwCTermsWithExtra[a].toString().contentEquals("date")
					|| DwCTermsWithExtra[a].toString().contentEquals("location"))) {
				DwCTerms[nTerms] = DwCTermsWithExtra[a].toString();
				header += "," + DwCTerms[nTerms]; //adds extra terms to header
				nTerms++;
			}
		}

		//writes the header
		writer.write(header+"\n");

		//Creates an array without the 3 basic terms
		extraContent= new String[DwCTerms.length];
		for (int a = 0; a < extraContent.length; a++) {
			extraContent[a]="";
		}



		//Gets an array with the finishing terms in order (counts the number of commas to get the number of entries)
		finishTerm = new String[(int) (finishingTermOrder.codePoints().filter(ch -> ch == ',').count()+1)];
		int b = 0;
		while(finishingTermOrder.length() > 0) {
			if (finishingTermOrder.contains(",")) {
				finishTerm[b] = finishingTermOrder.substring(0,finishingTermOrder.indexOf(",")); //Grabs first term
				finishingTermOrder = finishingTermOrder.substring(finishingTermOrder.indexOf(",")+1); //Deletes it
			} else {
				finishTerm[b] = finishingTermOrder;
				break;
			}			
			b++;
		}		


		//Reads the file again
		reader.close();
		reader = Files.newBufferedReader(Paths.get(input), StandardCharsets.UTF_8);

		lastTermIndex = -1;
		while ((line = reader.readLine())!= null) {


			//repeats until line is consumed
			while (line.length() >0) {
				//If a tag exists
				int tagStart = -1;
				if((tagStart = line.indexOf("<")+1)>0) {
					if(line.substring(tagStart,tagStart+"date".length()).contentEquals("date")) {

						if (firstTerm.contentEquals("date")) {
							//If a first term, checks for the regex before

							//Checks how far before the < it can go					
							if(tagStart < preSpeciesNtoLook) {
								preSpeciesLoc = 0;
							} else {
								preSpeciesLoc = tagStart -preSpeciesNtoLook;
							}

							//If the pre regex exists, initialize a new species
							if(line.substring(preSpeciesLoc,tagStart).matches(preSpecies)) {
								maybeWrite("date","before");
								emptyTerms();
							} else {
								line = line.substring(line.indexOf("</date>")+"</date>".length());
								continue;
							}
						}

						maybeWrite("date","before");

						//gets date from = to >
						date = line.substring(line.indexOf("=",tagStart+"date".length())+1,line.indexOf(">",tagStart+"date".length()+1));

						//gets verbatimDate from > to <
						verbatimDate = line.substring(line.indexOf(">",tagStart+"date".length()+1)+1,line.indexOf("<",tagStart+"date".length()+1));

						line = line.substring(line.indexOf("</date>")+"</date>".length());


						//If separate dates are not to be separated
						if (!separateDates) {


							if(line.length() > 1 && line.substring(1,"date".length()+1).contentEquals("date") && verbatimDate.length()<6) {
								tagStart = line.indexOf("<")+1;

								String firstDateElement;
								//gets the last date element (day or month) from = to >
								firstDateElement = line.substring(line.indexOf("=",tagStart+"date".length())+1,line.indexOf(">",tagStart+"date".length()+1));
								firstDateElement = firstDateElement.substring(firstDateElement.lastIndexOf("-")+1);

								//adds the element to the previous date
								date += "\\"+firstDateElement;

								//gets verbatimDate from > to <
								verbatimDate += line.substring(line.indexOf(">",tagStart+"date".length()+1)+1,line.indexOf("<",tagStart+"date".length()+1));								

								line = line.substring(line.indexOf("</date>")+"</date>".length());
							}							
						} 

						maybeWrite("date","after");




						continue;

					} else if (line.substring(tagStart,tagStart+"taxonomicName".length()).contentEquals("taxonomicName")) {


						if (firstTerm.contentEquals("taxonomicName")) {
							//If a first term, checks for the regex before

							//Checks how far before the < it can go					
							if(tagStart < preSpeciesNtoLook) {
								preSpeciesLoc = 0;
							} else {
								preSpeciesLoc = tagStart -preSpeciesNtoLook;
							}

							//If the pre regex exists, initialize a new species
							if(line.substring(preSpeciesLoc,tagStart).matches(preSpecies)) {
								maybeWrite("scientificName","before");
								emptyTerms();
							} else {
								line = line.substring(line.indexOf("</taxonomicName>")+"</taxonomicName>".length());
								continue;
							}
						}

						maybeWrite("scientificName","before");

						//tira o scientific name do = até ao >
						scientificName = line.substring(line.indexOf("=",tagStart+"taxonomicName".length()+1)+1,line.indexOf(">",tagStart+"taxonomicName".length()+1));
						//tira o verbatim scientific name do > até ao <
						verbatimScientificName = line.substring(line.indexOf(">",tagStart+"taxonomicName".length()+1)+1,line.indexOf("<",tagStart+"taxonomicName".length()+1));

						maybeWrite("scientificName","after");



						line = line.substring(line.indexOf("</taxonomicName>")+"</taxonomicName>".length());
						continue;



					} else if (line.substring(tagStart,tagStart+"location".length()).contentEquals("location")) {

						if (firstTerm.contentEquals("location")) {
							//If a first term, checks for the regex before

							//Checks how far before the < it can go					
							if(tagStart < preSpeciesNtoLook) {
								preSpeciesLoc = 0;
							} else {
								preSpeciesLoc = tagStart -preSpeciesNtoLook;
							}

							//If the pre regex exists, initialize a new species
							if(line.substring(preSpeciesLoc,tagStart).matches(preSpecies)) {
								maybeWrite("location","before");
								emptyTerms();
							} else {
								line = line.substring(line.indexOf("</location>")+"location".length()+3);
								continue;
							}
						}

						maybeWrite("location","before");


						//grabs location from > to <
						location = line.substring(line.indexOf(">",tagStart+"location".length())+1,line.indexOf("<",tagStart+"location".length()+1));
						
						//grabs location extra terms
						String[] extraTerms;
						extraTerms = line.substring(line.indexOf("=",tagStart+"location".length()+1)+1,line.indexOf(">",tagStart+"location".length()+1)).split("lon=");
						locationLat = extraTerms[0];
						locationLon = extraTerms[1];
						
						
						

						line = line.substring(line.indexOf("</location>")+"</location>".length());

						//For sectioned locations, gathers all
						while (line.indexOf("<location") <3 && line.indexOf("<location") > -1 ) {
							tagStart = line.indexOf("<")+1;

							location = location + ", "+line.substring(line.indexOf(">",tagStart+"location".length())+1,line.indexOf("<",tagStart+"location".length()+1));

							line = line.substring(line.indexOf("</location>")+"location".length()+3);
						}

						maybeWrite("location","after");

						continue;
					}  else {
						//Checks for the other tags
						boolean matched = false;
						for (int a = 0; a < DwCTerms.length; a++) {	


							String term = DwCTerms[a].toString();
							if(line.substring(tagStart,tagStart+term.length()).contentEquals(term)) {

								if (firstTerm.contentEquals(term)) {
									//If a first term, checks for the regex before

									//Checks how far before the < it can go					
									if(tagStart < preSpeciesNtoLook) {
										preSpeciesLoc = 0;
									} else {
										preSpeciesLoc = tagStart -preSpeciesNtoLook;
									}

									//If the pre regex exists, initialize a new species
									if(line.substring(preSpeciesLoc,tagStart).matches(preSpecies)) {
										maybeWrite(term,"before");
										emptyTerms();
									} else {
										line = line.substring(line.indexOf("</"+term+">")+("</"+term+">").length());
										continue;
									}
								}

								maybeWrite(term,"before");

								if (term.contentEquals("associatedTaxa")) {			

									//gets type of association from = to >
									extraContent[termIndex(term)] = "\""+line.substring(line.indexOf("=",tagStart+"date".length())+1,line.indexOf(">",tagStart+"date".length()+1))+"\":\"";

									//gets associated taxa from > to <
									extraContent[termIndex(term)] += line.substring(line.indexOf(">",tagStart+"date".length()+1)+1,line.indexOf("<",tagStart+"date".length()+1))+"\"";

								} else {

									extraContent[termIndex(term)] = line.substring(line.indexOf(">",tagStart+term.length())+1,line.indexOf("<",tagStart+term.length()+1));

								}

								line = line.substring(line.indexOf("</"+term+">")+("</"+term+">").length());

								maybeWrite(term,"after");
								matched = true;
								break;
							}
						} 
						if (matched) {
							continue;
						}
					}

				}	
				line = "";
			}
		}
		reader.close();
		writer.close();
	}

	/**
	 * Method to check if a line should be written
	 * 
	 * @param term String The term currently being analysed
	 * @param befOrAft String "before" or "after" depending on whether the check is being realized before ora after reading the tag
	 * @throws IOException
	 */

	private void maybeWrite(String term, String befOrAft) throws IOException {
		int position = existsIn(term,finishTerm);
		switch(befOrAft) {
		case "before":
			if (lastTermIndex > 0 && ((position > 0 && lastTermIndex <= position) || position == -1)) {
				writeLine();
			}
			lastTermIndex = position;
			break;

		case "after":
			if (position == 0) {
				writeLine();
			} 
			lastTermIndex = position;
			break;
		}
	}

	/**
	 *  Checks whether a String exists in a String array
	 * @param term String to search
	 * @param array String[] to check for matches
	 * @return int the position of the term in the array, -1 if it doesn't exist
	 */
	private int existsIn(String term, String[] array) {
		for (int i = 0 ; i < array.length; i++) {
			if (term.contentEquals(array[i])) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Writes a csv line with the available data
	 * @throws IOException 
	 */
	private void writeLine() throws IOException {
		String line = "\""+verbatimScientificName.replace("\"","\"\"") +  "\",\"" + scientificName.replace("\"","\"\"") + "\",\""  + verbatimDate.replace("\"","\"\"") + "\",\"" + date + "\",\""
				+ location.replace("\"","\"\"") + "\",\""+locationLat.replace("\"","\"\"") + "\",\""+locationLon.replace("\"","\"\"")+"\"";
		for (int i = 0 ; i < extraContent.length; i++) {
			line += ",\"" + extraContent[i].replace("\"","\"\"") + "\"";
			extraContent[i] = "";
		}

		//erases sequences that end lines, as those should not repeat
		if (existsIn("taxonomicName",finishTerm)>-1 ) {
			verbatimScientificName = "";
			scientificName = "";
		}
		if (existsIn("date",finishTerm)>-1 ) {
			verbatimDate = "";
			date = "";
		}
		if (existsIn("location",finishTerm)>-1 ) {
			location = "";
		}

		writer.write(line+"\n");	
	}

	/**
	 * Returns the index of the extra term
	 * @param term String term to check the index of
	 * @return int The index of the term, -1 if term doesn't exist 
	 */
	private int termIndex(String term) {

		for (int i = 0 ; i < DwCTerms.length; i++) {
			if (term.contentEquals(DwCTerms[i])) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Empties the terms in memory
	 */

	private void emptyTerms() {
		for (int i = 0 ; i < extraContent.length; i++) {
			extraContent[i] = "";
		}
		verbatimScientificName = "";
		scientificName = "";
		verbatimDate = "";
		date = "";
		location = "";
	}

}