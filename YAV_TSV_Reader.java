package YAV_Election_Analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Provides static methods for importing votes from a .tsv into a YAV_Ballot_Box object
 * 
 * @author Grant Barker, for use with Louisiana Youth & Government conference elections
 */

public class YAV_TSV_Reader {
	/**
	 * Searches the .tsv for at least one instance of each candidate's name and verifies the column alignment
	 * Reads the given .tsv file and adds ballots (from ballotStartColumn to the newline) to ballot box ybb
	 * @param tsv Tab-Separated Value file to be read from
	 * @param ybb YAV_Ballot_Box where the votes will be deposited
	 * @param nonBallotFirstRow Checks for appropriate column headers if true, starts counting ballots otherwise
	 * @param ballotStartColumn Where vote data begins in the .tsv (usually col. 6 or 'F' (this is 1-indexed))
	 * @return True if the .tsv was fully parsed correctly, false if the .tsv was misaligned or missing a name
	 */
	public static boolean go(File tsv, YAV_Ballot_Box ybb, boolean nonBallotFirstRow, int ballotStartColumn) {
		/**
		 * Checking that the .tsv contains at least one instance of each candidate's name
		 */
		ArrayList<String> candidateList = new ArrayList<String>();
		for (int i = 0; i < ybb.numElections; i++) {
			for (int j = 0; j < ybb.numCandidates[i]; j++) {
				candidateList.add(ybb.candidateOrder[i][j]);
			}
		}
		candidateList = candidateSearch(tsv, candidateList, ballotStartColumn); // Helper function
		if (!candidateList.isEmpty()) {
			StringBuilder notFound  = new StringBuilder();
			notFound.append(candidateList.get(0));
			if (candidateList.size() == 1) {
				return tsvError("Could not find candidate \"" + notFound.toString() + "\" anywhere in the .tsv.");
			}
			else if (candidateList.size() == 2) {
				notFound.append("\" and \"" + candidateList.get(1));
			}
			else {
				for (int i = 1; i < candidateList.size() - 1; i++) {
					notFound.append("\", \"" + candidateList.get(i));
				}
				notFound.append("\", and \"" + candidateList.get(candidateList.size() - 1));
			}
			return tsvError("Could not find candidates \"" + notFound.toString() + "\" anywhere in the .tsv.");
		}
		try(BufferedReader br = new BufferedReader(new FileReader(tsv.getAbsolutePath()))) {
			int lineCount = 0; // Row counter for debugging
			for(String line; (line = br.readLine()) != null; ) {
				lineCount++;
				if (nonBallotFirstRow) {
					/**
					 * Checking the .tsv for column names that correspond to the election the ballot box expects
					 */
					try {
						String[] lineSplit = line.split("\t");
						int col  = ballotStartColumn - 1;
						for (int i = 0; i < ybb.numElections; i++) {
							for (int j = 0; j < ybb.numCandidates[i]; j++) {
								if (!lineSplit[col].toLowerCase().contains(ybb.electionOrder[i].toLowerCase())) {
									return tsvError("Could not find election \"" + ybb.electionOrder[i] +
													"\" in Column " + (char) ('A' + col) + " header.");
								}
								col++;
							}
						}
					}
					catch (ArrayIndexOutOfBoundsException e) {
						return tsvError("Did not match the bounds determined by the .cfg file.", lineCount, ybb);
					}
					nonBallotFirstRow = false;
				}
				else {
					/**
					 * Reading the ballots
					 */
					try {
						String[] lineSplit = line.split("\t");
						String[] tsvBallot = new String[lineSplit.length - (ballotStartColumn - 1)];
						for (int i = 0; i < tsvBallot.length; i++) {
							tsvBallot[i] = lineSplit[i + (ballotStartColumn - 1)];
						}
						ybb.add(tsvBallot);
					}
					catch (ArrayIndexOutOfBoundsException e) {
						return tsvError("Did not match the bounds determined by the .cfg file.", lineCount, ybb);
					}
				}
		    }
		}
		catch (FileNotFoundException e) {
			return tsvError("The .tsv file could not be found.");
		}
		catch (IOException e) {
			return tsvError("Something went wrong. Maybe the .tsv file isn't formatted correctly?");
		}
		System.out.println("Counted " + ybb.box.size() + " cast ballots.");
		return true;
	}
	
	private static boolean tsvError(String error, int lineCount, YAV_Ballot_Box ybb) {
		System.err.println("Error at row " + lineCount + ": " + error);
		System.err.println("Counted " + ybb.box.size() + " cast ballots.");
		return false;
	}
	
	private static boolean tsvError(String error) {
		System.err.println("Error: " + error);
		return false;
	}

	/**
	 * Searches the given .tsv file to check that all candidates are listed at least once in the document
	 * @param tsv Tab-Separated Value file to be read from
	 * @param candidates List of candidates to search for in the .tsv
	 * @return Empty list if all candidates were found, all missing candidates otherwise
	 */
	private static ArrayList<String> candidateSearch(File tsv, ArrayList<String> candidates, int ballotStartColumn) {
		try(BufferedReader br = new BufferedReader(new FileReader(tsv.getAbsolutePath()))) {
			for(String line; (line = br.readLine()) != null; ) {
				try {
					String[] lineSplit = line.split("\t");
					for (int i = ballotStartColumn - 1; i < lineSplit.length; i++) {
						if (candidates.contains(lineSplit[i])) {
							candidates.remove(lineSplit[i]);
						}
					}
				}
				catch (ArrayIndexOutOfBoundsException e) {}
			}
		}
		catch (FileNotFoundException e) {}
		catch (IOException e) {}
		return candidates;
	}
}
