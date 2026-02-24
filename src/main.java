import DAO.UserDAO;
import Model.users;

public class main {
    public static void main(String[] args) {
        users u = new users();
        u.setName("Test User");
        u.setEmail("test@example.com");
        u.setPassword("1234");
        u.setRole("USER");
        u.setStatus("active");

        UserDAO dao = new UserDAO();
        dao.addUser(u);

        System.out.println("User inserted, check MySQL table.");
    }
}