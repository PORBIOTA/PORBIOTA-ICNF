package helpers;


import java.io.File;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletContext;

import org.json.simple.JSONObject;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

/**
 * 
 * Class that handles the databases and stores their locations
 * 
 * @author Tomé Neves de Matos
 *
 */

public class Databases {

	public static DB localeDB;
	public static DB speciesDB;

	public static HTreeMap<String, JSONObject>  localeMap;
	public static HTreeMap<String, JSONObject[]>  speciesMap;

	private static File database; 
	public static File downloadsFolder;
	public static String tagIndexDir;

	/**
	 * Constructor to initialize the databases
	 * @param path String Path to the folder where the database will be created
	 * @param context ServletContext The context of the servlet
	 */
	@SuppressWarnings("unchecked")
	public Databases(String path, ServletContext context) {	

		//use DBMaker to create a DB object stored on disk
		//provide output location of list
		database = new File(path + "/Database/");
		downloadsFolder = new File(path + "/DownloadsCache/");
		tagIndexDir = path + "/Database/TaggerDatabase/Index";


		if(!database.exists()) {
			database.mkdir();
		}
		if(!downloadsFolder.exists()) {
			downloadsFolder.mkdir();
		}


		localeDB = DBMaker.fileDB(database.getAbsolutePath() + "/localeCache.db").transactionEnable().closeOnJvmShutdown().fileLockDisable().make();
		speciesDB = DBMaker.fileDB(database.getAbsolutePath() + "/speciesCache.db").transactionEnable().closeOnJvmShutdown().fileLockDisable().make();


		//Create the database arrayList
		localeMap = (HTreeMap<String, JSONObject>) localeDB.hashMap("LocationCache").keySerializer(Serializer.STRING ).createOrOpen();
		speciesMap = (HTreeMap<String, JSONObject[]>) speciesDB.hashMap("SpeciesCache").keySerializer(Serializer.STRING ).createOrOpen();

		try {
			if(!(new File(database.getAbsolutePath()+"/TaggerDatabase/")).exists()) {
				System.out.println("Creating tag data...");
				UnzipUtility.unzip(context.getResourceAsStream("/META-INF/Resources/TaggerDatabase.zip"), database.getAbsolutePath());	

			}
		} catch (IOException | NullPointerException e) {
			System.out.println("Could not create tag data.");
			e.printStackTrace();
		}

	}


}
