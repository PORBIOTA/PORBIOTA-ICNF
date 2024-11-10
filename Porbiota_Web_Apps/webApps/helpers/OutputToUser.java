package helpers;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

/**
 * Class to allow users to download files from the server
 * 
 * @author Tomé Neves de Matos
 *
 */

public class OutputToUser extends HttpServlet {


	private static final long serialVersionUID = 9051342813275221514L;

	/**
	 * Outputs the given file to the user
	 * @param response HttpServletResponse The http servlet response from the request
	 * @param filePath String The path of file in the server to send to the user 
	 * @param context ServletContext The servlet context
	 * @param incomplete boolean If true it does not deleted the sent file from the server
	 * @throws ServletException
	 * @throws IOException
	 */
	public static void output(HttpServletResponse response, String filePath, ServletContext context, boolean incomplete) 
			throws ServletException, IOException {


		File downloadFile = new File(filePath);
		InputStream inStream;
		String mimeType; 

		//If the helper word is being asked for, set the parameters
		if (downloadFile.getName().equalsIgnoreCase("Tag Macros.docm")) {
			inStream =  context.getResourceAsStream("/META-INF/Resources/Tag Macros.docm");
			mimeType = context.getMimeType("Tag Macros.docm");
			response.setContentLength(25292); //Must change with file change
		} else {
			inStream = new FileInputStream(downloadFile);
			mimeType = context.getMimeType(filePath); 	
			response.setContentLength((int) downloadFile.length()); 
		}
	

		if (mimeType == null) {        
			// set to binary type if MIME mapping not found
			mimeType = "application/octet-stream";
		}

		// modifies response
		response.setContentType(mimeType);

		// forces download
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
		response.setHeader(headerKey, headerValue);

		ServletOutputStream outStream = response.getOutputStream();

		//Outputs the file to the user
		byte[] buffer = new byte[4096];
		int bytesRead = -1;

		while ((bytesRead = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, bytesRead);
		}

		inStream.close();
		outStream.flush();
		outStream.close();  

		//If the downloaded file is imcomplete or the macros document, do not delete
		if (!incomplete && !downloadFile.getName().equalsIgnoreCase("Tag Macros.docm")) {
			downloadFile.delete();
		}
	}
}

