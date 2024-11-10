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
import helpers.MetadataExtractorSeqLoad;

/**
 * Servlet implementation for the class MetadataExtractorSeqLoad
 * 
 * @author Tomé Neves de Matos
 */
@MultipartConfig
@WebServlet(  name = "Metadata Extractor",
urlPatterns = "/metadataExtractor", 
asyncSupported=true)
public class MetadataExtractorWeb extends HttpServlet {

	private static final long serialVersionUID = -554280085411963056L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {

		// Prepare messages.
		Map<String, String> messages = new HashMap<String, String>();
		request.setCharacterEncoding("UTF-8");
		request.setAttribute("messages", messages);

		// Get and validate name.
		Part filePart = request.getPart("name"); // Retrieves <input type="file" name="file">
		String fileName = Paths.get(getSubmittedFileName(filePart)).getFileName().toString(); // MSIE fix.

		//Checks if no file was given		
		if (fileName.contentEquals("")) {
			messages.put("name", String.format("Please select a file!"));	
			request.getRequestDispatcher("/WEB-INF/MetadataExtractor.jsp").forward(request, response);
		} else {
			//Creates a temporary file
			File file = File.createTempFile("tmp_" + fileName.substring(0, fileName.lastIndexOf('.')),fileName.substring(fileName.lastIndexOf('.'),fileName.length()), Databases.downloadsFolder);
			String outputFileName = "Metadata_" +file.getName().substring(4,file.getName().lastIndexOf("."))+".txt";
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
				messages.put("name", "Be sure to save the link below ir the file name!");
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
							(new MetadataExtractorSeqLoad()).extract(file.getAbsolutePath(), Databases.downloadsFolder.getAbsolutePath()+"/"+ outputFileName);
						} catch (RuntimeException e) {
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
			request.getRequestDispatcher("/WEB-INF/MetadataExtractor.jsp").forward(request, response);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		request.getRequestDispatcher("/WEB-INF/MetadataExtractor.jsp").forward(request, response);
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