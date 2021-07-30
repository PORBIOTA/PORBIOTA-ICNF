package csvEditorSequentialLoad;


import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



public class SpeciesExtractorSeqLoad {

	private static int latID=-1;
	private static int longID=-1;			 
	private static int scientificNameID=-1;
	private static int acceptedNameUsageID=-1;
	private static int kingdomID=-1;				
	private static int phylumID=-1;
	private static int classID=-1;				
	private static int orderID=-1;				
	private static int familyID=-1;				
	private static int genusID=-1;
	private static int specificEpithetID=-1;				
	private static int infraspecificEpithetID=-1;				
	private static int taxonRankID=-1;			
	private static int countryID=-1;					
	private static int dateID=-1;	




	//Arg 0 - Kingdom
	//Arg 1 - Input
	//Arg 2 - Output
	public static void main(String[] args) throws IOException {


		//Reads the input file
		CSVReader csvReader;
		String originalTablePath = args[1];
		try {
			Reader originalReader = Files.newBufferedReader(Paths.get(originalTablePath), StandardCharsets.UTF_8 );
			csvReader = new CSVReader(originalReader);


			// Reading Rows One by One in a String array
			String[] occ;

			//get headers
			attributeHeaders(csvReader.readNext());


			int counter = 1;

			//Creates a hashmap that stores species already fetched
			String kingdomOfSpecies = args[0];	
			HashMap<String, JSONObject[]> speciesJSONs = new HashMap<String, JSONObject[]>();

			JSONObject[] obj = null;
			JSONParser parser = null;


			//Opens writer
			String fileName = args[2];
			Path myPath = Paths.get(fileName);
			try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(myPath.toString()), "UTF-8"))) {


				//Writes header line
				String[] header = {"scientificName","acceptedNameUsage","kingdom","phylum","class","order","family","genus",
						"specificEpithet","infraspecificEpithet","taxonRank","scientificNameAuthorship","confidence","matchType","occurenceID"};
				writer.writeNext(header);


				String scientificNameCurrent;
				String acceptedNameUsageCurrent;
				String kingdomCurrent;				
				String phylumCurrent;
				String classCurrent;				
				String orderCurrent;				
				String familyCurrent;				
				String genusCurrent;
				String specificEpithetCurrent;				
				String infraspecificEpithetCurrent;				
				String taxonRankCurrent;								
				String scientificNameAuthorshipCurrent;
				String occurenceIDCurrent;
				long confidenceCurrent;
				String matchTypeCurrent;

				while ((occ = csvReader.readNext()) != null) {

					scientificNameCurrent = "";
					acceptedNameUsageCurrent = "";
					kingdomCurrent = "";				
					phylumCurrent = "";
					classCurrent = "";				
					orderCurrent = "";				
					familyCurrent = "";				
					genusCurrent = "";
					specificEpithetCurrent = "";				
					infraspecificEpithetCurrent = "";				
					taxonRankCurrent = "";								
					scientificNameAuthorshipCurrent = "";
					occurenceIDCurrent = "";
					confidenceCurrent = 0;
					matchTypeCurrent = "";

					System.out.println("Working on line " + counter);					
					counter++;
					obj = new JSONObject[2];

					occurenceIDCurrent = UUID.randomUUID().toString();
					try {
					scientificNameCurrent =	occ[scientificNameID].replaceAll("( sp.)$", "").replaceAll("( spp.)$", "").replaceAll("( spp)$","");
					} catch (ArrayIndexOutOfBoundsException e ) {
						System.out.println("No scientificName header found!");
						System.exit(1);
					}

					if (!speciesJSONs.containsKey(occ[scientificNameID])) {	

						String s = "https://api.gbif.org/v1/species/match?verbose=true&kingdom="+URLEncoder.encode(kingdomOfSpecies, "UTF-8")+"&name=";
						s += URLEncoder.encode(occ[scientificNameID], "UTF-8");
						URL url = new URL(s);

						// read from the URL
						Scanner scan = new Scanner(url.openStream(),"utf-8");
						String str = new String();
						while (scan.hasNext())
							str += scan.nextLine();
						scan.close();

						// build a JSON object
						parser = new JSONParser();		
						try {
							obj[0] = (JSONObject) parser.parse(str);
						} catch (ParseException e) {

							e.printStackTrace();
						} 	

						//If no match, goes to next unless fuzzy match
						if (((String) obj[0].get("matchType")).equalsIgnoreCase("none") ) {			
							if ( obj[0].get("alternatives") == null ) {
								String[] line = 	{occ[scientificNameID],
										"",
										"",
										"",
										"",
										"",
										"",
										"",
										"",
										"",
										"",
										"",
										"0",
										"",
										occurenceIDCurrent};

								writer.writeNext(line);

								continue;
							} else {
								obj[0] = (JSONObject) ((JSONArray) obj[0].get("alternatives")).get(0);
							}
						}


						//If synonym get new name
						if (((String) obj[0].get("rank")).equalsIgnoreCase("Species") || ((String) obj[0].get("rank")).equalsIgnoreCase("Subspecies")) {

							String canonicalSpeciesOnly = (String) obj[0].get("canonicalName");

							if (canonicalSpeciesOnly.lastIndexOf(" ") != canonicalSpeciesOnly.indexOf(" ")) {
								canonicalSpeciesOnly = canonicalSpeciesOnly.substring(0, canonicalSpeciesOnly.lastIndexOf(" "));
							}

							if (!canonicalSpeciesOnly.equalsIgnoreCase((String) obj[0].get("species"))) {

								s = "https://api.gbif.org/v1/species/match?verbose=true&kingdom="+URLEncoder.encode(kingdomOfSpecies, "UTF-8")+"&name=";
								s += URLEncoder.encode(((String) obj[0].get("species")), "UTF-8");
								url = new URL(s);

								// read from the URL
								scan = new Scanner(url.openStream(),"utf-8");
								str = new String();
								while (scan.hasNext())
									str += scan.nextLine();
								scan.close();

								// build a JSON object
								parser = new JSONParser();		
								try {
									obj[1] = (JSONObject) parser.parse(str);
								} catch (ParseException e) {

									e.printStackTrace();
								} 	
							}
						}
						//0 will be original call, 1 will be the call for the synonym species
						speciesJSONs.put(occ[scientificNameID],(JSONObject[]) obj.clone());

					}


					//Fetch original or synonym
					int apiCall = 0;
					if (speciesJSONs.get(occ[scientificNameID])[1] != null) {
						apiCall = 1;
					} 

					JSONObject[] currentJSON = speciesJSONs.get(occ[scientificNameID]);

					String speciesName = occ[scientificNameID];


					//Captures author names
					scientificNameAuthorshipCurrent = (String) currentJSON[apiCall].get("scientificName");

					// Try block is to catch species without author
					try {	
						if ("subspecies".equalsIgnoreCase((String) currentJSON[apiCall].get("rank"))) {
							if (scientificNameAuthorshipCurrent.contains("var. ")) {					
								scientificNameAuthorshipCurrent = scientificNameAuthorshipCurrent.substring(scientificNameAuthorshipCurrent.indexOf(" ", scientificNameAuthorshipCurrent.indexOf("var. ")+5)+1);
							} else if (scientificNameAuthorshipCurrent.contains("subsp. ")) {
								scientificNameAuthorshipCurrent = scientificNameAuthorshipCurrent.substring(scientificNameAuthorshipCurrent.indexOf(" ", scientificNameAuthorshipCurrent.indexOf("subsp. ")+7)+1);
							} else {
								//gets the string after the second space (" ")
								scientificNameAuthorshipCurrent = scientificNameAuthorshipCurrent.substring(scientificNameAuthorshipCurrent.indexOf(" ", scientificNameAuthorshipCurrent.indexOf(" ",scientificNameAuthorshipCurrent.indexOf(" ")+1)+1)+1);						}
						} else if ("species".equalsIgnoreCase((String) currentJSON[apiCall].get("rank"))){
							//gets after the second the first space (" ")
							scientificNameAuthorshipCurrent = scientificNameAuthorshipCurrent.substring(scientificNameAuthorshipCurrent.indexOf(" ", scientificNameAuthorshipCurrent.indexOf(" ")+1)+1);
						} else {
							scientificNameAuthorshipCurrent = "";
						}
					} catch (StringIndexOutOfBoundsException e) {
						scientificNameAuthorshipCurrent = "";
					}

					//Genus
					String speciesGenus = "";
					if(speciesName.contains(" ")){
						speciesGenus = (String) speciesName.subSequence(0, speciesName.indexOf(' '));
					} else {
						speciesGenus = speciesName;
					}

					//Specific and Infra Epithet
					String speciesEpithet = "";
					String speciesInfraEpithet = "";
					if(speciesName.contains(" ")){
						if (speciesName.indexOf(' ') != speciesName.lastIndexOf(' ')) {
							speciesEpithet = ((String) speciesName.subSequence(speciesName.indexOf(' ')+1, speciesName.lastIndexOf(' '))).replace(" subsp.", "").replace(" var.", "");
							speciesInfraEpithet = (String) speciesName.subSequence(speciesName.lastIndexOf(' ')+1,speciesName.length());
						} else {
							speciesEpithet = (String) speciesName.subSequence(speciesName.indexOf(' ')+1, speciesName.length());
							speciesInfraEpithet = "";
						}
					}

					//Define scientific name
					specificEpithetCurrent = speciesEpithet;
					infraspecificEpithetCurrent = speciesInfraEpithet;

					//Fixes strange matching with non matching items by making sure Kingdom matches come up empty in all taxonomic fields
					if (((String) speciesJSONs.get(occ[scientificNameID])[0].get("rank")).equalsIgnoreCase("KINGDOM")) {
						acceptedNameUsageCurrent = ((String) currentJSON[0].get("scientificName"));
						kingdomCurrent = ((String) speciesJSONs.get(occ[scientificNameID])[apiCall].get("kingdom"));
						specificEpithetCurrent = "";
						infraspecificEpithetCurrent = "";

					}else {

						//Sets the various taxonomic fields
						acceptedNameUsageCurrent = ((String) currentJSON[apiCall].get("scientificName"));

						confidenceCurrent = ((long) speciesJSONs.get(occ[scientificNameID])[0].get("confidence"));

						try {
							taxonRankCurrent = ((String) speciesJSONs.get(occ[scientificNameID])[0].get("rank"));
						} catch (NullPointerException e) {
						}

						try {
							kingdomCurrent = ((String) speciesJSONs.get(occ[scientificNameID])[apiCall].get("kingdom"));
						} catch (NullPointerException e) {
						}
						try {
							phylumCurrent = ((String) speciesJSONs.get(occ[scientificNameID])[apiCall].get("phylum"));
						} catch (NullPointerException e) {
						}
						try {
							classCurrent = ((String) speciesJSONs.get(occ[scientificNameID])[apiCall].get("class"));
						} catch (NullPointerException e) {
						}
						try {
							orderCurrent = ((String) speciesJSONs.get(occ[scientificNameID])[apiCall].get("order"));
						} catch (NullPointerException e) {
						}
						try {
							familyCurrent = ((String) speciesJSONs.get(occ[scientificNameID])[apiCall].get("family"));	
						} catch (NullPointerException e) {
						}

						try {
							genusCurrent = (speciesGenus);		
						} catch (NullPointerException e) {
						}

					}

					///WRITER

					String[] line = 	{scientificNameCurrent,
							acceptedNameUsageCurrent,
							kingdomCurrent,
							phylumCurrent,
							classCurrent,
							orderCurrent,
							familyCurrent,
							genusCurrent,
							specificEpithetCurrent,
							infraspecificEpithetCurrent,
							taxonRankCurrent,
							scientificNameAuthorshipCurrent,
							String.valueOf(confidenceCurrent),
							matchTypeCurrent,
							occurenceIDCurrent};

					writer.writeNext(line);

				}
				//No need to close as CSVWriter auto-closes
				System.out.println("Complete!");
			} catch (IOException e) {
				System.out.println("There is no output file or it is not accessible!");
				e.printStackTrace();
				System.exit(1);	

			} catch (CsvValidationException e) {			
				e.printStackTrace();
			}

		} catch (IOException e) {			
			System.out.println("There is no input file!");
			System.exit(1);
		}
		catch (RuntimeException e) {
			System.out.println("Either the .csv is not in UTF-8 or its delimiters are not commas(,)");
			e.printStackTrace();
			System.exit(1);
		} catch (CsvValidationException e) {
			e.printStackTrace();
		}
	}

	private static void attributeHeaders(String[] headers) {

		//Match headers to numbers
		for (int id = 0; id < headers.length; id++) {

			switch(headers[id]) {

			case "decimalLatitude":
				latID = id;
				continue;

			case "decimalLongitude":
				longID = id;
				continue;

			case "acceptedNameUsage":
				acceptedNameUsageID = id;
				continue;

			case "kingdom":
				kingdomID = id;
				continue;

			case "phylum":
				phylumID = id;
				continue;

			case "class":
				classID = id;
				continue;

			case "order":
				orderID = id;
				continue;

			case "family":
				familyID = id;
				continue;

			case "genus":
				genusID = id;
				continue;

			case "specificEpithet":
				specificEpithetID = id;
				continue;

			case "infraspecificEpithet":
				infraspecificEpithetID = id;
				continue;

			case "taxonRank":
				taxonRankID = id;
				continue;

			case "country":
				countryID = id;
				continue;

			case "eventDate":
				dateID = id;
				continue;

			case "scientificName":
				scientificNameID =id;
				continue;

			default:
				continue;

			}
		}		
	}

}
