package webPages;


import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import helpers.FolderChooser;

/**
 * Servlet to run at startup and boot the necessary folders and files
 * 
 * @author Tomé Neves de Matos
 */
@WebServlet("/Startup")
public class Startup extends HttpServlet {
	private static final long serialVersionUID = 1L;

	ServletContext context;
	public static boolean windows;

	@Override
	public void init() { //Runs at startup
		//Gives to the user the choice of the folder where to save the cache 
		this.context = getServletContext();
		Startup.windows = System.getProperty("os.name").contains("Windows");
        ImageIO.scanForPlugins();
		
		new FolderChooser(context); 
		
	}


	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}


	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
