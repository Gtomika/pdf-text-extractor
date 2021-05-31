## PDF szöveg kinyerő

Abban a mappában (és akár almappákban), ahol el lett indítva kinyeri az összes PDF fájlból 
a szöveget, és azt azonos nevű szövegfájlokba írja. Minden PDF helyett lehetőség van egy konkrét fájl 
választására, vagy reguláris kifejezéssel történő megadásra.

## Futtatás

Konzolról indítható:

*java -jar PdfTextExtractor.jar [argumentumok]*

A lehetséges argumentumokért lásd az *Argumentumok* részt.

## Argumentumok

 - **-mode**: megmondja, hogy hogyan kell kiválasztani a PDF fájlokat. Lehet 'all', ami minden PDF-et kiválaszt a jelen mappában. 
	Lehet 'single', ami egy PDF-et fog kiválasztani (ezt a '-path' argumentumban kell átadni). Lehet 'regex', ami reguláris kifejezés alapján választ (amit a '-regex' argumentumban kell 
	átadni). Alapértéke az 'all'.
 - **-path**: Ezzel kell megadni a PDF fájl RELATÍV útvonalát, ha a válaasztótt mód 'single' (-mode=single). Ha például a document.pdf ugyanabban a mappában van, ahol a JAR, akkor 
	ez '-path=document.pdf' lesz. Ha például a JAR mappájában van egy 'docs' nevű mappa, és abban van a PDF, akkor ez '-path=docs/document.pdf' lesz. A '-mode' argumot már előtte meg kell adni!
 - **-regex**: Ezzel kell megadni a JAVA reguláris kifejezést, amivel a PDF-ek kiválasztásra kerülnek, ha a mód regex (-mode=regex). Érvényes Java regex-nek kell lennie és nem tartalmazhatja a 
	szóköz karaktert, mert akkor az új argumentumként lenne értelmezve (szóköz helyett a *\\s* írható, ami azt fogja jelenteni). 
	Például az 'fn' kezdetű PDF fájlokat feldolgozó regex így adható meg: '-regex=fn.\*'  A '-mode' argumot már előtte meg kell adni!
 - **-recursive**: Ha ez az argumentum meg van adva, akkor a PDF-ek az almappákban is rekurzívan keresve lesznek. Ha nincs megadva, akkor csak a JAR mappájában lesznek 
	keresve, az almappákban már nem. Alapértékben nem lesz rekurzív. Hasznos ha az almappákban is vannak feldolgozandó PDF-ek.
 - **-overwrite**: Ezzel kell megadni, hogy hogyan legyenek felülírva a már létező *txt* fájlok. Lehet 'all', ami mindent felülír. Lehet 'none', ami semmit nem fog felülírni, ami már megvan. 
	Lehet 'select', ami minden talált PDF esetén egyenként megkérdezi a felülírást, ha már létezik a text fájl. Alapértékben nem lesz felülírás.
 - **-mute**: Ha ez meg van adv, akkor a program semmit sem fog kiírni a konzolra, csak a kezdeti és a befejező üzenetet. Egyébként sok egyéb információ is kiíródik. 
	Alapértékben ez ki van kapcsolva, azaz minden kiíródik.
 - **-logfile**: Ezzel lehet megadni egy fájl RELATÍV útvonalát, ahova bekerülnek a feldolgozással kapcsolatos információk (ezek ugyanazok, mint amik a konzolra is 
	kiíródnak, ha nincs megadott '-mute'. Ha ez a fájl létezik, akkor felül fog íródni. Például ha egy 'log.txt' szövegfájlba kérjük a logot, ami a JAR mappájába kerül, akkor 
	'-logfile=log.txt' az argumentum. Egy speciális értéke az 'auto', ilyenkor a log fájl neve automatikusan kerül generálásra. Alapértéke (ha nem adjuk meg) az, hogy nem lesz log fájl készítve.

#Példák

Bemutatok néhány példát a használatra. Ezeknél persze jóval többféleképpen lehet kombinálni 
az argumentumokat.

\1. Minden argumentum alapértéken:

*java -jar PdfTextExtractor.jar*

\2. Egy konkrét 'document.pdf' feldolgozása, a konzol némításával, de egy 'history.log' készítésével:

*java -jar PdfTextExtractor.jar -mode=single -path=document.pdf -mute -logfile=history.log*

\3. Rekurzívan minden PDF feldolgozása a JAR mappájában, egy automatikusan elnevezett logfájl használatával. A felülírást 
minden esetben meg akarjuk tenni:

*java -jar PdfTextExtractor.jar -mode=all -recursive -logfile=auto -overwrite=all*

\4. Reguláris kifejezéssel megadott PDF-ek feldolgozása, rekurzívan keresve, ahol azokat a PDF-eket dolgozzuk fel, amelyek nevében 
benne van az 'abc' szöveg. A felülírásról egyenként szeretnénk dönteni:

*java -jar PdfTextExtractor.jar -mode=regex -regex=.\*abc.\* -recursive -overwrite=select*

## Letöltés

[GitHub release oldal](https://github.com/Gtomika/pdf-text-extractor/releases/download/0.1/PdfTextExtractor.jar)