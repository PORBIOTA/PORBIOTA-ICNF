package csvEditorFullLoad;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

public class OccurenceForSpecies {
  
	//////

	@CsvBindByName (column = "scientificName")
	@CsvBindByPosition(position=0)
	private String scientificName;
	
	@CsvBindByName
	@CsvBindByPosition(position=1)
	private String acceptedNameUsage;

	@CsvBindByName
	@CsvBindByPosition(position=2)
	private String kingdom;
	
	@CsvBindByName
	@CsvBindByPosition(position=3)
	private String phylum;

	@CsvBindByName(column = "class")
	@CsvBindByPosition(position=4)
	private String class_;
	
	@CsvBindByName
	@CsvBindByPosition(position=5)
	private String order;
	
	@CsvBindByName
	@CsvBindByPosition(position=6)
	private String family;
	
	@CsvBindByName
	@CsvBindByPosition(position=7)
	private String genus;

	@CsvBindByName 
	@CsvBindByPosition(position=8)
	private String specificEpithet;
	
	@CsvBindByName 
	@CsvBindByPosition(position=9)
	private String infraspecificEpithet;
	
	@CsvBindByName 
	@CsvBindByPosition(position=10)
	private String taxonRank;	
	
	@CsvBindByName
	@CsvBindByPosition(position=11)
	private String scientificNameAuthorship;
	
	@CsvBindByName
	@CsvBindByPosition(position=12)
	private String occurenceID;
		
	@CsvBindByName
	@CsvBindByPosition(position=13)
	private long confidence;

	//Not active
	@CsvBindByName
	private String matchType;

	public String getNameAuthorship() {
		return scientificNameAuthorship;
	}
	
	public void setNameAuthorship(String foo) {
		this.scientificNameAuthorship = foo;
	}

	
	public long getConfidence() {
		return confidence;
	}
	
	public void setConfidence(long foo) {
		this.confidence = foo;
	}
	
	
	public String getAcceptedNameUsage() {
		return acceptedNameUsage;
	}
	
	public void setAcceptedNameUsage(String foo) {
		this.acceptedNameUsage = foo;
	}
	
	public String getTaxonRank() {
		return taxonRank;
	}

	public void setTaxonRank(String foo) {
		this.taxonRank = foo;
	}
	
	public String getInfraspecificEpithet() {
		return infraspecificEpithet;
	}

	public void setInfraspecificEpithet(String foo) {
		this.infraspecificEpithet = foo;
	}
	
	
	public String getSpecificEpithet() {
		return specificEpithet;
	}

	public void setSpecificEpithet(String foo) {
		this.specificEpithet = foo;
	}

	public String getKingdom() {
		return kingdom;
	}

	public void setKingdom(String foo) {
		this.kingdom = foo;
	}


	public String getPhylum() {
		return phylum;
	}

	public void setPhylum(String foo) {
		this.phylum = foo;
	}


	public String getOrder() {
		return order;
	}

	public void setOrder(String foo) {
		this.order = foo;
	}


	public String getFamily() {
		return family;
	}

	public void setFamily(String foo) {
		this.family = foo;
	}

	public String getGenus() {
		return genus;
	}

	public void setGenus(String foo) {
		this.genus = foo;
	}

	public String getClass_() {
		return class_;
	}

	public void setClass(String foo) {
		this.class_ = foo;
	}

	public String getMatchType() {
		return matchType;
	}

	public void setMatchType(String foo) {
		this.matchType = foo;
	}

	public void setScientificName(String foo) {
		this.scientificName = foo;
	}


	public String getScientificName() {
		return scientificName;
	}

	

	public String getoccurenceID() {
		return occurenceID;
	}

	public void setOccurenceID(String foo) {
		this.occurenceID = foo;
	}

	@Override
	public String toString() {
		return null;
	}
}
