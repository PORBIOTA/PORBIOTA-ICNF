<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>Table Joiner</title>
</head>
<body>
	<a href="/Porbiota_Web_Apps">Go back</a>
	<center>
		<h1>Select any number of .csvs to join:</h1>
		<%-- Possible use escapeXML?--%>
        <form method="post" enctype="multipart/form-data" onsubmit="block();">
            <input type="file" name="name" multiple><br><br>
            <input type="submit" value="Upload">
            			
			   <img id="ajax-loader" src="loader.gif" style="display: none;" />

  				 <script type="text/javascript">

			   function block() {
			       // This will add a "processing" animation while the system is busy with the file and also makes a "PROCESS" button deselected 
			       document.getElementById('ajax-loader').style.display='block';
			   }
   </script>
        </form>
		<p>${messages.name}</p>
		</class>
		<p>
			<a href="${messages.link}">${messages.success}</a>
		</p>
		<p>${messages.fileName}</p>
	</center>
<br>
		<p>Creates a UTF-8 CSV by merging two or more CSVs together. They need to have the same number of lines.</p>
	<p></p>
</body>