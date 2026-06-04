package com.example.votingveranda;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.sql.*;

public class AdminController {

    // Database connection information
    private final String DB_URL = "jdbc:mysql://localhost:3306/voting_veranda";
    private final String DB_USER = "root";
    private final String DB_PASS = "root";

    private Connection conn = null;

    // FXML IDs from admin-view.fxml
    @FXML private Label adminTitle;
    @FXML private Label statusLabel;
    @FXML private TextArea adminOutput;

    @FXML private Button viewVotersBtn;
    @FXML private Button viewCandidatesBtn;
    @FXML private Button viewVoteCountsBtn;
    @FXML private Button refreshBtn;
    @FXML private Button profileBtn;
    @FXML private Button logoutBtn;

    // Runs automatically when admin page opens
    @FXML
    public void initialize() {
        dbConnection();
        loadDashboardSummary();
    }

    // Connect to database
    private void dbConnection() {
        System.out.println("Connecting to database...");

        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

            if (conn != null) {
                System.out.println("Database connected successfully.");
                statusLabel.setText("Database Connected");
            }

        } catch (SQLException e) {
            System.out.println("Database connection failed.");
            System.out.println(e.getMessage());
            statusLabel.setText("Database Connection Failed");
            adminOutput.setText("Could not connect to the database.\n\nError: " + e.getMessage());
        }
    }

    // Shows a dashboard summary when page opens
    private void loadDashboardSummary() {
        if (conn == null) {
            adminOutput.setText("Database is not connected.");
            return;
        }

        StringBuilder output = new StringBuilder();

        output.append("ADMIN DASHBOARD SUMMARY\n");
        output.append("====================================\n\n");

        output.append("Welcome to the Voting Veranda Admin Dashboard.\n\n");
        output.append("Use the buttons above to:\n");
        output.append("- View all voters\n");
        output.append("- View all candidates\n");
        output.append("- View vote counts\n");
        output.append("- Refresh database data\n");
        output.append("- View admin profile\n\n");

        output.append("Current System Status: Active\n");

        adminOutput.setText(output.toString());
    }

    // Button: View all voters
    @FXML
    public void viewVoters(ActionEvent event) {
        if (conn == null) {
            adminOutput.setText("Database is not connected.");
            return;
        }

        String query = "SELECT * FROM voter";
        adminOutput.setText(runSelectQuery("VOTER INFORMATION", query));
    }

    // Button: View all candidates
    @FXML
    public void viewCandidates(ActionEvent event) {
        if (conn == null) {
            adminOutput.setText("Database is not connected.");
            return;
        }

        String query =
                "SELECT c.candidate_id, l.first_name, l.last_name, l.l_username, " +
                "c.party, p.position_name, c.campaign " +
                "FROM candidate c " +
                "INNER JOIN login l ON c.login_id = l.login_id " +
                "LEFT JOIN positions p ON c.position_id = p.position_id " +
                "ORDER BY c.candidate_id";

        adminOutput.setText(runSelectQuery("CANDIDATE INFORMATION", query));
    }

    // Button: View vote counts
    @FXML
    public void viewVoteCounts(ActionEvent event) {
        if (conn == null) {
            adminOutput.setText("Database is not connected.");
            return;
        }

        /*
         This query counts votes by candidate.
         It uses the vote table foreign key: votes_candidate_fk.
         If your table is named votes instead of vote, use the backup query below.
        */

        String query =
                "SELECT c.candidate_id, l.first_name, l.last_name, c.party, " +
                "p.position_name, COUNT(v.votes_candidate_fk) AS total_votes " +
                "FROM candidate c " +
                "INNER JOIN login l ON c.login_id = l.login_id " +
                "LEFT JOIN positions p ON c.position_id = p.position_id " +
                "LEFT JOIN vote v ON c.candidate_id = v.votes_candidate_fk " +
                "GROUP BY c.candidate_id, l.first_name, l.last_name, c.party, p.position_name " +
                "ORDER BY total_votes DESC";

        try {
            adminOutput.setText(runSelectQuery("VOTE COUNTS AND STANDINGS", query));
        } catch (Exception e) {
            adminOutput.setText("Vote count query failed.\n\nCheck if the table is named vote or votes.\n\nError: " + e.getMessage());
        }
    }

    // Button: Refresh
    @FXML
    public void refreshDashboard(ActionEvent event) {
        loadDashboardSummary();
        statusLabel.setText("Dashboard Refreshed");
    }

    // Reusable method to display any SELECT query nicely
    private String runSelectQuery(String title, String query) {
        StringBuilder output = new StringBuilder();

        output.append(title).append("\n");
        output.append("====================================\n\n");

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            ResultSetMetaData metaData = rs.getMetaData();

            int columnCount = metaData.getColumnCount();
            int rowCount = 0;

            while (rs.next()) {
                rowCount++;

                output.append("Record ").append(rowCount).append("\n");
                output.append("------------------------------------\n");

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    String value = rs.getString(i);

                    if (value == null || value.trim().isEmpty()) {
                        value = "N/A";
                    }

                    output.append(columnName).append(": ").append(value).append("\n");
                }

                output.append("\n");
            }

            if (rowCount == 0) {
                output.append("No records found.\n");
            }

        } catch (SQLException e) {
            output.append("SQL Error:\n");
            output.append(e.getMessage()).append("\n");
            e.printStackTrace();
        }

        return output.toString();
    }

    // Button: Admin Profile popup
    @FXML
    public void viewAdminProfile(ActionEvent event) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Admin Profile");

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        Label nameLabel = new Label("Admin User");
        Label roleLabel = new Label("Role: System Administrator");
        Label systemLabel = new Label("System: Voting Veranda");
        Label permissionLabel = new Label("Permissions: View voters, candidates, and vote standings");

        nameLabel.setStyle("-fx-text-fill: #549892; -fx-font-size: 28px; -fx-font-weight: bold;");
        roleLabel.setStyle("-fx-text-fill: #dfc07d; -fx-font-size: 16px;");
        systemLabel.setStyle("-fx-text-fill: #dfc07d; -fx-font-size: 16px;");
        permissionLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        VBox profileLayout = new VBox(15);
        profileLayout.setPadding(new Insets(25));
        profileLayout.setPrefWidth(500);
        profileLayout.setPrefHeight(300);
        profileLayout.setAlignment(Pos.CENTER);

        profileLayout.getChildren().addAll(
                nameLabel,
                roleLabel,
                systemLabel,
                permissionLabel
        );

        dialog.getDialogPane().setContent(profileLayout);
        dialog.getDialogPane().setStyle(
                "-fx-background-color: #0E1525;" +
                "-fx-font-family: 'Arial Rounded MT Bold';"
        );

        dialog.showAndWait();
    }

    // Button: Logout
    @FXML
    public void logout(ActionEvent event) {
        try {
            if (conn != null) {
                conn.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        javafx.stage.Stage stage = (javafx.stage.Stage) logoutBtn.getScene().getWindow();
        stage.close();
    }
}
