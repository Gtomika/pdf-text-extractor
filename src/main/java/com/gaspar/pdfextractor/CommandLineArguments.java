package com.gaspar.pdfextractor;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

/**
 * Beolvassa a parancssori argumentumokat, amiket aztán ebből az osztályból lehet visszakérni. Ezek 
 * az értékek érhetőek el:
 * <ul>
 * <li>-mode: megmondja, hogy hogyan kell kiválasztani a PDF fájlokat. Lehet 'all', ami minden talált PDF-et kiválaszt. 
 * Lehet 'single', ami egy PDF-et fog kiválasztani (ezt a '-path' argumentumban kell átadni). Lehet 'regex', ami reguláris kifejezés alapján választ (amit a '-regex' argumentumban kell 
 * átadni). Alapértéke az 'all'.</li>
 * <li>-folder: Ezzel lehet megadni, hogy az 'all' és 'regex' módok esetén melyik mappában történjen a keresés. Abszolút és relatív útvonal is lehet, ezt az információt a '-pathType'-al lehet 
 * megadni. Nem kell megadni, alapértéke a jelenlegi munkakönytár (ez leggyakrabban az, ahol a JAR található).</li>
 * <li>-path: Ezzel kell megadni a PDF fájl útvonalát, ha a választótt mód 'single' (-mode=single). Abszolút és relatív útvonal is lehet, ezt az információt a '-pathType'-al lehet 
 * megadni. Ha például a document.pdf ugyanabban a mappában van, ahol a JAR, akkor 
 * ez '-path=document.pdf' lesz (relatív útvonal). Ha például a JAR mappájában van egy 'docs' nevű mappa, és abban van a PDF, akkor ez '-path=docs/document.pdf' lesz (szintén relatív). A '-mode=single' argumot már előtte meg kell adni!</li>
 * <li>-pathType: Ezzel lehet megadni, hogy a '-path' vagy '-folder' argumentumban kapott útvonalak relatív vagy abszolút útvonalak-e. Csak a '-path' vagy a '-folder' után állhat. 
 * Lehetséges értékei 'relative' és 'absolute'. Alapértelmezetten relatív.</li>
 * <li>-regex: Ezzel kell megadni a JAVA reguláris kifejezést, amivel a PDF-ek kiválasztásra kerülnek, ha a mód regex (-mode=regex). Érvényes Java regex-nek kell lennie és nem tartalmazhatja a 
 * szóköz karaktert, mert akkor az új argumentumként lenne értelmezve (szóköz helyett a {@code \\s} írható, ami azt fogja jelenteni). 
 * Például az 'fn' kezdetű PDF fájlokat feldolgozó regex így adható meg: '-regex=fn.*'  A '-mode' argumot már előtte meg kell adni!</li>
 * <li>-recursive: Ha ez az argumentum meg van adva, akkor a PDF-ek az almappákban is rekurzívan keresve lesznek. Ha nincs megadva, akkor csak a JAR mappájában lesznek 
 * keresve, az almappákban már nem. Alapértékben nem lesz rekurzív.</li>
 * <li>-overwrite: Ezzel kell megadni, hogy hogyan legyenek felülírva a txt fájlok. Lehet 'all', ami mindent felülír. Lehet 'none', ami semmit nem fog felülírni, ami már megvan. 
 * Lehet 'select', ami minden talált PDF esetén egyenként megkérdezi a felülírást, ha már foglalt a text fájl. Alapértékben nem lesz felülírás</li>
 * <li>-mute: Ha ez meg van adv, akkor a program semmit sem fog kiírni a konzolra, csak a kezdeti és a befejező üzenetet. Egyébként sok egyéb információ is kiíródik. 
 * Alapértékben ez ki van kapcsolva, azaz minden kiíródik.</li>
 * <li>-logfile: Ezzel lehet megadni egy fájl RELATÍV útvonalát, ahova bekerülnek a feldolgozással kapcsolatos információk (ezek ugyanazok, mint amik a konzolra is 
 * kiíródnak, ha nincs megadott '-mute'. Ha ez a fájl létezik, akkor felül fog íródni. Például ha egy 'log.txt' szövegfájlba kérjük a logot, ami a JAR mappájába kerül, akkor 
 * '-logfile=log.txt' az argumentum. Egy speciális értéke az 'auto', ilyenkor a log fájl neve automatikusan kerül generálásra. Alapértéke (ha nem adjuk meg) az, hogy nem lesz log fájl</li>
 * </ul>
 * Példák:
 * <br>
 * 1. Minden argumentum alapértéken:
 * <pre>{@code
 * java -jar PdfTextExtractor.jar  
 * }</pre>
 * <br>
 * 2. Egy konkrét 'document.pdf' feldolgozása, a konzol némításával, de egy 'history.log' készítésével:
 * <pre>{@code
 * java -jar PdfTextExtractor.jar -mode=single -path=document.pdf -mute -logfile=history.log 
 * }</pre>
 * <br>
 * 3. Rekurzívan minden PDF feldolgozása a JAR mappájában, egy automatikusan elnevezett logfájl használatával:
 * <pre>{@code
 * java -jar PdfTextExtractor.jar -mode=all -recursive -logfile=auto 
 * }</pre>
 * 4. Reguláris kifejezéssel megadott PDF-ek feldolgozása, rekurzívan keresve, ahol azokat a PDF-eket dolgozzuk fel, amelyek nevében benne van az 'abc' szöveg:
 * <pre>{@code
 * java -jar PdfTextExtractor.jar -mode=regex -regex=.*abc.* -recursive 
 * }</pre>
 * @author Gáspár Tamás
 */
