package taggers;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import helpers.Databases;

/**
 * 
 * Class to build and use the lucene Species query engine
 * 
 * @author Tomé Neves de Matos
 *
 */

public class SpeciesTagger {
	
	/**
	 * Builds the species index based on the gbif backbone. Downloaded from: https://www.gbif.org/dataset/d7dddbf4-2cf0-4f39-9b2a-bb099caae36c
	 * @throws IOException
	 */

	public static void buildSpeciesIndex() throws IOException {


		Analyzer analyzer = new KeywordAnalyzer(); //WhitespaceAnalyzer()
		IndexWriterConfig config = new IndexWriterConfig(analyzer);

		//folderChooser.cacheFolder.getAbsolutePath() 
		IndexWriter indexWriter = new IndexWriter(
				FSDirectory.open(FileSystems.getDefault().getPath(Databases.tagIndexDir+"/SpeciesIndex")), config);

		BufferedReader originalReader = Files.newBufferedReader(Paths.get("G:\\Porbiota\\GitHub\\Location Words\\backbone-current\\Taxon.tsv"), StandardCharsets.UTF_8 );

		String tempLine;
		String[] line;
		try {
			while ((tempLine = originalReader.readLine())!=null ) {

				line = tempLine.split("\t");
				if (line[7].contentEquals("New") || line[7].contains("?")) {
					continue;
				}

				Document doc = new Document();
				doc.add(new TextField("scientificName", line[5], Field.Store.YES));
				doc.add(new TextField("scientificNameAuthorship", line[6], Field.Store.YES));
				doc.add(new TextField("canonicalName", line[7], Field.Store.YES));				
				doc.add(new TextField("genericName", line[8], Field.Store.YES));				
				doc.add(new TextField("specificEpithet", line[9], Field.Store.YES));	
				doc.add(new TextField("infraspecificEpithet", line[10], Field.Store.YES));	
				doc.add(new TextField("taxonRank", line[11], Field.Store.YES));	

				indexWriter.addDocument(doc);


			}
		} catch (IOException e) {
			e.printStackTrace();

		}
		originalReader.close();

		indexWriter.close();
	}
	
	/**
	 * Returns a String with the existing species names tagged if present in the GBIF database
	 * @param text String to tag
	 * @return String The tagged string
	 * @throws IOException
	 * @throws ParseException
	 */

	public static String tag(String text) throws IOException, ParseException {

		int maxWords = 4;
		text = speciesSearchHelper(text, maxWords);

		return text;

	}
	
	/**
	 *  Helper method for the tag method
	 * @param searchableText String to search
	 * @param maxWords int Maximum number of words to search at once
	 * @return String The tagged String
	 * @throws IOException
	 * @throws ParseException
	 */

