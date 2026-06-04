package com.example.votingveranda;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class AdminController {

    // information to connect to database using mySQL workbench
    private final String DB_URL  = "jdbc:mysql://localhost:3306/voting_veranda";
    private final String DB_USER = "root";
    private final String DB_PASS = "root";
    private java.sql.Connection conn = null;

    // hard-coded user to display on admin page
    private int currentUser = 1;

    // fx:ids from .fxml file
    @FXML private Label adminName;
    @FXML private Label statusLabel;
    @FXML private TextArea adminOutput;
    @FXML private Button viewVotersBtn;
    @FXML private Button viewCandidatesBtn;
    @FXML private Button viewVoteCountsBtn;
    @FXML private Button refreshBtn;
    @FXML private Button profileBtn;
    @FXML private Button logoutBtn;

    // database connection method
    private void db_connection() {
        // if "Connecting" and "Success" show up in terminal, database has connected
        System.out.println("Connecting");
        try {
            conn = java.sql.DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            if (conn != null) {
                System.out.println("Success");
                statusLabel.setText("Database Connected");
            }
        } catch (java.sql.SQLException e) {
            System.out.println("Fail");
            System.out.println(e.getMessage());
            e.printStackTrace();
            statusLabel.setText("Connection Failed");
            adminOutput.setText("Could not connect to the database.\n\nError: " + e.getMessage());
        }
    }

    @FXML
    // method used to initialize the admin page
    public void initialize() {
        db_connection();
        loadDashboard();
    }

    // display welcome summary on the admin page
    private void loadDashboard() {
        if (conn == null) {
            adminOutput.setText("Database is not connected.");
            return;
        }

        try {
            String query = "SELECT l.first_name, l.last_name FROM admins a " +
                           "INNER JOIN login l ON a.login_id = l.login_id " +
                           "WHERE a.admin_id = " + currentUser;
            java.sql.Statement stmt = conn.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                adminName.setText(rs.getString("first_name") + " " + rs.getString("last_name"));
            } else {
                adminName.setText("Admin");
            }

        } catch (Exception e) {
            System.out.println("SQL error: " + e.getMessage());
            e.printStackTrace();
            adminName.setText("Admin");
        }

        adminOutput.setText(
            "ADMIN DASHBOARD\n" +
            "====================================\n\n" +
            "Welcome to the Voting Veranda Admin Dashboard.\n\n" +
            "Use the buttons above to:\n" +
            "- View all registered voters\n" +
            "- View all candidates\n" +
            "- View live vote counts and standings\n\n" +
            "Current System Status: Active\n"
        );
    }

    // method to view all voters from database
    @FXML
    public void viewVoters(ActionEvent event) {
        if (conn == null) { adminOutput.setText("Database is not connected."); return; }

        try {
            String query = "SELECT v.voter_id, l.first_name, l.last_name, l.l_username, " +
                           "v.ssn, v.vote_status " +
                           "FROM voter v " +
                           "INNER JOIN login l ON v.login_id = l.login_id " +
                           "ORDER BY v.voter_id";
            java.sql.Statement stmt = conn.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(query);

            StringBuilder output = new StringBuilder();
            output.append("VOTER INFORMATION\n====================================\n\n");

            int count = 0;
            while (rs.next()) {
                count++;
                output.append("Voter ID:     ").append(rs.getInt("voter_id")).append("\n");
                output.append("Name:         ").append(rs.getString("first_name")).append(" ").append(rs.getString("last_name")).append("\n");
                output.append("Username:     ").append(rs.getString("l_username")).append("\n");
                output.append("SSN:          ").append(rs.getString("ssn")).append("\n");
                output.append("Vote Status:  ").append(rs.getBoolean("vote_status") ? "Voted" : "Has Not Voted").append("\n");
                output.append("------------------------------------\n\n");
            }
            if (count == 0) output.append("No voters found.\n");

            adminOutput.setText(output.toString());

        } catch (Exception e) {
            System.out.println("SQL error: " + e.getMessage());
            e.printStackTrace();
            adminOutput.setText("Error loading voters.");
        }
    }

    // method to view all candidates from database
    @FXML
    public void viewCandidates(ActionEvent event) {
        if (conn == null) { adminOutput.setText("Database is not connected."); return; }

        try {
            String query = "SELECT c.candidate_id, l.first_name, l.last_name, l.l_username, " +
                           "c.party, p.position_name, c.campaign " +
                           "FROM candidate c " +
                           "INNER JOIN login l ON c.login_id = l.login_id " +
                           "LEFT JOIN positions p ON c.position_id = p.position_id " +
                           "ORDER BY c.candidate_id";
            java.sql.Statement stmt = conn.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(query);

            StringBuilder output = new StringBuilder();
            output.append("CANDIDATE INFORMATION\n====================================\n\n");

            int count = 0;
            while (rs.next()) {
                count++;
                String campaign = rs.getString("campaign");
                if (campaign != null && campaign.length() > 120) {
                    campaign = campaign.substring(0, 120) + "...";
                }
                output.append("Candidate ID: ").append(rs.getInt("candidate_id")).append("\n");
                output.append("Name:         ").append(rs.getString("first_name")).append(" ").append(rs.getString("last_name")).append("\n");
                output.append("Username:     ").append(rs.getString("l_username")).append("\n");
                output.append("Party:        ").append(rs.getString("party")).append("\n");
                output.append("Position:     ").append(rs.getString("position_name")).append("\n");
                output.append("Campaign:     ").append(campaign != null ? campaign : "N/A").append("\n");
                output.append("------------------------------------\n\n");
            }
            if (count == 0) output.append("No candidates found.\n");

            adminOutput.setText(output.toString());

        } catch (Exception e) {
            System.out.println("SQL error: " + e.getMessage());
            e.printStackTrace();
            adminOutput.setText("Error loading candidates.");
        }
    }

    // method to view vote counts and standings from database
    @FXML
    public void viewVoteCounts(ActionEvent event) {
        if (conn == null) { adminOutput.setText("Database is not connected."); return; }

        try {
            String query = "SELECT c.candidate_id, " +
                           "CONCAT(l.first_name, ' ', l.last_name) AS candidate_name, " +
                           "c.party, p.position_name, COUNT(v.vote_id) AS total_votes " +
                           "FROM candidate c " +
                           "INNER JOIN login l ON c.login_id = l.login_id " +
                           "LEFT JOIN positions p ON c.position_id = p.position_id " +
                           "LEFT JOIN votes v ON c.candidate_id = v.candidate_id " +
                           "GROUP BY c.candidate_id, l.first_name, l.last_name, c.party, p.position_name " +
                           "ORDER BY total_votes DESC";
            java.sql.Statement stmt = conn.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(query);

            StringBuilder output = new StringBuilder();
            output.append("VOTE COUNTS AND STANDINGS\n====================================\n\n");

            int rank = 0;
            while (rs.next()) {
                rank++;
                output.append("Rank #").append(rank).append("\n");
                output.append("Candidate ID: ").append(rs.getInt("candidate_id")).append("\n");
                output.append("Name:         ").append(rs.getString("candidate_name")).append("\n");
                output.append("Party:        ").append(rs.getString("party")).append("\n");
                output.append("Position:     ").append(rs.getString("position_name")).append("\n");
                output.append("Total Votes:  ").append(rs.getInt("total_votes")).append("\n");
                output.append("------------------------------------\n\n");
            }
            if (rank == 0) output.append("No vote data found.\n");

            adminOutput.setText(output.toString());

        } catch (Exception e) {
            System.out.println("SQL error: " + e.getMessage());
            e.printStackTrace();
            adminOutput.setText("Error loading vote counts.");
        }
    }

    // method to refresh the dashboard
    @FXML
    public void refreshDashboard(ActionEvent event) {
        loadDashboard();
        statusLabel.setText("Refreshed");
    }

    // method to view admin profile
    @FXML
    public void goToProfile(ActionEvent actionEvent) {
        String adminFullName = "Unknown";
        String adminUsername = "Unknown";
        String adminId = String.valueOf(currentUser);

        try {
            String query = "SELECT l.first_name, l.last_name, l.l_username, a.admin_id " +
                           "FROM admins a INNER JOIN login l ON a.login_id = l.login_id " +
                           "WHERE a.admin_id = " + currentUser;
            java.sql.PreparedStatement ps = conn.prepareStatement(query);
            java.sql.ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                adminFullName = rs.getString("first_name") + " " + rs.getString("last_name");
                adminUsername = rs.getString("l_username");
                adminId = String.valueOf(rs.getInt("admin_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // profile dialog pop-up window
        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Admin Profile");

        // hidden close button (only exit through X on top of pop-up)
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
        javafx.scene.Node closeBtn = dialog.getDialogPane().lookupButton(javafx.scene.control.ButtonType.CLOSE);
        if (closeBtn != null) {
            closeBtn.managedProperty().bind(closeBtn.visibleProperty());
            closeBtn.setVisible(false);
        }

        javafx.scene.control.Label nameLabel     = new javafx.scene.control.Label(adminFullName);
        javafx.scene.control.Label usernameLabel = new javafx.scene.control.Label(adminUsername);
        javafx.scene.control.Label roleLabel     = new javafx.scene.control.Label("Role: System Administrator");
        javafx.scene.control.Label idLabel       = new javafx.scene.control.Label("Admin ID: " + adminId);

        javafx.scene.control.Button logOutBtn = new javafx.scene.control.Button("Log Out");

        // log out button action
        logOutBtn.setOnAction(e -> {
            dialog.close();
            javafx.stage.Stage stage = (javafx.stage.Stage) adminOutput.getScene().getWindow();
            stage.close();
        });

        // UI for dialog, buttons, and text labels
        javafx.scene.layout.VBox profileLayout = new javafx.scene.layout.VBox(15);
        profileLayout.setPadding(new javafx.geometry.Insets(20));
        profileLayout.setPrefWidth(500);
        profileLayout.setPrefHeight(330);
        profileLayout.setAlignment(javafx.geometry.Pos.CENTER);

        nameLabel.setStyle("-fx-text-fill: #549892; -fx-font-size: 28px; -fx-font-weight: bold;");
        usernameLabel.setStyle("-fx-text-fill: #dfc07d; -fx-font-size: 14px;");
        roleLabel.setStyle("-fx-text-fill: #dfc07d; -fx-font-size: 18px;");
        idLabel.setStyle("-fx-text-fill: #dfc07d; -fx-font-size: 18px;");
        logOutBtn.setStyle("-fx-background-color: #ea4335; -fx-font-size: 18px; -fx-text-fill: white; " +
                           "-fx-font-weight: bold; -fx-min-width: 180px; -fx-min-height: 35px; -fx-cursor: hand;");

        javafx.scene.layout.VBox.setMargin(nameLabel, new javafx.geometry.Insets(0, 0, -5, 0));
        javafx.scene.layout.VBox.setMargin(idLabel, new javafx.geometry.Insets(0, 0, 15, 0));
        profileLayout.getChildren().addAll(nameLabel, usernameLabel, roleLabel, idLabel, logOutBtn);

        dialog.getDialogPane().setContent(profileLayout);
        dialog.getDialogPane().setStyle("-fx-background-color: #0E1525; -fx-font-family: 'Arial Rounded MT Bold';");
        dialog.showAndWait();
    }

    // method to log out
    @FXML
    public void logout(ActionEvent event) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Database connection closed.");
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        javafx.stage.Stage stage = (javafx.stage.Stage) logoutBtn.getScene().getWindow();
        stage.close();
    }
}
