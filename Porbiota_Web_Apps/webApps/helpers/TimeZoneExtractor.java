package helpers;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import net.iakovlev.timeshape.TimeZoneEngine;

/**
 *  
 *
 * Class to extract time zones based on the coordinates and date
 *
 * @author Tomé Neves de Matos
 *
 */

public class TimeZoneExtractor {

	private  int latID=-1;
	private  int longID=-1;			 
	private  int scientificNameID=-1;
	private  int acceptedNameUsageID=-1;
	private  int kingdomID=-1;				
	private  int phylumID=-1;
	private  int classID=-1;				
	private  int orderID=-1;				
	private  int familyID=-1;				
	private  int genusID=-1;
	private  int specificEpithetID=-1;				
	private  int infraspecificEpithetID=-1;				
	private  int taxonRankID=-1;			
	private  int countryID=-1;					
	private  int dateID=-1;



	/**
	 * Main method for testing or independent usage
	 * @param args 	0 - Input File, 1 - Output File
	 * @throws IOException
	 * @throws CsvValidationException 
	 */
	public static void main(String[] args) throws IOException, CsvValidationException {
		(new TimeZoneExtractor()).extract(args[0], args[1]);
	}

	/**
	 * Reads a .csv containg two columns named decimalLatitude and decimalLongitude (and possibly eventDate) and outputs a .csv with the time zone for each line
	 * @param inputPath String The path to the .csv
	 * @param outputPath String The path to store the output .csv
	 * @throws IOException
	 */

	public void extract(String input, String output) throws IOException, CsvValidationException {


		//Reads the input file
		CSVReader csvReader;
		String originalTablePath = input;


		try {
			Reader originalReader = Files.newBufferedReader(Paths.get(originalTablePath), StandardCharsets.UTF_8 );
			csvReader = new CSVReader(originalReader);
			System.out.println("Initializing Engine");
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

			//Opens writer
			String fileName = output;
			Path myPath = Paths.get(fileName);
			try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(myPath.toString()), "UTF-8"))) {


				//Writes header line
				writer.writeNext(line);
				String lat = "";
				String lon = "";

				while ((occ = csvReader.readNext()) != null) {

					//Zero outs line to avoid conflicts
					line = new String[occ.length+1];
					for (int a = 0; a < occ.length; a++) {
						line[a] = occ[a];
					}

					counter++;

					///Gets the latitude and longitude
					try {
						lat = occ[latID];
						lon = occ[longID];
					} catch (ArrayIndexOutOfBoundsException e) {
						System.out.println("Could not find either decimalLatitude or decimalLongitude headers!");
						throw e;
					}


					//Grabs the time zone candidates for the given latitude and longitude
					Optional<ZoneId> maybeZoneId = engine.query(Double.parseDouble(lat), Double.parseDouble(lon));


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
					} catch (StringIndexOutOfBoundsException | ArrayIndexOutOfBoundsException e) {

					}
					try {
						month = Integer.parseInt(occ[dateID].substring(5,7));
					} catch (StringIndexOutOfBoundsException | ArrayIndexOutOfBoundsException e) {

					}					
					try {
						day = Integer.parseInt(occ[dateID].substring(8,10));		
					} catch (StringIndexOutOfBoundsException | ArrayIndexOutOfBoundsException e) {

					}

					//Grabs the given date and uses it to define the exact time zone based on the set obtained from the coordinates
					ZoneId timeZone = maybeZoneId.get();
					LocalDateTime dt = null;
					try {
						dt =  LocalDateTime.of(year, month, day, 1, 0, 0, 0);
					} catch ( DateTimeException e) {
						try {
							dt =  LocalDateTime.of(year, month, 1, 1, 0, 0, 0);
						} catch ( DateTimeException e1) {
							try {
								dt =  LocalDateTime.of(year, 1, 1, 1, 0, 0, 0);
							} catch ( DateTimeException e2) {
								dt =  LocalDateTime.of(2000, 1, 1, 1, 0, 0, 0);
							}
						}
					}
					ZonedDateTime zdt = dt.atZone(timeZone);
					ZoneOffset offset = zdt.getOffset();

					line[line.length-1] = offset.toString();

					writer.writeNext(line);

					if (counter%100 == 0) {
						//Shows progress
						System.out.println("Working on line " + counter);
						writer.flush();
					}
				}

				//No need to close as CSVWriter auto-closes
				System.out.println("Complete!");
				csvReader.close();


			} catch (IOException e) {			
				System.out.println("Output file is not accessible!");
				e.printStackTrace();
				csvReader.close();
				throw e;
			}
		} catch (IOException e) {			
			System.out.println("There is no input file!");
			e.printStackTrace();			
			throw e;
		}
		catch (RuntimeException e) {
			System.out.println("Either the .csv is not in UTF-8 or its delimiters are not commas(,)\n\n");
			e.printStackTrace();
			throw e;
		} catch (CsvValidationException e1) {
			e1.printStackTrace();
			throw e1;
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