public class CommandLineArguments {

	/**
	 * Leképezés ami tárolja, hogy mik az argumentumok értékei.
	 */
	private Map<String, String> arguments;
	
	/**
	 * Konstruktor.
	 * @param args A parancssori argumentumok, ahogyan a JVM adta.
	 * @throws IllegalArgumentException Ha az argumentumok nem értelmesek.
	 */
	public CommandLineArguments(String[] args) throws IllegalArgumentException {
		arguments = new HashMap<>();
		for(String arg: args) {
			parseArgument(arg);
		}
		applyDefaultValues();
	}
	
	/**
	 * Egy parancssori szöveges argumentumot dolgoz fel és helyes a {@link #arguments} leképezésbe.
	 * @param arg Az argumentum.
	 * @throws IllegalArgumentException Ha az argumentum nem értelmes.
	 */
	private void parseArgument(String arg) throws IllegalArgumentException {
		if(arg.startsWith(MODE + "=")) { //mód
			parseMode(arg);
		} else if(arg.startsWith(PATH) && !arg.startsWith(PATH_TYPE)) { //ez a path specifikáció a '-mode=single'-hez
			parsePath(arg);
 		} else if(arg.startsWith(FOLDER)) {
 			parseFolder(arg);
 		} else if(arg.startsWith(REGEX)) { //ez a regex specifikáció a '-mode=regex'-hez
 			parseRegex(arg);
 		} else if(arg.startsWith(OVERWRITE)) { //ez a felülírási szabály
 			parseOverwrite(arg);
 		} else if(arg.equals(RECURSIVE)) { //ez a rekurzív argumentum
 			if(!arguments.containsKey(RECURSIVE)) {
 				arguments.put(RECURSIVE, ""); //itt a konkrét érték nem lényeges
 			} else {
 				throw new IllegalArgumentException("Több megadott '-recursive', ami nem megengedett!");
 			}
 		} else if(arg.equals(MUTE)) { //mute argumentum
 			if(!arguments.containsKey(MUTE)) {
 				arguments.put(MUTE, ""); //itt a konkrét érték nem lényeges
 			} else {
 				throw new IllegalArgumentException("Több megadott '-mute', ami nem megengedett!");
 			}
 		} else if(arg.startsWith(LOG)) { //logfile argumentum
 			parseLogfile(arg);
 		} else if(arg.startsWith(PATH_TYPE)) { //path típus
 			parsePathType(arg);
 		} else { //ismeretlen
 			throw new IllegalArgumentException("Ismeretlen argumentum: " + arg);
 		}
	}
	
