<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Taxon Tagger</title>
</head>
<body>
	<a href="/Porbiota_Web_Apps">Go back</a>
	<center>
		<h1>Select .txt to tag species:</h1>
		<form action="taxonTagger" method="post"
			enctype="multipart/form-data" onsubmit="block();">			
			<input type="file" name="name" /><br><br> 
			
			<input type="checkbox" id="enter" name="enter" value="enter">			
			<label for="enter"> Add new lines to XML tags</label><br>
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

	<p>Tags all species on a .TXT based on <a href="http://taxonfinder.org/">TaxonFinder</a>.</p>

</body>
</html>