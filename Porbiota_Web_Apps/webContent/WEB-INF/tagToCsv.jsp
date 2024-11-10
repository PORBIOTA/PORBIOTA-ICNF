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
		<h1><small>Select tagged .txt to convert to .csv:</small></h1>
		<form action="tagToCsv" method="post"
			enctype="multipart/form-data" onsubmit="block();">
			<input type="file" name="name" />
			  
			
						
			<p><b>The tag term that initializes a list:</b></p>
			<input type="text" name="firstTerm" placeholder="term" size="30"/><br> 	
			
			<p><b>Regex to match before a first term to assure a new line:</b>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;
			<b>Number of characters to look back:</b></p>
			
			<input type="text" name="preSpecies" placeholder="regex" size="30"/>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp; 	

			<input type="text" name="preSpeciesNtoLook" placeholder="6" size="5"/><br> 

			
			
			<p><b>List of tag terms that might end a line in order from most important to least, separated by commas:</b></p>
			<input type="text" name="finishingTerms" placeholder="term1,term2,term3,...." size="100"/><br> 	
			<br>
			
			<input type="checkbox" id="separateDates" name="separateDates" value="separateDates">			
			<label for="separateDates"> Separate combined dates</label><br>
			
				 
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
	
	<p>Converts a <a href="/Porbiota_Web_Apps/txtTagger">tagged .txt</a> to a .csv</p>

	<p>The output file is in UTF-8.</p>



</body>
</html>