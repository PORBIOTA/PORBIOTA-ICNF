package csvEditorFullLoad;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

public class OccurenceForLocation {
    
	/// LOCATION
	@CsvBindByName(column="Lat")
	@CsvBindByPosition(position=0)
	private String Lat;
	
	@CsvBindByName(column="Long")
	@CsvBindByPosition(position=1)
	private String Long;
	
	public String getLat() {
		return Lat;
	}
	
	public void setLat(String foo) {
		this.Lat = foo;
	}	
	
	public String getLong() {
		return Long;
	}
	
	public void setLong(String foo) {
		this.Long = foo;
	}
	
	@CsvBindByName	
	@CsvBindByPosition(position=6)
	private String county;
	
	@CsvBindByName	
	@CsvBindByPosition(position=7)
	private String state;
	
	@CsvBindByName	
	@CsvBindByPosition(position=8)
	private String country;
	
	@CsvBindByName
	@CsvBindByPosition(position=9)
	private String countryCode;
	
	@CsvBindByName
	@CsvBindByPosition(position=2)
	private String village;
	
	@CsvBindByName
	@CsvBindByPosition(position=4)
	private String town;
	
	@CsvBindByName
	@CsvBindByPosition(position=3)
	private String city_district;
	
	@CsvBindByName
	@CsvBindByPosition(position=5)
	private String municipality;
	
	
	public String getCityDistrict() {
		return city_district;
	}
	
	public void setCityDistrict(String foo) {
		this.city_district = foo;
	}
	
	
	public String getMunicipality() {
		return municipality;
	}
	
	public void setMunicipality(String foo) {
		this.municipality = foo;
	}
	
	
	public String getTown() {
		return town;
	}
	
	public void setTown(String foo) {
		this.town = foo;
	}
	
	
	public String getVillage() {
		return village;
	}
	
	public void setVillage(String foo) {
		this.village = foo;
	}
	
	
	public String getCountry_code() {
		return countryCode;
	}
	
	public void setCountry_code(String foo) {
		this.countryCode = foo;
	}
		
	public String getCounty() {
		return county;
	}
	
	public void setCounty(String foo) {
		this.county = foo;
	}
	
	public String getState() {
		return state;
	}
	
	public void setState(String foo) {
		this.state = foo;
	}
	
	public String getCountry() {
		return country;
	}
	
	public void setCountry(String foo) {
		this.country = foo;
	}
	
	@Override
	public String toString() {
		return null;
	}
}