	/**
	 * Teljesen feltölti az {@link #arguments}-et, hogy minden argumentum értéke ismert legyen, még 
	 * akkor is, ha a felhasználó ezt nem adta meg. Alapértékeket használ.
	 * @throws IllegalArgumentException Ha a feltöltés nem lehetséges.
	 */
	private void applyDefaultValues() throws IllegalArgumentException {
		if(!arguments.containsKey(MODE)) { //ha nincs mód,akkor az alap az 'all'
			arguments.put(MODE, MODE_ALL);
		} else {
			//van mód
			if(arguments.get(MODE).equals(MODE_SINGLE) && !arguments.containsKey(PATH)) {
				throw new IllegalArgumentException("A megadott mód single, ilyenkor a '-path' segítségével meg kell adni egy PDF fájl relatív útvonalát!");
			}
			if(arguments.get(MODE).equals(MODE_REGEX) && !arguments.containsKey(REGEX)) {
				throw new IllegalArgumentException("A megadott mód regex, ilyenkor a '-regex' segítségével meg kell adni egy Java reguláris kifejezést!");
			}
		}
		if(!arguments.containsKey(OVERWRITE)) { //ha nincs overwrite, akkor az alap a 'none'
			arguments.put(OVERWRITE, OVERWRITE_NONE);
		}
		if(!arguments.containsKey(PATH_TYPE)) { //path type alapértelmezettje relatív
			arguments.put(PATH_TYPE, PATH_TYPE_REL);
		}
		//a folder alapértéke a jelen munkakönytár
		if(!arguments.containsKey(FOLDER)) {
			String pathType = arguments.get(PATH_TYPE);
			arguments.put(FOLDER, pathType.equals(PATH_TYPE_REL) ? "." : App.findCurrentDirectory());	
		}
		File f = new File(arguments.get(FOLDER));
		if(arguments.get(PATH_TYPE).equals(PATH_TYPE_REL) && f.isAbsolute()) {
			throw new IllegalArgumentException("Az általad megadott '-folder' útvonal abszolút. Ilyenkor használd a '-pathType=absolute' argumentumot!");
		} 
		if(arguments.get(PATH_TYPE).equals(PATH_TYPE_ABS) && !f.isAbsolute()) {
			throw new IllegalArgumentException("Abszolút útvonal típust adtál meg a '-pathType' argumentumban, de az átadott '-folder' útvonal relatív! Ha nem írod ki a '-pathType'-ot, akkor relatívnak fog számítani.");
		}
		//path/folder ellenőrzés, ha van: csak itt állhat rendelkezésre biztosan a path/folder és pathType (ha van path vagy folder)
		if(arguments.containsKey(PATH)) {
			File f1 = new File(arguments.get(PATH));
			if(arguments.get(PATH_TYPE).equals(PATH_TYPE_REL) && f1.isAbsolute()) {
				throw new IllegalArgumentException("Az általad megadott fájl útvonal abszolút. Ilyenkor használd a '-pathType=absolute' argumentumot!");
			} 
			if(arguments.get(PATH_TYPE).equals(PATH_TYPE_ABS) && !f1.isAbsolute()) {
				throw new IllegalArgumentException("Abszolút útvonal típust adtál meg a '-pathType' argumentumban, de az átadott útvonal relatív! Ha nem írod ki a '-pathType'-ot, akkor relatívnak fog számítani.");
			}
			if(!f1.exists()) {
				throw new IllegalArgumentException("A '-path'-al megadott fájlnak léteznie kell!");
			}
		}
		
		//mute, log és recursive-al itt nem kell törődni, mert azok alapból kikapcsoltak
	}
	
	/**
	 * Kiértékeli a '-mode' argumentumot.
	 * @param arg Az argumentum.
	 * @throws IllegalArgumentException Ha hibás az argumentum.
	 */
	private void parseMode(String arg) throws IllegalArgumentException {
		String[] split = arg.split("=");
		if(split[1].equals(MODE_ALL)) {
			//minden PDF
			if(!arguments.containsKey(MODE)) {
				arguments.put(MODE, MODE_ALL);
			} else {
				//már van megadott mód
				throw new IllegalArgumentException("Több megadott '-mode', ami nem megengedett!");
			}
		} else if(split[1].equals(MODE_SINGLE)) {
			//csak egy pdf
			if(!arguments.containsKey(MODE)) {
				arguments.put(MODE, MODE_SINGLE);
			} else {
				//már van megadott mód
				throw new IllegalArgumentException("Több megadott '-mode', ami nem megengedett!");
			}
		} else if(split[1].equals(MODE_REGEX)) {
			//regex alapján
			if(!arguments.containsKey(MODE)) {
				arguments.put(MODE, MODE_REGEX);
			} else {
				//már van megadott mód
				throw new IllegalArgumentException("Több megadott '-mode', ami nem megengedett!");
			}
		} else {
			String[] valids = { MODE_ALL, MODE_SINGLE, MODE_REGEX };
			throw new IllegalArgumentException("Érvénytelen '-mode' érték: " + split[1] + "! Csak ezek egyike lehet: " + Arrays.toString(valids));
		}
	}
	
