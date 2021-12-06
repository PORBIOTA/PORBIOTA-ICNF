<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Txt Tagger</title>
</head>
<body>
	<a href="/Porbiota_Web_Apps">Go back</a>
	<center>
		<h1><small>Select .txt to tag:</small></h1>
		<form action="txtTagger" method="post"
			enctype="multipart/form-data" onsubmit="block();">
			<input type="file" name="name" />
			<p><b>Select the type of date:</b></p>
			<input type="radio" name="dateType" value="Roman">
			 <label	for="Roman">Roman</label>
			  <input type="radio" name="dateType" value="Normal"> 
			  <label for="Normal">Normal</label> 
				 <input	type="radio" name="dateType" value="Both" checked="checked">
				 <label	for="Both">Both</label>				  
			
			<br><br><br>
						 <textarea name="customTags" style="width: 516px; height: 209px; ">Term 1&#13;&#10;Regex 1&#13;&#10;Term 2&#13;&#10;Regex 2&#13;&#10;...</textarea> 
			<br>
			<input type="checkbox" id="onlyCustom" name="onlyCustom" value="onlyCustom">			
			<label for="onlyCustom"> Only apply custom tags</label><br>
			
				 
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

	<p>Outputs the given .txt with the species names, locations and dates tagged.</p>
	
	<p> The species are tagged according to the <a href="https://www.gbif.org/dataset/d7dddbf4-2cf0-4f39-9b2a-bb099caae36c">GBIF backbone</a>.</p>
	<p> The locations are tagged according to <a href="http://download.geonames.org/export/dump/">GeoNames</a>.</p>
	
	<p>Additionally it also tags the custom tags given.</p>

	<p>The custom tags must be provided by alternating the term name with the regex to match it.</p>
	
	<p>It can then be converted to a .csv <a href="/Porbiota_Web_Apps/tagToCsv">here</a>.
	
	<p>To edit the tags you can use the following <a href="/Porbiota_Web_Apps/downloadResults?file=Tag Macros.docm">Microsoft Word file</a></p>
	
	<p>The Word file has the following shortcuts:</p>
	<ul>
		<li><b>Ctrl+Alt+L</b> - Tags a selected location</li>	
		<li><b>Ctrl+Alt+S or T</b> - Tags a selected species</li>
		<li><b>Ctrl+Alt+H</b> - Tags a selected associatedTaxa as host</li>
		<li><b>Ctrl+Alt+C</b> - Cleans tags in selection</li>		
		<li><b>Ctrl+Alt+P</b> - Paints all tags in the document </li>

	</ul>
	<p>For all of the shortcuts you can use Alt Gr instead of Ctrl+Alt
	<p>The output file is in UTF-8.</p>


</body>
</html>