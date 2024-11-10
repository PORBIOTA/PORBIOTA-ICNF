package webPages;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import helpers.CsvLineCounter;
import helpers.Databases;
import helpers.OutputToUser;

/**
 * Servlet implementation for the class OutputToUser
 * 
 * @author Tomé Neves de Matos
 */
@MultipartConfig
@WebServlet(  name = "Download Results",
urlPatterns = "/downloadResults")
public class ResultsDownloader extends HttpServlet {

	private static final long serialVersionUID = -554297185411963056L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		doPostandGet(request,response);


	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {

		doPostandGet(request, response);

	}

	private void doPostandGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Prepare messages.
		Map<String, String> messages = new HashMap<String, String>();
		request.setAttribute("messages", messages);

		//Gets the file do output to the user
		String fileName = request.getParameter("file");	
		boolean  force = Boolean.valueOf(request.getParameter("forceDownload"));
		if (fileName == null) {
			request.getRequestDispatcher("/WEB-INF/ResultsDownloader.jsp").forward(request, response);
			return;
		}

		//Checks if no file was given
		if (fileName.contentEquals("")) {

			messages.put("info", String.format("Please insert a file name!"));	
			request.getRequestDispatcher("/WEB-INF/ResultsDownloader.jsp").forward(request, response);

		} if (fileName.equalsIgnoreCase("Tag Macros.docm")) { //Outputs the help .docm file
			OutputToUser.output(response, Databases.downloadsFolder.getAbsolutePath()+"/"+ fileName, getServletContext(), force);
		}	else {

			//If the file given exists
			if (new File(Databases.downloadsFolder.getAbsolutePath()+"/"+ fileName).exists()) {					

				//If the tmp file still exists and not forced
				if (!force &&
						(fileName.substring(fileName.length()-3).equals("csv") && new File(Databases.downloadsFolder.getAbsolutePath()+"/tmp_" + fileName.substring(fileName.indexOf("_")+1,fileName.length())).exists()
								|| (new File(Databases.downloadsFolder.getAbsolutePath()+"/tmp_" + fileName.substring(fileName.indexOf("_")+1)).exists()))) {
					int newLines = CsvLineCounter.count(new File(Databases.downloadsFolder.getAbsolutePath()+"/"+ fileName));	
					int originalLines = CsvLineCounter.count(new File(Databases.downloadsFolder.getAbsolutePath()+"/tmp_" + fileName.substring(fileName.indexOf("_")+1,fileName.length())));	
					//If the WIP file still hasn't got the same number of lines as the input file outputs the percentage of completion
					double percentage = ((double)newLines)/((double)originalLines)*100;
					if (percentage > 100) {
						messages.put("info", String.format("Nearly finished..."));
					} else {
						messages.put("info", String.format("Currently at %.2f%%!", percentage));
					}
					messages.put("fileName", String.format("Downloading %s",fileName));
					messages.put("info2", String.format("You can refresh this page (F5) to get an update or download the file when ready."));
					messages.put("force", String.format("You can download the incomplete file here."));
					messages.put("forceLink", String.format("/Porbiota_Web_Apps/downloadResults?file=%s&forceDownload=true",fileName));
					request.getRequestDispatcher("/WEB-INF/ResultsDownloader.jsp").forward(request, response);


				} 	else if (!force && (
						new File(Databases.downloadsFolder.getAbsolutePath()+"/tmp_" + fileName.replace("_OCR.txt", ".jpg").replace("_OCR.pdf", ".jpg").replace("_OCR.csv", ".jpg")).exists()
						||new File(Databases.downloadsFolder.getAbsolutePath()+"/tmp_" + fileName.replace("_OCR.txt", ".jpeg").replace("_OCR.pdf", ".jpeg").replace("_OCR.csv", ".jpeg")).exists()
						||new File(Databases.downloadsFolder.getAbsolutePath()+"/tmp_" + fileName.replace("_OCR.txt", ".tif").replace("_OCR.pdf", ".tif").replace("_OCR.csv", ".tif")).exists()
						||new File(Databases.downloadsFolder.getAbsolutePath()+"/tmp_" + fileName.replace("_OCR.txt", ".tiff").replace("_OCR.pdf", ".tiff").replace("_OCR.csv", ".tiff")).exists()
						||new File(Databases.downloadsFolder.getAbsolutePath()+"/tmp_" + fileName.replace("_OCR.txt", ".pdf").replace("_OCR.pdf", ".pdf").replace("_OCR.csv", ".pdf")).exists()
						||new File(Databases.downloadsFolder.getAbsolutePath()+"/tmp_" + fileName.replace("_OCR.txt", ".png").replace("_OCR.pdf", ".png").replace("_OCR.csv", ".png")).exists())) {

					messages.put("fileName", String.format("Downloading %s",fileName));
					messages.put("info", String.format("Currently performing OCR. Expect around 20 seconds per page."));
					messages.put("info2", String.format("You can refresh this page (F5) to download the file when ready."));
					messages.put("force", String.format("You can download the incomplete file here. (Updates every page for .TXT)"));
					messages.put("forceLink", String.format("/Porbiota_Web_Apps/downloadResults?file=%s&forceDownload=true",fileName));
					request.getRequestDispatcher("/WEB-INF/ResultsDownloader.jsp").forward(request, response);				

				}else {
					//If forced or if the tmp no longer exists outputs the (possibly incomplete) file to the user
					String firstLine = null;
					BufferedReader lineCounterNew = null;
					try {
						lineCounterNew = Files.newBufferedReader(Paths.get(Databases.downloadsFolder.getAbsolutePath()+"/"+ fileName), StandardCharsets.UTF_8 );
						firstLine = lineCounterNew.readLine();
					} catch (MalformedInputException e) {

					}

					//If the first line of the file contains the error message, instead of the file, outputs the error to the user
					if (firstLine != null && firstLine.equals("Invalid CSV!")) {
						messages.put("info", String.format("Invalid File!"));
						messages.put("info2",String.format(lineCounterNew.readLine())); //Fetches the error message
						request.getRequestDispatcher("/WEB-INF/ResultsDownloader.jsp").forward(request, response);						
						new File(Databases.downloadsFolder.getAbsolutePath()+"/"+ fileName).delete();
						lineCounterNew.close();
					} else if (firstLine != null && firstLine.equals("The custom tags were improperly defined!"))  {
						messages.put("info", String.format("The custom tags were improperly defined!"));
						messages.put("info2",String.format(lineCounterNew.readLine())); //Fetches the error message
						request.getRequestDispatcher("/WEB-INF/ResultsDownloader.jsp").forward(request, response);						
						new File(Databases.downloadsFolder.getAbsolutePath()+"/"+ fileName).delete();
						lineCounterNew.close();						
					} else {
						//Otherwise outputs the file
						OutputToUser.output(response, Databases.downloadsFolder.getAbsolutePath()+"/"+ fileName, getServletContext(), force);
						lineCounterNew.close();
					}


				}
			} else {
				//If the file does not exist, informs the user
				messages.put("info", String.format("The file %s does not exist!", fileName));
				request.getRequestDispatcher("/WEB-INF/ResultsDownloader.jsp").forward(request, response);
			}
		}
	};

}