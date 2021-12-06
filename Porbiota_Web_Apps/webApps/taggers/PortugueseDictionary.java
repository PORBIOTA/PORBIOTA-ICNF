package taggers;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
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
 * Class to build and use the lucene Portuguese dictionary query engine
 * 
 * @author Tomé Neves de Matos
 *
 */

public class PortugueseDictionary {
	
	/**
	 * Builds the portuguese dictionary. Downloaded from: https://github.com/gusbemacbe/LanguagePortuguese
	 * @throws IOException
	 */
	
	public static void buildPortugueseIndex() throws IOException {


		Analyzer analyzer = new KeywordAnalyzer(); //WhitespaceAnalyzer()
		IndexWriterConfig config = new IndexWriterConfig(analyzer);

		//folderChooser.cacheFolder.getAbsolutePath() 
		IndexWriter indexWriter = new IndexWriter(
				FSDirectory.open(FileSystems.getDefault().getPath(Databases.tagIndexDir+"/PortugueseIndex")), config);

		BufferedReader originalReader = Files.newBufferedReader(Paths.get("G:\\Porbiota\\GitHub\\Location Words\\Dictionaries\\Portuguese (European - Before OA 1990).dic"), StandardCharsets.UTF_8 );
		String tempLine ;
		try {
			while ((tempLine = originalReader.readLine())!=null ) {

				Document doc = new Document();
				doc.add(new TextField("word", tempLine.trim(), Field.Store.YES));

				indexWriter.addDocument(doc);


			}
		} catch (IOException e) {
			e.printStackTrace();

		}
		originalReader.close();

		indexWriter.close();
	}

	/**
	 * Checks if the given string is a Portuguese word
	 * @param word
	 * @return
	 * @throws IOException
	 */
	
	static public boolean exists (String wordToSearch) throws IOException {
	
		String word = wordToSearch.replace(".", "").replace(",", "").replace("\n", "");
		IndexSearcher isearcher = null;
		DirectoryReader ireader = null;
	
		ireader = DirectoryReader.open(FSDirectory.open(FileSystems.getDefault().getPath(Databases.tagIndexDir+"/PortugueseIndex")));
		isearcher = new IndexSearcher(ireader);
	
		// Parse a simple query that searches for "text":
	
		TopDocs tophits = null;
		LuceneOrdering orderer = new LuceneOrdering();
		
		Term term = new Term("word", word);
		Automaton fuzzyAutomation =  new LevenshteinAutomata(word, true).toAutomaton(2);
		AutomatonQuery query = new AutomatonQuery(term, fuzzyAutomation);
		tophits = isearcher.search(query, 1000, new Sort(SortField.FIELD_SCORE));
		orderer.getResults(tophits, isearcher, word, "AutomatonQuery", "word");
		ireader.close();
	
		Entry<Document, Double> resultado;
		Map<Document, Double> resultados;
	
		resultados = orderer.sortByValue(); 
		if (resultados.size()>0) {
			resultado = resultados.entrySet().iterator().next();
			if (resultado.getValue()>99) {
				return true;
			}
		}  
		return false;
	}
}
