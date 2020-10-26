<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Schedule Form</title>
</head>
<body>
	<h1>Auto Scheduling and Manual Scheduling</h1>


	<a href="/load">AutoScheduling</a>
	<br>
	<br>


	<form action="/load/manualmode" method="post">

		<input type="datetime-local" name="selecteddate"
			value="2020-10-26 16:25:00" step="1"></br> </br>

		<c:forEach items="${ListOfFiles}" var="list">
			<input type="checkbox" name="names" value="${list}">
			</br>
			<c:out value="${list}"></c:out>
			</br>
		</c:forEach>

		<input type="submit" value="Submit">

	</form>
</body>
</html>