package votingveranda;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;
import javafx.fxml.FXML;
@FXML
public class Voter{
    public void goToProfile(ActionEvent actionEvent) {
        String voterFullName = "Unknown";
        String voterUsername = "Unknown";
        String voterId       = String.valueOf(currentUser);
        int voterability = 0;

        try {
            String query = "SELECT l.first_name, l.last_name, l.l_username, a.admin_id " +
                           "FROM admins a INNER JOIN login l ON a.login_id = l.login_id " +
                           "WHERE l.login_id = " + currentUser;
            java.sql.PreparedStatement ps = conn.prepareStatement(query);
            java.sql.ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                voterFullName = rs.getString("first_name") + " " + rs.getString("last_name");
                voterUsername = rs.getString("l_username");
                voterId       = String.valueOf(rs.getInt("voter_id"));


            }
            if (voterability == 0){
                System.out.println("Voter can Vote");
                
            } else if (voterability == 1){
                System.out.println("Voter has already voted, and cannot vote again.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // profile dialog pop-up window
        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Voter Profile");

        // hidden close button (only exit through X on top of pop-up)
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
        javafx.scene.Node closeBtn = dialog.getDialogPane().lookupButton(javafx.scene.control.ButtonType.CLOSE);
        if (closeBtn != null) {
            closeBtn.managedProperty().bind(closeBtn.visibleProperty());
            closeBtn.setVisible(false);
        }

        javafx.scene.control.Label nameLabel     = new javafx.scene.control.Label(voterFullName);
        javafx.scene.control.Label usernameLabel = new javafx.scene.control.Label(voterUsername);
        javafx.scene.control.Label roleLabel     = new javafx.scene.control.Label("Role: Registered Voter");
        javafx.scene.control.Label idLabel       = new javafx.scene.control.Label("Voter ID: " + voterId);

        javafx.scene.control.Button editProfileBtn = new javafx.scene.control.Button("Edit Profile");
        javafx.scene.control.Button logOutBtn      = new javafx.scene.control.Button("Log Out");

        final String currentName     = voterFullName;
        final String currentUsername = voterUsername;

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

                    System.out.println("Profile Updated Successfully!");

                    // refresh dashboard title and reopen profile
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
            javafx.stage.Stage stage = (javafx.stage.Stage) voterOutput.getScene().getWindow();
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
                System.out.println("Database connection closed.");
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        javafx.stage.Stage stage = (javafx.stage.Stage) logoutBtn.getScene().getWindow();
        stage.close();
    }
}
