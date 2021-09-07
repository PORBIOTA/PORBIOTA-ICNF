package helpers;


import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.HashMap;
import java.util.Optional;
import java.util.Properties;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mapdb.DB;
import org.mapdb.DBException;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

/**
 * 
 * @author Tomé Neves de Matos
 *
 * Main class that deals with calling the OpenStreetMaps API.
 *
 * @param args 0 - Input File, 1 - Output File
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

	public void extract(String inputPath, String outputPath,DB db, HTreeMap<String, JSONObject>  map) throws IOException {


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

			attributeHeaders(csvReader.readNext());


			int counter = 1;

			//Creates a hashmap that stores coordinates already fetched
			JSONObject obj = null;
			JSONParser parser = null;


			//Opens writer
			String fileName = outputPath;
			Path myPath = Paths.get(fileName);
			try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(myPath.toString()), "UTF-8"))) {


				//Writes header line
				String[] line = {"decimalLatitude","decimalLongitude","hamlet","borough","village","city_district","town","city","county","municipality","province","state","archipelago","country","countryCode","DwC_locality","DwC_municipality","DwC_county","DwC_stateProvince","DwC_islandGroup"};
				writer.writeNext(line);

				while ((occ = csvReader.readNext()) != null) {
					if(occ.length <2) {
						break;
					}
					//Zero outs line to avoid conflicts
					line = new String[line.length];

					//Shows progress
					counter++;

					///Gets the latitude and longitude

					String lat = df.format(Double.parseDouble(occ[latID]));
					String lon = df.format(Double.parseDouble(occ[longID]));
					String latlon = lat + lon;

					//If the set of coordinates was not previously fetched from the API, it does so
					if(!map.containsKey(latlon)) {
						String loc = "https://nominatim.openstreetmap.org/reverse?lat="+URLEncoder.encode(lat, "UTF-8")+"&lon="+URLEncoder.encode(lon, "UTF-8")+"&format=json&accept-language=en";
						URL url = new URL(loc);


						// read from the URL
						Scanner scan = new Scanner(url.openStream(),"utf-8");
						String str = new String();
						while (scan.hasNext())
							str += scan.nextLine();
						scan.close();

						// build a JSON object
						parser = new JSONParser();		
						try {
							obj = (JSONObject) parser.parse(str);
						} catch (ParseException e) {

							e.printStackTrace();
						} 	

						//Add the JSON object to the hashmap with the coordinates as key 
						map.put(latlon, obj);


					}

					//Fetches the JSON information and attributes it to the correct columns


					JSONObject address = (JSONObject) map.get(latlon).get("address");



					line[0] = lat;
					line[1] = lon;
					String countryCode;
					if (address != null ) {		
						line[2] = (String) Optional.ofNullable(address.get("hamlet")).orElse("");
						line[3] = (String) Optional.ofNullable(address.get("borough")).orElse("");
						line[4] = (String) Optional.ofNullable(address.get("village")).orElse("");
						line[5] = (String) Optional.ofNullable(address.get("city_district")).orElse("");
						line[6] = (String) Optional.ofNullable(address.get("town")).orElse("");
						line[7] = (String) Optional.ofNullable(address.get("city")).orElse("");
						line[8] = (String) Optional.ofNullable(address.get("county")).orElse("");
						line[9] = (String) Optional.ofNullable(address.get("municipality")).orElse("");
						line[10] = (String) Optional.ofNullable(address.get("province")).orElse("");
						line[11] = (String) Optional.ofNullable(address.get("state")).orElse("");
						line[12] = (String) Optional.ofNullable(address.get("archipelago")).orElse("");
						line[13] = (String) Optional.ofNullable(address.get("country")).orElse("");
						countryCode = (String) Optional.ofNullable(address.get("country_code")).orElse("");
						line[14] = countryCode.toUpperCase();
						
						
						//Fills DwC terms depending on the country
						switch (line[13]) {
						case "Portugal":
							
							//municipality = municipality, then city, then town
							if (!line[9].contentEquals("")) {
								line[16] = line[9];
							} else if (!line[7].contentEquals("")){
								line[16] = line[7];
							} else {
								line[16] = line[6];
							}		
							
							//locality = hamlet, then village, then city_district, then town, then city
							if (!line[2].contentEquals("")) {
								line[15] = line[2];
							} else if (!line[4].contentEquals("")){
								line[15] = line[4];									
							} else if (!line[6].contentEquals("")){
								line[15] = line[6];
							} else if (!line[5].contentEquals("")){
								line[15] = line[5];
							}  else {
								line[15] = line[7];
							}							

							//islandGroup = Archipelago
							line[19] = line[12]; 		

							//stateProvince = county
							line[18] = line[8];								

								
							
							
							break;
						case "Spain":
							//stateProvince = state
							line[18] = line[11];
							
							//county = county or province
							if (!line[8].contentEquals("")) {
								line[17] = line[8];
							} else {
								line[17] = line[10];
							}
							
							//municipality = municipality, then city, then town, then village
							if (!line[9].contentEquals("")) {
								line[16] = line[9];
							} else if (!line[7].contentEquals("")){
								line[16] = line[7];
							} else if (!line[6].contentEquals("")){
								line[16] = line[6];
							} else {
								line[16] = line[4];
							}
								
							//locality = hamlet, then borough, then village, then town, then city
							if (!line[2].contentEquals("")) {
								line[15] = line[2];
							} else if (!line[3].contentEquals("")){
								line[15] = line[3];
							} else if (!line[4].contentEquals("")){
								line[15] = line[4];
							} else if (!line[6].contentEquals("")){
								line[15] = line[6];
							} else {
								line[15] = line[7];
							}
							
							//islandGroup = Archipelago
							line[18] = line[12]; 	
							
							break;
						default:
							break;
						}

					}

					

					

					writer.writeNext(line);

					if (counter%100 == 0) {
						//persist changes on disk
						db.commit();
						writer.flush();
						System.out.println("Working on line " + counter);
					}
				}


				//close to protect from data corruption
				db.commit();
				//db.close();


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
