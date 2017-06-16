package YAV_Election_Analyzer;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Takes a YAV_Ballot_Box and analyzes all the votes according to Instant Runoff Voting rules
 * See http://wiki.electorama.com/wiki/Instant-runoff_voting for single-winner IRV information
 * See https://groups.google.com/forum/#!topic/openstv/m9sRnN7yQTs for how multiple-winner IRV
 *  is implemented here (by re-running the election with previous winners withdrawn)
 * 
 * @author Grant Barker, for use with Louisiana Youth & Government conference elections
 */

public class IRV_Analysis {
	//        Election #, PositionRank #, Pass #, Map from Candidate to Vote Count
	protected ArrayList<ArrayList<ArrayList<HashMap<String, Integer>>>> voteTally;
	//        Election #, PositionRank # (0 being highest), Candidate
	protected ArrayList<ArrayList<String>> winnerOrder;
	private final String indent = "    ";
	private DecimalFormat df;

	/**
	 * Iterates over each election and finds the winning candidates for each available position
	 * Fills the voteTally with each candidate's # of votes in each IRV round for each position in each election
	 * @param ybb Filled ballot box
	 */
	public IRV_Analysis(YAV_Ballot_Box ybb) {
		voteTally = new ArrayList<ArrayList<ArrayList<HashMap<String, Integer>>>>();
		winnerOrder = new ArrayList<ArrayList<String>>();
		df = new DecimalFormat("##.##");
		df.setRoundingMode(RoundingMode.DOWN);
		for (int i = 0; i < ybb.numElections ; i++) {
			ArrayList<ArrayList<HashMap<String, Integer>>> ithElection = new ArrayList<ArrayList<HashMap<String, Integer>>>();
			ArrayList<String> alreadyWon = new ArrayList<String>();
			winnerOrder.add(alreadyWon);
			for (int j = 0; j < ybb.numPositions[i]; j++) {
				if (j < ybb.numCandidates[i]) {
					ithElection.add(findSingleWinner(ybb, i, alreadyWon));
				}
			}
			voteTally.add(ithElection);
		}
		System.out.println();
		System.out.println("============ ELECTION WINNER ORDER ============");
		for (int i = 0; i < ybb.numElections ; i++) {
			System.out.println(ybb.electionOrder[i] + ":");
			for (int j = 0; j < winnerOrder.get(i).size(); j++) {
				System.out.println(indent + "#" + Integer.toString(j + 1) + ": " + winnerOrder.get(i).get(j));
			}
		}
		System.out.println("============ IRV ANALYSIS COMPLETE ============");
		System.out.println();
	}

	/**
	 * Iterates over ballots for a given election (ignoring blank entries) and tallies 1st-choice votes by IRV rules
	 *  If someone's 1st (or 2nd, and so on) -choice vote is in the alreadyWon list, it checks their next vote
	 *  If someone hasn't fully filled out the ballot and the checker finds a blank entry down-ballot, vote is scrapped
	 *  With IRV rules, lowest scoring candidates are eliminated if no one receives >50% and voting tally is re-run
	 *  If the final two candidates are tied, calls the "ranked-delta" tie breaker function
	 * @param ybb Filled ballot box
	 * @param electionNum Which election
	 * @param alreadyWon Candidates who have already won and will be ignored in this pass
	 * @return HashMap of each candidate to how many votes they received, in a list representing each round of IRV
	 */
	private ArrayList<HashMap<String, Integer>> findSingleWinner(YAV_Ballot_Box ybb, int electionNum,
																 ArrayList<String> alreadyWon) {
		ArrayList<HashMap<String, Integer>> returner = new ArrayList<HashMap<String, Integer>>();
		ArrayList<String> eliminatedCandidates = new ArrayList<String>();
		System.out.println(ybb.electionOrder[electionNum] + " Position #" + (alreadyWon.size() + 1) + ":");
		for (int i = 0; i < ybb.numCandidates[electionNum]; i++) {
			HashMap<String, Integer> ithPass = new HashMap<String, Integer>();
			int totalVotes = 0;
			if (eliminatedCandidates.size() == 0) System.out.println(indent + "Pass #" + (i + 1) + ":");
			else System.out.println(indent + "Pass #" + (i + 1) + " (eliminated " +
									eliminatedCandidates.get(eliminatedCandidates.size()-1) + "):");
			for (YAV_Ballot b : ybb.box) {
				for (int j = 0; j < ybb.numCandidates[electionNum]; j++) {
					if (!alreadyWon.contains(b.getNN(electionNum, j)) &&
							!eliminatedCandidates.contains(b.getNN(electionNum, j)) &&
							!b.getNN(electionNum, j).equals("")) {
						Integer newTally = 1;
						if (ithPass.containsKey(b.getNN(electionNum, j))) newTally += ithPass.get(b.getNN(electionNum, j));
						ithPass.put(b.getNN(electionNum,  j), newTally);
						totalVotes++;
						break;
					}
				}
			}
			if (i == 0) { // Check for candidates with 0 votes in the first round
				for (String c : ybb.candidateOrder[electionNum]) {
					if (!ithPass.containsKey(c) && !eliminatedCandidates.contains(c) && !alreadyWon.contains(c)) {
						ithPass.put(c, 0);
					}
				}
			}
			for (String c : ithPass.keySet()) {
				System.out.print(indent + indent + c + ": " + ithPass.get(c) + " vote");
				if (ithPass.get(c) != 1) System.out.print("s");
				System.out.println(" (" + df.format(100 * ((float) ithPass.get(c) / totalVotes)) + "%)");
			}
			for (String c : ithPass.keySet()) {
				if ((double) ithPass.get(c) / totalVotes > 0.5) {
					returner.add(ithPass);
					alreadyWon.add(c);
					return returner;
				}
				else if ((double) ithPass.get(c) / totalVotes == 0.5 && ithPass.size() == 2) {
					System.out.println("Tie between 2 candidates for this position, initiating ranked-delta tie breaker.");
					String d = "";
					for (String e : ithPass.keySet()) {
						if (e != c && (double) ithPass.get(e) / totalVotes == 0.5) {
							d = e;
							break;
						}
					}
					if (d.equals("")) {
						System.out.println("Something went wrong, couldn't find " + c + "'s competetor.");
						alreadyWon.add(c);
					}
					else {
						alreadyWon.add(rankedTieBreaker(ybb, electionNum, c, d));
					}
					returner.add(ithPass);
					return returner;
				}
			}
			ArrayList<String> leastPopular = new ArrayList<String>();
			int lowestTally = -1;
			for (String c : ithPass.keySet()) {
				if (leastPopular.isEmpty()) {
					leastPopular.add(c);
					lowestTally = ithPass.get(c);
				}
				else {
					if (ithPass.get(c) < lowestTally) {
						leastPopular.clear();
						leastPopular.add(c);
						lowestTally = ithPass.get(c);
					}
					else if (ithPass.get(c) == lowestTally) {
						leastPopular.add(c);
					}
				}
			}
			eliminatedCandidates.addAll(leastPopular);
		}
		System.out.println("Something broke, a winner was not found for some reason.");
		return returner;
	}

