package YAV_Election_Analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Provides static methods for reading the contents of a .cfg into a YAV_Config object
 * 
 * @author Grant Barker, for use with Louisiana Youth & Government conference elections
 */

public class YAV_CFG_Reader {
	public static final String indent = "    ";
	public enum State {ELECT, CANDID};					// Indicates which section of the .cfg is being read

	/**
	 * Reads the given .cfg file into the given YAV_Config object and returns true if nothing went wrong
	 * @param cfgFile Text file to be read from
	 * @param cfg YAV_Config object to pass the relevant data to
	 * @return True if the .cfg was fully parsed correctly, false otherwise
	 */
	public static boolean go(File cfgFile, YAV_Config cfg) {
		try(BufferedReader br = new BufferedReader(new FileReader(cfgFile.getAbsolutePath()))) {
			State section = State.ELECT;		// Sets the starting state for the what the BufferedReader is reading
			int lineCount = 0;					// Counter to keep track of what line of the .cfg we're on
			String currString = "";				// General String used to add candidates to the current election
			cfg.clearAll();
			System.out.println("Elections:");
			for(String line; (line = br.readLine()) != null; ) {
				lineCount++;
				if (!line.isEmpty()) {
					if (!line.substring(0, 1).equals(System.getProperty("line.separator")) &&
						!line.substring(0, 1).equals("#")) {
						try {
							if (line.substring(0, 1).equals("-")) section = State.CANDID;
							else section = State.ELECT;
							switch (section) {
							case ELECT:
								currString = line.substring(0, line.indexOf(','));
								Integer numOfWinners = Integer.parseInt(line.substring(line.indexOf(',') + 1,
																		line.length()).replaceAll("\\D+", ""));
								cfg.addElection(currString, numOfWinners);
								System.out.print(indent + currString + ": " + numOfWinners + " position");
								if (numOfWinners > 1) System.out.println("s");
								else System.out.println();
								break;
							case CANDID:
								cfg.addCanidate(currString, line.substring(1));
								System.out.println(indent + indent + line.substring(1));
								break;
							}
						}
						catch (NumberFormatException e) {
							return cfgError("Expecting a number (usually after a comma + space)", lineCount, line, cfg);
						}
						catch (StringIndexOutOfBoundsException e) { // Usually reached if 
							return cfgError("Expecting a new election name on this line, remove any excess text", lineCount, line, cfg);
						}
					}
				}
			}
		}
		catch (FileNotFoundException e) {
			return cfgError("The .cfg file could not be found.", cfg);
		}
		catch (IOException e) {
			return cfgError("Something went wrong. Maybe the .cfg file isn't formatted correctly?", cfg);
		}
		return true;
	}
	
	private static boolean cfgError(String error, int lineCount, String line, YAV_Config cfg) {
		System.err.println("Error: " + error);
		System.err.println("Line " + lineCount + ": " + line);
		cfg.clearAll();
		return false;
	}
	
	private static boolean cfgError(String error, YAV_Config cfg) {
		System.err.println("Error: " + error);
		cfg.clearAll();
		return false;
	}
}
