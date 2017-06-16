package YAV_Election_Analyzer;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A class for storing all essential information about how a ballot looks.
 * Stores all information represented in the .cfg file.
 * 
 * @author Grant Barker, for use with Louisiana Youth & Government conference elections
 */

public class YAV_Config {
	protected int numOfElections, numOfPositions, numOfCandidates;
	protected ArrayList<String> elections;
	protected HashMap<String, Integer> positions;
	protected HashMap<String, ArrayList<String>> candidates;

	/**
	 * Initialize all the variables
	 */
	public YAV_Config() {
		elections = new ArrayList<String>();
		positions = new HashMap<String, Integer>();
		candidates = new HashMap<String, ArrayList<String>>();
		numOfElections = 0;
		numOfPositions = 0;
		numOfCandidates = 0;
	}

	/**
	 * Create a new election with the number of positions indicated (how many winners there are)
	 * @param election Name of the election/top position (eg. "Speaker of the House")
	 * @param numOfPositionsAvailable How many positions are available (eg. Speaker has 4)
	 */
	public void addElection(String election, int numOfPositionsAvailable) {
		elections.add(election);
		positions.put(election, numOfPositionsAvailable);
		candidates.put(election, new ArrayList<String>());
		numOfElections++;
		numOfPositions += numOfPositionsAvailable;
	}

	/**
	 * Removes the indicated election
	 * @param election Name of the election to remove
	 */
	public void removeElection(String election) {
		numOfPositions -= positions.get(election);
		numOfElections--;
		candidates.remove(election);
		positions.remove(election);
		elections.remove(election);
	}

	/**
	 * Adds the candidate to the list of candidates for the specified election
	 * @param election Name of the election/top position (eg. "Speaker of the House")
	 * @param candidate Name of the candidate
	 */
	public void addCanidate(String election, String candidate) {
		candidates.get(election).add(candidate);
		numOfCandidates++;
	}

	/**
	 * Erases all data previously entered
	 */
	public void clearAll() {
		elections.clear();
		positions.clear();
		candidates.clear();
		numOfElections = 0;
		numOfPositions = 0;
		numOfCandidates = 0;
	}

	/**
	 * @return String representation of the config data
	 */
	public String toString() {
		return toString("    ");
	}

	/**
	 * Backbone method for toString(), which allows for variable indentation of the lists
	 * @param indent String that will precede every entry item in the lists (eg. "    ")
	 * @return String representation of the config date with the given indent
	 */
	public String toString(String indent) {
		StringBuilder returner = new StringBuilder();
		for (String s : elections) {
			returner.append(s + " (" + positions.get(s).toString() + " position");
			if (positions.get(s) > 1) returner.append("s");
			returner.append(") candidates:" + System.getProperty("line.separator") + indent);
			for (String t : candidates.get(s)) returner.append(t + System.getProperty("line.separator") + indent);
			returner.setLength(returner.length() - indent.length());
		}
		if (returner.length() > 1) returner.setLength(returner.length() - 1);
		return returner.toString();
	}
}
