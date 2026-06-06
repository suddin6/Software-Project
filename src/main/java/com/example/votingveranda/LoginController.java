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

    private final String DB_URL = "jdbc:mysql://localhost:3306/voting_veranda";
    private final String DB_USER = "root";
    private final String DB_PASS = "123toma456789!";

    @FXML
    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        String sql = "SELECT * FROM login WHERE l_username = ? AND l_password = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userType = rs.getInt("user_id");

                showAlert("Login Successful", "Welcome " + rs.getString("first_name") + "!");

                if (userType == 3) {
                    switchPage("admin-view.fxml");
                } else if (userType == 2) {
                    switchPage("candidate-view.fxml");
                } else {
                    showAlert("Voter Page", "Voter page is not connected yet.");
                }

            } else {
                showAlert("Login Failed", "Invalid username or password.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Could not connect to database.");
        }
    }

    private void switchPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
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