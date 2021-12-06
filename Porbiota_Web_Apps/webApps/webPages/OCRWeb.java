package webPages;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
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

import helpers.Databases;
import helpers.OCR;
import net.sourceforge.tess4j.TesseractException;

/**
 * Servlet implementation for the class OCR
 * 
 * @author Tomé Neves de Matos
 */
@MultipartConfig
@WebServlet(name = "OCR", urlPatterns = "/OCR", asyncSupported = true)
public class OCRWeb extends HttpServlet {

	private static final long serialVersionUID = -554270085451963056L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Prepare messages.
		Map<String, String> messages = new HashMap<String, String>();
		request.setCharacterEncoding("UTF-8");
		request.setAttribute("messages", messages);

		// Get file list
		List<Part> tempfileParts = (List<Part>) request.getParts();

		List<Part> fileParts =new ArrayList<Part>();
		for (int i = 0 ; i < tempfileParts.size()-3; i++) {	//-3 needs to be adjusted according to the number of other parametrs on page	
			fileParts.add(tempfileParts.get(i));			
		}

		String[] filePaths = new String[fileParts.size()+1]; //Create +1 for the output file
		String[] fileNames = new String[fileParts.size()+1];
		File[] files = new File[fileParts.size()];

		//Get language
		final String language = request.getParameter("language");	

		//Get OCR mode
		final int pageMode = Integer.valueOf(request.getParameter("PageMode"));

		//Get output, false .txt, true .pdf		
		final boolean pdf = Boolean.valueOf(request.getParameter("pdf"));


		//For all given files
		for (int f = 0; f < fileParts.size(); f++) {
			//Get their name
			fileNames[f] = Paths.get(getSubmittedFileName(fileParts.get(f))).getFileName().toString(); // MSIE fix.


			//Creates temporary files
			try {
				files[f] = File.createTempFile("tmp_"+fileNames[f].substring(0, fileNames[f].lastIndexOf('.')).replaceAll("[^A-Za-z0-9]",""),	fileNames[f].substring(fileNames[f].lastIndexOf('.'), fileNames[f].length()), Databases.downloadsFolder);
			}		 catch (StringIndexOutOfBoundsException e) {
				//Error if not enough csvs were given
				messages.put("name", "Select at least one file!");
				request.getRequestDispatcher("/WEB-INF/OCR.jsp").forward(request, response);
				return;
			}

			//Add their path to list
			filePaths[f] = files[f].getAbsolutePath();

			//Checks if image
			String extension = fileNames[f].substring(fileNames[f].lastIndexOf('.'));
			if (!extension.equalsIgnoreCase(".jpg") && !extension.equalsIgnoreCase(".jpeg") && !extension.equalsIgnoreCase(".png")&& !extension.equalsIgnoreCase(".pdf")
					&& !extension.equalsIgnoreCase(".tif") && !extension.equalsIgnoreCase(".tiff")) {
				files[f].delete();
				messages.put("name", "File is not a valid image or pdf!");
				request.getRequestDispatcher("/WEB-INF/OCR.jsp").forward(request, response);
				return;
			} else {
				//Download the file
				try (InputStream input = fileParts.get(f).getInputStream()) {
					Files.copy(input, files[f].toPath(), StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}


		//defines extension of the output file
		String extension = ".txt";
		if (pdf) {
			extension = ".pdf";
		}



		//Gives out the information of how to download the eventually outputted file
		filePaths[fileParts.size()] = (filePaths[0].substring(0,filePaths[0].lastIndexOf('.')) + "_OCR" + extension).replace("tmp_", "");
		fileNames[fileParts.size()] = fileNames[0].replace("tmp_", "");

		messages.put("link", String.format("/Porbiota_Web_Apps/downloadResults?file=%s_OCR" + extension,
				files[0].getName().substring(0,  files[0].getName().lastIndexOf('.')).replace("tmp_", "")));
		messages.put("fileName", String.format("The file name is %s_OCR" + extension,
				files[0].getName().substring(0,  files[0].getName().lastIndexOf('.')).replace("tmp_", "")));

		messages.put("name", "Be sure to save the link below or the file name!");
		messages.put("success", "Download your file when it is ready here");
		request.getRequestDispatcher("/WEB-INF/OCR.jsp").forward(request, response);

		request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
		final AsyncContext acontext = request.startAsync();
		acontext.start(new Runnable() {
			@Override
			public void run() {
				acontext.complete();
				try {
					(new OCR()).getText(filePaths, pageMode, language, pdf);

				} catch (ArrayIndexOutOfBoundsException | IOException | TesseractException e) {
					try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(Databases.downloadsFolder.getAbsolutePath()+"/"+
							fileNames[fileParts.size()].substring(0, fileNames[fileParts.size()].lastIndexOf('.')) + "_OCR.txt"),
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
		request.getRequestDispatcher("/WEB-INF/OCR.jsp").forward(request, response);
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