<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Time Zone Extractor</title>
</head>
<body>
	<a href="/Porbiota_Web_Apps">Go back</a>
	<center>
		<h1>Select .csv to extract Time Zones from:</h1>
		<form action="timeZoneExtractor" method="post"
			enctype="multipart/form-data" onsubmit="block();">
			<input type="file" name="name" /><br><br> <input type="submit" value="Upload"/>

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

	<p>	<b>The input file can have any number of columns, but the
			Longitude column must be named "decimalLongitude" and the Latitude
			"decimalLatitude".</b></p> 
			<p><b>Optionally, it can also include "eventDate" (YYYY-MM-DD) that is used to deal with Summer Time and time zone changes over the years. If not included, the date used is 2000-01-01.</b>	</p>

	<p>Adds a column to the inputted .CSV containing the Time Zone for the coordinates.</p>
	<p>"Z" denotes UTC+0.</p>


	<p>The output file is in UTF-8.</p>


</body>
</html>