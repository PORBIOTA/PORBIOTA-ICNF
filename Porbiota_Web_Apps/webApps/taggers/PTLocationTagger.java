package taggers;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.apache.commons.text.WordUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.AutomatonQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.automaton.Automaton;
import org.apache.lucene.util.automaton.LevenshteinAutomata;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import helpers.Databases;

/**
 * 
 * Class to build and use the lucene Portuguese location query engine
 * 
 * @author Tomé Neves de Matos
 *
 */

public class PTLocationTagger {

	
	/**
	 * Builds the PT location index based on the GeoNames database. Downloaded from: http://download.geonames.org/export/dump/
	 * @throws IOException
	 */
	
	public static void buildPortugueseLocationsIndex() throws IOException {

		Analyzer analyzer = new KeywordAnalyzer(); //WhitespaceAnalyzer()
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		// indexDir = new FSDirectory		
		IndexWriter indexWriter = new IndexWriter(
				FSDirectory.open(FileSystems.getDefault().getPath(Databases.tagIndexDir+"/PortugueseLocationsIndex")), config);


		BufferedReader	originalReader = Files.newBufferedReader(Paths.get("G:\\Porbiota\\GitHub\\Location Words\\PT\\PT.csv"), StandardCharsets.UTF_8 );

		CSVReader csvReader = new CSVReader(originalReader);


		String[] line;
		try {
			while ((line = csvReader.readNext())!=null ) {
				Document doc = new Document();
				doc.add(new TextField("name", line[1], Field.Store.YES));
				doc.add(new TextField("lat", line[4], Field.Store.YES));
				doc.add(new TextField("lon", line[5], Field.Store.YES));
				doc.add(new TextField("code", line[8], Field.Store.YES));

				switch(line[6]) {
				case "A":
					doc.add(new TextField("class", "Region", Field.Store.YES));
					break;
				case "H":
					doc.add(new TextField("class", "Waterbody", Field.Store.YES));
					break;					
				case "L":
					doc.add(new TextField("class", "Place", Field.Store.YES));
					break;
				case "P":
					doc.add(new TextField("class", "Locality", Field.Store.YES));
					break;					
				case "R":
					doc.add(new TextField("class", "Road", Field.Store.YES));
					break;
				case "S":
					doc.add(new TextField("class", "Place", Field.Store.YES));
					break;
				case "T":
					doc.add(new TextField("class", "Mountain", Field.Store.YES));
					break;
				case "U":
					doc.add(new TextField("class", "Undersea", Field.Store.YES));
					break;
				case "V":
					doc.add(new TextField("class", "Forest", Field.Store.YES));
					break;		
				default:
					doc.add(new TextField("class", "NA", Field.Store.YES));
				}


				indexWriter.addDocument(doc);

			}
		} catch (CsvValidationException | IOException e) {
			e.printStackTrace();			

		}
		csvReader.close();

		originalReader = Files.newBufferedReader(Paths.get("G:\\Porbiota\\GitHub\\Location Words\\countryNames\\countryInfo.txt"), StandardCharsets.UTF_8 );


		CSVParser CSVparser = new CSVParserBuilder().withSeparator('	').build();

		csvReader = new CSVReaderBuilder(originalReader).withCSVParser(CSVparser).build();

		try {
			while ((line = csvReader.readNext())!=null ) {

				Document doc = new Document();
				doc.add(new TextField("name", line[5], Field.Store.YES));
				doc.add(new TextField("lat", "NA", Field.Store.YES));
				doc.add(new TextField("lon", "NA", Field.Store.YES));
				doc.add(new TextField("code", line[4], Field.Store.YES));
				doc.add(new TextField("class", "Country", Field.Store.YES));


				indexWriter.addDocument(doc);

			}
		} catch (CsvValidationException | IOException e) {
			e.printStackTrace();

		}

		csvReader.close();


		indexWriter.close();
	}

	
	/**
	 * Returns a String with the existing location names tagged if present in the PT GeoNames database
	 * @param text String to tag
	 * @return String The tagged string
	 * @throws IOException
	 * @throws ParseException
	 */
	
	public static String tag(String text) throws IOException, ParseException {


		int maxWords = 4;
		String textForApi = locationSearchHelper(text, maxWords);
		return textForApi;

	}




