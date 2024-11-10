package helpers;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import webPages.Startup;

/**
 * 
 * Class to select the folder for the databases
 * 
 * @author Tomé Neves de Matos
 *
 */
public class FolderChooser extends JPanel {

	private static final long serialVersionUID = 3506415038888068406L;
	public static File cacheFolder;
	public static ServletContext context;

	public FolderChooser(ServletContext context) {
		FolderChooser.context = context;
		choose();

	}
	
	/**
	 * Opens a dialog to choose the folder in windows or reads the folder location from the config file in other OSs
	 */

	public void choose() {       

		
		if (Startup.windows) { //If running on Windows

			JFileChooser chooser = new JFileChooser(); 
			chooser.setCurrentDirectory(new File("C:/TesteRun/"));
			chooser.setDialogTitle("Porbiota Web Apps - Choose the chache folder:");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			//
			// disable the "All files" option.
			//
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setVisible(true);
			//    
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
				FolderChooser.cacheFolder = chooser.getSelectedFile();
				new Databases(FolderChooser.cacheFolder.getAbsolutePath(), FolderChooser.context); //Initializes the databases
				try {
					if(!(new File(cacheFolder.getAbsolutePath()+"/tessdata/")).exists()) {
						System.out.println("Creating OCR data...");
						UnzipUtility.unzip(context.getResourceAsStream("/META-INF/Resources/tessdata.zip"), cacheFolder.getAbsolutePath());
					}
				} catch (IOException | NullPointerException e) {
					System.out.println("Could not create OCR data.");
					e.printStackTrace();
				}
			}
			else {
				choose();
			}	


		} else { //if running on other OS
			Properties props = new Properties();
			try {
				props.load(context.getResourceAsStream("/META-INF/Resources/config.properties"));
			} catch (IOException e) {
				System.out.println("Failed to load properties!");
				e.printStackTrace();
			}
			
			System.out.println("Using folder "+ props.getProperty("PATH"));
			
			FolderChooser.cacheFolder = new File(props.getProperty("PATH"));
			if (!FolderChooser.cacheFolder.exists()) {
				FolderChooser.cacheFolder.mkdir();
			}
			
			new Databases(FolderChooser.cacheFolder.getAbsolutePath(), FolderChooser.context); //Initializates the databases
			try {
				if(!(new File(cacheFolder.getAbsolutePath()+"/tessdata/")).exists()) {
					System.out.println("Creating OCR data...");
					UnzipUtility.unzip(context.getResourceAsStream("/META-INF/Resources/tessdata.zip"), cacheFolder.getAbsolutePath());
				}
			} catch (IOException | NullPointerException e) {
				System.out.println("Could not create OCR data.");
				e.printStackTrace();
			}
		}
	}
}

