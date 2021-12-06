package helpers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import net.sourceforge.tess4j.ITesseract.RenderedFormat;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
/**
 * Class to convert a set of images or pdfs into a .txt or .pdf file
 * @author Tomé Neves de Matos
 *
 */
public class OCR {


	public OCR() {

	}
	/**
	 * Converts a set of images or pdfs into a .txt or .pdf file
	 * @param args String[] An array with the path of all the images to be OCRd. The last entry is the output file path.
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
	 * @param pdf boolean True to output a .pdf, false to output a .txt
	 * @throws IOException
	 * @throws InterruptedException
	 */
	
	public void getText(String[] args,int pageMode, String language, boolean pdf) throws IOException, TesseractException {

		Tesseract tesseract = new Tesseract();
		tesseract.setDatapath(FolderChooser.cacheFolder.getAbsolutePath() + "/tessdata/");
		tesseract.setLanguage(language);
		tesseract.setPageSegMode(pageMode);
		tesseract.setOcrEngineMode(1);

		if(pdf) {
			//Create a place holder to be replaced
			new File(args[args.length-1]).createNewFile();

			List<RenderedFormat> renderers = new ArrayList<RenderedFormat>();
			renderers.add(RenderedFormat.PDF);

			//get files names
			PDFMergerUtility merger = new PDFMergerUtility();

			String[] imagesName = new String[args.length-1];
			for (int i = 0; i < args.length-1; i++) {
				String nameWithExtension =  Paths.get(args[i]).getFileName().toString();
				imagesName[i] = Paths.get(args[args.length-1]).getParent()+ "/" + nameWithExtension.substring(0,nameWithExtension.lastIndexOf("."));
				tesseract.createDocuments(args[i], imagesName[i], renderers);	
				merger.addSource(imagesName[i]+".pdf");
			}	

			merger.setDestinationFileName(args[args.length-1]);
			merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

			for (int i = 0; i < imagesName.length; i++) {
				new File(imagesName[i]+".pdf").delete();
			}	


		} else {

			BufferedWriter writer = Files.newBufferedWriter(Paths.get(args[args.length-1]), StandardCharsets.UTF_8);
			for (int i = 0; i < args.length-1; i++) {
				File image = new File(args[i]);
				writer.write(removeLineBreaks(tesseract.doOCR(image)));
				writer.flush();
			}
			writer.close();

		}
		System.out.println("OCR Complete!");

	}

	/**
	 * Removes excess line breaks to join paragraphs together
	 * @param text String The text to remove the line breaks from
	 * @return String The text without the excess line breaks
	 */ 
	public String removeLineBreaks(String text) {
		return text.replace("-\n", "").replaceAll("(?<=\n|^)[\t ]+|[\t ]+(?=$|\n)", "").replaceAll("(?<=.)\n(?=.)", " ");
	}

}
