<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Metadata Extractor</title>
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
		<h1>Select .csv to extract metadata from:</h1>
		<form action="metadataExtractor" method="post"
			enctype="multipart/form-data" onsubmit="block();">
			<input type="file" name="name" /><br><br> <input type="submit" value="Upload" />
						
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

	<p>Creates a .txt with the metadata info extracted from a .csv
		darwinCore-like file.</p>

	<p>The following is extracted:</p>

	<table>
		<tr>
			<th>Information</th>
			<th>Header names must match those given here</th>
		</tr>
		<tr>
			<td><b>Number of rows</b></td>
			<td>[+1 if you wish to include the header]</td>
		</tr>
		<tr>
			<td><b>Number of species and names</b></td>
			<td>Based on "acceptedNameUsage"</td>
		</tr>
		<tr>
			<td><b>Number of genus and names</b></td>
			<td>Based on "genus"</td>
		</tr>
		<tr>
			<td><b>Number of families and names</b></td>
			<td>Based on "family"</td>
		</tr>
		<tr>
			<td><b>Number of orders and names</b></td>
			<td>Based on "order"</td>
		</tr>
		<tr>
			<td><b>Number of classes and names</b></td>
			<td>Based on "class"</td>
		</tr>
		<tr>
			<td><b>Number of phylums and names</b></td>
			<td>Based on "phylum"</td>
		</tr>
		<tr>
			<td><b>Number of kingdoms and names</b></td>
			<td>Based on "kingdom"</td>
		</tr>
		<tr>
			<td><b>Number of countries and names</b></td>
			<td>Based on "country"</td>
		</tr>
		<tr>
			<td><b>Bounding Box (Latitude and Longitude)</b></td>
			<td>Based on "decimalLatitude" and "decimalLongitude"</td>
		</tr>
		<tr>
			<td><b>Oldest and Newest Records</b></td>
			<td>Based on "eventDate" (The date must start with the year, for
				example "yyyy-MM-dd")</td>
		</tr>
	</table>



</body>
</html>