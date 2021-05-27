package com.gaspar.pdfextractor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
	 * If this flag is true, existing text files will be overwritten, if they already exist.
	 */
	private final boolean overwrite;
	
	/**
	 * Creates an extractor.
	 * @param path Path to the PDF file.
	 */
	public TextExtractor(String path, boolean overwrite) {
		this.path = path;
		this.textPath = generateTextPath(path);
		this.overwrite = overwrite;
	}
	
	/**
	 * Creates the text files path from the PDF path, see {@link #textPath}.
	 * @param path PDF path.
	 * @return Text path.
	 */
	private String generateTextPath(String path) {
		String extRemoved = FilenameUtils.removeExtension(path); //cut ".pdf"
		return extRemoved + ".txt"; //append ".txt"
	}
	
	/**
	 * Performs the text extraction. A text file will be generated on {@link #textPath}.
	 */
	public void extractText() {
		System.out.println("Beginning to extract text from " + path + "...");
		try {
			//open pdf file
			final PdfReader reader = new PdfReader(path);
			
			//encrypted?!
			if(reader.isEncrypted()) {
				System.err.println(path + " is encrypted, skipping...");
				return;
			}
			//already exists?
			final File textFile = new File(textPath);
			if(textFile.exists()) {
				if(overwrite) {
					//overwrite, mention this
					System.err.println("The file " + textPath + " already exists, and will be overwritten...");
				} else {
					//no overwrite
					System.err.println("The file " + textPath + " already exists, skipping...");
					return;
				}
			}
			
			//begin extraction
			int pageCount = reader.getNumberOfPages();
			System.out.println("This PDF contains " + pageCount + " pages.");
			
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
				System.err.println("Warning: " + textPath + " will be empty, could not extract anything!");
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
			
			System.out.println("Extracted into " + textPath + "!");
		} catch (IOException e) {
			//something failed
			System.err.println("Failed to extract from " + path + ":");
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Writes a string into the file.
	 * @param file The file.
	 * @param text The string.
	 */
	private void writeStringIntoFile(final File file, String text) throws IOException {
		FileWriter writer = new FileWriter(file);
		writer.write(text);
		writer.flush();
		writer.close();
	}
	
	/**
	 * Returns the internal word counter.
	 * @return Word counter.
	 */
	public static long getWordCount() {
		return wordCount;
	}
}
