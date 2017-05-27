import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintStream;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

/**
 * The windowing method for the program. Also houses essential objects like YAV_Config
 * and YAV_Ballot_Box so methods requiring them can be called from here.
 * 
 * @author Grant Barker, for use with Louisiana Youth & Government conference elections
 */

public class YAV_Frame extends JFrame implements ActionListener {
	private static final long serialVersionUID = -508626031643717348L; // Just shutting up Java warnings
	public static final String eightyWidth = "00        10        " +
											 "20        30        " +
											 "40        50        " +
											 "60        70        8";
	public enum Analyzer {IRV};	// Currently unused - in place for a drop-down of different analysis types
	public enum FileType {CFG, TSV};
	private Font sysFont;
	private int defaultWidth;
	private JTextArea textOut;
	private PrintStream console;
	private JButton cfgOpenB, tsvOpenB, analyzeB, cfgGenerateB;
	private JPanel mainPanel, buttonPanel;
	private JFileChooser fileChooser;
	private File cfgFile, tsvFile;
	private FileFilter cfgFilter, tsvFilter;
	private boolean cfgIsValid, tsvIsValid;
	private final int ballotStartColumn = 6;
	private IRV_Analysis analyzer;
	private YAV_Config cfg;
	private YAV_Ballot_Box ybb;
	
