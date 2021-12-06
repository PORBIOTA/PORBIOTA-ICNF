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
import java.util.List;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import helpers.CsvLineCounter;
import helpers.Databases;
import helpers.TableJoiner;

/**
 * Servlet implementation for the class TableJoiner
 * 
 * @author Tomé Neves de Matos
 */
@MultipartConfig
@WebServlet(name = "Table Joiner", urlPatterns = "/tableJoiner", asyncSupported = true)
public class TableJoinerWeb extends HttpServlet {

	private static final long serialVersionUID = -554280085451963056L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Prepare messages.
		Map<String, String> messages = new HashMap<String, String>();
		request.setCharacterEncoding("UTF-8");
		request.setAttribute("messages", messages);

		// Get all files and create arrays to store their information
		List<Part> fileParts = (List<Part>) request.getParts();
		String[] filePaths = new String[fileParts.size()+1];
		String[] fileNames = new String[fileParts.size()+1];
		File[] files = new File[fileParts.size()];


		//For each file
		for (int f = 0; f < fileParts.size(); f++) {
			//Get their names
			fileNames[f] = Paths.get(getSubmittedFileName(fileParts.get(f))).getFileName().toString(); // MSIE fix.
			try {
				//Create a temporary file
			files[f] = File.createTempFile(fileNames[f].substring(0, fileNames[f].lastIndexOf('.')), 
					fileNames[f].substring(fileNames[f].lastIndexOf('.'), fileNames[f].length()), Databases.downloadsFolder);
			} catch (StringIndexOutOfBoundsException e) {
				//Error if not enough csvs were given
				messages.put("name", "Select at least two .CSVs!");
				request.getRequestDispatcher("/WEB-INF/TableJoiner.jsp").forward(request, response);
				return;
			}
			
			//Store the temp file path
			filePaths[f] = files[f].getAbsolutePath();

			//Gives error if one file is not a csv
			if (!fileNames[f].substring(fileNames[f].lastIndexOf('.'), fileNames[f].length()).equalsIgnoreCase(".csv")) {
				files[f].delete();
				messages.put("name", "File is not a CSV!");
			} else {
				try (InputStream input = fileParts.get(f).getInputStream()) {
					Files.copy(input, files[f].toPath(), StandardCopyOption.REPLACE_EXISTING);
				}

			}
		}
		
		//Checks if the CSVs have the same number of lines
		int numberOfLines = CsvLineCounter.count(files[0]);
		for (int i = 1; i < files.length; i++) {
			if (numberOfLines != CsvLineCounter.count(files[i])) {
				messages.put("name", "The CSVs do not have the same number of lines!");
				request.getRequestDispatcher("/WEB-INF/TableJoiner.jsp").forward(request, response);
				for (int f = 0; f < fileParts.size(); f++) {
					files[f].delete();
				}
				return;
			}			
		}
		
		
		//Creates the output file
		filePaths[fileParts.size()] = filePaths[0].substring(0,filePaths[0].lastIndexOf('.')) + "_output.csv";
		fileNames[fileParts.size()] = fileNames[0];
		
		
		//Gives out the information of how to download the eventually outputted file
		messages.put("link", String.format("/Porbiota_Web_Apps/downloadResults?file=%s_output.csv",
				files[0].getName().substring(0,  files[0].getName().lastIndexOf('.'))));
		messages.put("name", "Be sure to save the link below or the file name!");
		messages.put("success", "Download your file when it is ready here");
		messages.put("fileName", String.format("The file name is %s_output.csv",
				files[0].getName().substring(0,  files[0].getName().lastIndexOf('.'))));
		
		//Outputs the webpage to the user
		request.getRequestDispatcher("/WEB-INF/TableJoiner.jsp").forward(request, response);

		//Makes sure the server supports asynchronous processes and then runs one
		request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
		final AsyncContext acontext = request.startAsync();
		acontext.start(new Runnable() {
			@Override
			public void run() {
				acontext.complete();
				try {
					//Runs the main program asynchronously
					(new TableJoiner()).join(filePaths);

				} catch (ArrayIndexOutOfBoundsException | IOException e) {
					//In case the application gives an error, outputs a file that will later be read to inform the user of the error
					try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(Databases.downloadsFolder.getAbsolutePath()+"/"+
							 fileNames[fileParts.size()].substring(0, fileNames[fileParts.size()].lastIndexOf('.')) + "_output.csv"),
							StandardCharsets.UTF_8)) {
						writer.write("Invalid CSV!\n");
						writer.write(e.toString());
						writer.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} finally {
					for (int f = 0; f < fileParts.size(); f++) {
						files[f].delete();
					}

				}
			}
		});
		

	}



	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.getRequestDispatcher("/WEB-INF/TableJoiner.jsp").forward(request, response);
	}

	//Gets the file name
	private static String getSubmittedFileName(Part part) {
		for (String cd : part.getHeader("content-disposition").split(";")) {
			if (cd.trim().startsWith("filename")) {
				String fileName = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
				return fileName.substring(fileName.lastIndexOf('/') + 1).substring(fileName.lastIndexOf('\\') + 1); // MSIE
				// fix.
			}
		}
		return null;
	}

}