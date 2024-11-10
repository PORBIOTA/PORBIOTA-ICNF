<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Initial Column Adder</title>
</head>
<body>
	<a href="/Porbiota_Web_Apps">Go back</a>
	<center>
	<!--  &emsp; spacing character -->
		<h1>Select .csv to add columns to:</h1>
		<form action="columnAdder" method="post"
			enctype="multipart/form-data"  onsubmit="block();">
			<input type="file" name="name" />
			
			<br>			
			<p><b>Insert the collection code:
			&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;
			Insert the dataset name:</b></p>
			<input type="text" name="collectionCode" size="30"/>
			&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp; 
			<input type="text" name="datasetName" size="30"/><br>  		
		
			<br>			
			<p><b>Insert the collection type:
			&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;
			Insert the basis of record:</b></p>
			<input type="text" name="type" placeholder="Leave blank if event" size="30"/>
			&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;		
			<input type="text" name="basisOfRecord" placeholder="Leave blank if human observation" size="30"/><br>  	

			<br>
			<p><b>Insert the institution Code:
			&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;
			Insert the instituion ID:</b></p>
			<input type="text" name="institutionCode" placeholder="Leave blank for ICNF" size="30"/>
			&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;
			<input type="text" name="institutionID" placeholder="Leave blank for ICNF" size="30"/><br>
			
			<br>
			<p><b>Insert the last modified date:</b></p>
			<input type="text" name="modified" placeholder="YYYY-MM-DD" size="30"/>
			
			<br><br>
			
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

	<p>Creates a .CSV by adding the following columns to it:</p>
		<ul>
		<li>collectionCode</li>	
		<li>type</li>
		<li>modified</li>
		<li>license - Always "https://creativecommons.org/licenses/by/4.0/legalcode" </li>
		<li>institutionID</li>
		<li>institutionCode</li>
		<li>datasetName</li>
		<li>basisOfRecord</li>
		<li>occurrenceStatus  - Always "Present"</li>
	</ul>
</body>
</html>