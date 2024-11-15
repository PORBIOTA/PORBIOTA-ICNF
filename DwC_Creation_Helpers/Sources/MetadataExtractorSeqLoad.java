package webSolo;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

/**
 * 
 * Class to extract metadata information from a DwC .csv
 *
 * @author Tom� Neves de Matos
 * 
 */

public class MetadataExtractorSeqLoad {

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
	 */
	 
	public static void main(String[] args) {
		(new MetadataExtractorSeqLoad()).extract(args[0], args[1]);
	}

	/**
	 * Extracts metadata information from a DwC .csv
	 * @param inputPath
	 * @param outputPath
	 */
	public void extract(String inputPath, String outputPath) {

		int counter = 1;


		//The hashsets to store each category
		HashSet <String> species = new HashSet<String>();
		HashSet <String> genus = new HashSet<String>();
		HashSet <String> order = new HashSet<String>();
		HashSet <String> Class = new HashSet<String>();
		HashSet <String> family = new HashSet<String>();
		HashSet <String> phylum = new HashSet<String>();
		HashSet <String> kingdom = new HashSet<String>();
		HashSet <String> country = new HashSet<String>();

		int speciesCount = 0;
		int genusCount = 0;
		int familyCount = 0;
		int orderCount = 0;
		int classCount = 0;
		int phylumCount = 0;
		int kingdomCount = 0;
		int countryCount = 0;


		String[] newestDate = new String[10];
		for (int i = 0;i <10 ; i++) {
			newestDate[i]=	"0001";

		}
		//Get present day
		DateFormat df = new SimpleDateFormat("yyyy");
		String presentDay = df.format(new Date());

		String[] oldestDate = new String[10];
		for (int i = 0;i <10 ; i++) {
			oldestDate[i]=	presentDay;
		}

		double latNorth = -99999;
		double latSouth = 999999;
		double longEast = -99999;
		double longWest = 99999;


		//Reads the input file
		CSVReader csvReader;
		String originalTablePath = inputPath;
		try {
			Reader originalReader = Files.newBufferedReader(Paths.get(originalTablePath), StandardCharsets.UTF_8);
			csvReader = new CSVReader(originalReader);


			// Reading Rows One by One in a String array
			String[] occ;

			//get headers


			attributeHeaders(csvReader.readNext());


			try {

				while ((occ = csvReader.readNext()) != null) {

					//Shows progress
					System.out.println("Working on line " + counter);
					counter++;

					///Counts Species and etc
					if (!species.contains(occ[acceptedNameUsageID]) && occ[acceptedNameUsageID].length() != 0 && !occ[taxonRankID].equalsIgnoreCase("GENUS")) {				
						species.add(occ[acceptedNameUsageID]);
						speciesCount++;
					}

					if (!genus.contains(occ[genusID]) && occ[genusID].length() != 0) {
						genus.add(occ[genusID]);
						genusCount++;
					}

					if (!family.contains(occ[familyID]) && occ[familyID].length() != 0) {
						family.add(occ[familyID]);
						familyCount++;
					}

					if (!order.contains(occ[orderID]) && occ[orderID].length() != 0) {
						order.add(occ[orderID]);
						orderCount++;
					}

					if (!Class.contains(occ[classID]) && occ[classID].length() != 0) {
						Class.add(occ[classID]);
						classCount++;
					}

					if (!phylum.contains(occ[phylumID]) && occ[phylumID].length() != 0) {
						phylum.add(occ[phylumID]);
						phylumCount++;
					}

					if (!kingdom.contains(occ[kingdomID]) && occ[kingdomID].length() != 0) {
						kingdom.add(occ[kingdomID]);
						kingdomCount++;
					}

					if (!country.contains(occ[countryID]) && occ[countryID].length() != 0) {
						country.add(occ[countryID]);
						countryCount++;
					}


					if(dateID > -1 && occ[dateID].length() > 3) {
						//Checks for earliest date

						for (int i = 0;i <10 ; i++) {		

							if (occ[dateID].substring(0,4).compareTo(oldestDate[i]) < 0) {
								//pushes all others forward
								for (int a = 8; a>=i; a--) {
									oldestDate[a+1] = oldestDate[a]; 
								}					
								oldestDate[i] = occ[dateID].substring(0,4);
								break;
							} else if (occ[dateID].substring(0,4).compareTo(oldestDate[i]) == 0) {
								break;
							}
						}

						//Checks for newest date
						for (int i = 0;i <10 ; i++) {				
							if (occ[dateID].substring(0,4).compareTo(newestDate[i]) > 0) {
								//pushes all others forward
								for (int a = 8; a>=i; a--) {
									newestDate[a+1] = newestDate[a]; 
								}					
								newestDate[i] = occ[dateID].substring(0,4);
								break;
							} else if (occ[dateID].substring(0,4).compareTo(newestDate[i]) == 0) {
								break;
							}
						}
					}
					//Checks for bounding box
					if (Double.parseDouble(occ[latID])>latNorth) {
						latNorth = Double.parseDouble(occ[latID]);
					} else if (Double.parseDouble(occ[latID])<latSouth) {
						latSouth= Double.parseDouble(occ[latID]);
					}

					if (Double.parseDouble(occ[longID])>longEast) {
						longEast = Double.parseDouble(occ[longID]);
					} else if (Double.parseDouble(occ[longID])<longWest) {
						longWest= Double.parseDouble(occ[longID]);
					}


				}
			} catch (ArrayIndexOutOfBoundsException e) {
				throw e;
			} finally {
				csvReader.close();
			}

		} catch (IOException e) {
			System.out.println("No Input File!");
			System.exit(1);
		} catch (CsvValidationException e) {
			e.printStackTrace();
		} 


		///Writes the output .txt

		String fileName = outputPath;
		Path myPath = Paths.get(fileName);

		try (BufferedWriter writer = Files.newBufferedWriter(myPath, StandardCharsets.UTF_8)) {

			writer.write("Number of rows: " + (counter-1) + "\n");			
			writer.write("Number of species: " + speciesCount + "\n");
			writer.write("Number of genus: " + genusCount + "\n");
			writer.write("Number of families: " + familyCount + "\n");
			writer.write("Number of orders: " + orderCount + "\n");
			writer.write("Number of classes: " + classCount + "\n");
			writer.write("Number of phylums: " + phylumCount + "\n");
			writer.write("Number of kingdoms: " + kingdomCount + "\n");
			writer.write("Number of countries: " + countryCount + "\n");
			writer.write("\n\nBounding Box:\n");
			writer.write("Northermost Latitude: "+ latNorth +"\n");
			writer.write("Southernmost Latitude: "+ latSouth +"\n");
			writer.write("Easternmost Longitude: "+ longEast +"\n");
			writer.write("Westernmost Longitude: "+ longWest +"\n");
			if (dateID > -1) {
				writer.write("\n\nOldest Record:" + oldestDate[0] +"\n");
				writer.write("The next oldest 9 record dates: ");
				for (int i = 1; i<10; i++) {
					writer.write(oldestDate[i] + " ");
				}
				writer.write("\n\nNewest Record:" + newestDate[0] +"\n");
				writer.write("The next newest 9 record dates: ");
				for (int i = 1; i<10; i++) {
					writer.write(newestDate[i] + " ");
				}
			}

			List<String> list;
			Iterator<String> iter;



			writer.write("\n\nKingdom names:\n");			
			list = new ArrayList<String>(kingdom);
			Collections.sort(list);
			iter = list.iterator();	     
			while(iter.hasNext()) {
				writer.write((String) iter.next() + "\n");
			}

			writer.write("\nPhylum names:\n");			
			list = new ArrayList<String>(phylum);
			Collections.sort(list);
			iter = list.iterator();	     
			while(iter.hasNext()) {
				writer.write((String) iter.next() + "\n");
			}

			writer.write("\nClass names:\n");			
			list = new ArrayList<String>(Class);
			Collections.sort(list);
			iter = list.iterator();	     
			while(iter.hasNext()) {
				writer.write((String) iter.next() + "\n");
			}

			writer.write("\nOrder names:\n");			
			list = new ArrayList<String>(order);
			Collections.sort(list);
			iter = list.iterator();	     
			while(iter.hasNext()) {
				writer.write((String) iter.next() + "\n");
			}

			writer.write("\nFamily names:\n");			
			list = new ArrayList<String>(family);
			Collections.sort(list);
			iter = list.iterator();	     
			while(iter.hasNext()) {
				writer.write((String) iter.next() + "\n");
			}


			writer.write("\nGenus names:\n");			
			list = new ArrayList<String>(genus);
			Collections.sort(list);
			iter = list.iterator();	     
			while(iter.hasNext()) {
				writer.write((String) iter.next() + "\n");
			}

			writer.write("\nSpecies names:\n");	
			list = new ArrayList<String>(species);
			Collections.sort(list);
			iter = list.iterator();	     
			while(iter.hasNext()) {
				writer.write((String) iter.next() + "\n");
			}

			writer.write("\nCountry names:\n");			
			list = new ArrayList<String>(country);
			Collections.sort(list);
			iter = list.iterator();	     
			while(iter.hasNext()) {
				writer.write((String) iter.next() + "\n");
			}

			///Write XML code for species
			writer.write("\n\nXML code:\n");			
			list = new ArrayList<String>(kingdom);
			Collections.sort(list);
			iter = list.iterator();	     
			while(iter.hasNext()) {
				writer.write("<taxonomicClassification>\n<taxonRankName>kingdom</taxonRankName>\n <taxonRankValue>" + (String) iter.next() + "</taxonRankValue>\n</taxonomicClassification>\n");
			}

			list = new ArrayList<String>(phylum);
			Collections.sort(list);
			iter = list.iterator();	     
			while(iter.hasNext()) {
				writer.write("<taxonomicClassification>\n<taxonRankName>phylum</taxonRankName>\n <taxonRankValue>" + (String) iter.next() + "</taxonRankValue>\n</taxonomicClassification>\n");
			}

			list = new ArrayList<String>(Class);
			Collections.sort(list);
			iter = list.iterator();	     
			while(iter.hasNext()) {
				writer.write("<taxonomicClassification>\n<taxonRankName>class</taxonRankName>\n <taxonRankValue>" + (String) iter.next() + "</taxonRankValue>\n</taxonomicClassification>\n");
			}

			list = new ArrayList<String>(order);
			Collections.sort(list);
			iter = list.iterator();	     
			while(iter.hasNext()) {
				writer.write("<taxonomicClassification>\n<taxonRankName>order</taxonRankName>\n <taxonRankValue>" + (String) iter.next() + "</taxonRankValue>\n</taxonomicClassification>\n");
			}

			list = new ArrayList<String>(family);
			Collections.sort(list);
			iter = list.iterator();	     
			while(iter.hasNext()) {
				writer.write("<taxonomicClassification>\n<taxonRankName>family</taxonRankName>\n <taxonRankValue>" + (String) iter.next() + "</taxonRankValue>\n</taxonomicClassification>\n");
			}


			list = new ArrayList<String>(genus);
			Collections.sort(list);
			iter = list.iterator();	     
			while(iter.hasNext()) {
				writer.write("<taxonomicClassification>\n<taxonRankName>genus</taxonRankName>\n <taxonRankValue>" + (String) iter.next() + "</taxonRankValue>\n</taxonomicClassification>\n");
			}

			writer.close();

		} catch (IOException ex) {

			System.out.println("There is no output file or it is not accessible!");
			System.exit(1);	
		}


		System.out.println("Complete!");

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
