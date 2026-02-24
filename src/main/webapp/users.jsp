<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="DAO.*" %>
<%@ page import="Model.*" %>

<%
    UserDAO dao = new UserDAO();
    List<users> userList = dao.getAllUsers();
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Users Table</title>
</head>
<body>
    <h2>Users</h2>

    <table border="1" cellpadding="5" cellspacing="0">
        <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Email</th>
            <th>Role</th>
            <th>Status</th>
        </tr>
        <%
            for (users u : userList) {
        %>
        <tr>
            <td><%= u.getUserId() %></td>
            <td><%= u.getName() %></td>
            <td><%= u.getEmail() %></td>
            <td><%= u.getRole() %></td>
            <td><%= u.getStatus() %></td>
        </tr>
        <%
            }
        %>
    </table>

</body>
</html>

