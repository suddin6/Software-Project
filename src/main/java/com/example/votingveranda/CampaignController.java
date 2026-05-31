package com.example.votingveranda;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class CampaignController {
    private void db_connection() {
        final String DB_URL = "jdbc:mysql://localhost:3306/voting_veranda";
        final String DB_USER = "root";
        final String DB_PASS = "root";

        System.out.println("Connecting");

        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            if (conn != null) {
                System.out.println("Success");
            }
        } catch (java.sql.SQLException e) {
            System.out.println("Fail");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private Label welcomeText;

    @FXML

    public void initialize() {
        db_connection();
    }

    public void makeChanges(ActionEvent actionEvent) {

    }

    public void goToProfile(ActionEvent actionEvent) {
    }
}
