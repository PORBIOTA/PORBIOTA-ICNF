<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Species Extractor</title>
<style>
.success {
	color: green;
}
</style>
</head>
<body>
	<a href="/Porbiota_Web_Apps">Go back</a>
	<center>
		<h1><small>Select .csv to extract species from:</small></h1>
		<form action="speciesExtractor" method="post"
			enctype="multipart/form-data" onsubmit="block();">
			<input type="file" name="name" />
			<p><b>Select the kingdom of the species:</b></p>
			<input type="radio" name="kingdom" value="animalia">
			 <label	for="animalia">Animalia</label>
			  <input type="radio" name="kingdom" value="plantae"> 
			  <label for="plantae">Plantae</label> 
				 <input	type="radio" name="kingdom" value="fungi">
				 <label	for="fungi">Fungi</label>
				 <input	type="radio" name="kingdom" value="archaea">
				 <label	for="archaea">Archaea</label> <input type="radio" name="kingdom" value="bacteria"> 
				<label for="bacteria">Bacteria</label>
				<input type="radio" name="kingdom" value="chromista"> 
				<label for="chromista">Chromista</label>
			<input type="radio" name="kingdom" value="protozoa">
			 <label	for="protozoa">Protozoa</label> 
				<input type="radio" name="kingdom" value="viruses"> 
				<label for="viruses">Viruses</label><br>
			<br> <input type="radio" name="kingdom" value="" checked="checked">
				 <label for="">More than one</label> <br>
				  
				 			  <br><p><b>New or old species' description for acceptedNameUsage:</b></p>
			<input type="radio" name="new" value="true"checked="checked">
			 <label	for="true">Use newer species' description.</label><br><br>
			  <input type="radio" name="new" value="false" > 
			  <label for="false">Keep older species' description.</label> 
			  
			  				 			 <br> <br><p><b>Get the species author from:</b></p>
			<input type="radio" name="author" value="true"checked="checked">
			 <label	for="true">GBIF</label><br><br>
			  <input type="radio" name="author" value="false" > 
			  <label for="false">The given species name</label> 
			
			<br><br><br><input type="checkbox" id="append" name="append" value="append">			
			<label for="append"> Append to original .CSV</label><br>
			
			
				 
			<br> <input type="submit" value="Upload" />
						
			   <img id="ajax-loader" src="loader.gif" style="display: none;" />

  				 <script type="text/javascript">

			   function block() {
			       // This will add a "processing" animation while the system is busy with the file and also makes a "PROCESS" button deselected 
			       document.getElementById('ajax-loader').style.display='block';
			   }
   </script>
		</form>

		<p>${messages.name}</p>

		<p>
			<a href="${messages.link}">${messages.success}</a>
		</p>
		<p>${messages.fileName}</p>
	</center>

	<p>
		<b>The input file can have any number of columns, but the Species
			column must be named &quot;scientificName&quot;.</b>
	</p>


	<p>Creates a CSV with the results obtained by cross referencing the
		species name in one CSV with those from GBIF API</p>
	<p>Aditionally it creates an occurenceID for each occurence.</p>

	<p>These fields are filled based on the original CSV species name:</p>
	<ul>
		<li>scientificName</li>
		<li>genus</li>
		<li>specificEpithet</li>
		<li>infraspecificEpithet</li>
	</ul>

	<p>All the other fields are based on GBIF data. If one so chooses (new or old description), the
		acceptedNameUsage uses the latest name of the species and not
		necessarily the one that matches the scientificName given.</p>

	<p>The output file is in UTF-8.</p>

	<p>There is no need to clean duplicates, the program does not
		repeat calls.</p>

	<p>The order and number of rows of the results is preserved, so
		simply <a href="/Porbiota_Web_Apps/tableJoiner">joining the produced table</a> to the original one is OK.</p>


</body>
</html>