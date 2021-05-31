package com.gaspar.pdfextractor;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

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
	 * Parancssori argumentumok objektuma.
	 */
	private static CommandLineArguments arguments;
	
	/**
	 * Üzenetküldő és log-oló objektum.
	 */
	private static Logger logger;
	
	/**
	 * Belépési pont.
	 * @param args JVM által átadott nyers argumentumok.
	 */
    public static void main(String[] args) {
    	//argumentumok beolvasása
    	try {
    		arguments = new CommandLineArguments(args);
    	} catch(IllegalArgumentException e) {
    		//hiba (itt még nincs üzenetküldő objektum)
    		System.err.println("Hibás argumentumok: " + e.getMessage());
    		if(e.getCause() != null) {
    			System.err.println("A hiba kiváltó oka:");
    			e.getCause().printStackTrace();
    		}
    		//ezután nem lehet továbbmenni
    		return;
    	}
    	//üzenetküldő létrehozása
    	logger = new Logger(arguments);
    	
    	final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm:ss");
    	LocalDateTime now = LocalDateTime.now();
    	String timestamp = formatter.format(now);
    	
    	String welcomeMessage = "A szövegkinyerés indul. Idő: " + timestamp + "\n";
    	if(arguments.isMuted()) {
    		welcomeMessage += "\nA konzol némítva lett, több üzenet a végéig nem jelenik meg.";
    	}
    	logger.logUnmutable(welcomeMessage); //ez mindig kiíródik
    	
    	logger.log("A jelenlegi munkakönyvtár: " + CURRENT_DIRECTORY);
   
    	logger.log(arguments.toString()); //argumentumok kiírása
    	
    	long startTime = System.currentTimeMillis();
    	logger.log("Kezdem a futásidő mérését.");
		
		//list the PDF files in the working directory
    	logger.log("Listázom a feldolgozandó PDF fájlokat.");
		final List<File> pdfFiles = listPdfFiles();
		if(pdfFiles.isEmpty()) {
			logger.logError("Egy PDF fájl sem felelt meg a kritériumoknak!");
		} else {
			logger.log("Összesen " + pdfFiles.size() + " megfelelő PDF fájlt találtam, indul a kinyerés.");
		}
		
		//összes fájl átadása kinyerésre
		for(File pdfFile: pdfFiles) {
			TextExtractor extractor = new TextExtractor(pdfFile.getAbsolutePath(), arguments.getOverwrite(), logger);
			extractor.extractText();
		}
		
		long endTime = System.currentTimeMillis();
		long executionTime = endTime - startTime;
		String timeMessage = "Az kinyerés véget ért, a futásidő: " + executionTime + " ezredmásodperc.";
		if(arguments.getOverwrite().equals(CommandLineArguments.OVERWRITE_SELECT)) {
			timeMessage += " Figyelem: ebben a felhasználói bemenetre való várakozás is benne van!";
		}
		logger.log(timeMessage);
		//minden befejeződik
		String finalMessage = "Siker! Összesen " + TextExtractor.getWordCount() + " szó lett kinyerve " + pdfFiles.size() + " darab PDF fájlból, " + executionTime + " ezredmásodperc alatt!";
		logger.logUnmutable(finalMessage); //ez mindig kiíródik
		logger.closeLogFileIfNeeded();
    }
    
    /**
     * Egy listába rakja azokat a PDF fájlokat, amelyeket fel kell dolgozni. Ez a {@link #arguments} 
     * értékeitől függ (főleg a módtól).
     * @return A PDF-ek listája.
     */
    private static List<File> listPdfFiles() {
    	boolean recursive = arguments.isRecursive();
    	if(arguments.getMode().equals(CommandLineArguments.MODE_SINGLE)) { //egy fájl
    		
    		String path = arguments.getPath();
    		logger.log("Egy fájlos mód. A PDF fájl útvonala: " + path);
    		return Arrays.asList(new File(path));
    		
    	} else if(arguments.getMode().equals(CommandLineArguments.MODE_REGEX)) { //regex mód
    		
    		logger.log("Regexnek megfelelő PDF fájlok keresése, " + (recursive ? "rekurzívan." : "nem rekurzívan."));
    		logger.log("A regex: " + arguments.getRegex());
    		String[] filter = {"pdf"};
    		List<File> pdfFiles = (List<File>)FileUtils.listFiles(new File(arguments.getFolder()), filter, recursive);
  
    		final Pattern regex = Pattern.compile(arguments.getRegex());
    		int sizeBefore = pdfFiles.size();
    		pdfFiles.removeIf(file -> {
    			String name = file.getName();
    			//ha nem felel meg a regexnem akkor el lesz távolítva
    			return !regex.matcher(name).matches();
    		});
    		int sizeAFter = pdfFiles.size();
    		logger.log("Az eredeti " + sizeBefore + " darab PDF-ből " + sizeAFter + " maradt, amiknek a neve megfelet a reguláris kifejezésnek.");
    		
    		return pdfFiles;
    	} else { //csak az 'all' maradt
    		
    		logger.log("Minden ebben a mappában lévő PDF fájl keresése, " + (recursive ? "rekurzívan." : "nem rekurzívan."));
    		String[] filter = {"pdf"};
    		final List<File> pdfFiles = (List<File>)FileUtils.listFiles(new File(arguments.getFolder()), filter, recursive);
    		return pdfFiles;
    	}
    }
    
	/**
	 * Gets the path to the folder in which the app is running. The \ is not 
	 * included in the end of the path.
	 * @return The path.
	 */
	public static String findCurrentDirectory() {
		String absolutePath = new File("").getAbsolutePath();
	    return absolutePath;
	}
}
