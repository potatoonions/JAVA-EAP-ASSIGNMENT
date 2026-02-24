package Controller;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import DAO.UserDAO;
import Model.users;

@WebServlet("/api/users")
public class UsersApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
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
        
        response.getWriter().write(json.toString());
    }
}
