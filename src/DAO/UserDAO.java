package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import Model.users;
import util.DBUtil;

public class UserDAO {

    public void addUser(users user) {
        String sql = "INSERT INTO users (name, email, password, role, status) VALUES (?,?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getStatus());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public users getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    users u = new users();
                    u.setUserId(rs.getInt("user_id"));
                    u.setName(rs.getString("name"));
                    u.setEmail(rs.getString("email"));
                    u.setPassword(rs.getString("password"));
                    u.setRole(rs.getString("role"));
                    u.setStatus(rs.getString("status"));
                    return u;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<users> getAllUsers() {
        List<users> result = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users u = new users();
                u.setUserId(rs.getInt("user_id"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setRole(rs.getString("role"));
                u.setStatus(rs.getString("status"));
                result.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public users getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    users u = new users();
                    u.setUserId(rs.getInt("user_id"));
                    u.setName(rs.getString("name"));
                    u.setEmail(rs.getString("email"));
                    u.setPassword(rs.getString("password"));
                    u.setRole(rs.getString("role"));
                    u.setStatus(rs.getString("status"));
                    return u;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateUser(users user) {
        String sql = "UPDATE users SET name=?, email=?, password=?, role=?, status=? WHERE user_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getStatus());
            ps.setInt(6, user.getUserId());
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deactivateUser(int userId) {
        String sql = "UPDATE users SET status=? WHERE user_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "inactive");
            ps.setInt(2, userId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}