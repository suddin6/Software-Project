package com.example.votingveranda;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class AdminController {
    private java.sql.Connection conn = null;

    // hard-coded user to display on admin page
    private int currentUser;

    // fx:ids from .fxml file
    @FXML private Label adminName;
    @FXML private TextArea adminOutput;
    @FXML private Button logoutBtn;

    public void setCurrentUser(int loginID) {
        this.currentUser = loginID;
        loadDashboard();
    }

    @FXML
    // method used to initialize the admin page
    public void initialize() {
        this.conn = DatabaseAPI.db_connection();
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
                           "WHERE l.login_id = " + currentUser;
            java.sql.Statement stmt = conn.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                adminName.setText(rs.getString("first_name") + " " + rs.getString("last_name"));
            } else {
                adminName.setText("Admin");
            }

        } catch (Exception e) {
            System.out.println("SQL Error: " + e.getMessage());
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
            "- View live vote counts and standings\n" +
            "- Edit voter information\n" +
            "- Edit candidate information\n\n" +
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
            output.append("𝐕𝐎𝐓𝐄𝐑 𝐈𝐍𝐅𝐎𝐑𝐌𝐀𝐓𝐈𝐎𝐍\n====================================\n\n");

            int count = 0;
            while (rs.next()) {
                count++;
                // FIX: spacing aligned to 14 visual chars per label
                output.append("𝐕𝐨𝐭𝐞𝐫 𝐈𝐃: ").append(rs.getInt("voter_id")).append("\n");
                output.append("𝐍𝐚𝐦𝐞: ").append(rs.getString("first_name")).append(" ").append(rs.getString("last_name")).append("\n");
                output.append("𝐔𝐬𝐞𝐫𝐧𝐚𝐦𝐞: ").append(rs.getString("l_username")).append("\n");
                output.append("𝐒𝐒𝐍: ").append(rs.getString("ssn")).append("\n");
                output.append("𝐕𝐨𝐭𝐞 𝐒𝐭𝐚𝐭𝐮𝐬: ").append(rs.getBoolean("vote_status") ? "Voted" : "Has Not Voted").append("\n");
                output.append("\n------------------------------------\n\n");
            }
            if (count == 0) output.append("No voters found.\n");

            adminOutput.setText(output.toString());

        } catch (Exception e) {
            System.out.println("SQL Error: " + e.getMessage());
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
            output.append("𝐂𝐀𝐍𝐃𝐈𝐃𝐀𝐓𝐄 𝐈𝐍𝐅𝐎𝐑𝐌𝐀𝐓𝐈𝐎𝐍\n====================================\n\n");

            int count = 0;
            while (rs.next()) {
                count++;
                String campaign = rs.getString("campaign");
                // FIX: spacing aligned to 15 visual chars per label
                output.append("𝐂𝐚𝐧𝐝𝐢𝐝𝐚𝐭𝐞 𝐈𝐃: ").append(rs.getInt("candidate_id")).append("\n");
                output.append("𝐍𝐚𝐦𝐞: ").append(rs.getString("first_name")).append(" ").append(rs.getString("last_name")).append("\n");
                output.append("𝐔𝐬𝐞𝐫𝐧𝐚𝐦𝐞: ").append(rs.getString("l_username")).append("\n");
                output.append("𝐏𝐚𝐫𝐭𝐲: ").append(rs.getString("party")).append("\n");
                output.append("𝐏𝐨𝐬𝐢𝐭𝐢𝐨𝐧: ").append(rs.getString("position_name")).append("\n");
                output.append("𝐂𝐚𝐦𝐩𝐚𝐢𝐠𝐧: ").append(campaign != null ? campaign : "N/A").append("\n");
                output.append("\n------------------------------------\n\n");
            }
            if (count == 0) output.append("No candidates found.\n");

            adminOutput.setText(output.toString());

        } catch (Exception e) {
            System.out.println("SQL Error: " + e.getMessage());
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
            output.append("𝐕𝐎𝐓𝐄 𝐂𝐎𝐔𝐍𝐓𝐒 𝐀𝐍𝐃 𝐒𝐓𝐀𝐍𝐃𝐈𝐍𝐆𝐒\n====================================\n\n");

            int rank = 0;
            while (rs.next()) {
                rank++;
                // FIX: spacing aligned to 15 visual chars per label
                output.append("𝐑𝐚𝐧𝐤 #").append(rank).append("\n");
                output.append("𝐂𝐚𝐧𝐝𝐢𝐝𝐚𝐭𝐞 𝐈𝐃: ").append(rs.getInt("candidate_id")).append("\n");
                output.append("𝐍𝐚𝐦𝐞: ").append(rs.getString("candidate_name")).append("\n");
                output.append("𝐏𝐚𝐫𝐭𝐲: ").append(rs.getString("party")).append("\n");
                output.append("𝐏𝐨𝐬𝐢𝐭𝐢𝐨𝐧: ").append(rs.getString("position_name")).append("\n");
                output.append("𝐓𝐨𝐭𝐚𝐥 𝐕𝐨𝐭𝐞𝐬: ").append(rs.getInt("total_votes")).append("\n");
                output.append("\n------------------------------------\n\n");
            }
            if (rank == 0) output.append("No vote data found.\n");

            adminOutput.setText(output.toString());

        } catch (Exception e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            adminOutput.setText("Error loading vote counts.");
        }
    }

    // method to edit a voter's information
    @FXML
    public void editVoter(ActionEvent event) {
        if (conn == null) { adminOutput.setText("Database is not connected."); return; }

        javafx.scene.control.TextInputDialog idDialog = new javafx.scene.control.TextInputDialog();
        idDialog.setTitle("Edit Voter");
        idDialog.setHeaderText("Enter the voter ID you want to edit:");
        idDialog.setContentText("Voter ID:");

        java.util.Optional<String> idResult = idDialog.showAndWait();
        if (!idResult.isPresent()) return;

        int voterId;
        try {
            voterId = Integer.parseInt(idResult.get().trim());
        } catch (NumberFormatException e) {
            adminOutput.setText("Invalid voter ID. Please enter a number.");
            return;
        }

        try {
            String selectQuery = "SELECT v.voter_id, v.ssn, v.vote_status, v.login_id, " +
                                 "l.first_name, l.last_name, l.l_username " +
                                 "FROM voter v INNER JOIN login l ON v.login_id = l.login_id " +
                                 "WHERE v.voter_id = " + voterId;
            java.sql.Statement stmt = conn.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(selectQuery);

            if (!rs.next()) {
                adminOutput.setText("No voter found with ID: " + voterId);
                return;
            }

            int loginId = rs.getInt("login_id");

            javafx.scene.control.TextField firstNameField = new javafx.scene.control.TextField(rs.getString("first_name"));
            javafx.scene.control.TextField lastNameField  = new javafx.scene.control.TextField(rs.getString("last_name"));
            javafx.scene.control.TextField usernameField  = new javafx.scene.control.TextField(rs.getString("l_username"));
            javafx.scene.control.TextField ssnField       = new javafx.scene.control.TextField(rs.getString("ssn"));
            javafx.scene.control.CheckBox  voteStatusBox  = new javafx.scene.control.CheckBox("Has voted");
            voteStatusBox.setSelected(rs.getBoolean("vote_status"));

            javafx.scene.control.Dialog<javafx.scene.control.ButtonType> dialog = new javafx.scene.control.Dialog<>();
            dialog.setTitle("Edit Voter");
            dialog.setHeaderText("Edit voter information:");
            javafx.scene.control.ButtonType saveButton = new javafx.scene.control.ButtonType("Save", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButton, javafx.scene.control.ButtonType.CANCEL);

            javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
            grid.setHgap(10); grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20));
            grid.add(new javafx.scene.control.Label("First Name:"),  0, 0); grid.add(firstNameField, 1, 0);
            grid.add(new javafx.scene.control.Label("Last Name:"),   0, 1); grid.add(lastNameField,  1, 1);
            grid.add(new javafx.scene.control.Label("Username:"),    0, 2); grid.add(usernameField,  1, 2);
            grid.add(new javafx.scene.control.Label("SSN:"),         0, 3); grid.add(ssnField,       1, 3);
            grid.add(new javafx.scene.control.Label("Vote Status:"), 0, 4); grid.add(voteStatusBox,  1, 4);
            dialog.getDialogPane().setContent(grid);

            java.util.Optional<javafx.scene.control.ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == saveButton) {
                String updateLogin = "UPDATE login SET first_name = ?, last_name = ?, l_username = ? WHERE login_id = ?";
                java.sql.PreparedStatement ps1 = conn.prepareStatement(updateLogin);
                ps1.setString(1, firstNameField.getText());
                ps1.setString(2, lastNameField.getText());
                ps1.setString(3, usernameField.getText());
                ps1.setInt(4, loginId);
                ps1.executeUpdate();

                String updateVoter = "UPDATE voter SET ssn = ?, vote_status = ? WHERE voter_id = ?";
                java.sql.PreparedStatement ps2 = conn.prepareStatement(updateVoter);
                ps2.setString(1, ssnField.getText());
                ps2.setBoolean(2, voteStatusBox.isSelected());
                ps2.setInt(3, voterId);
                ps2.executeUpdate();

                adminOutput.setText("Voter updated successfully.\n\n");
                viewVoters(event);
            }

        } catch (Exception e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            adminOutput.setText("Error editing voter:\n" + e.getMessage());
        }
    }

    // method to edit a candidate's information
    @FXML
    public void editCandidate(ActionEvent event) {
        if (conn == null) { adminOutput.setText("Database is not connected."); return; }

        javafx.scene.control.TextInputDialog idDialog = new javafx.scene.control.TextInputDialog();
        idDialog.setTitle("Edit Candidate");
        idDialog.setHeaderText("Enter the candidate ID you want to edit:");
        idDialog.setContentText("Candidate ID:");

        java.util.Optional<String> idResult = idDialog.showAndWait();
        if (!idResult.isPresent()) return;

        int candidateId;
        try {
            candidateId = Integer.parseInt(idResult.get().trim());
        } catch (NumberFormatException e) {
            adminOutput.setText("Invalid candidate ID. Please enter a number.");
            return;
        }

        try {
            String selectQuery = "SELECT c.candidate_id, c.party, c.campaign, c.login_id, " +
                                 "l.first_name, l.last_name, l.l_username, p.position_name " +
                                 "FROM candidate c " +
                                 "INNER JOIN login l ON c.login_id = l.login_id " +
                                 "LEFT JOIN positions p ON c.position_id = p.position_id " +
                                 "WHERE c.candidate_id = " + candidateId;
            java.sql.Statement stmt = conn.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(selectQuery);

            if (!rs.next()) {
                adminOutput.setText("No candidate found with ID: " + candidateId);
                return;
            }

            int loginId = rs.getInt("login_id");

            javafx.scene.control.TextField firstNameField = new javafx.scene.control.TextField(rs.getString("first_name"));
            javafx.scene.control.TextField lastNameField  = new javafx.scene.control.TextField(rs.getString("last_name"));
            javafx.scene.control.TextField usernameField  = new javafx.scene.control.TextField(rs.getString("l_username"));
            javafx.scene.control.TextField partyField     = new javafx.scene.control.TextField(rs.getString("party"));

            String posName = rs.getString("position_name");
            javafx.scene.control.TextField positionField = new javafx.scene.control.TextField(posName != null ? posName : "");

            String camp = rs.getString("campaign");
            javafx.scene.control.TextArea campaignArea = new javafx.scene.control.TextArea(camp != null ? camp : "");
            campaignArea.setWrapText(true);
            campaignArea.setPrefHeight(120);

            javafx.scene.control.Dialog<javafx.scene.control.ButtonType> dialog = new javafx.scene.control.Dialog<>();
            dialog.setTitle("Edit Candidate");
            dialog.setHeaderText("Edit candidate information:");
            javafx.scene.control.ButtonType saveButton = new javafx.scene.control.ButtonType("Save", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButton, javafx.scene.control.ButtonType.CANCEL);

            javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
            grid.setHgap(10); grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20));
            grid.add(new javafx.scene.control.Label("First Name:"), 0, 0); grid.add(firstNameField, 1, 0);
            grid.add(new javafx.scene.control.Label("Last Name:"),  0, 1); grid.add(lastNameField,  1, 1);
            grid.add(new javafx.scene.control.Label("Username:"),   0, 2); grid.add(usernameField,  1, 2);
            grid.add(new javafx.scene.control.Label("Party:"),      0, 3); grid.add(partyField,     1, 3);
            grid.add(new javafx.scene.control.Label("Position:"),   0, 4); grid.add(positionField,  1, 4);
            grid.add(new javafx.scene.control.Label("Campaign:"),   0, 5); grid.add(campaignArea,   1, 5);
            dialog.getDialogPane().setContent(grid);

            java.util.Optional<javafx.scene.control.ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == saveButton) {
                String insertPosition = "INSERT IGNORE INTO positions (position_name) VALUES (?)";
                java.sql.PreparedStatement ps0 = conn.prepareStatement(insertPosition);
                ps0.setString(1, positionField.getText());
                ps0.executeUpdate();

                String updateLogin = "UPDATE login SET first_name = ?, last_name = ?, l_username = ? WHERE login_id = ?";
                java.sql.PreparedStatement ps1 = conn.prepareStatement(updateLogin);
                ps1.setString(1, firstNameField.getText());
                ps1.setString(2, lastNameField.getText());
                ps1.setString(3, usernameField.getText());
                ps1.setInt(4, loginId);
                ps1.executeUpdate();

                String updateCandidate = "UPDATE candidate SET party = ?, campaign = ?, " +
                        "position_id = (SELECT position_id FROM positions WHERE LOWER(position_name) = LOWER(?) LIMIT 1) " +
                        "WHERE candidate_id = ?";
                java.sql.PreparedStatement ps2 = conn.prepareStatement(updateCandidate);
                ps2.setString(1, partyField.getText());
                ps2.setString(2, campaignArea.getText());
                ps2.setString(3, positionField.getText());
                ps2.setInt(4, candidateId);
                ps2.executeUpdate();

                adminOutput.setText("Candidate updated successfully.\n\n");
                viewCandidates(event);
            }

        } catch (Exception e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            adminOutput.setText("Error editing candidate:\n" + e.getMessage());
        }
    }

    // method to refresh the dashboard
    @FXML
    public void refreshDashboard(ActionEvent event) {
        loadDashboard();
    }

    // method to view and edit admin profile
    @FXML
    public void goToProfile(ActionEvent actionEvent) {
        String adminFullName = "Unknown";
        String adminUsername = "Unknown";
        String adminId       = String.valueOf(currentUser);

        try {
            String query = "SELECT l.first_name, l.last_name, l.l_username, a.admin_id " +
                           "FROM admins a INNER JOIN login l ON a.login_id = l.login_id " +
                           "WHERE l.login_id = " + currentUser;
            java.sql.PreparedStatement ps = conn.prepareStatement(query);
            java.sql.ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                adminFullName = rs.getString("first_name") + " " + rs.getString("last_name");
                adminUsername = rs.getString("l_username");
                adminId       = String.valueOf(rs.getInt("admin_id"));
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

        javafx.scene.control.Button editProfileBtn = new javafx.scene.control.Button("Edit Profile");
        javafx.scene.control.Button logOutBtn      = new javafx.scene.control.Button("Log Out");

        final String currentName     = adminFullName;
        final String currentUsername = adminUsername;

        // edit profile button actions
        editProfileBtn.setOnAction(e -> {
            javafx.scene.control.TextInputDialog firstNameDialog = new javafx.scene.control.TextInputDialog(currentName.split(" ")[0]);
            firstNameDialog.setTitle("Edit First Name");
            firstNameDialog.setHeaderText("Enter New First Name:");
            java.util.Optional<String> firstNameResult = firstNameDialog.showAndWait();

            javafx.scene.control.TextInputDialog lastNameDialog = new javafx.scene.control.TextInputDialog(currentName.contains(" ") ? currentName.split(" ", 2)[1] : "");
            lastNameDialog.setTitle("Edit Last Name");
            lastNameDialog.setHeaderText("Enter New Last Name:");
            java.util.Optional<String> lastNameResult = lastNameDialog.showAndWait();

            javafx.scene.control.TextInputDialog usernameDialog = new javafx.scene.control.TextInputDialog(currentUsername);
            usernameDialog.setTitle("Edit Username");
            usernameDialog.setHeaderText("Enter New Username:");
            java.util.Optional<String> usernameResult = usernameDialog.showAndWait();

            if (firstNameResult.isPresent() && lastNameResult.isPresent() && usernameResult.isPresent()) {
                try {
                    String updateQuery = "UPDATE login SET first_name = ?, last_name = ?, l_username = ? " +
                                        "WHERE login_id = ?";
                    try (java.sql.PreparedStatement ps = conn.prepareStatement(updateQuery)) {
                        ps.setString(1, firstNameResult.get());
                        ps.setString(2, lastNameResult.get());
                        ps.setString(3, usernameResult.get());
                        ps.setInt(4, currentUser);
                        int rows = ps.executeUpdate();
                        System.out.println("Profile updated. Rows affected: " + rows);
                    }

                    loadDashboard();
                    dialog.close();
                    javafx.application.Platform.runLater(() -> goToProfile(actionEvent));

                } catch (Exception err) {
                    err.printStackTrace();
                }
            }
        });

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
        profileLayout.setPrefHeight(380);
        profileLayout.setAlignment(javafx.geometry.Pos.CENTER);

        nameLabel.setStyle("-fx-text-fill: #549892; -fx-font-size: 28px; -fx-font-weight: bold;");
        usernameLabel.setStyle("-fx-text-fill: #dfc07d; -fx-font-size: 14px;");
        roleLabel.setStyle("-fx-text-fill: #dfc07d; -fx-font-size: 18px;");
        idLabel.setStyle("-fx-text-fill: #dfc07d; -fx-font-size: 18px;");
        editProfileBtn.setStyle("-fx-background-color: #dfc07d; -fx-font-size: 18px; -fx-text-fill: #0E1525; " +
                                "-fx-font-weight: bold; -fx-min-width: 180px; -fx-min-height: 35px; -fx-cursor: hand;");
        logOutBtn.setStyle("-fx-background-color: #ea4335; -fx-font-size: 18px; -fx-text-fill: white; " +
                           "-fx-font-weight: bold; -fx-min-width: 180px; -fx-min-height: 35px; -fx-cursor: hand;");

        javafx.scene.layout.VBox.setMargin(nameLabel, new javafx.geometry.Insets(0, 0, -5, 0));
        javafx.scene.layout.VBox.setMargin(idLabel, new javafx.geometry.Insets(0, 0, 15, 0));
        profileLayout.getChildren().addAll(nameLabel, usernameLabel, roleLabel, idLabel, editProfileBtn, logOutBtn);

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
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        javafx.stage.Stage stage = (javafx.stage.Stage) logoutBtn.getScene().getWindow();
        stage.close();
    }
}