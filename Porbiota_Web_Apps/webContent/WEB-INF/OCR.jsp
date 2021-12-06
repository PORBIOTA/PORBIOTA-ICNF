<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>OCR</title>
</head>
<body>
	<a href="/Porbiota_Web_Apps">Go back</a>
	<center>
		<h1>Select any number of images or one .PDF to OCR:</h1>
		<%-- Possible use escapeXML?--%>
		<form method="post" enctype="multipart/form-data" onsubmit="block();">
			
			<input type="file" name="name" multiple><br>
			<br>

			<p>
				<b>Select the OCR mode:</b>
			</p>
		 <select name="PageMode" id="PageMode">
					<option value="1">Automatic page segmentation with OSD.</option>
				<option value="0">Orientation and script detection (OSD)
					only.</option>			
				<option value="2">Automatic page segmentation, but no OSD,
					or OCR.</option>
				<option value="3">Fully automatic page segmentation, but no
					OSD.</option>
				<option value="4">Assume a single column of text of
					variable sizes.</option>
				<option value="5">Assume a single uniform block of
					vertically aligned text.</option>
				<option value="6">Assume a single uniform block of text.</option>
				<option value="7">Treat the image as a single text line.</option>
				<option value="8">Treat the image as a single word.</option>
				<option value="9">Treat the image as a single word in a
					circle.</option>
				<option value="10">Treat the image as a single character.</option>
				<option value="11">Sparse text. Find as much text as
					possible in no particular order.</option>
				<option value="12">Sparse text with OSD.</option>
				<option value="13">Raw line. Treat the image as a single
					text line, bypassing hacks that are Tesseract-specific.</option>
			</select>
			<br>
					<p><b>Select the language:</b></p>
			<input type="radio" name="language" value="eng">
			 <label	for="eng">English</label><br><br>
			  <input type="radio" name="language" value="por" checked="checked"> 
			  <label for="por">Portuguese</label> 
			  
			  <p><b>Select the output:</b></p>
			<input type="radio" name="pdf" value="false">
			 <label	for="false">.TXT</label><br><br>
			  <input type="radio" name="pdf" value="true" checked="checked"> 
			  <label for="true">.PDF</label> 
			  
			<br> 
			<br>
			<input type="submit" value="Upload"> <img id="ajax-loader"
				src="loader.gif" style="display: none;" />

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
		<p><b>Takes as input .PDF, .JPG, .TIF .PNG files.</b></p>	
	
	<p>Creates a .TXT or .PDF by performing OCR on multiple images or one .PDF using tesseract.</p>
	<p> For preserving tables, try <a href="OCRTable">this app</a>.</p>
</body>