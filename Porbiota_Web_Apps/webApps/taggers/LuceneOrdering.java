package taggers;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
/**
 * Almost entirely from: https://github.com/subhayandutta/fuzzyinjava/blob/master/src/com/sd/fuzzy/FuzzyTest.java
 * @author Subhayan Dutta
 *
 */
public class LuceneOrdering {
	
	/**
	 * Map of matched strings and %cent match
	 */
	private ConcurrentHashMap<Document , Double> matchedFirstNameVsPercentage;
	
	/**
	 * A convenient way to display the matched results along with the query used for the search
	 * @param tophits - The top hits from the query
	 * @param isearcher - The Searcher to use
	 * @param searchString - The String which is searched
	 * @param queryName - The name of the query used. In this example Automation query
	 * @throws IOException
	 */
	
	
	public LuceneOrdering() {
		matchedFirstNameVsPercentage = new ConcurrentHashMap<>();
	}
	

	void getResults(TopDocs tophits, IndexSearcher isearcher, String searchString, String queryName, String field) throws IOException {
		matchedFirstNameVsPercentage.clear();
		for (ScoreDoc scoreDoc : tophits.scoreDocs) {
			Document doc = isearcher.doc(scoreDoc.doc);
			if (matchedFirstNameVsPercentage.containsKey(doc))
				continue;
			double percentMatch = 100 * similarity(searchString, doc.get(field));
			if ( matchedFirstNameVsPercentage.containsKey(doc) ) {
				double percentVal = matchedFirstNameVsPercentage.get(doc);
				if ( doc.get(field).trim().equalsIgnoreCase(searchString) ) {
					percentVal = 100;
				}				
				matchedFirstNameVsPercentage.put(doc, percentVal);
			}
			else {
				double percentVal = percentMatch;
				try {
					percentVal = matchedFirstNameVsPercentage.get(doc);
					if ( doc.get(field).trim().equalsIgnoreCase(searchString) ) {
						percentVal = 100;
					}
				} catch (NullPointerException npe) {
				}
				matchedFirstNameVsPercentage.put(doc, percentVal);
			}
		}
	}
	

	//Ref: https://stackoverflow.com/questions/47905195/how-to-do-percentage-match-between-list-of-strings-in-apache-lucene/48628326
	double similarity(String s1, String s2) {
		String longer = s1, shorter = s2;
		if (s1.length() < s2.length()) { // longer should always have greater length
			longer = s2; shorter = s1;
		}
		int longerLength = longer.length();
		if (longerLength == 0) { return 1.0; /* both strings are zero length */ }
		/* // If you have Apache Commons Text 
		     // you can use it to calculate the edit distance:
		    LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
		    return (longerLength - levenshteinDistance.apply(longer, shorter)) / (double) longerLength; */
		return (double)(longerLength - editDistance(longer, shorter)) / (double) longerLength;

	}

	int editDistance(String s1, String s2) {
		s1 = s1.toLowerCase();
		s2 = s2.toLowerCase();

		int[] costs = new int[s2.length() + 1];
		for (int i = 0; i <= s1.length(); i++) {
			int lastValue = i;
			for (int j = 0; j <= s2.length(); j++) {
				if (i == 0)
					costs[j] = j;
				else {
					if (j > 0) {
						int newValue = costs[j - 1];
						if (s1.charAt(i - 1) != s2.charAt(j - 1))
							newValue = Math.min(Math.min(newValue, lastValue),
									costs[j]) + 1;
						costs[j - 1] = lastValue;
						lastValue = newValue;
					}
				}
			}
			if (i > 0)
				costs[s2.length()] = lastValue;
		}
		return costs[s2.length()];
	}

	/**
	 * Convenient function to sort the data by value, highest value first
	 * @param hm
	 * @return
	 */
	HashMap<Document, Double> sortByValue() { 
		// Create a list from elements of HashMap 
		List<Map.Entry<Document, Double> > list = 
				new LinkedList<Map.Entry<Document, Double> >(matchedFirstNameVsPercentage.entrySet()); 

		// Sort the list 
		Collections.sort(list, new Comparator<Map.Entry<Document, Double> >() { 
			@Override
			public int compare(Map.Entry<Document, Double> o1,  
					Map.Entry<Document, Double> o2) 
			{ 
				return (o2.getValue()).compareTo(o1.getValue()); 
			} 
		}); 

		// put data from sorted list to hashmap  
		HashMap<Document, Double> temp = new LinkedHashMap<Document, Double>(); 
		for (Map.Entry<Document, Double> aa : list) { 
			temp.put(aa.getKey(), aa.getValue()); 
		} 
		return temp; 
	} 

}
