package csvEditorFullLoad;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TableJoiner {

	public static void main(String[] args) throws IOException {
		try {
			String fileNameLeft = args[0];	
			Path myPathLeft = Paths.get(fileNameLeft);
			BufferedReader brLeft = Files.newBufferedReader(myPathLeft,StandardCharsets.UTF_8);
					try {
						String fileNameRight = args[1];
						Path myPathRight = Paths.get(fileNameRight);
						BufferedReader brRight = Files.newBufferedReader(myPathRight,StandardCharsets.UTF_8);
								try {
									String fileNameOutput = args[2];
									Path myPathOutput = Paths.get(fileNameOutput);
									BufferedWriter bw = Files.newBufferedWriter(myPathOutput,StandardCharsets.UTF_8);

											for (String lineLeft = brLeft.readLine(); lineLeft != null; lineLeft = brLeft.readLine()) {
												String lineRight = brRight.readLine();
												if (lineRight == null) {
													break;
												}
												bw.write(lineLeft+","+lineRight+"\r");
											}
									System.out.println("Complete! Outputted file to " +myPathOutput );
									bw.close();
								} catch (IOException|InvalidPathException e) {
									System.out.println ("Output file in use or invalid!");
									System.exit(1);
								}		} catch (IOException|InvalidPathException e) {
									System.out.println ("No Right File!");
									System.exit(1);
								}
		} catch (IOException|InvalidPathException e) {
			System.out.println ("No Left File!");
			System.exit(1);
		}
	}

}