	/**
	 * Kiértékeli a '-folder' argumentumot.
	 * @param arg Az argumentum.
	 * @throws IllegalArgumentException Ha hibás az argumentum.
	 */
	private void parseFolder(String arg) throws IllegalArgumentException {
		if(arguments.containsKey(FOLDER)) {
			throw new IllegalArgumentException("Több megadott '-folder', ami nem megendgedett!");
		}
		if(!arguments.containsKey(MODE)) {
			throw new IllegalArgumentException("A '-mode' argumentumnak a '-folder' előtt kell lennie!");
		} else if(!arguments.get(MODE).equals(MODE_ALL) && !arguments.get(MODE).equals(MODE_REGEX)) {
			throw new IllegalArgumentException("'-folder' esetén csak az 'all' és 'regex' módok megengedettek!");
		}
		//van megfelelő mód
		String[] split = arg.split("=");
		File folder = new File(split[1]);
		if(!folder.isDirectory()) {
			throw new IllegalArgumentException("A megadott '-folder' értéknek egy mappára kell mutatnia, de ez egy fájlra mutat: " + folder.getAbsolutePath());
		}
		if(!folder.exists()) {
			throw new IllegalArgumentException("A '-folder'-ben megadott mappának léteznie kell, enélkül biztosan nem lesz benne PDF! Ez nem létezik: " + folder.getAbsolutePath());
		}
 		String folderName = split[1];
		if(!folderName.endsWith(File.pathSeparator)) folderName += File.separator;
		arguments.put(FOLDER, folderName);
	}
	
	/**
	 * Kiértékeli a '-path' argumentumot.
	 * @param arg Az argumentum.
	 * @throws IllegalArgumentException Ha hibás az argumentum.
	 */
	private void parsePath(String arg) throws IllegalArgumentException {
		if(!arguments.containsKey(MODE)) {
			throw new IllegalArgumentException("A '-mode' argumentumnak a '-path' előtt kell lennie!");
		} else if(!arguments.get(MODE).equals(MODE_SINGLE)) {
			throw new IllegalArgumentException("'-path' esetén csak az egyszeres mód megengedett: '-mode=single'!");
		}
		String[] split = arg.split("=");
		//rendben, van mód és az single. az, hogy értelmes-e a path az az applyDefaultValues bab van értelmezve
		String fileName = Paths.get(split[1]).getFileName().toString();
		if(!FilenameUtils.getExtension(fileName).equals("pdf")) {
			throw new IllegalArgumentException("A '-path'-al megadott fájlnak pdf fájlnak kell lennie!");
		}
		//egy PDF fájlról van szó
		if(!arguments.containsKey(PATH)) {
			arguments.put(PATH, split[1]);
		} else {
			throw new IllegalArgumentException("Több megadott '-path', ami nem megendgedett!");
		}
	}
	
	/**
	 * Kiértékeli a '-regex' argumentumot.
	 * @param arg Az argumentum.
	 * @throws IllegalArgumentException Ha hibás az argumentum.
	 */
	private void parseRegex(String arg) throws IllegalArgumentException {
		if(!arguments.containsKey(MODE)) {
			throw new IllegalArgumentException("A '-mode' argumentumnak a '-regex' előtt kell lennie!");
		} else if(!arguments.get(MODE).equals(MODE_REGEX)) {
			throw new IllegalArgumentException("'-regex' esetén csak a regex mód megengedett: '-mode=regex'!");
		}
		String[] split = arg.split("=");
		//rendben, van mód, értelmes-e a regex
		try {
			Pattern.compile(split[1]);
		} catch (Exception e) {
			throw new IllegalArgumentException("A megadott kifejezés nem egy értelmes Java regex! Ok:", e);
		}
		//a regex valid
		if(!arguments.containsKey(REGEX)) {
			arguments.put(REGEX, split[1]);
		} else {
			throw new IllegalArgumentException("Több megadott '-regex', ami nem megendgedett!");
		}
	}
	
