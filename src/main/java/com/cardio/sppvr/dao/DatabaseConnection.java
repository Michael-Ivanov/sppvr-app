package com.cardio.sppvr.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Управляет подключением к PostgreSQL.
 * Параметры: localhost:5432, БД «sppvr», пользователь «sppvr».
 * Реализует паттерн Singleton для переиспользования соединения.
 */
public class DatabaseConnection {

    private static final String URL      = "jdbc:postgresql://localhost:5432/sppvr";
    private static final String USER     = "sppvr";
    private static final String PASSWORD = "sppvr_pass";

    private static Connection connection;

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }
}
