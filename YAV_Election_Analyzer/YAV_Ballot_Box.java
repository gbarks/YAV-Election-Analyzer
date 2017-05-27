package YAV_Election_Analyzer;

import java.util.HashSet;

/**
 * A class for housing the set of ballots. Helps to build up that set from the .tsv
 * 
 * @author Grant Barker, for use with Louisiana Youth & Government conference elections
 */

public class YAV_Ballot_Box {
	protected HashSet<YAV_Ballot> box;
	protected int numElections, maxNumOfCandidates, numCandidates[], numPositions[];
	protected String electionOrder[], candidateOrder[][];
	private YAV_Ballot currentBallot;

	/**
	 * Creates an empty ballot box with the parameters indicated by the .cfg file
	 * @param cfg Used to create arrays sized to how many elections, positions, and candidates there are
	 */
	public YAV_Ballot_Box(YAV_Config cfg) {
		box = new HashSet<YAV_Ballot>();
		numElections = cfg.numOfElections;
		electionOrder = new String[numElections];
		numPositions  = new int[numElections];
		numCandidates = new int[numElections];
		maxNumOfCandidates = 1;
		candidateOrder = new String[numElections][0];
		for (int i = 0; i < numElections; i++) {
			electionOrder[i] = cfg.elections.get(i);
			numPositions[i] = cfg.positions.get(electionOrder[i]);
			numCandidates[i] = cfg.candidates.get(electionOrder[i]).size();
			if (maxNumOfCandidates < numCandidates[i]) maxNumOfCandidates = numCandidates[i];
			candidateOrder[i] = new String[numCandidates[i]];
			for (int j = 0; j < numCandidates[i]; j++) {
				candidateOrder[i][j] = cfg.candidates.get(electionOrder[i]).get(j);
			}
		}
	}

	/**
	 * Takes a String array such that 0-i correspond to each candidate ranking in the first election
	 * 							  (i+1)-j correspond to each candidate ranking in the second election
	 * 							  (j+1)-k correspond to each candidate ranking in the third election
	 *  and transfers that information to a YAV_Ballot object before adding it to the ballot box
	 * @param tsvBallot Array of Strings read from the .tsv to represent a ballot
	 */
	public void add(String[] tsvBallot) {
		currentBallot = new YAV_Ballot(numElections, maxNumOfCandidates);
		int k = 0;
		for (int i = 0; i < numElections; i++) {
			for (int j = 0; j < numCandidates[i]; j++) {
				try {
					currentBallot.setNN(i, j, tsvBallot[k + j]);
				}
				catch (ArrayIndexOutOfBoundsException e) {
					// Do nothing, the ballot just didn't have a vote in the final column(s)
				}
			}
			k += numCandidates[i];
		}
		box.add(currentBallot);
	}

	/**
	 * Empties the ballot box
	 */
	public void clear() {
		box.clear();
	}

	/**
	 * @return String representation of the ballot box
	 */
	public String toString() {
		StringBuilder returner = new StringBuilder();
		for (YAV_Ballot b : box) {
			returner.append(b + System.getProperty("line.separator"));
		}
		if (returner.length() > 1) returner.setLength(returner.length() - 1);
		return returner.toString();
	}
	
	/**
	 * Returns all relevant info in a human-readable String
	 */
	public String toReadableString() {
		StringBuilder returner = new StringBuilder();
		returner.append("Ballots will not necessarily be ordered the same way they were entered."
						+ System.getProperty("line.separator"));
		int count = 0;
		for (YAV_Ballot b : box) {
			count++;
			String num = "";
			if 		(count < 10) num = "00" + count;
			else if (count < 100) num = "0" + count;
//			else if (count < 1000) num = "0" + count;
			else 					num = "" + count;
			for (int i = 0; i < numElections; i++) {
				returner.append(num + "-" + i + ": ");
				for (int j = 0; j < numCandidates[i]; j++) {
					returner.append(b.getNN(i, j));
					if (j < numCandidates[i] - 1) returner.append(", ");
					else returner.append(System.getProperty("line.separator"));
				}
			}
		}
		if (returner.length() > 1) returner.setLength(returner.length() - 1);
		return returner.toString();
	}
}