	/**
	 * Helper method for the locationSearchHelper method. Returns the ordered matches from a single query.
	 * @param searchString String The query
	 * @param field String The lucene field to search
	 * @return Map<Document, Double> A Map ordered by match proximity
	 * @throws IOException
	 * @throws ParseException
	 */
	private static Map<Document, Double> searchLocations(String searchString, String field) throws IOException, ParseException {

		IndexSearcher isearcher = null;
		DirectoryReader ireader = null;

		ireader = DirectoryReader.open(FSDirectory.open(FileSystems.getDefault().getPath(Databases.tagIndexDir+"/PortugueseLocationsIndex")));
		isearcher = new IndexSearcher(ireader);

		// Parse a simple query that searches for "text":

		TopDocs tophits = null;
		LuceneOrdering orderer = new LuceneOrdering();

		Term term = new Term(field, searchString);
		Automaton fuzzyAutomation =  new LevenshteinAutomata(searchString, true).toAutomaton(2);		
		AutomatonQuery query = new AutomatonQuery(term, fuzzyAutomation);
		tophits = isearcher.search(query, 1000, new Sort(SortField.FIELD_SCORE));
		orderer.getResults(tophits, isearcher, searchString, "AutomatonQuery", field);
		ireader.close();

		return orderer.sortByValue(); 

	}
	
	/**
	 *  Helper method for the tag method
	 * @param searchableText String to search
	 * @param maxWords int Maximum number of words to search at once
	 * @return String The tagged String
	 * @throws IOException
	 * @throws ParseException
	 */
	
	private static String locationSearchHelper (String searchableText,int maxWords) throws IOException, ParseException {
		String field = "name";
		//Clean text of already tagged
		String searchableTextNoTags = searchableText.replaceAll("<location.*?>.*?>", "").replaceAll(" +", " ");		

		String[] textExploded = searchableTextNoTags.split(" |\n");

		String text = searchableTextNoTags;
		String key;	
		Entry<Document, Double> resultado;
		Map<Document, Double> resultados;
		int threshold = 80;
		for (int i = maxWords; i > 0; i--) {
			System.out.println("Max Words: " + i + " Field: " + field);

			for (int x = 0; x < textExploded.length-i; x++) {
				threshold = 80;

				if (textExploded[x].length()>0) {

					key = "";
					int deCount = 0;
					for (int y = 0; y < i+deCount; y++) {
						key += (textExploded[x+y]+" ");
						if (textExploded[x+y].contentEquals("de")) { //When location has a "de" searches for 3 more
							deCount ++;
						}
					}

					key = key.trim().replaceAll("\\.+$", "").replaceAll(",+$", "").replaceAll("[\\)\\(\\]\\[]", "");
					if (key.contains("taxonomicName")) {
						continue;
					}

					//Extra checks for single length words
					if (i == 1 || !key.trim().contains(" ")) {
						//does not give results that are Portuguese words
						//	 if(existsInPortuguese(key)) {
						//		continue;							
						//	 }			 					
						threshold = 90;

						if (key.contains("-")) {
							threshold = 80;
						}



						//List usual error prone words
						if (key.contains("Norte")||
								key.contains("Sul")||
								key.contains("Este")||
								key.contains("Oeste")||
								key.contains("Estação")||
								key.contains("Mar")||
								key.contains("Portugal")) {
							continue;
						}
					}

					//Only checks words, or sentences, that start with uppercase
					if ((key.length()>0 && Character.isUpperCase(key.codePointAt(0)))) {

						String keyCapitalized = WordUtils.capitalizeFully(key);
						resultados =searchLocations(keyCapitalized,field);
						Map <Document,Double> tempResultados = resultados;						
						if (resultados.size()>0) {

							for (int pass = 0; pass < 2; pass++) {
								Iterator<Entry<Document, Double>> resultIterator = resultados.entrySet().iterator();
							
								resultado = resultIterator.next();

								while (	resultado != null && pass==0 && 
										!resultado.getKey().getField("class").stringValue().contentEquals("Locality")) {
									//Loops until a locality is found in the first pass
									try {
										resultado = resultIterator.next();
									} catch (NoSuchElementException e) {
										resultado = null;
									}
								}				

								
								if (resultado != null && resultado.getValue()>threshold) {		

									System.out.println("Matched " + key + " to " + resultado.getKey().getField("name").stringValue().replace("?", "\\?") +  " with a score of "+ resultado.getValue());

									//Updates text
									int previousTextSize = text.length();
									text = text.replaceAll("(?<![a-zA-Z])"+key+"(?![a-zA-Z])", "<location lat="+resultado.getKey().getField("lat").stringValue()+" lon=" +resultado.getKey().getField("lon").stringValue()+">"+key+"</location>");

									//Only walks back if any actual match happens
									if (text.length() != previousTextSize) {
										x--;
									}
									searchableTextNoTags = text.replaceAll("<location.*?>.*?>", "");
									textExploded = searchableTextNoTags.replaceAll(" +", " ").split(" |\n");	
									break;
								} 
								//If the threshold isn't achieved in the first pass, it goes back and tries for more generic 
								resultados = tempResultados;

							}
						}

					}
				}
			}
		}
		return text;	
	}


}
