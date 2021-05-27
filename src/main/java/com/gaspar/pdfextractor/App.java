package com.gaspar.pdfextractor;

import java.awt.GridLayout;
import java.io.File;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.FileUtils;

/**
 * Main class for the text extractor.
 */
public class App {
	
	/**
	 * Contains the path to the current working directory.
	 */
	public static final String CURRENT_DIRECTORY = findCurrentDirectory();
	
	/**
	 * Entry point.
	 * @param args Command line arguments.
	 */
    public static void main( String[] args ) {
    	//megerősítés
    	int answer = JOptionPane.showConfirmDialog(null, "Most minden ebben a mappában (és az almappákban) lévő PDF fájlhoz egy (azonos nevű) szöveg fájl lesz generálva, ami az abban lévő szöveget tartalmazza.", 
    			"Szöveg kinyerése", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
    	if(answer == JOptionPane.OK_OPTION) {
    		System.out.println("Running in " + CURRENT_DIRECTORY);
    		
    		int overwriteAnswer = JOptionPane.showConfirmDialog(null, "Felülírjam a már meglévő TXT fájlokat? Egyébként ki lesznek hagyva.", "Felülírás", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    		final boolean overwrite = overwriteAnswer == JOptionPane.YES_OPTION;
    		
    		//list the PDF files in the working directory
    		String[] filter = {"pdf"};
    		final List<File> pdfFiles = (List<File>)FileUtils.listFiles(new File(CURRENT_DIRECTORY), filter, true);
    		
    		System.out.println("Found " + pdfFiles.size() + " PDF files in the current directory, extracting...");
    		
    		//show loading window, will be updated form the background thread
    		displayLoadingWindow(pdfFiles.size());
    		
    		//start the background thread
    		new Thread(new Runnable() {
				
				public void run() {
					
					//pass all to the text extractor and extract
		    		for(File pdfFile: pdfFiles) {
		    			TextExtractor extractor = new TextExtractor(pdfFile.getAbsolutePath(), overwrite);
		    			extractor.extractText();
		    			//update progress after each one
		    			updateProgress();
		    		}
		    		//close window
		    		closeWindow();
		    		//everything is finished
		    		String finalMessage = "Siker! Összesen " + TextExtractor.getWordCount() + " szó lett kinyerve " + pdfFiles.size() + " darab PDF fájlból!";
		    		System.out.println(finalMessage);
		    		JOptionPane.showMessageDialog(null, finalMessage, "Vége", JOptionPane.INFORMATION_MESSAGE);
				}
				
			}).start();
    	}
    }
    
    /**
     * Window of the application.
     */
    private static JFrame window;
    
    /**
     * Displays progress of text extraction.
     */
    private static JProgressBar progressBar;
    
    /**
     * Displays the window of the application.
     * @param taskSize The amount of PDF files.
     */
    private static void displayLoadingWindow(int taskSize) {
    	window = new JFrame("Szöveg kinyerés");
    	window.setLocationRelativeTo(null);
    	
    	JPanel root = new JPanel();
    	root.setLayout(new GridLayout(0,1));
    	root.setAlignmentX(JPanel.CENTER_ALIGNMENT);
    	root.setAlignmentY(JPanel.CENTER_ALIGNMENT);
    	
    	Border margin = new EmptyBorder(10,10,10,10);
    	
    	JLabel label = new JLabel("A kinyerés folyamatban van. Ez eltarthat egy darabig...");
    	label.setBorder(new CompoundBorder(margin, label.getBorder()));
    	root.add(label);
    	
    	progressBar = new JProgressBar(0, taskSize);
    	progressBar.setValue(0);
    	progressBar.setAlignmentX(JPanel.CENTER_ALIGNMENT);
    	progressBar.setAlignmentY(JPanel.CENTER_ALIGNMENT);
    	progressBar.setBorder(new CompoundBorder(margin, progressBar.getBorder()));
    	root.add(progressBar);
    	
    	window.getContentPane().add(root);
    	
    	window.pack();
    	window.setVisible(true);
    }
	
    /**
     * Called by the worker when the extraction from a PDF file finished.
     * Increases the progress bar value.
     */
    public static synchronized void updateProgress() {
    	SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if(progressBar == null) return;
		    	int progress = progressBar.getValue();
		    	progressBar.setValue(progress + 1);
			}
		});
    }
    
    /**
     * Called by the worker when all extraction is finished. Closes the progress window.
     */
    public static synchronized void closeWindow() {
    	SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if(window == null) return;
				window.dispose();
			}
		});
    }
    
	/**
	 * Gets the path to the folder in which the app is running. The \ is not 
	 * included in the end of the path.
	 * @return The path.
	 */
	private static String findCurrentDirectory() {
		String absolutePath = new File("").getAbsolutePath();
	    return absolutePath;
	}
}
