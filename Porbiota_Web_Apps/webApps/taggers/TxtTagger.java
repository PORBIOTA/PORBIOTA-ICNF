package taggers;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Class to tag a .txt file for species, locations, dates and given custom fields 
 * 
 * @author Tomé Neves de Matos
 *
 */

public class TxtTagger {

	/**
	 * Removes tags inside of tags in a string
	 * @param text String to clean
	 * @return String with not tags inside of tags
	 */
	
	private static String extraTagsCleaner(String text) {

		//Repeats to deal with tags inside of tags inside of tags
		int textSize = 0;
		while (textSize != text.length()) {
			textSize = text.length();
			text = text.replaceAll("(<date((?!>).)*>?((?!</date>).)*)<date((?!>).)*>?(((?!</date>).)*)</date>","$1$5");
			text = text.replaceAll("(<location((?!>).)*>?((?!</location>).)*)<location((?!>).)*>?(((?!</location>).)*)</location>","$1$5");
			text = text.replaceAll("(<taxonomicName((?!>).)*>?((?!</taxonomicName>).)*)<taxonomicName((?!>).)*>?(((?!</taxonomicName>).)*)</taxonomicName>","$1$5");
		}
		return text;
	}
	
	/**
	 * Removes all the tagged text, stores it, and replaces each tagged occurrence with XXXXXX<numberOfTag>XXXXXX
	 * @param text String To strip the tagged text from.
	 * @return List<String> A list with all the stripped tags along with the the given String stripped of tagged text as the last entry.
	 */

	private static List<String> extractTags(String text) {

		List<String> backedUpTags = new ArrayList<String>();
		int start;
		int end;
		int count = 0;

		while(text.indexOf("<taxonomicName") > -1) {
			start = text.indexOf("<taxonomicName");
			end = text.indexOf("</taxonomicName>")+"</taxonomicName>".length();
			backedUpTags.add(text.substring(start,end));
			text = text.substring(0,start)+"XXXXXX"+count+"XXXXXX"+text.substring(end);	
			count++;
		}

		while(text.indexOf("<location") > -1) {
			start = text.indexOf("<location");
			end = text.indexOf("</location>",start)+"</location>".length();
			backedUpTags.add(text.substring(start,end));
			text = text.substring(0,start)+"XXXXXX"+count+"XXXXXX"+text.substring(end);	
			count++;
		}

		while(text.indexOf("<date") > -1) {
			start = text.indexOf("<date");
			end = text.indexOf("</date>",start)+"</date>".length();
			backedUpTags.add(text.substring(start,end));
			text = text.substring(0,start)+"XXXXXX"+count+"XXXXXX"+text.substring(end);	
			count++;
		}

		backedUpTags.add(text);

		return backedUpTags;
	}
	/**
	 * Restores the tagged text taken out by the extractTags method
	 * @param backedUpMatches List<String> List of removed tagged
	 * @param text String The text to restore the tags to
	 * @return String The text with the restored tags
	 */

	private static String tagsRestorer(List<String> backedUpMatches, String text) {

		int count = 0;

		while(text.indexOf("XXXXXX"+count+"XXXXXX") > 0) {
			text = text.replace ("XXXXXX"+count+"XXXXXX",backedUpMatches.get(count));
			count++;		
		}

		return text;
	}

	public TxtTagger() {



	}

	public static void main(String[] args) throws Exception {
		TxtTagger tagger = new TxtTagger();
		String ctags = "individualCount\n"
				+ "(?<=</date>,?\\s?)\\d{1,3}(?=.*ex)\n"
				+ "measurementValue\n"
				+ "([\"”]V.M.[\"”]|[\"”]pitfall[\"”]|[\"”]R.E.[\"”])";
		tagger.tag("G:\\Porbiota\\PDFs para OCR\\OCR Testes\\Tagged_BSPEN_1995_04_30_OCR3701424691165545517.txt","G:\\Porbiota\\PDFs para OCR\\OCR Testes\\BSPEN_1995_04_30_OCR_tagged2.txt","Both",ctags,false);
	}

	/**
	 * Builds the various lucene indexes
	 * @throws IOException
	 */

	public void buildIndexes() throws IOException {
		SpeciesTagger.buildSpeciesIndex();
		PortugueseDictionary.buildPortugueseIndex();
		PTLocationTagger.buildPortugueseLocationsIndex();
		AOLocationTagger.buildAngolaLocationsIndex();
	}

	/**
	 * Tags species, locations, dates and given custom fields on the given .txt 
	 * @param file String The path of the txt
	 * @param dateType String "Roman" for dates with month in Roman numbers, "Normal" for the rest, "Both" for mixed type.
	 * @param customTagsInLine String Terms and Regexs for each of the custom fields, separated by newlines
	 * @param onlyCustom boolean true to only tag the custom fields
	 * @throws Exception
	 */

	public void tag(String file, String outputfile, String dateType, String customTagsInLine, boolean onlyCustom) throws Exception {

		String[] customTags = customTagsInLine.split("(\\r\\n|\\r|\\n)");

		System.out.println(customTagsInLine);
		//Checks that custom tags are in pairs
		if (customTags.length>0 && customTags[0].matches("Term 1")) {
			customTags = new String[0];
		} else if (customTags.length % 2 != 0) {
			if (!customTags[customTags.length-1].contentEquals("")) {
				throw new CustomTagsException();
			}
		}
		
		BufferedReader reader = Files.newBufferedReader(Paths.get(file), StandardCharsets.UTF_8 );
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputfile),StandardCharsets.UTF_8);
		writer.write("");
		writer.flush();

		String taggedText = "";
		String currentLine = "";
		while ((currentLine = reader.readLine())!=null) {
			taggedText += currentLine + "\n";
		}
		reader.close();
		
		if (!onlyCustom) {

			//Tags date according to type
			if (dateType.contentEquals("Roman")) {
				taggedText=	DateTagger.dateRomanTagger(taggedText);
			} else if (dateType.contentEquals("Normal")) {
				taggedText=	DateTagger.dateNormalTagger(taggedText);
			} else {
				taggedText=	DateTagger.dateTagger(taggedText);
			}

			taggedText = PTLocationTagger.tag(taggedText);
			taggedText = extraTagsCleaner(taggedText);
			List<String> backedUpMatches = extractTags(taggedText);
			taggedText = backedUpMatches.get(backedUpMatches.size()-1);		


			taggedText = SpeciesTagger.tag(taggedText);		
			taggedText = extraTagsCleaner(taggedText);
			taggedText = tagsRestorer(backedUpMatches, taggedText);				

		}

		//Runs through the custom tags and tags the text
		for(int i = 0; i < customTags.length; i += 2 ) {
			String field = customTags[i].trim();
			String regex = customTags[i+1].trim();
			System.out.println(field+":"+regex);
			CustomTagger cTag = new CustomTagger(field,regex);
			taggedText = cTag.tag(taggedText);
		}

		writer.write(taggedText);
		writer.close();

	}

	/**
	 * Exception to warn of improper custom tags
	 */
	public class CustomTagsException extends Exception
	{
		private static final long serialVersionUID = 9086692181595301564L;

		public CustomTagsException()
		{
			super();
		}
	}

}