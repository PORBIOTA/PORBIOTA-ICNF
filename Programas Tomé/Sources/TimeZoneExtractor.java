package timeZoneExtractor;


import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import net.iakovlev.timeshape.TimeZoneEngine;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Optional;

/**
 * 
 * @author Tomé Neves de Matos
 *
 * Main class that deals with calling the OpenStreetMaps API.
 *
 * @param args 0 - Input File, 1 - Output File
 *
 */

public class TimeZoneExtractor {

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


	public static void main(String[] args) throws IOException {


		//Reads the input file
		CSVReader csvReader;
		String originalTablePath = args[0];


		try {
			Reader originalReader = Files.newBufferedReader(Paths.get(originalTablePath), StandardCharsets.UTF_8 );
			csvReader = new CSVReader(originalReader);

			TimeZoneEngine engine = TimeZoneEngine.initialize();

			// Reading Rows One by One in a String array
			String[] occ;

			//get headers

			String[] headerline =csvReader.readNext();
			String[] line = new String[headerline.length+1];
			for (int a = 0; a < headerline.length; a++) {
				line[a] = headerline[a];
			}
			line[line.length-1] = "TimeZone";
			attributeHeaders(headerline);

			int counter = 1;


			//Creates a hashmap that stores coordinates already fetched
			HashMap<String, Optional<ZoneId>> occurenceLocations = new HashMap<String, Optional<ZoneId>>();


			//Opens writer
			String fileName = args[1];
			Path myPath = Paths.get(fileName);
			try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(myPath.toString()), "UTF-8"))) {


				//Writes header line
				writer.writeNext(line);
				String lat = "";
				String lon = "";
				String latlon = "";

				while ((occ = csvReader.readNext()) != null) {

					//Zero outs line to avoid conflicts
					line = new String[occ.length+1];
					for (int a = 0; a < occ.length; a++) {
						line[a] = occ[a];
					}

					//Shows progress
					System.out.println("Working on line " + counter);
					counter++;

					///Gets the latitude and longitude
					try {
					 lat = occ[latID];
					 lon = occ[longID];
					 latlon = lat + lon;
					} catch (ArrayIndexOutOfBoundsException e) {
						System.out.println("Could not find either decimalLatitude or decimalLongitude headers!");
						System.exit(1);						
					}

					//If the set of coordinates was not previously fetched from the API, it does so
					if(!occurenceLocations.containsKey(latlon)) {

						Optional<ZoneId> maybeZoneId = engine.query(Double.parseDouble(lat), Double.parseDouble(lon));
						//Add the JSON object to the hashmap with the coordinates as key 
						occurenceLocations.put(latlon, maybeZoneId);

					}

					//Fetches the JSON information and attributes it to the correct columns
					int year = 2000;
					int month = 1;
					int day =1 ;
					try {
						occ[dateID].toString();
					} catch (ArrayIndexOutOfBoundsException e) {
						System.out.println("No event date! Defaulting to January 1st.");
					}
							
					try {
						year = Integer.parseInt(occ[dateID].substring(0,4));
					} catch (ArrayIndexOutOfBoundsException e) {
						
					}
					try {
						month = Integer.parseInt(occ[dateID].substring(5,7));
					} catch (ArrayIndexOutOfBoundsException e) {

					}					
					try {
						day = Integer.parseInt(occ[dateID].substring(8,10));		
					} catch (ArrayIndexOutOfBoundsException e) {

					}


					ZoneId timeZone = ((Optional<ZoneId>) occurenceLocations.get(latlon)).get();

					LocalDateTime dt =  LocalDateTime.of(year, month, day, 1, 0, 0, 0);
					ZonedDateTime zdt = dt.atZone(timeZone);
					ZoneOffset offset = zdt.getOffset();

					line[line.length-1] = offset.toString();

					writer.writeNext(line);

					if (counter%100 == 0) {
						writer.flush();
					}
				}

				//No need to close as CSVWriter auto-closes
				System.out.println("Complete!");


			} catch (IOException e) {			
				System.out.println("Output file is not accessible!");
				e.printStackTrace();
				System.exit(1);
			}
			///Counts Species and etc
		} catch (IOException e) {			
			System.out.println("There is no input file!");
			e.printStackTrace();
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