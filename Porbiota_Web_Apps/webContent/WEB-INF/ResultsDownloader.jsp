<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>Download Results</title>
</head>
<body>
	<a href="/Porbiota_Web_Apps">Go back</a>
	<center>
		<h1>Select file to download:</h1>
		<%-- Possible use escapeXML?--%>
		<form action="downloadResults" method="post">
			<input type="text" name="file" /><br><br> <input type="submit"
				value="Download" />
		</form>
		<p>${messages.fileName}</p>
		<p>${messages.info}</p>
		<p>${messages.info2}</p>
		<p>
			<a href="${messages.forceLink}">${messages.force}</a>
		</p>

		<p>Once the complete file is downloaded, it is deleted from the server.</p>
	</center>
	<p></p>
</body>