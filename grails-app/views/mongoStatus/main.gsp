<%--
  Created by IntelliJ IDEA.
  User: Owner2
  Date: Mar 12, 2010
  Time: 2:45:12 PM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head><title>MongoDB Status</title></head>
<body>

<div>
	<h3>Servers</h3>
	<table>
	<tr>
		<th style='width:120px'>ID</th>
		<th style='width:120px'>Host</th>
		<th style='width:120px'>Status</th>
		<th >DBs</th>
	</tr>
	<g:each in="${servers}">
		<tr>
			<td>${it.id}</td>
			<td>${it.config.host}</td>
			<td>${it.status.ok}</td>
			<td><g:each in="${it.dbs.databases}">
				<div>${it.name}</div>
			</g:each></td>
		</tr>
	</g:each>
	</table>
</div>

</body>
</html>