package csvEditorSequentialLoad;


import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
import java.util.Optional;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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

	private static int latID=0;
	private static int longID=0;			 
	private static int scientificNameID=0;
	private static int acceptedNameUsageID=0;
	private static int kingdomID=0;				
	private static int phylumID=0;
	private static int classID=0;				
	private static int orderID=0;				
	private static int familyID=0;				
	private static int genusID=0;
	private static int specificEpithetID=0;				
	private static int infraspecificEpithetID=0;				
	private static int taxonRankID=0;			
	private static int countryID=0;					
	private static int dateID=0;	

	public static void main(String[] args) throws IOException {


		//Reads the input file
		CSVReader csvReader;
		String originalTablePath = args[0];
		

		try {
			Reader originalReader = Files.newBufferedReader(Paths.get(originalTablePath), StandardCharsets.UTF_8 );
			csvReader = new CSVReader(originalReader);


			// Reading Rows One by One in a String array
			String[] occ;

			//get headers

			attributeHeaders(csvReader.readNext());


			int counter = 1;

			//Creates a hashmap that stores coordinates already fetched
			HashMap<String, JSONObject> occurenceLocations = new HashMap<String, JSONObject>();

			JSONObject obj = null;
			JSONParser parser = null;


			//Opens writer
			String fileName = args[1];
			Path myPath = Paths.get(fileName);
			try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(myPath.toString()), "UTF-8"))) {

				
				//Writes header line
				String[] line = {"decimalLatitude","decimalLongitude","village","city_district","town","county","municipality","state","country","countryCode"};
				writer.writeNext(line);

				while ((occ = csvReader.readNext()) != null) {
					
					//Zero outs line to avoid conflicts
					line = new String[line.length];

					//Shows progress
					System.out.println("Working on line " + counter);
					counter++;

					///Gets the latitude and longitude
					String lat = occ[latID];
					String lon = occ[longID];
					String latlon = lat + lon;

					//If the set of coordinates was not previously fetched from the API, it does so
					if(!occurenceLocations.containsKey(latlon)) {
						String loc = "https://nominatim.openstreetmap.org/reverse?lat="+URLEncoder.encode(lat, "UTF-8")+"&lon="+URLEncoder.encode(lon, "UTF-8")+"&format=json&zoom=12&accept-language=en";
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
						occurenceLocations.put(latlon, (JSONObject) obj.clone());

					}

					//Fetches the JSON information and attributes it to the correct columns
					JSONObject address = (JSONObject) occurenceLocations.get(latlon).get("address");

					
					line[0] = lat;
					line[1] = lon;
					String countryCode;
					if (address != null ) {		

						line[2] = (String) Optional.ofNullable(address.get("village")).orElse("");
						line[3] = (String) Optional.ofNullable(address.get("city_district")).orElse("");
						line[4] = (String) Optional.ofNullable(address.get("town")).orElse("");
						line[5] = (String) Optional.ofNullable(address.get("county")).orElse("");
						line[6] = (String) Optional.ofNullable(address.get("municipality")).orElse("");
						line[7] = (String) Optional.ofNullable(address.get("state")).orElse("");
						line[8] = (String) Optional.ofNullable(address.get("country")).orElse("");
						countryCode = (String) Optional.ofNullable(address.get("country_code")).orElse("");
						line[9] = countryCode.toUpperCase();
					}
					
					writer.writeNext(line);
					
					if (counter%100 == 0) {
						writer.flush();
					}
				}
				
				//No need to close as CSVWriter auto-closes
				System.out.println("Complete!");


			} catch (IOException e) {			
				System.out.println("Output file is not accessible!");
				System.exit(1);
			}
			///Counts Species and etc
		} catch (IOException e) {			
			System.out.println("There is no input file!");
			System.exit(1);
		}
		catch (RuntimeException e) {
			System.out.println("Either the .csv is not in UTF-8 or its delimiters are not commas(,)\n\n");			
			e.printStackTrace();
			System.exit(1);
		} catch (CsvValidationException e1) {
			e1.printStackTrace();
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
