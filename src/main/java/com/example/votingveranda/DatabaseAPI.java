package com.example.votingveranda;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseAPI {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/voting_veranda";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    public static Connection db_connection() {
        try {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (SQLException e) {
            System.out.println("Connection Fail: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