	/**
	 * Kiértékeli az '-overwrite' argumentumot.
	 * @param arg Az argumentum.
	 * @throws IllegalArgumentException Ha hibás az argumentum.
	 */
	private void parseOverwrite(String arg) throws IllegalArgumentException {
		if(arguments.containsKey(OVERWRITE)) {
			//már van megadott overwrite
			throw new IllegalArgumentException("Több megadott '-overwrite', ami nem megengedett!");
		}
		String[] split = arg.split("=");
		if(split[1].equals(OVERWRITE_ALL)) {
			//minden felülírása
			arguments.put(OVERWRITE, OVERWRITE_ALL);
		} else if(split[1].equals(OVERWRITE_NONE)) {
			//semmilyen felülírás
			arguments.put(OVERWRITE, OVERWRITE_NONE);
		} else if(split[1].equals(OVERWRITE_SELECT)) {
			//regex alapján
			arguments.put(OVERWRITE, OVERWRITE_SELECT);
		} else {
			String[] valids = { OVERWRITE_ALL, OVERWRITE_ALL, OVERWRITE_SELECT };
			throw new IllegalArgumentException("Érvénytelen '-overwrite' érték: " + split[1] + "! Csak ezek egyike lehet: " + Arrays.toString(valids));
		}
	}
	
	/**
	 * Kiértékeli az '-pathType' argumentumot.
	 * @param arg Az argumentum.
	 * @throws IllegalArgumentException Ha hibás az argumentum.
	 */
	private void parsePathType(String arg) {
		String[] split = arg.split("=");
		if(arguments.containsKey(PATH_TYPE)) {
			throw new IllegalArgumentException("Több megadott '-pathType', ami nem megengedett!");
		}
		if(!arguments.containsKey(PATH) && !arguments.containsKey(FOLDER)) {
			throw new IllegalArgumentException("A '-pathType' előtt szerepelnie kell a '-path' vagy '-folder' argumentumnak!");
		}
		//van path/folder és nincs még path type
		if(split[1].equals(PATH_TYPE_REL)) {
			arguments.put(PATH_TYPE, PATH_TYPE_REL);
		} else if(split[1].equals(PATH_TYPE_ABS)) {
			arguments.put(PATH_TYPE, PATH_TYPE_ABS);
		} else {
			String[] valids = { PATH_TYPE_REL, PATH_TYPE_ABS };
			throw new IllegalArgumentException("Érvénytelen '-pathType' érték: " + split[1] + "! Csak ezek egyike lehet: " + Arrays.toString(valids));
		}
	}
	
	/**
	 * Kiértékeli az '-logfile' argumentumot.
	 * @param arg Az argumentum.
	 * @throws IllegalArgumentException Ha hibás az argumentum.
	 */
	private void parseLogfile(String arg) throws IllegalArgumentException {
		if(arguments.containsKey(LOG)) {
			throw new IllegalArgumentException("Több megadott '-logfile', ami nem megengedett!");
		}
		//nincs még logfile
		String[] split = arg.split("=");
		if(split[1].equals(LOG_AUTO)) {
			//automatikus logfájl név
			arguments.put(LOG, LOG_AUTO);
		} else {
			//ez egy fájl relatív útvonala
			arguments.put(LOG, split[1]);
		}
	}
	
	public String getMode() {
		return arguments.get(MODE);
	}
	
	//az érték null lesz ha nem MODE_SINGLE-ben vagyunk.
	public String getPath() {
		return arguments.get(PATH);
	}
	
	//az érték null lesz, ha nem MODE_REGEX-ben vagyunk.
	public String getRegex() {
		return arguments.get(REGEX);
	}
	
	public String getOverwrite() {
		return arguments.get(OVERWRITE);
	}
	
	public boolean isRecursive() {
		return arguments.containsKey(RECURSIVE);
	}
	
	public boolean isMuted() {
		return arguments.containsKey(MUTE);
	}
	
	public boolean isLogging() {
		return arguments.containsKey(LOG);
	}
	
	public boolean isAutoLogging() {
		return arguments.containsKey(LOG) && arguments.get(LOG).equals(LOG_AUTO);
	}
	
	//null lesz, ha nincs logging
	public String getLogPath() {
		return arguments.get(LOG);
	}
	
