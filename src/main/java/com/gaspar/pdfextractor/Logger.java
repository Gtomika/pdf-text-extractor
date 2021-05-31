package com.gaspar.pdfextractor;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Segítségével üzeneteket lehet kiírni a felhasználónak (konzolra és/vagy log fájlba).
 * @author Gáspár Tamás
 */
public class Logger {
	
	/**
	 * Ez alapján tudja, hogy kell-e logfájl, vagy kiírás.
	 */
	private CommandLineArguments arguments;

	/**
	 * Ezzel ír a log fájlba, ha szükséges.
	 */
	private PrintWriter logFile;
	
	/**
	 * Logger létrehozása.
	 * @param arguments Parancssori argumentumok objektuma.
	 */
	public Logger(CommandLineArguments arguments) {
		this.arguments = arguments;
		//log fájl megnyitása, ha kell
		String name = null;
		if(arguments.isLogging()) {
			try {
				if(arguments.isAutoLogging()) {
					name = generateLogName();
					logFile = new PrintWriter(new FileWriter(new File(name)));
				} else {
					name = arguments.getLogPath();
					logFile = new PrintWriter(new FileWriter(new File(name)));
				}
			} catch (Exception e) {
				//valamiért nem lehetett megnyitni a log fájl. ilyenkor nem lesz logolás
				logFile = null;
				System.err.println("A " + name + " nevű log fájlt nem lehetett megnyitni! Nem lesz log-olás.");
			}
		}
	}
	
	/**
	 * Automatikusan generál log fájl nevet a jelenlegi rendszeridőből.
	 * @return A log fájl név.
	 */
	private String generateLogName() {
		return "log_" + String.valueOf(System.currentTimeMillis()) + ".log";
	}
	
	/**
	 * Ha van log fájl nyitva, akkor azt bezárja.
	 */
	public void closeLogFileIfNeeded() {
		if(logFile != null) {
			logFile.flush();
			logFile.close();
		}
	}
	
	/**
	 * Egy üzenetet küld a felhasználónak. Az, hogy ténylegesen hova kerül kiírásra, 
	 * az a {@link #arguments} értékeitől függ.
	 * @param message Az üzenet. A végére sortörés kerül.
	 */
	public void log(String message) {
		//kell-e konzolra írni?
		if(!arguments.isMuted()) {
			System.out.println(message);
		}
		//kell-e, lehet-e log fájlba írni?
		if(arguments.isLogging() && logFile != null) {
			logFile.println(message);
		}
	}
	
	/**
	 * Hibaüzenete küld a felhasználónak. Az, hogy ténylegesen hova kerül kiírásra, 
	 * az a {@link #arguments} értékeitől függ, de NEM lehet némítani.
	 * @param message Az üzenet. A végére sortörés kerül.
	 */
	public void logError(String message) {
		//kell konzolra írni
		System.err.println(message);
		//kell-e, lehet-e log fájlba írni?
		if(arguments.isLogging() && logFile != null) {
			logFile.println("HIBA: " + message);
		}
	}
	
	/**
	 * Hasonló mint {@link #log(String)}, csak ezt nem lehet a '-mute' argumentummal 
	 * némítani.
	 * @param message Az üzenet. A végére sortörés kerül.
	 */
	public void logUnmutable(String message) {
		//kell-e konzolra írni? IGEN
		System.out.println(message);
		//kell-e, lehet-e log fájlba írni?
		if(arguments.isLogging() && logFile != null) {
			logFile.println(message);
		}
	}
}
