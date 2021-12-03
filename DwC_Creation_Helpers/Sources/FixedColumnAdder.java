package webSolo;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Arrays;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
/**
 * 
 * Class to add the initial fixed value columns of a DwC .csv file
 * 
 * @author Tomé Neves de Matos
 *
 */
public class FixedColumnAdder {

	
	/**
	 * Main method for testing or independent usage

	 * @param args String[]
	 * 	 0 - The path to the .csv
	 *	 1 - The path to store the output .csv
	 *   2 - Collection Code
	 *   3 - Type
	 *   4 - Modified Date
	 *   4 - license
	 *   5 - insitutionID
	 *   6 - institutionCode
	 *   7 - datasetName
	 *   8 - basisOfRecord	  
	 *   9 - occurrenceStatus
	 * @throws IOException
	 */
	
	public static void main(String args[]) throws IOException {
		String input = args[0];
		String output = args[1];
		
		String[] values = new String[args.length-2];
		
		for (int i = 2; i < args.length; i++) {
			values[i-2] = args[i];
		}
		
		addColumn(input,output,values);
	}
	/**
	 * Adds the initial fixed value columns of a DwC .csv file
	 * @param inputPath String The path to the .csv
	 * @param outputPath String The path to store the output .csv
	 * @param values String[] An array with the values for each of the fixed columns
	 *   0 - Collection Code
	 *   1 - Type
	 *   2 - Modified Date
	 *   3 - license
	 *   4 - insitutionID
	 *   5 - institutionCode
	 *   6 - datasetName
	 *   7 - basisOfRecord	  
	 *   8 - occurrenceStatus
	 * @throws IOException
	 */

	public static void addColumn(String input, String output, String[] values) throws IOException {

		String[] header = {"collectionCode","type","modified","license","institutionID","institutionCode","datasetName","basisOfRecord","occurrenceStatus"};

		
		if (header.length != values.length) {
			System.out.println("Number of values does not match the number of fixed columns (9)!");
			return;
		}
		
		try {
			BufferedReader br =  Files.newBufferedReader(Paths.get(input),StandardCharsets.UTF_8);	
			CSVReader csvReader = new CSVReader(br);
			//Opens writer
			try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"))) {

				String[] currentLine = csvReader.readNext();
				
				//Writes header line		
			    String[] toWrite = Arrays.copyOf(header, currentLine.length+header.length);
			    System.arraycopy(currentLine, 0, toWrite, header.length, currentLine.length);
				writer.writeNext(toWrite);	


				//Writes all other lines
				while ((currentLine = csvReader.readNext()) != null) {

				    toWrite = Arrays.copyOf(values, currentLine.length+values.length);
				    System.arraycopy(currentLine, 0, toWrite, values.length, currentLine.length);				    
					writer.writeNext(toWrite);	

				}
				System.out.println("Complete!" );
			} catch (IOException | CsvValidationException e) {
				
			}
			csvReader.close();

		} catch (IOException|InvalidPathException e) {
			System.out.println ("Output file in use or invalid!");
		}		

	}
}
