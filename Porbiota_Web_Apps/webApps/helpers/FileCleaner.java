package helpers;

import static org.apache.commons.io.filefilter.TrueFileFilter.TRUE;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AgeFileFilter;
/**
 * 
 * Class to clean up older files
 * 
 * @author Tomé Neves de Matos
 *
 */
public class FileCleaner implements Runnable {

	@Override
	public void run() {
		try {
			System.out.println("Deleting files older than 7 days...");

			LocalDate thresholdDate = LocalDate.now().minusDays(7);
			Date threshold = Date.from(thresholdDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

			Iterator<File> filesToDelete = FileUtils.iterateFiles(Databases.downloadsFolder, new AgeFileFilter(threshold), TRUE);

			while (filesToDelete.hasNext()) {
				filesToDelete.next().delete();
			}
		} catch (RuntimeException e) {
			System.out.println("Error deleting files.");
		}
	}
}