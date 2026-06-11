package com.example.votingveranda;

import javafx.fxml.FXML;

import javafx.event.ActionEvent;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import javax.swing.*;
import java.sql.SQLException;

public class VoterController {
    private java.sql.Connection conn = null;
    static int votes_candidate_fk = 0;

    private int currentUser;
    @FXML private Label voterName;
    @FXML private javafx.scene.layout.VBox chartsContainer;

    public void setCurrentUser(int loginID) {
        this.currentUser = loginID;
        loadDashboard();
        viewStandings();
    }

    @FXML
    // method used to initialize the candidate page
    public void initialize() {
        this.conn = DatabaseAPI.db_connection();
    }

    // Method for casting vote
    @FXML
    public void castVote(ActionEvent event) {
        String name = "";
        String position = "";
        int selectedPosition = 1;
        int candidate_id = 0;
        int vote_status = 0;

        String Candidate = JOptionPane.showInputDialog(null,"Please insert name of Candidate you wish to vote for: ");
        if (Candidate == null) { return; }

        try {
            String query = "SELECT c.candidate_id, CONCAT(l.first_name, ' ', l.last_name) AS c_name, c.party, COUNT(v.vote_id) AS total_votes, p.position_id, p.position_name, vo.vote_status " +
                    "FROM candidate c " +
                    "INNER JOIN login l ON c.login_id = l.login_id " +
                    "INNER JOIN positions p ON c.position_id = p.position_id " +
                    "LEFT JOIN votes v ON c.candidate_id = v.candidate_id " +
                    "LEFT JOIN voter vo ON vo.login_id = ? " +
                    "GROUP BY c.candidate_id, l.first_name, l.last_name, c.party, p.position_id, p.position_name, vo.vote_status";
            java.sql.PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, currentUser);
            java.sql.ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String dbName = rs.getString("c_name");

                if (Candidate.trim().equalsIgnoreCase(dbName)) {
                    name = dbName;
                    position = rs.getString("position_name");
                    candidate_id = rs.getInt("candidate_id");
                    vote_status = rs.getInt("vote_status");
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (Candidate != null && Candidate.equalsIgnoreCase(name)) {
            String insertQuery = "INSERT INTO votes (voter_id, candidate_id) VALUES(?, ?)";
            try {
                java.sql.PreparedStatement insertPS = conn.prepareStatement(insertQuery);
                insertPS.setInt(1, currentUser);
                insertPS.setInt(2, candidate_id);
                int rows = insertPS.executeUpdate();

                if (rows > 0){
                    System.out.println("You have voted for " + name);
                    votes_candidate_fk = votes_candidate_fk + 1;
                    viewStandings();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Invalid candidate, please try again.");
        }
    }

    private void loadDashboard() {
        if (conn == null) {
            voterName.setText("Try again later");
            return;
        }

        try {
            String query = "SELECT l.first_name, l.last_name, v.vote_status FROM voter v " +
                    "INNER JOIN login l ON v.login_id = v.login_id " +
                    "WHERE l.login_id = " + currentUser;
            java.sql.Statement stmt = conn.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                voterName.setText(rs.getString("first_name") + " " + rs.getString("last_name"));
            } else {
                voterName.setText("Unknown Name");
            }

        } catch (Exception e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            voterName.setText("Name Not Found");
        }
    }

    // method to view standings of candidates relative to position
    private void viewStandings() {
        javafx.application.Platform.runLater(() -> {
            chartsContainer.getChildren().clear();

            if (conn == null) {
                return;
            }

            int positionID = -1;
            String positionName = "";

            try {
                String getPosQuery = "SELECT position_id, position_name FROM positions";
                java.sql.Statement posSTMT = conn.createStatement();
                java.sql.ResultSet posRS = posSTMT.executeQuery(getPosQuery);

                while (posRS.next()) {
                    positionID = posRS.getInt("position_id");
                    positionName = posRS.getString("position_name");

                    String query = "SELECT CONCAT(l.first_name, ' ', l.last_name) AS c_name, c.party, COUNT(v.vote_id) AS total_votes " +
                            "FROM candidate c " +
                            "INNER JOIN login l ON c.login_id = l.login_id " +
                            "LEFT JOIN votes v ON c.candidate_id = v.candidate_id " +
                            "WHERE c.position_id = " + positionID + " " +
                            "GROUP BY c.candidate_id, l.first_name, l.last_name, c.party";

                    java.sql.Statement stmt = conn.createStatement();
                    java.sql.ResultSet rs = stmt.executeQuery(query);

                    XYChart.Series<String, Number> demSeries = new XYChart.Series<>();
                    demSeries.setName("Democratic Party");

                    XYChart.Series<String, Number> repSeries = new XYChart.Series<>();
                    repSeries.setName("Republican Party");

                    XYChart.Series<String, Number> greenSeries = new XYChart.Series<>();
                    greenSeries.setName("Green Party");

                    boolean dataPresent = false;

                    while (rs.next()) {
                        dataPresent = true;

                        String name = rs.getString("c_name");
                        String party = rs.getString("party");
                        int votes = rs.getInt("total_votes");

                        if ("Democrat".equalsIgnoreCase(party)) {
                            demSeries.getData().add(new XYChart.Data<>(name, votes));
                        } else if ("Republican".equalsIgnoreCase(party)) {
                            repSeries.getData().add(new XYChart.Data<>(name, votes));
                        } else if ("Green Party".equalsIgnoreCase(party)) {
                            greenSeries.getData().add(new XYChart.Data<>(name, votes));
                        }
                    }

                    if (!dataPresent) {
                        continue;
                    }

                    Label sectionTitle = new Label(positionName + " Standings");
                    sectionTitle.setMaxWidth(Double.MAX_VALUE);
                    sectionTitle.setStyle("-fx-alignment: CENTER; " +
                            "-fx-text-fill: #dfc07d; " +
                            "-fx-font-family: 'Arial Rounded MT Bold'; " +
                            "-fx-font-size: 20px; " +
                            "-fx-padding: 20 0 10 0;");
                    chartsContainer.getChildren().add(sectionTitle);

                    javafx.scene.chart.CategoryAxis xAxis = new javafx.scene.chart.CategoryAxis();
                    javafx.scene.chart.NumberAxis yAxis = new javafx.scene.chart.NumberAxis();
                    yAxis.setTickLabelFill(javafx.scene.paint.Color.web("#dfc07d"));

                    StackedBarChart<String, Number> chart = new StackedBarChart<>(xAxis, yAxis);
                    chart.setHorizontalGridLinesVisible(true);
                    chart.setVerticalGridLinesVisible(true);

                    chart.lookup(".chart-plot-background").setStyle("-fx-background-color: #f4f4f4;");

                    javafx.scene.Node lines = chart.lookup(".chart-horizontal-grid-lines");
                    if (lines != null) {
                        lines.setStyle("-fx-stroke: #e0e0e0; -fx-stroke-dash-array: 2 2;");
                    }

                    javafx.scene.Node Vlines = chart.lookup(".chart-vertical-grid-lines");
                    if (Vlines != null) {
                        Vlines.setStyle("-fx-stroke: #e0e0e0; -fx-stroke-dash-array: 2 2;");
                    }

                    chart.setPrefHeight(320);
                    chart.setAnimated(false);
                    chart.setCategoryGap(0);
                    chart.setStyle(
                            "-fx-background-color: transparent; "
                                    + "-fx-font-size: 13px; "
                                    + "-fx-text-fill: #dfc07d; "
                                    + "-fx-text-background-color: #dfc07d; "
                                    + "CHART_COLOR_1: #1a73e8; "
                                    + "CHART_COLOR_2: #ea4335; "
                                    + "CHART_COLOR_3: #34a853;"
                    );

                    xAxis.setTickLabelFill(javafx.scene.paint.Color.web("#dfc07d"));
                    yAxis.setTickLabelFill(javafx.scene.paint.Color.web("#dfc07d"));

                    if (demSeries.getData().size() > 0) {
                        chart.getData().add(demSeries);
                    }

                    if (repSeries.getData().size() > 0) {
                        chart.getData().add(repSeries);
                    }

                    if (greenSeries.getData().size() > 0) {
                        chart.getData().add(greenSeries);
                    }

                    chartsContainer.getChildren().add(chart);

                    for (XYChart.Series<String, Number> series : chart.getData()) {
                        String barColor = "-fx-bar-fill: #dfc07d";

                        if ("Democratic Party".equals(series.getName())) {
                            barColor = "-fx-bar-fill: #1a73e8";
                        } else if ("Republican Party".equals(series.getName())) {
                            barColor = "-fx-bar-fill: #ea4335";
                        } else if ("Green Party".equals(series.getName())) {
                            barColor = "-fx-bar-fill: #34a853";
                        }

                        for (XYChart.Data<String, Number> data : series.getData()) {
                            if (data.getNode() != null) {
                                data.getNode().setStyle(barColor);
                            }
                        }
                    }

                    javafx.scene.Node legend = chart.lookup(".chart-legend");
                    if (legend instanceof javafx.scene.layout.Region) {
                        javafx.scene.layout.Region legReg = (javafx.scene.layout.Region) legend;

                        legReg.setStyle("-fx-alignment: CENTER; "
                                + "-fx-background-color: transparent; "
                                + "-fx-hgap: 30px; "
                                + "-fx-vgap: 0px; "
                                + "-fx-font-family: 'Arial Rounded MT Bold'; "
                                + "-fx-font-size: 14px; "
                                + "-fx-text-fill: #dfc07d"
                        );

                        for (javafx.scene.Node label : legReg.lookupAll(".chart-legend-item-label")) {
                            label.setStyle("-fx-font-family: 'Arial Rounded MT Bold'; -fx-font-size: 14px; -fx-text-fill: #dfc07d;");
                        }
                    }
                }
            } catch (Exception error) {
                System.out.println("Error finding position: " + error.getMessage());
                error.printStackTrace();
                return;
            }
        });
    }

    @FXML
    public void goToProfile(ActionEvent actionEvent) {
        String voterFullName = "Unknown";
        String voterUsername = "Unknown";
        String ssn = "Unknown";
        String voterId       = "Unknown";
        String voteStatusText = "Has Not Voted";
        int voterability = 0;

        try {
            String query = "SELECT l.first_name, l.last_name, l.l_username, v.ssn, v.voter_id, v.vote_status " +
                           "FROM login l LEFT JOIN voter v ON l.login_id = v.login_id " +
                           "WHERE l.login_id = " + currentUser;
            java.sql.PreparedStatement ps = conn.prepareStatement(query);
            java.sql.ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                voterFullName = rs.getString("first_name") + " " + rs.getString("last_name");
                voterUsername = rs.getString("l_username");
                ssn = rs.getString("ssn");
                voterId       = String.valueOf(rs.getInt("voter_id"));
                voterability = rs.getInt("vote_status");
            }

            if (voterability == 1){
                voteStatusText = "Has Voted";
            } else {
                voteStatusText = "Has Not Voted";
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
        javafx.scene.control.Label ssnLabel       = new javafx.scene.control.Label("SSN: " + ssn);
        javafx.scene.control.Label roleLabel     = new javafx.scene.control.Label("Role: Registered Voter");
        javafx.scene.control.Label idLabel       = new javafx.scene.control.Label("Voter ID: " + voterId);
        javafx.scene.control.Label statusLabel       = new javafx.scene.control.Label("Vote Status: " + voteStatusText);

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

                    loadDashboard();

                    dialog.close();
                    javafx.application.Platform.runLater(() -> goToProfile(actionEvent));

                } catch (Exception err) {
                    err.printStackTrace();
                }
            }
        });

