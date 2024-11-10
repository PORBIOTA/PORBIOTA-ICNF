<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Location Extractor</title>
<style>
.error {
	color: red;
}

.success {
	color: green;
}
</style>
</head>
<body>
	<a href="/Porbiota_Web_Apps">Go back</a>
	<center>
		<h1>Select .csv to extract locations from:</h1>
		<form action="locationExtractor" method="post"
			enctype="multipart/form-data" onsubmit="block();">			
			<input type="file" name="name" /><br><br> 
			
			<input type="checkbox" id="append" name="append" value="append">			
			<label for="append"> Append to original .CSV</label><br>
<br>
			<input type="submit" value="Upload"/>
			
			

			   <img id="ajax-loader" src="loader.gif" style="display: none;" />

  				 <script type="text/javascript">

			   function block() {
			       // This will add a "processing" animation while the system is busy with the file and also makes a "PROCESS" button deselected 
			       document.getElementById('ajax-loader').style.display='block';
			   }
   </script>
</form>
		
		<class =error>
		<p>${messages.name}</p>
		</class>
		<p>
			<a href="${messages.link}">${messages.success}</a>
		</p>
		<p>${messages.fileName}</p>

		</span>
	</center>

	<p>
		<b>The input file can have any number of columns, but the
			Longitude column must be named "decimalLongitude" and the Latitude
			"decimalLatitude".</b>
	</p>

	<p>Creates a .CSV with the results obtained by inputing WGS84
		coordinates into the OpenStreetMaps API.</p>

	<p>The following is extracted:</p>

	<ul>
		<li>Hamlet</li>	
		<li>Village</li>
		<li>Town</li>
		<li>City District</li>
		<li>Municipality</li>
		<li>County</li>
		<li>State</li>
		<li>Archipelago</li>
		<li>Country</li>
		<li>Country Code</li>
	</ul>

	<p>Additionally, it creates the DwC columns for Portugal and Spain, based on the following set of rules: </p>

	<p>Portugal:</p>
	<ul>
	
		<li>stateProvince = county</li>
		<li>county = there is no county for Portugal
		<li>municipality = municipality, if blank, city, if blank, town</li>
		<li>locality = hamlet, if blank, village, if blank, city_district, if blank, town, if blank, city</li>
		<li>islandGroup = Archipelago</li>
	
	</ul>

	<p>Spain:</p>
	<ul>
		<li>stateProvince = state</li>
		<li>county = county or province</li>
		<li>municipality = municipality, if blank, city, if blank, town, if blank, village</li>
		<li>locality = hamlet, if blank, borough, if blank, village, if blank, town, if blank, city</li>
		<li>islandGroup = Archipelago</li>
	</ul>

	<p>The output file is in UTF-8.</p>

	<p>The order and number of rows of the results is preserved, so
		simply <a href="/Porbiota_Web_Apps/tableJoiner">joining the produced table</a> to the original one is OK.</p>

</body>
</html>