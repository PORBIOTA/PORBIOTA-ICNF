package helpers;

import java.io.File;
import java.io.IOException;

import webPages.Startup;
/**
 * Class to OCR an image of a table into a .csv file
 * 
 * @author Tomé Neves de Matos
 *
 */
public class OCRforTable {

	/**
	 * Converts an image of a table into a .csv file
	 * @param int pageMode
		 			 0 - Orientation and script detection (OSD)	only.		
		 			 1 - Automatic page segmentation with OSD.
					 2 - Automatic page segmentation, but no OSD, or OCR.
					 3 - Fully automatic page segmentation, but no OSD.
					 4 - Assume a single column of text of variable sizes.
					 5 - Assume a single uniform block of	vertically aligned text.
					 6 - Assume a single uniform block of text.
					 7 - Treat the image as a single text line.
					 8 - Treat the image as a single word.
				  	 9 - Treat the image as a single word in a circle.
					 10 - Treat the image as a single character.
					 11 - Sparse text. Find as much text as possible in no particular order.
					 12 - Sparse text with OSD.
				 	 13 - Raw line. Treat the image as a single text line, bypassing hacks that are Tesseract-specific.
	 * @param language String por - Portuguese, eng - English 
	 * @param input String Path to the image to OCR
	 * @param output String Path of the outputted .csv
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void getText(String pageMode, String language, String input, String output) throws IOException, InterruptedException {

		String tesseractFolder = FolderChooser.cacheFolder.getAbsolutePath() + "/Tesseract-OCR/";
		String tessdata = FolderChooser.cacheFolder.getAbsolutePath() + "/tessdata/";

		//Create a place holder to be replaced
		new File(output).createNewFile();		

		//Run python code
		System.out.println("Running python script...");
		if(Startup.windows) {
			Process process = new ProcessBuilder(tesseractFolder + "/OCR_Table_For_Java.exe",pageMode,language,tesseractFolder + "/tesseract.exe",input,output).start();
			//Process process = Runtime.getRuntime().exec( new String[]{"python", tessdata + "OCR_Table_For_Java.py" , pageMode, language , tesseractFolder + "/tesseract.exe" , input , output}) ;
			process.waitFor();
		} else {
			Process process = Runtime.getRuntime().exec( new String[]{"/var/spool/venv/bin/python3", tessdata + "OCR_Table_For_Java.py" , pageMode, language , "/usr/bin/tesseract" , input , output}) ;
			process.waitFor();
		}
		System.out.println("Completed table OCR!");

	}

}