        // log out button actions
        logOutBtn.setOnAction(e -> {
            dialog.close();
            javafx.stage.Stage stage = (javafx.stage.Stage) chartsContainer.getScene().getWindow();
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
        ssnLabel.setStyle("-fx-text-fill: #dfc07d; -fx-font-size: 18px;");
        idLabel.setStyle("-fx-text-fill: #dfc07d; -fx-font-size: 18px;");
        statusLabel.setStyle("-fx-text-fill: #dfc07d; -fx-font-size: 18px;");
        editProfileBtn.setStyle("-fx-background-color: #dfc07d; -fx-font-size: 18px; -fx-text-fill: #0E1525; " +
                                "-fx-font-weight: bold; -fx-min-width: 180px; -fx-min-height: 35px; -fx-cursor: hand;");
        logOutBtn.setStyle("-fx-background-color: #ea4335; -fx-font-size: 18px; -fx-text-fill: white; " +
                           "-fx-font-weight: bold; -fx-min-width: 180px; -fx-min-height: 35px; -fx-cursor: hand;");

        javafx.scene.layout.VBox.setMargin(nameLabel, new javafx.geometry.Insets(0, 0, -5, 0));
        javafx.scene.layout.VBox.setMargin(statusLabel, new javafx.geometry.Insets(0, 0, 15, 0));
        profileLayout.getChildren().addAll(nameLabel, usernameLabel, ssnLabel, roleLabel, idLabel, statusLabel, editProfileBtn, logOutBtn);

        dialog.getDialogPane().setContent(profileLayout);
        dialog.getDialogPane().setStyle("-fx-background-color: #0E1525; -fx-font-family: 'Arial Rounded MT Bold';");
        dialog.showAndWait();
    }
}