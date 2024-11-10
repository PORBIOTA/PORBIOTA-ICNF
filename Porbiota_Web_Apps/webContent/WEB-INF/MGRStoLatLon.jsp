<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>MRGS to Lat Long</title>
</head>
<body>
	<a href="/Porbiota_Web_Apps">Go back</a>
	<center>
		<h1>Select .csv to convert from:</h1>
		<form action="MGRStoLatLon" method="post"
			enctype="multipart/form-data" onsubmit="block();">
			<input type="file" name="name" />
			<p><b>Insert the UTM Zone:</b></p>
			<input type="text" name="zone" placeholder="Leave blank if included in the .csv" size="30"/><br> 
			<p><b>Select the original CRS of the coordinates:</b></p>
			<input type="radio" name="ED50" value="true">
			 <label	for="true">ED50</label><br><br>
			  <input type="radio" name="ED50" value="false" checked="checked"> 
			  <label for="false">WGS84</label> 
			  <br>
			   <br> 			
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
			MGRS column must be named "MGRS".</b>
	</p>

	<p>Creates a .CSV by converting MGRS coordinates to decimal Longitude and Latitude.</p>
		<p>The order and number of rows of the results is preserved, so
		simply <a href="/Porbiota_Web_Apps/tableJoiner">joining the produced table</a> to the original one is OK.</p>


</body>
</html>