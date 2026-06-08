package com.example.votingveranda;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.*;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    private java.sql.Connection conn = null;

    private int currentUser;

    @FXML
    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        String sql = "SELECT * FROM login WHERE l_username = ? AND l_password = ?";

        try {
            this.conn = DatabaseAPI.db_connection();

            try (PreparedStatement stmt = this.conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, password);

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int userType = rs.getInt("user_id");
                    this.currentUser = rs.getInt("login_id");
                    showAlert("Login Successful", "Welcome " + rs.getString("first_name") + "!");
                    if (userType == 3) {
                        switchPage("admin-view.fxml");
                    } else if (userType == 2) {
                        switchPage("candidate-view.fxml");
                    } else {
                        switchPage("voter-view.fxml");
                        //                    showAlert("Voter Page", "Voter page is not connected yet.");
                    }
                } else {
                    showAlert("Login Failed", "Invalid username or password.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Could not connect to database.");
        }
    }

    private void switchPage(String fxmlFile) {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();

            boolean wasMaximized = stage.isMaximized();

            double currentWidth = stage.getScene().getWidth();
            double currentHeight = stage.getScene().getHeight();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(loader.load(), currentWidth, currentHeight);

            if (fxmlFile.equals("candidate-view.fxml")) {
                CandidateController controller = loader.getController();
                controller.setCurrentUser(this.currentUser);
            }

            if (fxmlFile.equals("admin-view.fxml")) {
                AdminController controller = loader.getController();
                controller.setCurrentUser(this.currentUser);
            }

            if (fxmlFile.equals("voter-view.fxml")) {
                VoterController controller = loader.getController();
                controller.setCurrentUser(this.currentUser);
            }

            stage.setScene(scene);
            stage.setMaximized(wasMaximized);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Page Error", "Could not open " + fxmlFile);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}