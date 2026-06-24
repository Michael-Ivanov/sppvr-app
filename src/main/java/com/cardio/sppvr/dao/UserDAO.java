package com.cardio.sppvr.dao;

import com.cardio.sppvr.model.SystemUser;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object для таблицы system_user.
 * Все операции выполняются через параметризованные запросы (PreparedStatement),
 * что исключает SQL-инъекции.
 */
public class UserDAO {

    public SystemUser findByLogin(String login) throws SQLException {
        String sql = "SELECT * FROM system_user WHERE login = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<SystemUser> findAll() throws SQLException {
        List<SystemUser> users = new ArrayList<>();
        String sql = "SELECT * FROM system_user ORDER BY user_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        }
        return users;
    }

    public boolean existsByLogin(String login) throws SQLException {
        String sql = "SELECT COUNT(*) FROM system_user WHERE login = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public void create(SystemUser user) throws SQLException {
        String sql = "INSERT INTO system_user (login, password_hash, role) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole());
            stmt.executeUpdate();
        }
    }

    public void update(SystemUser user) throws SQLException {
        String sql = "UPDATE system_user SET login = ?, role = ?, is_blocked = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getRole());
            stmt.setBoolean(3, user.isBlocked());
            stmt.setInt(4, user.getUserId());
            stmt.executeUpdate();
        }
    }

    public void updateFailedAttempts(int userId, int attempts) throws SQLException {
        String sql = "UPDATE system_user SET failed_attempts = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, attempts);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    public void updateLastLogin(int userId, LocalDateTime time) throws SQLException {
        String sql = "UPDATE system_user SET last_login = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(time));
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    public void blockUser(int userId) throws SQLException {
        String sql = "UPDATE system_user SET is_blocked = TRUE, failed_attempts = 0 WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    public void unblockUser(int userId) throws SQLException {
        String sql = "UPDATE system_user SET is_blocked = FALSE, failed_attempts = 0 WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    public void changePassword(int userId, String newHash) throws SQLException {
        String sql = "UPDATE system_user SET password_hash = ?, password_changed = TRUE WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newHash);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    private SystemUser mapRow(ResultSet rs) throws SQLException {
        Timestamp lastLoginTs  = rs.getTimestamp("last_login");
        Timestamp createdAtTs  = rs.getTimestamp("created_at");
        return new SystemUser(
                rs.getInt("user_id"),
                rs.getString("login"),
                rs.getString("password_hash"),
                rs.getString("role"),
                rs.getBoolean("is_blocked"),
                rs.getInt("failed_attempts"),
                lastLoginTs  != null ? lastLoginTs.toLocalDateTime()  : null,
                rs.getBoolean("password_changed"),
                createdAtTs  != null ? createdAtTs.toLocalDateTime()  : null
        );
    }
}
