package helpers;


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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mapdb.DB;
import org.mapdb.HTreeMap;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;



public class SpeciesExtractorSeqLoad {

	private int latID=-1;
	private int longID=-1;			 
	private int scientificNameID=-1;
	private int acceptedNameUsageID=-1;
	private int kingdomID=-1;				
	private int phylumID=-1;
	private int classID=-1;				
	private int orderID=-1;				
	private int familyID=-1;				
	private int genusID=-1;
	private int specificEpithetID=-1;				
	private int infraspecificEpithetID=-1;				
	private int taxonRankID=-1;			
	private int countryID=-1;					
	private int dateID=-1;	


	public SpeciesExtractorSeqLoad() {

	}


	/**
	 * Main method for testing or independent usage
	 * @param args 	0 - Kingdom, 1 - Input File, 2 - Output File
	 * @throws IOException
	 */

	public static void main(String[] args) throws IOException {
		(new SpeciesExtractorSeqLoad()).extract(args[1], args[2], args[0],null, null, false, true, true);

	}


	/**
	 * Reads a .csv containg a column named scientificName and cross references it with the GBIF database, outputing a .csv with the results
	 * @param inputPath String The path to the .csv
	 * @param outputPath String The path to store the output .csv
	 * @param kingdom String Kingdom of the species being searched, "" for multiple
	 * @param db DB The database where the obtained results are stored, , can be set null along with speciesJSONs
	 * @param speciesJSONs HTreeMap<String, JSONObject[]> The TreeMap of the database, can be set null along with db
	 * @param append boolean true - appends the results to the original .csv; false - creates a .csv solely with the results
	 * @param newOrOld boolean  true new species' names , false original species' names
	 * @param acceptedAuthorName true - Fetches the author names from gbif, false - Fetches the author names from the given species name
	 * @throws IOException
	 */
	public void extract(String inputPath, String outputPath, String kingdom,DB db, HTreeMap<String, JSONObject[]>  speciesJSONs, boolean append, boolean newOrOld, boolean acceptedAuthorName) throws IOException {

		
		HashMap<String, JSONObject[]> speciesJSONsNoDB = null;
		if(db == null) { //This call, along with elsewhere on the code permits the usage when ran through the main method, independent of a database
			speciesJSONsNoDB = new HashMap<String, JSONObject[]>();
		}

		//Reads the input file
		CSVReader csvReader;
		String originalTablePath = inputPath;
		try {
			Reader originalReader = Files.newBufferedReader(Paths.get(originalTablePath), StandardCharsets.UTF_8 );
			csvReader = new CSVReader(originalReader);


			// Reading Rows One by One in a String array
			String[] occ;

			//get headers
			String[] header = csvReader.readNext();

			attributeHeaders(header);

			int counter = 1;

			//Creates a hashmap that stores species already fetched
			String kingdomOfSpecies;
			if (kingdom == null) {
				kingdomOfSpecies = "";
			} else {
				kingdomOfSpecies = kingdom;
			}

			JSONObject[] obj = null;
			JSONParser parser = null;
			JSONObject[] tempJSON = null;


			//Opens writer
			String fileName = outputPath;
			Path myPath = Paths.get(fileName);
			try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(myPath.toString()), "UTF-8"))) {


				String[] headerToWrite = {"originalSientificName","scientificName","acceptedNameUsage","kingdom","phylum","class","order","family","genus",	"specificEpithet","infraspecificEpithet","taxonRank","scientificNameAuthorship","confidence","matchType","occurrenceID"};
				String[] headerToAppend = {"scientificName","acceptedNameUsage","kingdom","phylum","class","order","family","genus","specificEpithet","infraspecificEpithet","taxonRank","scientificNameAuthorship","confidence","matchType","status","occurrenceID"};

				//Writes header line
				if (append) {
					header[scientificNameID] = "originalScientificName";
					//Writes header line
					headerToWrite = Arrays.copyOf(header, headerToAppend.length+header.length);
					System.arraycopy(headerToAppend, 0, headerToWrite, header.length, headerToAppend.length);
				}

				writer.writeNext(headerToWrite);


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
				String statusCurrent;


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
					statusCurrent = "";


					counter++;
					obj = new JSONObject[2];
					tempJSON = null;


					occurenceIDCurrent = UUID.randomUUID().toString();
					try {
						scientificNameCurrent =	occ[scientificNameID].replaceAll("( sp.)$", "").replaceAll("( spp.)$", "").replaceAll("( spp)$","");
					} catch (ArrayIndexOutOfBoundsException e ) {
						System.out.println("No scientificName header found!");
						throw e;						
					}

					String JSONkey = Boolean.toString(newOrOld)+ kingdomOfSpecies+occ[scientificNameID];
					if ((speciesJSONs == null || !speciesJSONs.containsKey(JSONkey)) && (speciesJSONsNoDB == null || !speciesJSONsNoDB.containsKey(JSONkey))) {	
						String s = "https://api.gbif.org/v1/species/match?verbose=true&kingdom="+URLEncoder.encode(kingdomOfSpecies, "UTF-8")+"&name=";
						s += URLEncoder.encode(occ[scientificNameID], "UTF-8");
						System.out.println(s);
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
						if (((String) obj[0].get("matchType")).equalsIgnoreCase("none") || ((String) obj[0].get("rank")).equalsIgnoreCase("kingdom")) {			
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
										"",
										occurenceIDCurrent};

								if (append) {
									String[] appendToWrite;
									appendToWrite = Arrays.copyOf(occ, occ.length+line.length);
									System.arraycopy(line, 0, appendToWrite, occ.length, line.length);
									writer.writeNext(appendToWrite);
								} else {
									writer.writeNext(line);
								}

								continue;
							} else {
								obj[0] = (JSONObject) ((JSONArray) obj[0].get("alternatives")).get(0);
							}
						}


						//If synonym get new name
						if (newOrOld && ((String) obj[0].get("rank")).equalsIgnoreCase("Species") || ((String) obj[0].get("rank")).equalsIgnoreCase("Subspecies")) {

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
						if (db != null) {
							speciesJSONs.put(JSONkey,obj.clone());
						} else {
							speciesJSONsNoDB.put(JSONkey,obj.clone());
						}

					}


					//Fetch original or synonym
					int apiCall = 0;

					JSONObject[] currentJSON = null;
					if (db != null) {
						if (speciesJSONs.get(JSONkey)[1] != null) {
							apiCall = 1;
						} 
						currentJSON = speciesJSONs.get(JSONkey);
					} else {
						if (speciesJSONsNoDB.get(JSONkey)[1] != null) {
							apiCall = 1;
						} 
						currentJSON = speciesJSONsNoDB.get(JSONkey);
					}



					String speciesName = scientificNameCurrent;


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

					if(speciesName.trim().contains(" ")){						
						String[] speciesExploded = speciesName.trim().split(" ");
						speciesEpithet = speciesExploded[1];
						try {
							if (((String) currentJSON[0].get("rank")).equalsIgnoreCase("SUBSPECIES") || ((String) currentJSON[0].get("rank")).equalsIgnoreCase("VARIETY")) {
								for (int s = 0 ; s < speciesExploded.length; s++) {
									if (speciesExploded[s].contentEquals("v.") ||speciesExploded[s].contentEquals("var.") || speciesExploded[s].contentEquals("subsp.") || speciesExploded[s].contentEquals("ssp.")) {
										speciesInfraEpithet = speciesExploded[s+1];
										break;
									}
								}
								if (speciesInfraEpithet.contentEquals("")) {
									speciesInfraEpithet = speciesExploded[2];
								}
							}

						} catch (NullPointerException e) {

						}
					}


					//Define scientific name
					specificEpithetCurrent = speciesEpithet;
					infraspecificEpithetCurrent = speciesInfraEpithet;

					//Captures author names
					if (acceptedAuthorName) {

						scientificNameAuthorshipCurrent = (String) currentJSON[apiCall].get("scientificName");

						// Try block is to catch species without author
						try {	
							if ("subspecies".equalsIgnoreCase((String) currentJSON[apiCall].get("rank"))) {
								if (scientificNameAuthorshipCurrent.contains("var. ")) {					
									scientificNameAuthorshipCurrent = scientificNameAuthorshipCurrent.substring(scientificNameAuthorshipCurrent.indexOf(" ", scientificNameAuthorshipCurrent.indexOf("var. ")+5)).trim();
								} else if (scientificNameAuthorshipCurrent.contains("subsp. ")) {
									scientificNameAuthorshipCurrent = scientificNameAuthorshipCurrent.substring(scientificNameAuthorshipCurrent.indexOf(" ", scientificNameAuthorshipCurrent.indexOf("subsp. ")+7)).trim();
								} else {
									//gets the string after the third space (" ")
									scientificNameAuthorshipCurrent = scientificNameAuthorshipCurrent.substring(scientificNameAuthorshipCurrent.indexOf(" ", scientificNameAuthorshipCurrent.indexOf(" ",scientificNameAuthorshipCurrent.indexOf(" ")+1)+1)).trim();						}
							} else if ("species".equalsIgnoreCase((String) currentJSON[apiCall].get("rank"))){
								//gets the string after the second  space (" ")
								scientificNameAuthorshipCurrent = scientificNameAuthorshipCurrent.substring(scientificNameAuthorshipCurrent.indexOf(" ", scientificNameAuthorshipCurrent.indexOf(" ")+1)).trim();
							} else if ("genus".equalsIgnoreCase((String) currentJSON[apiCall].get("rank"))){
								//gets the string after the the first space (" ")
								scientificNameAuthorshipCurrent = scientificNameAuthorshipCurrent.substring(scientificNameAuthorshipCurrent.indexOf(" ")).trim();
							}
						} catch (StringIndexOutOfBoundsException e) {	
							//To fetch author name in strange situations where the subspecies does not have one because it is equal to the species' one
							if ("subspecies".equalsIgnoreCase((String) currentJSON[apiCall].get("rank")) && infraspecificEpithetCurrent.equalsIgnoreCase(specificEpithetCurrent)) {
								if ((speciesJSONs == null || !speciesJSONs.containsKey(kingdomOfSpecies + speciesGenus + " " + specificEpithetCurrent))
										&& (speciesJSONsNoDB == null||!speciesJSONsNoDB.containsKey(kingdomOfSpecies + speciesGenus + " " + specificEpithetCurrent))) {	

									String s = "https://api.gbif.org/v1/species/match?verbose=true&kingdom="+URLEncoder.encode(kingdomOfSpecies, "UTF-8")+"&name=";
									s += URLEncoder.encode(speciesGenus + " " + specificEpithetCurrent, "UTF-8");
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
									} catch (ParseException er) {

										e.printStackTrace();
									} 	


									//0 will be original call, 1 will be the call for the synonym species
									if (db !=null) {
										speciesJSONs.put(kingdomOfSpecies + speciesGenus + " " + specificEpithetCurrent,obj.clone());
									} else {
										speciesJSONsNoDB.put(kingdomOfSpecies + speciesGenus + " " + specificEpithetCurrent,obj.clone());

									}

								}

								tempJSON = speciesJSONs.get(kingdomOfSpecies + speciesGenus + " " + specificEpithetCurrent);
								scientificNameAuthorshipCurrent = (String) tempJSON[0].get("scientificName");
								try {
									//gets the string after the second  space (" ")
									scientificNameAuthorshipCurrent = scientificNameAuthorshipCurrent.substring(scientificNameAuthorshipCurrent.indexOf(" ", scientificNameAuthorshipCurrent.indexOf(" ")+1)).trim();
								} catch(StringIndexOutOfBoundsException er) {
									scientificNameAuthorshipCurrent = "";
								}
							} else {						
								scientificNameAuthorshipCurrent = "";
							}
						}

						//If grabbing author name from the original text
					} else {
						scientificNameAuthorshipCurrent = occ[scientificNameID];

						// Try block is to catch species without author
						try {	
							if ("subspecies".equalsIgnoreCase((String) currentJSON[0].get("rank"))) {
								if (scientificNameAuthorshipCurrent.contains("var. ")) {					
									scientificNameAuthorshipCurrent = scientificNameAuthorshipCurrent.substring(scientificNameAuthorshipCurrent.indexOf(" ", scientificNameAuthorshipCurrent.indexOf("var. ")+5)).trim();
								} else if (scientificNameAuthorshipCurrent.contains("subsp. ")) {
									scientificNameAuthorshipCurrent = scientificNameAuthorshipCurrent.substring(scientificNameAuthorshipCurrent.indexOf(" ", scientificNameAuthorshipCurrent.indexOf("subsp. ")+7)).trim();
								} else {
									//gets the string after the third space (" ")
									scientificNameAuthorshipCurrent = scientificNameAuthorshipCurrent.substring(scientificNameAuthorshipCurrent.indexOf(" ", scientificNameAuthorshipCurrent.indexOf(" ",scientificNameAuthorshipCurrent.indexOf(" ")+1)+1)).trim();						}
							} else if ("species".equalsIgnoreCase((String) currentJSON[apiCall].get("rank"))){
								//gets the string after the second  space (" ")
								scientificNameAuthorshipCurrent = scientificNameAuthorshipCurrent.substring(scientificNameAuthorshipCurrent.indexOf(" ", scientificNameAuthorshipCurrent.indexOf(" ")+1)).trim();
							} else if ("genus".equalsIgnoreCase((String) currentJSON[apiCall].get("rank"))){
								//gets the string after the the first space (" ")
								scientificNameAuthorshipCurrent = scientificNameAuthorshipCurrent.substring(scientificNameAuthorshipCurrent.indexOf(" ")).trim();
							}
						} catch (StringIndexOutOfBoundsException e) {	
							scientificNameAuthorshipCurrent = "";
						}
					}



					//Fixes strange matching with non matching items by making sure Kingdom matches come up empty in all taxonomic fields

					if (((String) currentJSON[0].get("rank")).equalsIgnoreCase("KINGDOM")) {
						acceptedNameUsageCurrent = ((String) currentJSON[0].get("scientificName"));
						kingdomCurrent = ((String) currentJSON[apiCall].get("kingdom"));
						specificEpithetCurrent = "";
						infraspecificEpithetCurrent = "";

					}else {

						//Sets the various taxonomic fields
						acceptedNameUsageCurrent = ((String) currentJSON[apiCall].get("scientificName"));						
						//Adds author name when needed
						if (tempJSON != null) {
							acceptedNameUsageCurrent += " " + scientificNameAuthorshipCurrent;
						}

						confidenceCurrent = ((long) currentJSON[0].get("confidence"));

						try {
							taxonRankCurrent = ((String) currentJSON[apiCall].get("rank"));
						} catch (NullPointerException e) {
						}

						try {
							kingdomCurrent = ((String) currentJSON[apiCall].get("kingdom"));
						} catch (NullPointerException e) {
						}
						try {
							phylumCurrent = ((String) currentJSON[apiCall].get("phylum"));
						} catch (NullPointerException e) {
						}
						try {
							classCurrent = ((String) currentJSON[apiCall].get("class"));
						} catch (NullPointerException e) {
						}
						try {
							orderCurrent = ((String) currentJSON[apiCall].get("order"));
						} catch (NullPointerException e) {
						}
						try {
							familyCurrent = ((String) currentJSON[apiCall].get("family"));	
						} catch (NullPointerException e) {
						}


						genusCurrent = speciesGenus;		


						try {
							matchTypeCurrent = ((String) currentJSON[apiCall].get("matchType"));		
						} catch (NullPointerException e) {
						}

						try {
							statusCurrent = ((String) currentJSON[0].get("status"));		
						} catch (NullPointerException e) {
						}

					}

					///WRITER
					if (append) {
						String[] line = 	{occ[scientificNameID].replace(" spp.", ""),
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
								statusCurrent,
								occurenceIDCurrent};

						String[] appendToWrite;
						appendToWrite = Arrays.copyOf(occ, occ.length+line.length);
						System.arraycopy(line, 0, appendToWrite, occ.length, line.length);
						writer.writeNext(appendToWrite);

					} else {
						String[] line = 	{occ[scientificNameID],
								occ[scientificNameID].replace(" spp.", ""),
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



					if (counter%100 == 0) {
						//persist changes on disk
						db.commit();
						writer.flush();
						System.out.println("Working on line " + counter);					
					}

				}
				//No need to close as CSVWriter auto-closes
				System.out.println("Complete!");
				if (db !=null) {
					db.commit();
				}

			} catch (IOException e) {
				System.out.println("There is no output file or it is not accessible!");
				throw e;
			} catch (CsvValidationException e) {			
				e.printStackTrace();
			} finally {
				csvReader.close();
			}

		} catch (IOException e) {		

			System.out.println("There is no input file!");
			throw e;
		}
		catch (RuntimeException e) {
			System.out.println("Either the .csv is not in UTF-8 or its delimiters are not commas(,)");
			e.printStackTrace();
			throw e;
		} catch (CsvValidationException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Matches the columns to the correct variables
	 * @param headers String[] An array of column names to be attributed to the different variables used
	 */

	private void attributeHeaders(String[] headers) {

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