	//null lesz ha nincs path
	public String getPathType() {
		return arguments.get(PATH_TYPE);
	}
	
	public String getFolder() {
		return arguments.get(FOLDER);
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("-----------------------------------------------------------------------\n");
		b.append("A kapott argumentumok:\n");
		//mód
		b.append("PDF keresési mód (-mode): ");
		String mode = getMode();
		if(mode.equals(MODE_ALL)) {
			b.append("Minden PDF fájl.\n");
			String folderName = getFolder();
			b.append("A mappa, amiben keresek: " + (folderName.equals(".") ? " jelenlegi munkakönyvtár" : folderName));
			if(getPathType().equals(PATH_TYPE_REL)) {
				b.append(" (RELATÍV útvonal)\n");
			} else {
				b.append(" (ABSZOLÚT útvonal)\n");
			}
		} else if(mode.equals(MODE_SINGLE)) {
			b.append("Egy konkrét PDF fájl.\n");
			b.append("A PDF fájl útvonala (-path): " + getPath());
			if(getPathType().equals(PATH_TYPE_REL)) {
				b.append(" (RELATÍV útvonal)\n");
			} else {
				b.append(" (ABSZOLÚT útvonal)\n");
			}
		} else if(mode.equals(MODE_REGEX)) {
			b.append("Reguláris kifejezésnek megfelelő PDF fájlok.\n");
			b.append("A reguláris kifejezés (-regex): " + getRegex() + "\n");
			String folderName = getFolder();
			b.append("A mappa, amiben keresek: " + (folderName.equals(".") ? " jelenlegi munkakönyvtár" : folderName));
			if(getPathType().equals(PATH_TYPE_REL)) {
				b.append(" (RELATÍV útvonal)\n");
			} else {
				b.append(" (ABSZOLÚT útvonal)\n");
			}
		}
		//rekurzív?
		if(isRecursive()) {
			b.append("Rekurzív keresés (-recursive), az almappák is keresve lesznek.\n");
		} else {
			b.append("Nincs rekurzív keresés, csak az aktuális mappa lesz keresve.\n");
		}
		//felülírási szabály
		String ow = getOverwrite();
		if(ow.equals(OVERWRITE_ALL)) {
			b.append("Felülírási szabály (-overwrite): Felülírás mindenhol.\n");
		} else if(ow.equals(OVERWRITE_NONE)) {
			b.append("Felülírási szabály (-overwrite): Eredeti megtartása mindenhol (nincs felülírás).\n");
		} else if(ow.equals(OVERWRITE_SELECT)) {
			b.append("Felülírási szabály (-overwrite): Külön-külön keröl eldöntésre.\n");
		}
		//mute?
		if(isMuted()) {
			b.append("A konzolra kiírás némítva van (-mute).\n");
		} else {
			b.append("A konzolra kiírás engedélyezve van.\n");
		}
		//logfájl
		if(isLogging()) {
			b.append("A log fájl engedélyezve van (-logfile).\n");
			if(isAutoLogging()) {
				b.append("Automatikusan generált log fájl név lesz használva.\n");
			} else {
				b.append("A log fájl RELATÍV útvonala: " + getLogPath() + "\n");
			}
		} else {
			b.append("A log fájl ki van kapcsolva.\n");
		}
		b.append("-----------------------------------------------------------------------");
		return b.toString();
	}
	
	// konstansok -----------------------------------------------------------------------------------------------------------------
	
	private static final String MODE = "-mode";
	
	public static final String MODE_ALL = "all";
	
	public static final String MODE_SINGLE = "single";
	
	public static final String MODE_REGEX = "regex";
	
	private static final String FOLDER = "-folder";
	
	private static final String PATH = "-path";
	
	private static final String PATH_TYPE = "-pathType";
	
	private static final String PATH_TYPE_REL = "relative";
	
	private static final String PATH_TYPE_ABS = "absolute";
	
	private static final String REGEX = "-regex";
	
	private static final String RECURSIVE = "-recursive";
	
	private static final String OVERWRITE = "-overwrite";
	
	public static final String OVERWRITE_ALL = "all";
	
	public static final String OVERWRITE_NONE = "none";
	
	public static final String OVERWRITE_SELECT = "select";
	
	private static final String MUTE = "-mute";
	
	private static final String LOG = "-logfile";
	
	private static final String LOG_AUTO = "auto";
	
}
