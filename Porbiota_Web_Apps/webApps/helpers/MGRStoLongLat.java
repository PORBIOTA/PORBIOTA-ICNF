package helpers;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

import com.berico.coords.Coordinates;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;


/**
 * 
 * Class to convert MGRS coordinates to WGS84.
 *
 * @author Tomé Neves de Matos
 *
 */

public class MGRStoLongLat {

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
	private  int mgrsID=0;	


	public MGRStoLongLat() {

	}
	
	/**
	 * Main method for testing or independent usage
	 * @param args 	0 - Input File, 1 - Output File, 2 - MGRS UTM Zone (if not needed use "")
	 * @throws IOException
	 */
	
	public static void main(String args[]) throws IOException {
		(new MGRStoLongLat()).convert(args[0], args[1], args[2], false, false);
	}
	
	/**
	 * Convert MGRS coordinates to WGS84
	 * @param inputPath String The path to the .csv
	 * @param outputPath String The path to store the output .csv
	 * @param Zone String The UTM Zone (if included in the the MGRS coordinate use "")
	 * @param ED50 boolean true if the original CRS of the coordinates is ED50, false if it is WGS84 
	 * @param append boolean true - appends the results to the original .csv; false - creates a .csv solely with the results
	 * @throws IOException
	 */

	public void convert(String inputPath, String outputPath, String Zone, boolean ED50, boolean append) throws IOException {


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


			//Opens writer
			String fileName = outputPath;
			Path myPath = Paths.get(fileName);
			try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(myPath.toString()), "UTF-8"))) {


				//Writes header line
				String[] headerToWrite = {"MGRS","decimalLatitude","decimalLongitude"};
				String[] headerToAppend = {"decimalLatitude","decimalLongitude"};

				int dif = 0; //difference between the two headers, only declared if append

				//Writes header line
				if (append) {
					dif = 1; //difference between the two headers
					//Writes header line
					headerToWrite = Arrays.copyOf(header, headerToAppend.length+header.length);
					System.arraycopy(headerToAppend, 0, headerToWrite, header.length, headerToAppend.length);
				}

				writer.writeNext(headerToWrite);

				CRSFactory crsFactory = new CRSFactory();
				CoordinateReferenceSystem WGS84 = crsFactory.createFromParameters("WGS84", "+proj=longlat +datum=WGS84 +no_defs");
				CoordinateReferenceSystem UTM = crsFactory.createFromParameters("ED50",  "+proj=longlat +ellps=intl +towgs84=-87,-98,-121,0,0,0,0 +no_defs");    
				CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
				CoordinateTransform UTMtoWGS84 = ctFactory.createTransform(UTM,WGS84);

				
				while ((occ = csvReader.readNext()) != null) {
					//Zero outs line to avoid conflicts
					String[] line = new String[headerToAppend.length+1-dif];

					//Shows progress
					counter++;

					///Gets the latitude and longitude

					boolean error = false;
					double[] latLon = null;
					try {
						latLon = Coordinates.latLonFromMgrs(Zone + " " + occ[mgrsID]);
					} catch (IllegalArgumentException e) {
						error = true;
					}

					line[0] = occ[mgrsID];
					if (!error) {
						ProjCoordinate result = new ProjCoordinate(latLon[1],latLon[0]);

						if (ED50) {
							UTMtoWGS84.transform(new ProjCoordinate(result.x, result.y), result);
						} 

						line[1-dif] = Double.toString(result.y);
						line[2-dif] = Double.toString(result.x);
				
					} else {
						line[1-dif] = "Invalid MGRS";
						line[2-dif] = "Invalid MGRS";
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
						writer.flush();
						System.out.println("Working on line " + counter);
					}
				}



				System.out.println("Complete!");


			} catch (IOException  e) {			
				System.out.println("Output file is not accessible!");

			} finally {
				csvReader.close();
			}
		} catch (IOException e) {			
			System.out.println("There is no input file!");
			throw e;
		}
		catch (RuntimeException e) {
			System.out.println("Either the .csv is not in UTF-8 or its delimiters are not commas(,)\n\n");		
			e.printStackTrace();
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

			case "MGRS":
				mgrsID =id;
				continue;

			default:
				continue;

			}
		}

	}
}