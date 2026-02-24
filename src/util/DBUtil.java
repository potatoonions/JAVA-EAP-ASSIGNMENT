package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {

    private static final String URL  = "jdbc:mysql://localhost:3306/crs_db"; // your DB name
    private static final String USER = "root";          // your MySQL user
    private static final String PASS = "admin";  // your MySQL password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}