	private static String speciesSearchHelper (String searchableText,int maxWords) throws IOException, ParseException {
		String field = "canonicalName";
		//Clean text of already tagged
		String searchableTextNoTags = searchableText.replaceAll("<taxonomicName.*?>.*?>", "").replaceAll(" +", " ");		

		String[] textExploded = searchableTextNoTags.split(" |\n");


		String text = searchableTextNoTags;
		String key;	
		String newkey;
		Entry<Document, Double> resultado;
		Map<Document, Double> resultados;
		Entry<Document, Double> resultadoAuthor;
		Map<Document, Double> resultadosAuthor;
		boolean authorMatched = false;
		int threshold = 80;
		Set<String> lastMatchedGenus = new LinkedHashSet<String>();
		for (int i = maxWords; i > 0; i--) {


			System.out.println("Max Words: " + i + " Field: " + field);

			for (int x = 0; x < textExploded.length-i; x++) {

				//To check if a "sp." exists
				String spExists = "";

				//Resets threshold
				threshold = 80;
				//Only checks words, or sentences, that start with uppercase
				if (textExploded[x].length()>0) {
					authorMatched = false;
					key = "";
					for (int y = 0; y < i; y++) {
						key += (textExploded[x+y]+" ");
					}

					key = key.trim().replaceAll("\\.+$", "").replaceAll(",+$", "").replaceAll("[\\)\\(\\]\\[]", "");


					//Checks and clears "sp."
					if (key.contains(" ") && key.substring(key.length()-3).equalsIgnoreCase(" sp")) {
						spExists = " sp.";						
						key = key.substring(0,key.length()-3);					
					}

					//Extra checks for single length words
					if (i == 1 || !key.contains(" ")) {
						//does not give results that are Portuguese words
						if(PortugueseDictionary.exists(key)) {
							continue;							
						}			 					
						threshold = 99;						
					}

					if ((key.length()>0 && Character.isUpperCase(key.codePointAt(0)))) {

						resultados =searchSpecies(key,field);
						if (resultados.size()>0) {
							resultado = resultados.entrySet().iterator().next();
							if (resultado.getValue()>threshold) {	

								//if matched, checks for authors
								for (int ii = 5; ii > 0; ii--) {
									String authorKey = "";
									for (int y = 0; y < i+ii && y+x < textExploded.length; y++) {
										authorKey += (textExploded[x+y]+" ");
									}
									resultadosAuthor =searchSpecies(authorKey,"scientificName");
									if (resultadosAuthor.size()>0) {
										resultadoAuthor = resultadosAuthor.entrySet().iterator().next();
										if (resultadoAuthor.getValue()>threshold) {	
											System.out.println("Matched with Author " + authorKey + " to " + resultadoAuthor.getKey().getField("scientificName").stringValue() + " with a taxon rank of "+ resultadoAuthor.getKey().getField("taxonRank").stringValue()+ " and with a score of "+ resultadoAuthor.getValue());

											//Updates text
											int previousTextSize = text.length();
											text = text.replace(authorKey.trim(), "<taxonomicName" +" scientificName="+resultadoAuthor.getKey().getField("scientificName").stringValue()+">"+authorKey.trim()+"</taxonomicName>");
											searchableTextNoTags = text.replaceAll("<taxonomicName.*?>.*?>", "");	
											textExploded = searchableTextNoTags.replaceAll(" +", " ").split(" |\n");	

											if (previousTextSize != text.length()) {
												x--;	
											}										


											lastMatchedGenus.add(resultadoAuthor.getKey().getField("genericName").stringValue());
											authorMatched = true;
											break;
										}
									}
								}

								//done checking for authors

								if(!authorMatched) {									
									System.out.println("Matched " + key + " to " + resultado.getKey().getField("scientificName").stringValue().replace("?", "\\?") + " with a taxon rank of "+ resultado.getKey().getField("taxonRank").stringValue()+ " and with a score of "+ resultado.getValue());

									//Updates text
									int previousTextSize = text.length();

									text = text.replace(key.trim(), "<taxonomicName" +" scientificName="+resultado.getKey().getField("scientificName").stringValue()+">"+key.trim()+spExists+"</taxonomicName>");
									searchableTextNoTags = text.replaceAll("<taxonomicName.*?>.*?>", "");	
									textExploded = searchableTextNoTags.replaceAll(" +", " ").split(" |\n");	
									//x = x - (previousTextExplodedSize - textExploded.length);
									if (previousTextSize != text.length()) {
										x--;	
									}		

									lastMatchedGenus.add(resultado.getKey().getField("genericName").stringValue());
								}
							}

							//Checks "X." for previously captured genus 
						}   else if (key.substring(1,2).contentEquals(".")) {
							Object[] genusArray =  lastMatchedGenus.toArray();
							for (int aa = genusArray.length ; aa > 0; aa--) {
								String genus = genusArray[aa-1].toString();
								if (key.substring(0,1).contentEquals(genus.substring(0,1))) {								
									newkey = genus + key.substring(2);
									resultados =searchSpecies(newkey,field);
									if (resultados.size()>0) {
										resultado = resultados.entrySet().iterator().next();
										if (resultado.getValue()>threshold) {		
											System.out.println("Matched " + key + " to " + resultado.getKey().getField("scientificName").stringValue() + " with a taxon rank of "+ resultado.getKey().getField("taxonRank").stringValue()+ " and with a score of "+ resultado.getValue());

											//Updates text
											int previousTextSize = text.length();

											text = text.replace(key.trim(), "<taxonomicName" +" scientificName="+resultado.getKey().getField("scientificName").stringValue()+">"+key.trim()+"</taxonomicName>");
											searchableTextNoTags = text.replaceAll("<taxonomicName.*?>.*?>", "");	
											textExploded = searchableTextNoTags.replaceAll(" +", " ").split(" |\n");	
											//	x = x - (previousTextExplodedSize - textExploded.length);
											if (previousTextSize != text.length()) {
												x--;	
											}		
											break;
										}

									}						
								}
							}
						}
					}

				}
			}
		}


		
		//Repeats to deal with tags inside of tags inside of tags
		int textSize = 0;
		while (textSize != text.length()) {
			textSize = text.length();
			text = text.replaceAll("(<taxonomicName((?!>).)*>?((?!</taxonomicName>).)*)<taxonomicName((?!>).)*>?(((?!</taxonomicName>).)*)</taxonomicName>","$1$5");
		}

		//After tagging the entire text, checks for the existence of author names after the tags, and if so, moves the tag to capture them
		Pattern pattern = Pattern.compile("</taxonomicName>[&A-Za-zÀ-ÖØ-öø-ÿ\\(\\)\\s]+,\\s?\\d{4}\\)?");
		Matcher matcher = pattern.matcher(text);      
		while(matcher.find() ) {
			
			//checks if the author was already matched 
			if (text.substring(matcher.start()-10,matcher.start()).matches(".*\\d{4}.*")) {
				continue;
			}

			//Tags the text			
			text  = text.substring(0,matcher.start()) + 
					text.substring(matcher.start()+"</taxonomicName>".length(),matcher.end()) +
					"</taxonomicName>" +
					text.substring(matcher.end());		
		}		
		return text;	
	}
		
		/**
		 * Helper method for the speciesSearchHelper method. Returns the ordered matches from a single query.
		 * @param searchString String The query
		 * @param field String The lucene field to search
		 * @return Map<Document, Double> A Map ordered by match proximity
		 * @throws IOException
		 * @throws ParseException
		 */
	private static Map<Document, Double> searchSpecies(String searchString, String field) throws IOException, ParseException {

		IndexSearcher isearcher = null;
		DirectoryReader ireader = null;

		ireader = DirectoryReader.open(FSDirectory.open(FileSystems.getDefault().getPath(Databases.tagIndexDir+"/SpeciesIndex")));
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



}