	public YAV_Frame() {
		cfg = new YAV_Config();
		
		this.setTitle("Youth & Government Alternative Vote Election Analyzer");
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        sysFont = new Font("monospaced", Font.PLAIN, 12);
        defaultWidth = new JLabel().getFontMetrics(sysFont).stringWidth(eightyWidth);
        
        textOut = new JTextArea();
        textOut.setEditable(false);
        textOut.setFont(sysFont);
        textOut.setLineWrap(true);
        textOut.setWrapStyleWord(true);

        // Redirects console output to the above JTextArea, for a cheap means of making a GUI
		console = new PrintStream(new TextAreaOutputStream(textOut));
		System.setOut(console);
		System.setErr(console);
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(true);

		cfgOpenB = new JButton("Open .cfg");
		cfgOpenB.setToolTipText("The .cfg file contains election information");
		cfgOpenB.addActionListener(this);
		/*cfgGenerateB = new JButton("Generate Template .cfg");		// This will be a wizard in a future build.
		cfgGenerateB.setToolTipText("Grant, fill this out later"); 	// The current .cfg generator simply saves a
		cfgGenerateB.addActionListener(this);*/						//  sample .cfg somewhere, which is redundant
		tsvOpenB = new JButton("Open .tsv");
		tsvOpenB.setToolTipText("The .tsv file is the spreadsheet of voting data");
		tsvOpenB.addActionListener(this);
		analyzeB = new JButton("Process Election");
		analyzeB.setToolTipText("Process ballots according to instant-runoff rules");
		analyzeB.addActionListener(this);
		
        tsvOpenB.setEnabled(false);
        analyzeB.setEnabled(false);
        
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.setMaximumSize(new Dimension(defaultWidth + 12, 60));
        buttonPanel.add(cfgOpenB);
		//buttonPanel.add(cfgGenerateB);
		buttonPanel.add(tsvOpenB);
        buttonPanel.add(analyzeB);

		mainPanel.add(new JScrollPane(textOut));
        mainPanel.add(buttonPanel);

        fileChooser = new JFileChooser();
        cfgFilter = new FileFilter() {
        	public String getDescription() {return "Config Files (*.cfg)"; }
        	public boolean accept(File f) {
        		if (f.isDirectory()) return true;
        	    else {
        	        String filename = f.getName().toLowerCase();
        	        return filename.endsWith(".cfg");
        	    }
        	}
        };
        tsvFilter = new FileFilter() {
        	public String getDescription() {return "Tab Separated Values (*.tsv)";}
        	public boolean accept(File f) {
        		if (f.isDirectory()) return true;
        	    else {
        	        String filename = f.getName().toLowerCase();
        	        return filename.endsWith(".tsv");
        	    }
        	}
        };
        
		this.setPreferredSize(new Dimension(defaultWidth + 12, 400));
		this.add(mainPanel);
        this.pack();
        this.setLocation(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width/2
        					- this.getWidth()/2,
        				 GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height/2
        					- this.getHeight()/2);
        this.setLocationByPlatform(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		/**
		 * BUTTON PANEL, CFG GENERATOR BUTTON ACTION: // See Line 80 - currently unused
		 */
	    /*else if (e.getSource() == cfgGenerateB) {
	    	fileChooser.setFileFilter(cfgFilter);
			fileChooser.setSelectedFile(new File("Sample Election Config.cfg"));
			int returnVal = fileChooser.showSaveDialog(YAV_Frame.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				//YAV_Config_Generator.go(fileChooser.getSelectedFile());
			}
	    }*/
		/**
		 * BUTTON PANEL, CFG OPEN BUTTON ACTION:
		 */
	    if (e.getSource() == cfgOpenB) {
	    	System.out.print("Opening the .cfg file... ");
	    	fileChooser.setFileFilter(cfgFilter);
	        int returnVal = fileChooser.showOpenDialog(YAV_Frame.this);
	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            cfgFile = fileChooser.getSelectedFile();
	            System.out.println("\"" + cfgFile.getName() + "\"");
	            cfgIsValid = YAV_CFG_Reader.go(cfgFile, cfg);
	            if (cfgIsValid) {
	            	System.out.println(".cfg opened without any errors. " +
	            					   "Please open your .tsv with the button below.");
	            	System.out.println();
	            	tsvOpenB.setEnabled(true);
	            }
	            else {
	            	System.out.println("Could not validate the contents of the .cfg file. See above error report.");
	            	System.out.println();
	            	tsvOpenB.setEnabled(false);
	            }
	        }
	        else if (returnVal == JFileChooser.CANCEL_OPTION) {
	        	System.out.println("Operation canceled.");
            	System.out.println();
	        }
			analyzeB.setEnabled(false);
	    }
	    /**
		 * BUTTON PANEL, TSV OPEN BUTTON ACTION:
		 */
	    else if (e.getSource() == tsvOpenB) {
	    	System.out.print("Opening the .tsv file... ");
	    	fileChooser.setFileFilter(tsvFilter);
	    	int returnVal = fileChooser.showOpenDialog(YAV_Frame.this);
	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            tsvFile = fileChooser.getSelectedFile();
	            System.out.println("\"" + tsvFile.getName() + "\"");
            	ybb = new YAV_Ballot_Box(cfg);
	            tsvIsValid = YAV_TSV_Reader.go(tsvFile, ybb, true, ballotStartColumn);
	            if (tsvIsValid) {
	            	System.out.println(".tsv opened without any errors. Click \"Process Election\" below.");
	            	System.out.println();
	            	analyzeB.setEnabled(true);
	            }
		        else {
		        	System.out.println("Could not process all the contents of the .tsv file. See above error report.");
		        	System.out.println();
		        	analyzeB.setEnabled(false);
		        }
	        }
	        else if (returnVal == JFileChooser.CANCEL_OPTION) {
	        	System.out.println("Operation canceled.");
            	System.out.println();
	        }
	    }
	    /**
		 * BUTTON PANEL, PROCESS ELECTION BUTTON ACTION:
		 */
	    else if (e.getSource() == analyzeB) {
	    	if (cfgIsValid && tsvIsValid) {
	    		analyzer = new IRV_Analysis(ybb);
	    		System.out.println("IRV analysis completed.");
            	System.out.println();
	    	}
	    	else {
	    		System.out.println("Please select a valid .cfg and .tsv file first.");
            	System.out.println();
	    	}
	    }
	}
}
