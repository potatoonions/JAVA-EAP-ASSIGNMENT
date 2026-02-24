<%@ page language="java" contentType="application/json; charset=UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="DAO.*" %>
<%@ page import="Model.*" %>
<%
    UserDAO dao = new UserDAO();
    List<users> userList = dao.getAllUsers();
    
    StringBuilder json = new StringBuilder("[");
    for (int i = 0; i < userList.size(); i++) {
        users u = userList.get(i);
        json.append("{");
        json.append("\"userId\":").append(u.getUserId()).append(",");
        json.append("\"name\":\"").append(u.getName()).append("\",");
        json.append("\"email\":\"").append(u.getEmail()).append("\",");
        json.append("\"role\":\"").append(u.getRole()).append("\",");
        json.append("\"status\":\"").append(u.getStatus()).append("\"");
        json.append("}");
        if (i < userList.size() - 1) {
            json.append(",");
        }
    }
    json.append("]");
    
    out.print(json.toString());
%>
