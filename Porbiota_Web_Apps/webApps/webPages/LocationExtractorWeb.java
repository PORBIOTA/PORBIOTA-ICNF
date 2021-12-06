package webPages;


import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import helpers.Databases;
import helpers.LocationExtractorSeqLoad;

/**
 * Servlet implementation for the class LocationExtractorSeqLoad
 * 
 * @author Tom� Neves de Matos
 */
@MultipartConfig
@WebServlet(  name = "Location Extractor",
urlPatterns = "/locationExtractor", 
asyncSupported=true)
public class LocationExtractorWeb extends HttpServlet {

	private static final long serialVersionUID = -554280085451963056L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {

		// Prepare messages.
		Map<String, String> messages = new HashMap<String, String>();
		request.setCharacterEncoding("UTF-8");
		request.setAttribute("messages", messages);

		// Get and validate name.
		Part filePart = request.getPart("name"); // Retrieves <input type="file" name="name">
		String fileName = Paths.get(getSubmittedFileName(filePart)).getFileName().toString(); // MSIE fix.

		//Gets append checkbox
		String s[] = request.getParameterValues("append");
		Boolean appendValue = false;
		if (s != null && s.length != 0) {
			appendValue = true;
		}
		final boolean append;
		if (appendValue){
			append = true;
		}else { 
			append = false;
		}

		//Checks if no file was given		
		if (fileName.contentEquals("")) {
			messages.put("name", String.format("Please select a file!"));	
			request.getRequestDispatcher("/WEB-INF/LocationExtractor.jsp").forward(request, response);
		} else {
			//Creates a temporary file
			File file = File.createTempFile("tmp_" + fileName.substring(0, fileName.lastIndexOf('.')),fileName.substring(fileName.lastIndexOf('.'),fileName.length()), Databases.downloadsFolder);
			String outputFileName = "Location_" +file.getName().substring(4,file.getName().length());
			//Gives an error if the file given was not a .csv
			if (!fileName.substring(fileName.lastIndexOf('.'),fileName.length()).equalsIgnoreCase(".csv")) {
				file.delete();
				messages.put("name", "File is not a CSV!");
			} else {
				//Downloads the file to the server
				try (InputStream input = filePart.getInputStream()) {
					Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING);				
				}

				//Gives out the information of how to download the eventually outputted file
				messages.put("link", String.format("/Porbiota_Web_Apps/downloadResults?file=%s",outputFileName));	
				messages.put("name", "Be sure to save the link below or the file name!");
				messages.put("success", "Download your file when it is ready here");	
				messages.put("fileName",String.format("The file name is %s",outputFileName));

				//Makes sure the server supports asynchronous processes and then runs one
				request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
				final AsyncContext acontext = request.startAsync();
				acontext.start(new Runnable() {
					@Override
					public void run() {
						acontext.complete();
						try {
							//Runs the main program asynchronously
							(new LocationExtractorSeqLoad()).extract(file.getAbsolutePath(), Databases.downloadsFolder.getAbsolutePath()+"/"+ outputFileName,Databases.localeDB, Databases.localeMap, append);					
						} catch (ArrayIndexOutOfBoundsException | IOException | NumberFormatException | NullPointerException  e) {
							//In case the application gives an error, outputs a file that will later be read to inform the user of the error
							try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(Databases.downloadsFolder.getAbsolutePath()+"/"+ outputFileName), StandardCharsets.UTF_8)) {
								writer.write("Invalid CSV!\n");
								writer.write(e.toString());
								writer.close();
							} catch (IOException e1) {
								e1.printStackTrace();
							}	
						} finally {
							file.delete();
						}
					}
				});
			}
			//Outputs the webpage to the user
			request.getRequestDispatcher("/WEB-INF/LocationExtractor.jsp").forward(request, response);

		} 
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		request.getRequestDispatcher("/WEB-INF/LocationExtractor.jsp").forward(request, response);
	}

	//Gets the file name
	private static String getSubmittedFileName(Part part) {
		for (String cd : part.getHeader("content-disposition").split(";")) {
			if (cd.trim().startsWith("filename")) {
				String fileName = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
				return fileName.substring(fileName.lastIndexOf('/') + 1).substring(fileName.lastIndexOf('\\') + 1); // MSIE fix.
			}
		}
		return null;
	}

}