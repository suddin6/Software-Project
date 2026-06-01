package com.example.votingveranda;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class CampaignController {
    // information to connect to database using mySQL workbench
    private final String DB_URL = "jdbc:mysql://localhost:3306/voting_veranda";
    private final String DB_USER = "root";
    private final String DB_PASS = "root";
    private java.sql.Connection conn = null;

    // hard-coded user to display on candidate page
    private int currentUser = 1;

    // fx:ids from .fxml file
    @FXML private Label campaignText;
    @FXML private Label candidateName;

    // database connection method
    private void db_connection() {
        // if "Connecting" and "Success" show up in terminal, database has connected
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
    // method used to initialize the candidate page
    public void initialize() {
        db_connection();
        loadCampaign(currentUser);
        viewStandings(currentUser);
    }

    // display the campaign on the candidate page
    private void loadCampaign(int candidateID) {
        if (conn == null) {
            campaignText.setText("Not shown yet");
            candidateName.setText("Try again later");
            return;
        }

        // using an SQL query to display the campaign and candidate name
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
