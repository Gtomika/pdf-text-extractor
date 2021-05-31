package com.gaspar.pdfextractor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.apache.commons.io.FilenameUtils;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

/**
 * Extracts text from a PDF file.
 * @author Gáspár Tamás
 */
public class TextExtractor {
	
	/**
	 * Used to read input from user.
	 */
	private static final Scanner scanner = new Scanner(System.in);
	
	/**
	 * Stores how many words were extracted.
	 */
	private static long wordCount = 0;
	
	/**
	 * Path of the PDF
	 */
	private final String path;
	
	/**
	 * Path to which the generated text fill will be placed. This is the same as 
	 * the PDF path ({@link #path}), only the file extension is txt.
	 */
	private final String textPath;

	/**
	 * Determindes overwrite mode.
	 */
	private final String overwrite;
	
	/**
	 * Logger object.
	 */
	private final Logger logger;
	
	/**
	 * Creates an extractor.
	 * @param path Path to the PDF file.
	 */
	public TextExtractor(String path, String overwrite, Logger logger) {
		this.path = path;
		this.textPath = generateTextPath(path);
		this.overwrite = overwrite;
		this.logger = logger;
	}
	
	/**
	 * Creates the text files path from the PDF path, see {@link #textPath}.
	 * @param path PDF path.
	 * @return Text path.
	 */
	private String generateTextPath(String p) {
		String extRemoved = FilenameUtils.removeExtension(p); //cut ".pdf"
		return extRemoved + ".txt"; //append ".txt"
	}
	
	/**
	 * Performs the text extraction. A text file will be generated on {@link #textPath}.
	 */
	public void extractText() {
		logger.log("Kezdem a kinyerést a " + path + " PDF fájlból...");
		try {
			//open pdf file
			final PdfReader reader = new PdfReader(path);
			
			//encrypted?!
			if(reader.isEncrypted()) {
				logger.log("Titkosított PDF, ezért kihagyom.");
				return;
			}
			//already exists?
			final File textFile = new File(textPath);
			if(textFile.exists()) {
				if(overwrite.equals(CommandLineArguments.OVERWRITE_ALL)) {
					//overwrite, mention this
					logger.log("A " + textPath + " fájl már létezik, felülírom.");
				} else if (overwrite.equals(CommandLineArguments.OVERWRITE_NONE)){
					//no overwrite
					logger.log("A " + textPath + " fájl létezik, és a felülírás ki van kapcsolva, ezért kihagyom.");
					return;
				} else {
					//csak a select overwrite mód maradt
					logger.logUnmutable("A " + textPath + " már létezik. Felülírjam? (I/N)");
					String input = scanner.nextLine();
					if(input.equalsIgnoreCase("I")) {
						logger.log("A felhasználó válasza IGEN, ezért felülírom.");
					} else if(input.equalsIgnoreCase("N")) {
						logger.log("A felhasználó válasza NEM, ezért kihagyom.");
						return;
					} else {
						logger.log("A felhasználó válasza " + input + ", amit nem tudok értelmezni. A fájlt kihagyom.");
						return;
					}
				}
			}
			
			//begin extraction
			int pageCount = reader.getNumberOfPages();
			logger.log("Ez a PDF " + pageCount + " oldalt tartalmaz.");
			
			final StringBuilder textBuilder = new StringBuilder(); //appends text from the pages
			
			for(int page = 1; page <= pageCount; page++) {
				String textFromPage = PdfTextExtractor.getTextFromPage(reader, page);
				textBuilder.append(textFromPage);
				//append line break
				if(page < pageCount) textBuilder.append(System.lineSeparator());
			}
			
			String pdfText = textBuilder.toString();
			
			//warning if it is empty
			if(pdfText.isEmpty()) {
				logger.logError("A " + path + " PDF-ből semmilyen szöveget nem sikerült kinyerni!");
			}
			
			//count words
			StringTokenizer tokenizer = new StringTokenizer(pdfText);
			wordCount += tokenizer.countTokens();
			
			//create and write file
			if(textFile.exists()) {
				textFile.delete();
			}
			textFile.createNewFile();
			writeStringIntoFile(textFile, pdfText);
			
			logger.log("Sikeres kinyerés a " + textPath + " fájlba.");
		} catch (IOException e) {
			//something failed
			logger.logError("A " + path + " fájlból nem sikerült a kinyerés. A hiba oka:");
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Writes a string into the file.
	 * @param file The file.
	 * @param text The string.
	 */
	private void writeStringIntoFile(final File file, String text) throws IOException {
		try(FileWriter writer = new FileWriter(file)) {
			writer.write(text);
			writer.flush();
		}
	}
	
	/**
	 * Returns the internal word counter.
	 * @return Word counter.
	 */
	public static long getWordCount() {
		return wordCount;
	}
}
