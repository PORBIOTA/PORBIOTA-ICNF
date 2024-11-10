package helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * 
 * Class to count the number of lines in a .csv
 * 
 * @author Tomé Neves de Matos
 *
 */

public class CsvLineCounter {

	/**
	 * Counts the number of lines in a .csv
	 * @param csv String The path to the .csv file
	 * @return int The number of lines
	 */
	public static int count(File csv) {
		BufferedReader lineCounter = null;
		int lines = 0;
		try {

			lineCounter = Files.newBufferedReader(csv.toPath(), StandardCharsets.UTF_8 );
			while (lineCounter.readLine()!=null) {
				lines++;
			}
			lineCounter.close();
			return lines;

		} catch (IOException e) {
			return 0;
		}
	}
}
