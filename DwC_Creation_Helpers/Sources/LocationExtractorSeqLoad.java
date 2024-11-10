package webSolo;


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
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mapdb.DB;
import org.mapdb.HTreeMap;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

/**
 * 
 * Class that deals with calling the OpenStreetMaps API.
 * 
 * @author Tomé Neves de Matos
 * 
 */

public class LocationExtractorSeqLoad {

	private  int latID=0;
	private  int longID=0;			 
	private  int scientificNameID=0;
	private  int acceptedNameUsageID=0;
	private  int kingdomID=0;				
	private  int phylumID=0;
	private  int classID=0;				
	private  int orderID=0;				
	private  int familyID=0;				
	private  int genusID=0;
	private  int specificEpithetID=0;				
	private  int infraspecificEpithetID=0;				
	private  int taxonRankID=0;			
	private  int countryID=0;					
	private  int dateID=0;	

	public LocationExtractorSeqLoad() {

	}
	
	/**
	 * Main method for testing or independent usage
	 * @param args 	0 - Input File, 1 - Output File
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
	(new LocationExtractorSeqLoad()).extract(args[0], args[1],null,null, false);

	}

	/**
	 * Reads a .csv containg two columns named decimalLatitude and decimalLongitude and cross references it with OpenStreetMaps, outputing a .csv with the results
	 * @param inputPath String The path to the .csv
	 * @param outputPath String The path to store the output .csv
	 * @param db DB The database where the obtained results are stored, , can be set null along with map
	 * @param map HTreeMap<String, JSONObject[]> The TreeMap of the database, can be set null along with db
	 * @param append boolean true - appends the results to the original .csv; false - creates a .csv solely with the results
	 * @throws IOException
	 */
	public void extract(String inputPath, String outputPath,DB db, HTreeMap<String, JSONObject>  map, boolean append) throws IOException {


		HashMap<String, JSONObject> mapNoDB = null;
		if(db == null) { //This call, along with elsewhere on the code permits the usage when ran through the main method, independent of a database
			mapNoDB = new HashMap<String, JSONObject>();
		}
		
		//Reads the input file
		CSVReader csvReader;
		String originalTablePath = inputPath;
		DecimalFormat df = new DecimalFormat("#.0000000");

		try {
			Reader originalReader = Files.newBufferedReader(Paths.get(originalTablePath), StandardCharsets.UTF_8 );
			csvReader = new CSVReader(originalReader);


			// Reading Rows One by One in a String array
			String[] occ;

			//get headers
			String[] header = csvReader.readNext();

			attributeHeaders(header);


			int counter = 1;

			//Creates a hashmap that stores coordinates already fetched
			JSONObject obj = null;
			JSONParser parser = null;


			//Opens writer
			String fileName = outputPath;
			Path myPath = Paths.get(fileName);
			try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(myPath.toString()), "UTF-8"))) {


				String[] headerToWrite = {"decimalLatitude","decimalLongitude","hamlet","borough","village","city_district","town","city","county","municipality","province","state","archipelago","country","countryCode","DwC_locality","DwC_municipality","DwC_county","DwC_stateProvince","DwC_islandGroup"};
				String[] headerToAppend = {"hamlet","borough","village","city_district","town","city","county","municipality","province","state","archipelago","country","countryCode","DwC_locality","DwC_municipality","DwC_county","DwC_stateProvince","DwC_islandGroup"};
				int dif = 0; //difference between the two headers, only declared if append

				//Writes header line
				if (append) {
					dif = 2; //difference between the two headers

					//Writes header line
					headerToWrite = Arrays.copyOf(header, headerToAppend.length+header.length);
					System.arraycopy(headerToAppend, 0, headerToWrite, header.length, headerToAppend.length);
				}

				writer.writeNext(headerToWrite);


				while ((occ = csvReader.readNext()) != null) {
					if(occ.length <2) {
						break;
					}
					//Zero outs line to avoid conflicts
					String[] line = new String[headerToAppend.length+2-dif];

					//Shows progress
					counter++;

					///Gets the latitude and longitude

					String lat = df.format(Double.parseDouble(occ[latID]));
					String lon = df.format(Double.parseDouble(occ[longID]));
					String latlon = lat + lon;

					//If the set of coordinates was not previously fetched from the API, it does so
					if((map == null  || !map.containsKey(latlon) && (mapNoDB == null ||!mapNoDB.containsKey(latlon)))) {
						String loc = "https://nominatim.openstreetmap.org/reverse?lat="+URLEncoder.encode(lat, "UTF-8")+"&lon="+URLEncoder.encode(lon, "UTF-8")+"&format=json&accept-language=en";
						URL url = new URL(loc);


						// read from the URL
						Scanner scan = new Scanner(url.openStream(),"utf-8");
						String str = new String();
						while (scan.hasNext())
							str += scan.nextLine();
						scan.close();
						
						System.out.println(loc);
						
						// build a JSON object
						parser = new JSONParser();		
						try {
							obj = (JSONObject) parser.parse(str);
						} catch (ParseException e) {

							e.printStackTrace();
						} 	

						//Add the JSON object to the hashmap with the coordinates as key 
						if (db !=null) {
							map.put(latlon, obj);
						} else {
							mapNoDB.put(latlon, obj);
						}


					}

					//Fetches the JSON information and attributes it to the correct columns


					JSONObject address = null;
					if (db !=null) {
						address = (JSONObject) map.get(latlon).get("address");
					} else {
						address = (JSONObject) mapNoDB.get(latlon).get("address");
					}


					if (!append) {
						line[0] = lat;
						line[1] = lon;
					}
					String countryCode;
					if (address != null ) {		
						line[2-dif] = (String) Optional.ofNullable(address.get("hamlet")).orElse("");
						line[3-dif] = (String) Optional.ofNullable(address.get("borough")).orElse("");
						line[4-dif] = (String) Optional.ofNullable(address.get("village")).orElse("");
						line[5-dif] = (String) Optional.ofNullable(address.get("city_district")).orElse("");
						line[6-dif] = (String) Optional.ofNullable(address.get("town")).orElse("");
						line[7-dif] = (String) Optional.ofNullable(address.get("city")).orElse("");
						line[8-dif] = (String) Optional.ofNullable(address.get("county")).orElse("");
						line[9-dif] = (String) Optional.ofNullable(address.get("municipality")).orElse("");
						line[10-dif] = (String) Optional.ofNullable(address.get("province")).orElse("");
						line[11-dif] = (String) Optional.ofNullable(address.get("state")).orElse("");
						line[12-dif] = (String) Optional.ofNullable(address.get("archipelago")).orElse("");
						line[13-dif] = (String) Optional.ofNullable(address.get("country")).orElse("");
						countryCode = (String) Optional.ofNullable(address.get("country_code")).orElse("");
						line[14-dif] = countryCode.toUpperCase();


						//Fills DwC terms depending on the country
						switch (line[13-dif]) {
						case "Portugal":

							//municipality = municipality, then city, then town
							if (!line[9-dif].contentEquals("")) {
								line[16-dif] = line[9-dif];
							} else if (!line[7-dif].contentEquals("")){
								line[16-dif] = line[7-dif];
							} else {
								line[16-dif] = line[6-dif];
							}		

							//locality = hamlet, then village, then city_district, then town, then city
							if (!line[2-dif].contentEquals("")) {
								line[15-dif] = line[2-dif];
							} else if (!line[4-dif].contentEquals("")){
								line[15-dif] = line[4-dif];									
							} else if (!line[6-dif].contentEquals("")){
								line[15-dif] = line[6-dif];
							} else if (!line[5-dif].contentEquals("")){
								line[15-dif] = line[5-dif];
							}  else {
								line[15-dif] = line[7-dif];
							}							

							//islandGroup = Archipelago
							line[19-dif] = line[12-dif]; 		

							//stateProvince = county
							line[18-dif] = line[8-dif];								




							break;
						case "Spain":
							//stateProvince = state
							line[18-dif] = line[11-dif];

							//county = county or province
							if (!line[8-dif].contentEquals("")) {
								line[17-dif] = line[8-dif];
							} else {
								line[17-dif] = line[10-dif];
							}

							//municipality = municipality, then city, then town, then village
							if (!line[9-dif].contentEquals("")) {
								line[16-dif] = line[9-dif];
							} else if (!line[7-dif].contentEquals("")){
								line[16-dif] = line[7-dif];
							} else if (!line[6-dif].contentEquals("")){
								line[16-dif] = line[6-dif];
							} else {
								line[16-dif] = line[4-dif];
							}

							//locality = hamlet, then borough, then village, then town, then city
							if (!line[2-dif].contentEquals("")) {
								line[15-dif] = line[2-dif];
							} else if (!line[3-dif].contentEquals("")){
								line[15-dif] = line[3-dif];
							} else if (!line[4-dif].contentEquals("")){
								line[15-dif] = line[4-dif];
							} else if (!line[6-dif].contentEquals("")){
								line[15-dif] = line[6-dif];
							} else {
								line[15-dif] = line[7-dif];
							}

							//islandGroup = Archipelago
							line[18-dif] = line[12-dif]; 	

							break;
						default:
							break;
						}

					}

					if (append) {
						String[] appendToWrite;
						appendToWrite = Arrays.copyOf(occ, occ.length+line.length);
						System.arraycopy(line, 0, appendToWrite, occ.length, line.length);
						writer.writeNext(appendToWrite);
					} else {
						writer.writeNext(line);
					}


					if (counter%100 == 0) {
						//persist changes on disk
						db.commit();
						writer.flush();
						System.out.println("Working on line " + counter);
					}
				}


				//close to protect from data corruption
				if (db != null) {
					db.commit();
				}


				System.out.println("Complete!");


			} catch (IOException e) {			
				System.out.println("Output file is not accessible!");
				throw e;
			} finally {
				csvReader.close();
			}
			///Counts Species and etc
		} catch (IOException e) {			
			System.out.println("There is no input file!");
			throw e;
		}
		catch (RuntimeException e) {
			System.out.println("Either the .csv is not in UTF-8 or its delimiters are not commas(,)\n\n");			
			throw e;
		} catch (CsvValidationException e1) {
			e1.printStackTrace();
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