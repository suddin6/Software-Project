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

    private final String DB_URL = "jdbc:mysql://localhost:3306/voting_veranda";
    private final String DB_USER = "root";
    private final String DB_PASS = "root";

    private Connection conn = null;

    // Temporary admin ID.
    // Later, the login page can set this based on which admin logs in.
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

    // This can be used later when the login page sends the real admin ID.
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
            System.out.println("Database connection failed.");
            System.out.println(e.getMessage());
            statusLabel.setText("Database Connection Failed");
            adminOutput.setText("Could not connect to the database.\n\nError: " + e.getMessage());
        }
    }

    private void loadDashboardSummary() {
        if (conn == null) {
            adminOutput.setText("Database is not connected.");
            return;
        }

        StringBuilder output = new StringBuilder();

        output.append("ADMIN DASHBOARD SUMMARY\n");
        output.append("====================================\n\n");
        output.append("Welcome to the Voting Veranda Admin Dashboard.\n\n");
        output.append("Admin can:\n");
        output.append("- View all voters\n");
        output.append("- View all candidates\n");
        output.append("- View vote counts and standings\n");
        output.append("- Edit voter information\n");
        output.append("- Edit candidate information\n");
        output.append("- View admin profile\n\n");
        output.append("Current System Status: Active\n");

        adminOutput.setText(output.toString());
    }

    @FXML
    public void viewVoters(ActionEvent event) {
        if (conn == null) {
            adminOutput.setText("Database is not connected.");
            return;
        }

        String query =
                "SELECT v.voter_id, l.first_name, l.last_name, l.l_username, " +
                "v.ssn, v.vote_status " +
                "FROM voter v " +
                "INNER JOIN login l ON v.login_id = l.login_id " +
                "ORDER BY v.voter_id";

        adminOutput.setText(runSelectQuery("VOTER INFORMATION", query));
    }

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

    @FXML
    public void viewVoteCounts(ActionEvent event) {
        if (conn == null) {
            adminOutput.setText("Database is not connected.");
            return;
        }

        // Fixed query:
        // Database table is votes.
        // Candidate foreign key column is candidate_id.
        String query =
                "SELECT c.candidate_id, " +
                "CONCAT(l.first_name, ' ', l.last_name) AS candidate_name, " +
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
        if (conn == null) {
            adminOutput.setText("Database is not connected.");
            return;
        }

        TextInputDialog idDialog = new TextInputDialog();
        idDialog.setTitle("Edit Voter");
        idDialog.setHeaderText("Enter the voter ID you want to edit:");
        idDialog.setContentText("Voter ID:");

        java.util.Optional<String> idResult = idDialog.showAndWait();

        if (idResult.isEmpty()) {
            return;
        }

        int voterId;

        try {
            voterId = Integer.parseInt(idResult.get());
        } catch (NumberFormatException e) {
            adminOutput.setText("Invalid voter ID.");
            return;
        }

        try {
            String selectQuery =
                    "SELECT v.voter_id, v.ssn, v.vote_status, v.login_id, " +
                    "l.first_name, l.last_name, l.l_username " +
                    "FROM voter v " +
                    "INNER JOIN login l ON v.login_id = l.login_id " +
                    "WHERE v.voter_id = ?";

            PreparedStatement ps = conn.prepareStatement(selectQuery);
            ps.setInt(1, voterId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                adminOutput.setText("No voter found with ID: " + voterId);
                return;
            }

            int loginId = rs.getInt("login_id");

            TextField firstNameField = new TextField(rs.getString("first_name"));
            TextField lastNameField = new TextField(rs.getString("last_name"));
            TextField usernameField = new TextField(rs.getString("l_username"));
            TextField ssnField = new TextField(rs.getString("ssn"));

            CheckBox voteStatusBox = new CheckBox("Has voted");
            voteStatusBox.setSelected(rs.getBoolean("vote_status"));

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edit Voter");
            dialog.setHeaderText("Edit voter information:");

            ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));

            grid.add(new Label("First Name:"), 0, 0);
            grid.add(firstNameField, 1, 0);

            grid.add(new Label("Last Name:"), 0, 1);
            grid.add(lastNameField, 1, 1);

            grid.add(new Label("Username:"), 0, 2);
            grid.add(usernameField, 1, 2);

            grid.add(new Label("SSN:"), 0, 3);
            grid.add(ssnField, 1, 3);

            grid.add(new Label("Vote Status:"), 0, 4);
            grid.add(voteStatusBox, 1, 4);

            dialog.getDialogPane().setContent(grid);

            java.util.Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == saveButton) {
                String updateLogin =
                        "UPDATE login SET first_name = ?, last_name = ?, l_username = ? WHERE login_id = ?";

                PreparedStatement updateLoginPS = conn.prepareStatement(updateLogin);
                updateLoginPS.setString(1, firstNameField.getText());
                updateLoginPS.setString(2, lastNameField.getText());
                updateLoginPS.setString(3, usernameField.getText());
                updateLoginPS.setInt(4, loginId);
                updateLoginPS.executeUpdate();

                String updateVoter =
                        "UPDATE voter SET ssn = ?, vote_status = ? WHERE voter_id = ?";

                PreparedStatement updateVoterPS = conn.prepareStatement(updateVoter);
                updateVoterPS.setString(1, ssnField.getText());
                updateVoterPS.setBoolean(2, voteStatusBox.isSelected());
                updateVoterPS.setInt(3, voterId);
                updateVoterPS.executeUpdate();

                adminOutput.setText("Voter updated successfully.\n\n");
                viewVoters(event);
            }

        } catch (SQLException e) {
            adminOutput.setText("Error editing voter:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void editCandidate(ActionEvent event) {
        if (conn == null) {
            adminOutput.setText("Database is not connected.");
            return;
        }

        TextInputDialog idDialog = new TextInputDialog();
        idDialog.setTitle("Edit Candidate");
        idDialog.setHeaderText("Enter the candidate ID you want to edit:");
        idDialog.setContentText("Candidate ID:");

        java.util.Optional<String> idResult = idDialog.showAndWait();

        if (idResult.isEmpty()) {
            return;
        }

        int candidateId;

        try {
            candidateId = Integer.parseInt(idResult.get());
        } catch (NumberFormatException e) {
            adminOutput.setText("Invalid candidate ID.");
            return;
        }

        try {
            String selectQuery =
                    "SELECT c.candidate_id, c.party, c.campaign, c.login_id, " +
                    "l.first_name, l.last_name, l.l_username, p.position_name " +
                    "FROM candidate c " +
                    "INNER JOIN login l ON c.login_id = l.login_id " +
                    "LEFT JOIN positions p ON c.position_id = p.position_id " +
                    "WHERE c.candidate_id = ?";

            PreparedStatement ps = conn.prepareStatement(selectQuery);
            ps.setInt(1, candidateId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                adminOutput.setText("No candidate found with ID: " + candidateId);
                return;
            }

            int loginId = rs.getInt("login_id");

            TextField firstNameField = new TextField(rs.getString("first_name"));
            TextField lastNameField = new TextField(rs.getString("last_name"));
            TextField usernameField = new TextField(rs.getString("l_username"));
            TextField partyField = new TextField(rs.getString("party"));
            TextField positionField = new TextField(rs.getString("position_name"));

            TextArea campaignArea = new TextArea(rs.getString("campaign"));
            campaignArea.setWrapText(true);
            campaignArea.setPrefHeight(120);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edit Candidate");
            dialog.setHeaderText("Edit candidate information:");

            ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));

            grid.add(new Label("First Name:"), 0, 0);
            grid.add(firstNameField, 1, 0);

            grid.add(new Label("Last Name:"), 0, 1);
            grid.add(lastNameField, 1, 1);

            grid.add(new Label("Username:"), 0, 2);
            grid.add(usernameField, 1, 2);

            grid.add(new Label("Party:"), 0, 3);
            grid.add(partyField, 1, 3);

            grid.add(new Label("Position:"), 0, 4);
            grid.add(positionField, 1, 4);

            grid.add(new Label("Campaign:"), 0, 5);
            grid.add(campaignArea, 1, 5);

            dialog.getDialogPane().setContent(grid);

            java.util.Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == saveButton) {
                String insertPosition =
                        "INSERT IGNORE INTO positions (position_name) VALUES (?)";

                PreparedStatement insertPositionPS = conn.prepareStatement(insertPosition);
                insertPositionPS.setString(1, positionField.getText());
                insertPositionPS.executeUpdate();

                String updateLogin =
                        "UPDATE login SET first_name = ?, last_name = ?, l_username = ? WHERE login_id = ?";

                PreparedStatement updateLoginPS = conn.prepareStatement(updateLogin);
                updateLoginPS.setString(1, firstNameField.getText());
                updateLoginPS.setString(2, lastNameField.getText());
                updateLoginPS.setString(3, usernameField.getText());
                updateLoginPS.setInt(4, loginId);
                updateLoginPS.executeUpdate();

                String updateCandidate =
                        "UPDATE candidate SET party = ?, campaign = ?, " +
                        "position_id = (SELECT position_id FROM positions WHERE LOWER(position_name) = LOWER(?) LIMIT 1) " +
                        "WHERE candidate_id = ?";

                PreparedStatement updateCandidatePS = conn.prepareStatement(updateCandidate);
                updateCandidatePS.setString(1, partyField.getText());
                updateCandidatePS.setString(2, campaignArea.getText());
                updateCandidatePS.setString(3, positionField.getText());
                updateCandidatePS.setInt(4, candidateId);
                updateCandidatePS.executeUpdate();

                adminOutput.setText("Candidate updated successfully.\n\n");
                viewCandidates(event);
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

    @FXML
    public void viewAdminProfile(ActionEvent event) {
        String adminName = "Unknown Admin";
        String username = "Unknown";
        String adminIdText = String.valueOf(currentAdmin);

        if (conn != null) {
            try {
                String query =
                        "SELECT a.admin_id, l.first_name, l.last_name, l.l_username " +
                        "FROM admins a " +
                        "INNER JOIN login l ON a.login_id = l.login_id " +
                        "WHERE a.admin_id = ?";

                PreparedStatement ps = conn.prepareStatement(query);
                ps.setInt(1, currentAdmin);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    adminIdText = String.valueOf(rs.getInt("admin_id"));
                    adminName = rs.getString("first_name") + " " + rs.getString("last_name");
                    username = rs.getString("l_username");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Admin Profile");

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        Label nameLabel = new Label(adminName);
        Label usernameLabel = new Label("Username: " + username);
        Label roleLabel = new Label("Role: System Administrator");
        Label adminIdLabel = new Label("Admin ID: " + adminIdText);
        Label systemLabel = new Label("System: Voting Veranda");

        nameLabel.setStyle("-fx-text-fill: #549892; -fx-font-size: 28px; -fx-font-weight: bold;");
        usernameLabel.setStyle("-fx-text-fill: #dfc07d; -fx-font-size: 16px;");
        roleLabel.setStyle("-fx-text-fill: #dfc07d; -fx-font-size: 16px;");
        adminIdLabel.setStyle("-fx-text-fill: #dfc07d; -fx-font-size: 16px;");
        systemLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        VBox profileLayout = new VBox(15);
        profileLayout.setPadding(new Insets(25));
        profileLayout.setPrefWidth(500);
        profileLayout.setPrefHeight(330);
        profileLayout.setAlignment(Pos.CENTER);

        profileLayout.getChildren().addAll(
                nameLabel,
                usernameLabel,
                roleLabel,
                adminIdLabel,
                systemLabel
        );

        dialog.getDialogPane().setContent(profileLayout);
        dialog.getDialogPane().setStyle(
                "-fx-background-color: #0E1525;" +
                "-fx-font-family: 'Arial Rounded MT Bold';"
        );

        dialog.showAndWait();
    }

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
