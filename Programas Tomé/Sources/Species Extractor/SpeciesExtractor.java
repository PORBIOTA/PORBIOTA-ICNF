package csvEditorFullLoad;


import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



public class SpeciesExtractor {

	//Arg 0 - Kingdom
	//Arg 1 - Input
	//Arg 2 - Output
	public static void main(String[] args) throws IOException {


		//READER
		String fileName = args[1];
		Path myPath = Paths.get(fileName);

		List<OccurenceForSpecies> occurences = null;

		try (BufferedReader br = Files.newBufferedReader(myPath,StandardCharsets.UTF_8)) {

			HeaderColumnNameMappingStrategy<OccurenceForSpecies> strategy
			= new HeaderColumnNameMappingStrategy<>();
			strategy.setType(OccurenceForSpecies.class);

			CsvToBean<OccurenceForSpecies> csvToBean = new CsvToBeanBuilder<OccurenceForSpecies>(br)
					.withMappingStrategy(strategy)
					.withIgnoreLeadingWhiteSpace(true)
					.build();

			occurences = csvToBean.parse();

		} catch (IOException e) {
			System.out.println("There is no input file!");
			System.exit(1);
		}


		//WORKING THE DATA

		String kingdomOfSpecies = args[0];
		ListIterator<OccurenceForSpecies> occurenceIterator = occurences.listIterator();		
		HashMap<String, JSONObject[]> speciesJSONs = new HashMap<String, JSONObject[]>();
		JSONObject[] obj = new JSONObject[2];
		JSONParser parser = null;


		int counter = 1;
		while (occurenceIterator.hasNext()) {
			System.out.println("Working on line " + counter);
			counter++;

			OccurenceForSpecies occ = occurenceIterator.next();
			occ.setOccurenceID(UUID.randomUUID().toString());
			occ.setScientificName(	occ.getScientificName().replace(" sp.", ""));

			if (!speciesJSONs.containsKey(occ.getScientificName())) {	

				String s = "https://api.gbif.org/v1/species/match?verbose=true&kingdom="+URLEncoder.encode(kingdomOfSpecies, "UTF-8")+"&name=";
				s += URLEncoder.encode(occ.getScientificName(), "UTF-8");
				URL url = new URL(s);

				// read from the URL
				Scanner scan = new Scanner(url.openStream(),"utf-8");
				String str = new String();
				while (scan.hasNext())
					str += scan.nextLine();
				scan.close();

				// build a JSON object
				parser = new JSONParser();		
				try {
					obj[0] = (JSONObject) parser.parse(str);
				} catch (ParseException e) {

					e.printStackTrace();
				} 	

				//If synonym get new name	
				if ((!((String) obj[0].get("canonicalName")).equalsIgnoreCase((String) obj[0].get("species"))) && (((String) obj[0].get("rank")).equalsIgnoreCase("Species") || ((String) obj[0].get("rank")).equalsIgnoreCase("Subspecies"))) {

					s = "https://api.gbif.org/v1/species/match?verbose=true&kingdom="+URLEncoder.encode(kingdomOfSpecies, "UTF-8")+"&name=";
					s += URLEncoder.encode(((String) obj[0].get("species")), "UTF-8");
					url = new URL(s);

					// read from the URL
					scan = new Scanner(url.openStream(),"utf-8");
					str = new String();
					while (scan.hasNext())
						str += scan.nextLine();
					scan.close();

					// build a JSON object
					parser = new JSONParser();		
					try {
						obj[1] = (JSONObject) parser.parse(str);
					} catch (ParseException e) {

						e.printStackTrace();
					} 	
				}

				//0 will be original call, 1 will be the call for the synonym species
				speciesJSONs.put(occ.getScientificName(),(JSONObject[]) obj.clone());

			}

			//Fetch original or synonym
			int apiCall = 0;
			if (speciesJSONs.get(occ.getScientificName())[1] != null) {
				apiCall = 1;
			} 

			JSONObject[] currentJSON = speciesJSONs.get(occ.getScientificName());

			String speciesName = occ.getScientificName();

			String authorship = (String) currentJSON[apiCall].get("scientificName");

			//Delete species authors
			if(speciesName.contains("(")){
				speciesName = (String) speciesName.subSequence(0, speciesName.indexOf('('));
			}

			//Store species authors
			if(authorship.contains("(")){
				occ.setNameAuthorship((String) authorship.subSequence(authorship.indexOf('(')+1, authorship.indexOf(')')));
				authorship = (String) authorship.subSequence(0, authorship.indexOf('('));
			} else {

				//Checks for author with date but no ( ) to delete
				try {
					if(speciesName.substring(speciesName.length()-2).matches("\\d+")) {					

						//Gets rid of date
						authorship += (String) speciesName.subSequence(speciesName.lastIndexOf(" "),speciesName.length());
						speciesName = (String) speciesName.subSequence(0,speciesName.lastIndexOf(" "));

						//Gets rid of author
						authorship = (String) speciesName.subSequence(speciesName.lastIndexOf(" "),speciesName.length()) + authorship;
						speciesName = (String) speciesName.subSequence(0,speciesName.lastIndexOf(" "));


						//Checks for von
						speciesName = (String) speciesName.replace(" von","");

						//Checks for &
						if (speciesName.substring(speciesName.length()-1).matches("&")) {
							//Gets rid of &
							speciesName = (String) speciesName.subSequence(0,speciesName.lastIndexOf(" "));

							//Gets rid of author
							speciesName = (String) speciesName.subSequence(0,speciesName.lastIndexOf(" "));

						}

					} 
				} catch (StringIndexOutOfBoundsException e) {

				}

				//Checks for author with date but no ( ) to store
				try {
					if(authorship.substring(authorship.length()-2).matches("\\d+")) {					

						String authorNoPar = "";

						//Removes spaces from vons
						authorship = (String) authorship.replace(" von "," von");

						//Stores and removes date
						authorNoPar += (String) authorship.subSequence(authorship.lastIndexOf(" "),authorship.length());
						authorship = (String) authorship.subSequence(0,authorship.lastIndexOf(" "));

						//Stores and removes author
						authorNoPar = (String) authorship.subSequence(authorship.lastIndexOf(" "),authorship.length()) + authorNoPar;
						authorship = (String) authorship.subSequence(0,authorship.lastIndexOf(" "));


						occ.setNameAuthorship(authorNoPar);

						//Checks for &
						if (authorship.substring(authorship.length()-1).matches("&")) {
							//Stores and removes &						
							authorNoPar = (String) authorship.subSequence(authorship.lastIndexOf(" "),authorship.length()) + authorNoPar;
							authorship = (String) authorship.subSequence(0,authorship.lastIndexOf(" "));

							//Stores and removes the author
							authorNoPar = (String) authorship.subSequence(authorship.lastIndexOf(" "),authorship.length()) + authorNoPar;
							authorship = (String) authorship.subSequence(0,authorship.lastIndexOf(" "));

							occ.setNameAuthorship(authorNoPar);
						}		


					} 
				} catch (StringIndexOutOfBoundsException e) {

				}
			}

			//Genus
			String speciesGenus = "";
			if(speciesName.contains(" ")){
				speciesGenus = (String) speciesName.subSequence(0, speciesName.indexOf(' '));
			} else {
				speciesGenus = speciesName;
			}

			//Specific and Infra Epithet
			String speciesEpithet = "";
			String speciesInfraEpithet = "";
			if(speciesName.contains(" ")){
				if (speciesName.indexOf(' ') != speciesName.lastIndexOf(' ')) {
					speciesEpithet = (String) speciesName.subSequence(speciesName.indexOf(' ')+1, speciesName.lastIndexOf(' '));
					speciesInfraEpithet = (String) speciesName.subSequence(speciesName.lastIndexOf(' ')+1,speciesName.length());
				} else {
					speciesEpithet = (String) speciesName.subSequence(speciesName.indexOf(' ')+1, speciesName.length());
					speciesInfraEpithet = "";
				}
			}

			occ.setSpecificEpithet(speciesEpithet);
			occ.setInfraspecificEpithet(speciesInfraEpithet);



			//Define scientific name

			//Fixes strange matching with non matching items by making sure Kingdom matches come up empty in all taxonomic fields
			if (((String) speciesJSONs.get(occ.getScientificName())[0].get("rank")).equalsIgnoreCase("KINGDOM")) {
				occ.setAcceptedNameUsage((String) currentJSON[0].get("scientificName"));
				occ.setKingdom((String) speciesJSONs.get(occ.getScientificName())[apiCall].get("kingdom"));
				occ.setSpecificEpithet("");
				occ.setInfraspecificEpithet("");
				occ.setGenus("");
				occ.setPhylum("");
				occ.setClass("");
				occ.setOrder("");
				occ.setFamily("");
				occ.setConfidence((long) speciesJSONs.get(occ.getScientificName())[0].get("confidence"));
				occ.setTaxonRank((String) speciesJSONs.get(occ.getScientificName())[0].get("rank"));


			} else {
				
				//Sets the various taxonomic fields
				occ.setAcceptedNameUsage((String) currentJSON[apiCall].get("scientificName"));

				occ.setConfidence((long) speciesJSONs.get(occ.getScientificName())[0].get("confidence"));

				try {
					occ.setTaxonRank((String) speciesJSONs.get(occ.getScientificName())[0].get("rank"));
				} catch (NullPointerException e) {
				}

				try {
					occ.setKingdom((String) speciesJSONs.get(occ.getScientificName())[apiCall].get("kingdom"));
				} catch (NullPointerException e) {
				}
				try {
					occ.setPhylum((String) speciesJSONs.get(occ.getScientificName())[apiCall].get("phylum"));
				} catch (NullPointerException e) {
				}
				try {
					occ.setClass((String) speciesJSONs.get(occ.getScientificName())[apiCall].get("class"));
				} catch (NullPointerException e) {
				}
				try {
					occ.setOrder((String) speciesJSONs.get(occ.getScientificName())[apiCall].get("order"));
				} catch (NullPointerException e) {
				}
				try {
					occ.setFamily((String) speciesJSONs.get(occ.getScientificName())[apiCall].get("family"));	
				} catch (NullPointerException e) {
				}

				try {
					occ.setGenus(speciesGenus);		
				} catch (NullPointerException e) {
				}

			}

		}



		///WRITER

		fileName = args[2];
		myPath = Paths.get(fileName);

		try (BufferedWriter writer = Files.newBufferedWriter(myPath, StandardCharsets.UTF_8)) {


			CustomBeanToCSVMappingStrategy<OccurenceForSpecies> mappingStrategy = new CustomBeanToCSVMappingStrategy<>();
			mappingStrategy.setType(OccurenceForSpecies.class);

			StatefulBeanToCsv<OccurenceForSpecies> beanToCsv = new StatefulBeanToCsvBuilder<OccurenceForSpecies>(writer)
					.withSeparator(CSVWriter.DEFAULT_SEPARATOR)
					.withMappingStrategy(mappingStrategy)
					.build();

			beanToCsv.write(occurences);

			writer.close();

		} catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException |
				IOException ex) {

			System.out.println("There is no output file or it is not accessible!");
			System.exit(1);	

		}

		System.out.println("Complete!");


	}

}