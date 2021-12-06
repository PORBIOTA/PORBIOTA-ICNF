package helpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

/**
 * Class to join several .csv files with the same number of lines side by side
 * @author Tomé Neves de Matos
 *
 */
public class TableJoiner {

	
	public TableJoiner() {
		
	}
	/**
	 * Main method for testing or independent usage
	 * @param args String[] An array with the path of all the .csv files to be joined. The last entry is the output file path.
	 * @throws IOException
	 */
	public static void main (String[] args) throws IOException {
		(new TableJoiner()).join(args);
	}
	
	/**
	 * Joins several .csv files with the same number of lines side by side
	 * @param args String[] An array with the path of all the .csv files to be joined. The last entry is the output file path.
	 * @throws IOException
	 */
	public void join(String[] args) throws IOException {
		try {
			String[] fileNameJoin = new String[args.length-1]; 
			BufferedReader[] brJoin = new BufferedReader[args.length-1];
			String[] linesToJoin = new String[args.length-1];
			for (int i = 0; i < args.length-1; i++) {
				fileNameJoin[i] = args[i];					
				brJoin[i] = Files.newBufferedReader(Paths.get(fileNameJoin[i]),StandardCharsets.UTF_8);
			}
			
			String fileNameOutput = args[args.length-1];
			BufferedWriter bw = Files.newBufferedWriter(Paths.get(fileNameOutput),StandardCharsets.UTF_8);

			for (linesToJoin[0] = brJoin[0].readLine(); linesToJoin[0] != null; linesToJoin[0] =  brJoin[0].readLine()) {
				bw.write(linesToJoin[0]);
				for (int i = 1; i < args.length-1; i++) {
					linesToJoin[i] = brJoin[i].readLine();			
					if (linesToJoin[i] == null) {
						break;
					}
					bw.write(","+linesToJoin[i]);
				}
				bw.write("\r");
			}
			System.out.println("Complete! Outputted file to " +args[args.length-1] );
			bw.close();
			for (int i = 0; i < args.length-1; i++) {
				brJoin[i].close();
			}

		} catch (IOException|InvalidPathException e) {
			System.out.println ("Output file in use or invalid!");
		}		

	}


}
