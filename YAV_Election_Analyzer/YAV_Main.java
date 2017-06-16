package YAV_Election_Analyzer;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * The main method for LA Youth & Gov's YAV (Y&G Alternative Vote) analysis program.
 * See YAV_Election_Analyzer.YAV_Frame for GUI setup and most of the heavy lifting.
 * See IRV_Analysis for how it (by default) analyzes a ballot box (from the .tsv).
 *    (IRV = Instant-Runoff Voting)
 * 
 * @author Grant Barker, for use with Louisiana Youth & Government conference elections
 */

public class YAV_Main {
	public static void main(String args[]) {
		
		// Tries to set the UI to look like that of the user's OS
		try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
		catch (UnsupportedLookAndFeelException a){System.out.println(a);}
		catch (ClassNotFoundException b){System.out.println(b);}
		catch (IllegalAccessException c){System.out.println(c);}
		catch (InstantiationException d){System.out.println(d);}
		
		YAV_Frame frame = new YAV_Frame(); // Where the bulk of the program operates
		frame.setVisible(true);
        
		System.out.println("-----------------------------------------------------------------------------");
		System.out.println(" Louisiana Youth & Gov YAV Election Analyzer v1.1 (authored by Grant Barker) ");
		System.out.println("-----------------------------------------------------------------------------");
		System.out.println("Please open your \"Google Sheets\" exported .tsv file below.");
    	System.out.println();
	}
}
