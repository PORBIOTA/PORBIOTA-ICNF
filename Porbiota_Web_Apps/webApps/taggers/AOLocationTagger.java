package taggers;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;

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
 * Class to build and use the lucene Angola location query engine
 * 
 * @author Tomé Neves de Matos
 *
 */

public class AOLocationTagger {

	/**
	 * Builds the AO location index based on the GeoNames database. Downloaded from: http://download.geonames.org/export/dump/
	 * @throws IOException
	 */
	public static void buildAngolaLocationsIndex() throws IOException {
	
		Analyzer analyzer = new KeywordAnalyzer(); //WhitespaceAnalyzer()
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		// indexDir = new FSDirectory		
		IndexWriter indexWriter = new IndexWriter(
				FSDirectory.open(FileSystems.getDefault().getPath(Databases.tagIndexDir+"/AngolaLocationsIndex")), config);
	
	
		BufferedReader	originalReader = Files.newBufferedReader(Paths.get("G:\\Porbiota\\GitHub\\Location Words\\Angola\\AO.txt"), StandardCharsets.UTF_8 );
	
		String tempLine;
		String[] line;
		try {
			while ((tempLine = originalReader.readLine())!=null ) {
	
				line = tempLine.split("\t");
				Document doc = new Document();
				doc.add(new TextField("name", line[1], Field.Store.YES));
				doc.add(new TextField("lat", line[4], Field.Store.YES));
				doc.add(new TextField("lon", line[5], Field.Store.YES));
				doc.add(new TextField("code", line[8], Field.Store.YES));
	
				indexWriter.addDocument(doc);
	
	
			}
		} catch (IOException e) {
			e.printStackTrace();
	
		}
		originalReader.close();
	
	
		originalReader = Files.newBufferedReader(Paths.get("G:\\Porbiota\\GitHub\\Location Words\\countryNames\\countryInfo.txt"), StandardCharsets.UTF_8 );
	
	
		CSVParser CSVparser = new CSVParserBuilder().withSeparator('	').build();
	
		CSVReader csvReader = new CSVReaderBuilder(originalReader).withCSVParser(CSVparser).build();
	
		try {
			while ((line = csvReader.readNext())!=null ) {
	
				Document doc = new Document();
				doc.add(new TextField("name", line[5], Field.Store.YES));
				doc.add(new TextField("lat", "NA", Field.Store.YES));
				doc.add(new TextField("lon", "NA", Field.Store.YES));
				doc.add(new TextField("code", line[4], Field.Store.YES));
	
				indexWriter.addDocument(doc);
	
			}
		} catch (CsvValidationException | IOException e) {
			e.printStackTrace();
	
		}
	
		csvReader.close();
	
	
		indexWriter.close();
	}

	/**
	 * Returns a String with the existing location names tagged if present in the AO GeoNames database
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

		ireader = DirectoryReader.open(FSDirectory.open(FileSystems.getDefault().getPath(Databases.tagIndexDir+"/AngolaLocationsIndex")));
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

					key = key.trim().replaceAll("\\.+$", "").replaceAll(",+$", "").replace(")", "").replace("(", "");
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
						if (resultados.size()>0) {
							resultado = resultados.entrySet().iterator().next();
							if (resultado.getValue()>threshold) {		

								System.out.println("Matched " + key + " to " + resultado.getKey().getField("name").stringValue().replace("?", "\\?") +  " with a score of "+ resultado.getValue());

								//Updates text
								int previousTextSize = text.length();
								text = text.replaceAll("(?<![a-zA-Z])"+key+"(?![a-zA-Z])", "<location>"+key+"</location>");
								
								//Only walks back if any actual match happens
								if (text.length() != previousTextSize) {
									x--;
								}
								searchableTextNoTags = text.replaceAll("<location.*?>.*?>", "");
								textExploded = searchableTextNoTags.replaceAll(" +", " ").split(" |\n");	
							}

						}

					}
				}
			}
		}
		return text;	
	}


}
