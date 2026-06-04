package com.example.votingveranda;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.sql.*;

public class AdminController {

    private final String DB_URL  = "jdbc:mysql://localhost:3306/voting_veranda";
    private final String DB_USER = "root";
    private final String DB_PASS = "root";

    private Connection conn = null;
    private int currentAdmin = 1;

    @FXML private Label adminTitle;
    @FXML private Label statusLabel;
    @FXML private TextArea adminOutput;

    @FXML private Button viewVotersBtn;
    @FXML private Button viewCandidatesBtn;
    @FXML private Button viewVoteCountsBtn;
    @FXML private Button editVoterBtn;
    @FXML private Button editCandidateBtn;
    @FXML private Button refreshBtn;
    @FXML private Button profileBtn;
    @FXML private Button logoutBtn;

    @FXML
    public void initialize() {
        dbConnection();
        loadDashboardSummary();
    }

    public void setCurrentAdmin(int adminId) {
        this.currentAdmin = adminId;
    }

    private void dbConnection() {
        System.out.println("Connecting to database...");
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            if (conn != null) {
                System.out.println("Database connected successfully.");
                statusLabel.setText("Database Connected");
            }
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            statusLabel.setText("Database Connection Failed");
            adminOutput.setText("Could not connect to the database.\n\nError: " + e.getMessage());
        }
    }

    private void loadDashboardSummary() {
        if (conn == null) { adminOutput.setText("Database is not connected."); return; }
        adminOutput.setText(
            "ADMIN DASHBOARD SUMMARY\n" +
            "====================================\n\n" +
            "Welcome to the Voting Veranda Admin Dashboard.\n\n" +
            "Admin can:\n" +
            "- View all voters\n" +
            "- View all candidates\n" +
            "- View vote counts and standings\n" +
            "- Edit voter information\n" +
            "- Edit candidate information\n" +
            "- View admin profile\n\n" +
            "Current System Status: Active\n"
        );
    }

    @FXML
    public void viewVoters(ActionEvent event) {
        if (conn == null) { adminOutput.setText("Database is not connected."); return; }
        String query =
            "SELECT v.voter_id, l.first_name, l.last_name, l.l_username, v.ssn, v.vote_status " +
            "FROM voter v INNER JOIN login l ON v.login_id = l.login_id ORDER BY v.voter_id";
        adminOutput.setText(runSelectQuery("VOTER INFORMATION", query));
    }

    @FXML
    public void viewCandidates(ActionEvent event) {
        if (conn == null) { adminOutput.setText("Database is not connected."); return; }
        String query =
            "SELECT c.candidate_id, l.first_name, l.last_name, l.l_username, " +
            "c.party, p.position_name, c.campaign " +
            "FROM candidate c " +
            "INNER JOIN login l ON c.login_id = l.login_id " +
            "LEFT JOIN positions p ON c.position_id = p.position_id " +
            "ORDER BY c.candidate_id";
        adminOutput.setText(runSelectQuery("CANDIDATE INFORMATION", query));
    }

    @FXML
    public void viewVoteCounts(ActionEvent event) {
        if (conn == null) { adminOutput.setText("Database is not connected."); return; }
        String query =
            "SELECT c.candidate_id, CONCAT(l.first_name, ' ', l.last_name) AS candidate_name, " +
            "c.party, p.position_name, COUNT(v.vote_id) AS total_votes " +
            "FROM candidate c " +
            "INNER JOIN login l ON c.login_id = l.login_id " +
            "LEFT JOIN positions p ON c.position_id = p.position_id " +
            "LEFT JOIN votes v ON c.candidate_id = v.candidate_id " +
            "GROUP BY c.candidate_id, l.first_name, l.last_name, c.party, p.position_name " +
            "ORDER BY total_votes DESC";
        adminOutput.setText(runSelectQuery("VOTE COUNTS AND STANDINGS", query));
    }

    @FXML
    public void editVoter(ActionEvent event) {
        if (conn == null) { adminOutput.setText("Database is not connected."); return; }

        TextInputDialog idDialog = new TextInputDialog();
        idDialog.setTitle("Edit Voter");
        idDialog.setHeaderText("Enter the voter ID you want to edit:");
        idDialog.setContentText("Voter ID:");

        java.util.Optional<String> idResult = idDialog.showAndWait();
        // FIX: replaced .isEmpty() with !isPresent() for Java 8 compatibility
        if (!idResult.isPresent()) return;

        int voterId;
        try {
            voterId = Integer.parseInt(idResult.get().trim());
        } catch (NumberFormatException e) {
            adminOutput.setText("Invalid voter ID. Please enter a number.");
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT v.voter_id, v.ssn, v.vote_status, v.login_id, " +
                "l.first_name, l.last_name, l.l_username " +
                "FROM voter v INNER JOIN login l ON v.login_id = l.login_id " +
                "WHERE v.voter_id = ?")) {

            ps.setInt(1, voterId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) { adminOutput.setText("No voter found with ID: " + voterId); return; }

                int loginId = rs.getInt("login_id");
                TextField firstNameField = new TextField(rs.getString("first_name"));
                TextField lastNameField  = new TextField(rs.getString("last_name"));
                TextField usernameField  = new TextField(rs.getString("l_username"));
                TextField ssnField       = new TextField(rs.getString("ssn"));
                CheckBox  voteStatusBox  = new CheckBox("Has voted");
                voteStatusBox.setSelected(rs.getBoolean("vote_status"));

                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Edit Voter");
                dialog.setHeaderText("Edit voter information:");
                ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

                GridPane grid = new GridPane();
                grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
                grid.add(new Label("First Name:"),  0, 0); grid.add(firstNameField, 1, 0);
                grid.add(new Label("Last Name:"),   0, 1); grid.add(lastNameField,  1, 1);
                grid.add(new Label("Username:"),    0, 2); grid.add(usernameField,  1, 2);
                grid.add(new Label("SSN:"),         0, 3); grid.add(ssnField,       1, 3);
                grid.add(new Label("Vote Status:"), 0, 4); grid.add(voteStatusBox,  1, 4);
                dialog.getDialogPane().setContent(grid);

                java.util.Optional<ButtonType> result = dialog.showAndWait();
                if (result.isPresent() && result.get() == saveButton) {
                    try (PreparedStatement up1 = conn.prepareStatement(
                            "UPDATE login SET first_name=?, last_name=?, l_username=? WHERE login_id=?")) {
                        up1.setString(1, firstNameField.getText());
                        up1.setString(2, lastNameField.getText());
                        up1.setString(3, usernameField.getText());
                        up1.setInt(4, loginId);
                        up1.executeUpdate();
                    }
                    try (PreparedStatement up2 = conn.prepareStatement(
                            "UPDATE voter SET ssn=?, vote_status=? WHERE voter_id=?")) {
                        up2.setString(1, ssnField.getText());
                        up2.setBoolean(2, voteStatusBox.isSelected());
                        up2.setInt(3, voterId);
                        up2.executeUpdate();
                    }
                    adminOutput.setText("Voter updated successfully.\n\n");
                    viewVoters(event);
                }
            }
        } catch (SQLException e) {
            adminOutput.setText("Error editing voter:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void editCandidate(ActionEvent event) {
        if (conn == null) { adminOutput.setText("Database is not connected."); return; }

        TextInputDialog idDialog = new TextInputDialog();
        idDialog.setTitle("Edit Candidate");
        idDialog.setHeaderText("Enter the candidate ID you want to edit:");
        idDialog.setContentText("Candidate ID:");

        java.util.Optional<String> idResult = idDialog.showAndWait();
        // FIX: replaced .isEmpty() with !isPresent() for Java 8 compatibility
        if (!idResult.isPresent()) return;

        int candidateId;
        try {
            candidateId = Integer.parseInt(idResult.get().trim());
        } catch (NumberFormatException e) {
            adminOutput.setText("Invalid candidate ID. Please enter a number.");
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT c.candidate_id, c.party, c.campaign, c.login_id, " +
                "l.first_name, l.last_name, l.l_username, p.position_name " +
                "FROM candidate c " +
                "INNER JOIN login l ON c.login_id = l.login_id " +
                "LEFT JOIN positions p ON c.position_id = p.position_id " +
                "WHERE c.candidate_id = ?")) {

            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) { adminOutput.setText("No candidate found with ID: " + candidateId); return; }

                int loginId = rs.getInt("login_id");
                TextField firstNameField = new TextField(rs.getString("first_name"));
                TextField lastNameField  = new TextField(rs.getString("last_name"));
                TextField usernameField  = new TextField(rs.getString("l_username"));
                TextField partyField     = new TextField(rs.getString("party"));

                // FIX: null check — position_name can be null due to LEFT JOIN
                String posName = rs.getString("position_name");
                TextField positionField = new TextField(posName != null ? posName : "");

                // FIX: null check — campaign can also be null
                String camp = rs.getString("campaign");
                TextArea campaignArea = new TextArea(camp != null ? camp : "");
                campaignArea.setWrapText(true);
                campaignArea.setPrefHeight(120);

                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Edit Candidate");
                dialog.setHeaderText("Edit candidate information:");
                ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

                GridPane grid = new GridPane();
                grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
                grid.add(new Label("First Name:"), 0, 0); grid.add(firstNameField, 1, 0);
                grid.add(new Label("Last Name:"),  0, 1); grid.add(lastNameField,  1, 1);
                grid.add(new Label("Username:"),   0, 2); grid.add(usernameField,  1, 2);
                grid.add(new Label("Party:"),      0, 3); grid.add(partyField,     1, 3);
                grid.add(new Label("Position:"),   0, 4); grid.add(positionField,  1, 4);
                grid.add(new Label("Campaign:"),   0, 5); grid.add(campaignArea,   1, 5);
                dialog.getDialogPane().setContent(grid);

                java.util.Optional<ButtonType> result = dialog.showAndWait();
                if (result.isPresent() && result.get() == saveButton) {
                    try (PreparedStatement ins = conn.prepareStatement(
                            "INSERT IGNORE INTO positions (position_name) VALUES (?)")) {
                        ins.setString(1, positionField.getText());
                        ins.executeUpdate();
                    }
                    try (PreparedStatement up1 = conn.prepareStatement(
                            "UPDATE login SET first_name=?, last_name=?, l_username=? WHERE login_id=?")) {
                        up1.setString(1, firstNameField.getText());
                        up1.setString(2, lastNameField.getText());
                        up1.setString(3, usernameField.getText());
                        up1.setInt(4, loginId);
                        up1.executeUpdate();
                    }
                    try (PreparedStatement up2 = conn.prepareStatement(
                            "UPDATE candidate SET party=?, campaign=?, " +
                            "position_id=(SELECT position_id FROM positions WHERE LOWER(position_name)=LOWER(?) LIMIT 1) " +
                            "WHERE candidate_id=?")) {
                        up2.setString(1, partyField.getText());
                        up2.setString(2, campaignArea.getText());
                        up2.setString(3, positionField.getText());
                        up2.setInt(4, candidateId);
                        up2.executeUpdate();
                    }
                    adminOutput.setText("Candidate updated successfully.\n\n");
                    viewCandidates(event);
                }
            }
        } catch (SQLException e) {
            adminOutput.setText("Error editing candidate:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void refreshDashboard(ActionEvent event) {
        loadDashboardSummary();
        statusLabel.setText("Dashboard Refreshed");
    }

    private String runSelectQuery(String title, String query) {
        StringBuilder output = new StringBuilder();
        output.append(title).append("\n").append("====================================\n\n");
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(query)) {
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount(), rows = 0;
            while (rs.next()) {
                rows++;
                output.append("Record ").append(rows).append("\n------------------------------------\n");
                for (int i = 1; i <= cols; i++) {
                    String val = rs.getString(i);
                    if (val == null || val.trim().isEmpty()) val = "N/A";
                    output.append(meta.getColumnLabel(i)).append(": ").append(val).append("\n");
                }
                output.append("\n");
            }
            if (rows == 0) output.append("No records found.\n");
        } catch (SQLException e) {
            output.append("SQL Error:\n").append(e.getMessage()).append("\n");
            e.printStackTrace();
        }
        return output.toString();
    }

    @FXML
    public void viewAdminProfile(ActionEvent event) {
        String adminName = "Unknown Admin", username = "Unknown";
        String adminIdText = String.valueOf(currentAdmin);

        if (conn != null) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT a.admin_id, l.first_name, l.last_name, l.l_username " +
                    "FROM admins a INNER JOIN login l ON a.login_id = l.login_id WHERE a.admin_id = ?")) {
                ps.setInt(1, currentAdmin);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        adminIdText = String.valueOf(rs.getInt("admin_id"));
                        adminName   = rs.getString("first_name") + " " + rs.getString("last_name");
                        username    = rs.getString("l_username");
                    }
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Admin Profile");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        Label nameLabel     = new Label(adminName);
        Label usernameLabel = new Label("Username: " + username);
        Label roleLabel     = new Label("Role: System Administrator");
        Label adminIdLabel  = new Label("Admin ID: " + adminIdText);
        Label systemLabel   = new Label("System: Voting Veranda");

        nameLabel.setStyle("-fx-text-fill: #549892; -fx-font-size: 28px; -fx-font-weight: bold;");
        usernameLabel.setStyle("-fx-text-fill: #dfc07d; -fx-font-size: 16px;");
        roleLabel.setStyle("-fx-text-fill: #dfc07d; -fx-font-size: 16px;");
        adminIdLabel.setStyle("-fx-text-fill: #dfc07d; -fx-font-size: 16px;");
        systemLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(25));
        layout.setPrefWidth(500); layout.setPrefHeight(330);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(nameLabel, usernameLabel, roleLabel, adminIdLabel, systemLabel);

        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().setStyle("-fx-background-color: #0E1525; -fx-font-family: 'Arial Rounded MT Bold';");
        dialog.showAndWait();
    }

    @FXML
    public void logout(ActionEvent event) {
        try {
            if (conn != null && !conn.isClosed()) { conn.close(); System.out.println("DB closed."); }
        } catch (SQLException e) { e.printStackTrace(); }
        ((javafx.stage.Stage) logoutBtn.getScene().getWindow()).close();
    }
}
