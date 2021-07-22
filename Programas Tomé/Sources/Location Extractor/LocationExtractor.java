package csvEditorFullLoad;


import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * 
 * @author Tomé
 *
 * Main class that deals with calling the OpenStreetMaps API.
 *
 * @param args 0 - Input File, 1 - Output File
 *
 */

public class LocationExtractor {

	public static void main(String[] args) throws IOException {


		//Reads the input file and binds the data to beans
		String fileName = args[0];
		Path myPath = Paths.get(fileName);

		List<OccurenceForLocation> occurences = null;

		try (BufferedReader br = Files.newBufferedReader(myPath,StandardCharsets.UTF_8)) {

			HeaderColumnNameMappingStrategy<OccurenceForLocation> strategy
			= new HeaderColumnNameMappingStrategy<>();
			strategy.setType(OccurenceForLocation.class);

			CsvToBean<OccurenceForLocation> csvToBean = new CsvToBeanBuilder<OccurenceForLocation>(br)
					.withMappingStrategy(strategy)
					.withIgnoreLeadingWhiteSpace(true)
					.build();

			occurences = csvToBean.parse();

		} catch (IOException e) {
			System.out.println("There is no input file!");
			System.exit(1);
		}


		//WORKING THE DATA

		//Creates an iterator to go through all occurences
		ListIterator<OccurenceForLocation> occurenceIterator = occurences.listIterator();	
		
		//Creates a hashmap that stores coordinates already fetched
		HashMap<String, JSONObject> occurenceLocations = new HashMap<String, JSONObject>();
		
		JSONObject obj = null;
		JSONParser parser = null;

		int counter = 1;

		while (occurenceIterator.hasNext()) {

			//Shows progress
			System.out.println("Working on line " + counter);
			counter++;

			OccurenceForLocation occ = occurenceIterator.next();

			///Gets the latitude and longitude
			String lat = occ.getLat();
			String lon = occ.getLong();
			String latlon = lat + lon;

			
			//Tries to use the coordinates, but catches errors if they do not exist or if OpenStreetMaps does not map them.
			try {

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

				occ.setVillage((String) address.get("village"));
				occ.setCityDistrict((String) address.get("city_district"));
				occ.setCounty((String) address.get("county"));
				occ.setMunicipality((String) address.get("municipality"));
				occ.setState((String) address.get("state"));
				occ.setCountry((String) address.get("country"));
				occ.setTown((String) address.get("town"));
				occ.setCountry_code((String) address.get("country_code"));


			} catch (IOException|NullPointerException e) {
				System.out.println("No coordinates.");
			}

		}

		///Writes the output csv

		fileName = args[1];
		myPath = Paths.get(fileName);

		try (BufferedWriter writer = Files.newBufferedWriter(myPath, StandardCharsets.UTF_8)) {

			CustomBeanToCSVMappingStrategy<OccurenceForLocation> mappingStrategy = new CustomBeanToCSVMappingStrategy<>();
			mappingStrategy.setType(OccurenceForLocation.class);

			StatefulBeanToCsv<OccurenceForLocation> beanToCsv = new StatefulBeanToCsvBuilder<OccurenceForLocation>(writer)
					.withSeparator(CSVWriter.DEFAULT_SEPARATOR)
					.withMappingStrategy(mappingStrategy)
					.build();

			beanToCsv.write(occurences);

			writer.close();

		} catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException |
				IOException ex) {
		
			System.out.println("There is no output file or it is not accessible!");
			System.exit(1);	

			Logger.getLogger(LocationExtractor.class.getName()).log(
					Level.SEVERE, ex.getMessage(), ex);
		}


		System.out.println("Complete!");

	}

}