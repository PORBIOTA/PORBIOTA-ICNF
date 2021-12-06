<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Configuration</title>
</head>
<body>
	<a href="/Porbiota_Web_Apps">Go back</a>
	<center>
		<h1>Select the folder where to store the cache:</h1>
		<form action="metadataExtractor" method="post"
			enctype="multipart/form-data">
			<input type="file" webkitdirectory directory name="name"  /> <input type="submit" value="Upload" />
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
</body>
</html>