	/**
	 * Uses a "ranked-delta" analysis (coined by the author, with help from Kathryn James), described as follows:
	 *  Determines the "rank" of the two candidates in each ballot (from 0 (best) to n-1 (worst))
	 *  If one of the two candidates isn't present in the ballot, assigns that candidate a rank of n
	 *  Takes the "delta" between each ranking; a negative delta favors candidateA, positive delta favors candidateB
	 *  The cumulative delta across all ballots determines who wins the tie
	 *  If the final delta == 0, reruns the delta calculation without considering ballots with only one candidate
	 *  If the final delta still == 0, calls the random tie breaker function
	 * @param ybb Filled ballot box
	 * @param electionNum Which election
	 * @param candidateA First tied candidate
	 * @param candidateB Second tied candidate
	 * @return Ranked-delta winner, or a random winner if the candidates' deltas are tied
	 */
	public String rankedTieBreaker(YAV_Ballot_Box ybb, int electionNum, String candidateA, String candidateB) {
		String tieWinner = "";
		int totalDelta = 0;
		for (YAV_Ballot b : ybb.box) {
			int Arank = -1;
			int Brank = -1;
			for (int i = 0; i < ybb.numCandidates[electionNum]; i++) {
				if (b.getNN(electionNum, i).equals(candidateA)) Arank = i;
				if (b.getNN(electionNum, i).equals(candidateB)) Brank = i;
			}
			if (Arank != -1 && Brank != -1) {
				totalDelta += Arank - Brank;
			}
			else if (Arank != -1) {
				Brank = ybb.numCandidates[electionNum] + 1;
				totalDelta += Arank - Brank;
			}
			else if (Brank != -1){
				Arank = ybb.numCandidates[electionNum] + 1;
				totalDelta += Arank - Brank;
			}
		}
		if (totalDelta == 0) {
			for (YAV_Ballot b : ybb.box) {
				int Arank = -1;
				int Brank = -1;
				for (int i = 0; i < ybb.numCandidates[electionNum]; i++) {
					if (b.getNN(electionNum, i).equals(candidateA)) Arank = i;
					if (b.getNN(electionNum, i).equals(candidateB)) Brank = i;
				}
				if (Arank != -1 && Brank != -1) {
					totalDelta += Arank - Brank;
				}
				/*
				Removed the two checks from the otherwise identical above loop which assigns a
				 low rank to candidates unlisted on a ballot with the other candidate. Instead,
				 these ballots which do not compare both candidates are simply ignored, which
				 was necessary for drawing a conclusion in an otherwise perfectly tied race.
				 */
			}
			// If rerunning the loop without those checks didn't work, assigns a random winner
			if (totalDelta == 0) {
				tieWinner = randomTieBreaker(candidateA, candidateB);
				System.out.println("Uh oh, we've got a fully unbreakable tie here. Random winner: " + tieWinner);
				return tieWinner;
			}
		}
		if (totalDelta < 0) {
			tieWinner = candidateA;
			System.out.print("Total ranking difference favors " + tieWinner + " by a delta of ");
			if (totalDelta == -1) System.out.println(-1 * totalDelta + " point.");
			else System.out.println(-1 * totalDelta + " points.");
		}
		else {
			tieWinner = candidateB;
			System.out.println("Total ranking difference favors " + tieWinner + " by a delta of ");
			if (totalDelta == 1) System.out.println(totalDelta + " point.");
			else System.out.println(totalDelta + " points.");
		}
		return tieWinner;
	}

	/**
	 * @param candidateA First tied candidate
	 * @param candidateB Second tied candidate
	 * @return One of the above candidates with a 50-50 probability
	 */
	public String randomTieBreaker(String candidateA, String candidateB) {
		if (Math.random() < 0.5) {
			return candidateA;
		}
		return candidateB;
	}
}
