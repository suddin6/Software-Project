package com.example.votingveranda;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class CampaignController {
    private final String DB_URL = "jdbc:mysql://localhost:3306/voting_veranda";
    private final String DB_USER = "root";
    private final String DB_PASS = "root";
    private java.sql.Connection conn = null;

    private int currentUser = 1;
    @FXML private Label campaignText;
    @FXML private Label candidateName;

    private void db_connection() {
        System.out.println("Connecting");

        try {
            conn = java.sql.DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
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

    public void initialize() {
        db_connection();
        loadCampaign(currentUser);
        viewStandings(currentUser);
    }

    private void loadCampaign(int candidateID) {
        if (conn == null) {
            campaignText.setText("Not shown yet");
            candidateName.setText("Try again later");
            return;
        }

        try {
            String query = "SELECT l.first_name, l.last_name, c.campaign FROM candidate c " +
                            "INNER JOIN login l ON c.login_id = l.login_id " +
                            "WHERE candidate_id = " + candidateID;
            java.sql.Statement stmt = conn.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                campaignText.setText(rs.getString("campaign"));
                candidateName.setText(rs.getString("first_name") + " " + rs.getString("last_name"));
            } else {
                campaignText.setText("Campaign Not Loading");
                candidateName.setText("Unknown Name");
            }

        } catch (Exception e) {
            System.out.println("SQL error: " + e.getMessage());
            e.printStackTrace();
            campaignText.setText("Displaying campaign for " + candidateID);
            candidateName.setText("Name Not Found");
        }
    }

    private void viewStandings(int currentUser) {
    }

    @FXML
    public void makeChanges(ActionEvent actionEvent) {
    }

    @FXML
    public void goToProfile(ActionEvent actionEvent) {
    }
}
