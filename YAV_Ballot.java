package YAV_Election_Analyzer;

/**
 * A class for keeping track of all the data in an individual's vote.
 * 
 * @author Grant Barker, for use with Louisiana Youth & Government conference elections
 */

public class YAV_Ballot {
	/**
	 * Row # = election (number determined by the order of YAV_Config.elections)
	 * Column # = ranking number (the String in this location is a candidate's name, "" is no vote)
	 * Rectangular 2D array, no rows of unequal length (length determined by maxNumOfCandidates)
	 */
	protected String[][] candidateRanking;
	
	/**
	 * Creates the bounds for a given ballot and sets all votes to the empty String
	 * @param int numElections - Number of elections being evaluated (including ones not used by this ballot)
	 * @param int maxNumOfCandidates - Maximum number of candidates running for any given position
	 */
	public YAV_Ballot(int numElections, int maxNumOfCandidates) {
		candidateRanking = new String[numElections][maxNumOfCandidates];
		// Set each item as an empty String so elections the person didn't vote in aren't counted
		String[] nullVoteRow = new String[maxNumOfCandidates];
		for (int i = 0; i < nullVoteRow.length; i++) {
			nullVoteRow[i] = "";
		}
		for (int i = 0; i < candidateRanking.length; i++) {
			this.setNth(i, nullVoteRow.clone());
		}
	}
	
	/**
	 * Getters/setters for the array of preferential votes (0-indexed)
	 * No checking that the parameters are within the bounds of the array, yet, so be careful (sorry)
	 */
	public String getNN(int electionNum, int choiceNum) {
		return candidateRanking[electionNum][choiceNum];
	}
	public void setNN(int electionNum, int choiceNum, String choice) {
		candidateRanking[electionNum][choiceNum] = choice;
	}
	
	/**
	 * Getter/setter for Nth row to quickly initialize a whole row
	 */
	public String[] getNth(int electionNum) {
		return candidateRanking[electionNum];
	}
	public void setNth(int electionNum, String... choices) {
		for (int i = 0; i < candidateRanking[electionNum].length; i++) {
			if (i < choices.length) candidateRanking[electionNum][i] = choices[i];
			else break;
		}
	}
	
	/**
	 * Returns all relevant info contained on the ballot in a debug-friendly String
	 */
	public String toString() {
		String result = "";
		for (int i = 0; i < candidateRanking.length; i++) {
			if (candidateRanking[i][0].equals("")) result = result.concat("null ");
			else {
				for (int j = 0; j < candidateRanking[i].length; j++) {
					result = result.concat(candidateRanking[i][j] + " ");
				}
			}
			result = result.substring(0, result.length() - 1);
			result = result.concat(System.getProperty("line.separator"));
		}
		if (result.contains(System.getProperty("line.separator"))) result = result.substring(0, result.length() - 1);
		return result;
	}
}
