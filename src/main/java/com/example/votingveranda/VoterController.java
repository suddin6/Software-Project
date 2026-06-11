package com.example.votingveranda;

import javafx.application.Platform;
import javafx.fxml.FXML;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.swing.*;
import java.sql.*;
import java.util.Optional;

public class VoterController {
    private Connection conn = null;
    static int votes_candidate_fk = 0;

    private int currentUser;
    @FXML private Label voterName;
    @FXML private VBox chartsContainer;

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
        String campaign = "";

        int selectedPosition = 1;
        int candidate_id = 0;
        int vote_status = 0;
        Button voteButton = null;

        JOptionPane.showMessageDialog(null, "Here are the current candidates,: " + selectedPosition + " has " + candidate_id + ", and " + (selectedPosition - 1) + " has " + candidate_id);

        String Candidate = JOptionPane.showInputDialog(null,"Please insert name of Candidate you wish to vote for: ");
        if (Candidate == null) { return; }

        try {
            String query = "SELECT c.candidate_id, c.campaign, CONCAT(l.first_name, ' ', l.last_name) AS c_name, c.party, COUNT(v.vote_id) AS total_votes, p.position_id, p.position_name, vo.vote_status " +
                    "FROM candidate c " +
                    "INNER JOIN login l ON c.login_id = l.login_id " +
                    "INNER JOIN positions p ON c.position_id = p.position_id " +
                    "LEFT JOIN votes v ON c.candidate_id = v.candidate_id " +
                    "LEFT JOIN voter vo ON vo.login_id = ? " +
                    "GROUP BY c.candidate_id, l.first_name, l.last_name, c.party, c.campaign, p.position_id, p.position_name, vo.vote_status";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, currentUser);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String dbName = rs.getString("c_name");

                if (Candidate.trim().equalsIgnoreCase(dbName)) {
                    name = dbName;
                    String position = JOptionPane.showInputDialog(null, "Please choose if you want to vote them for senator, or for president: ");
                    position = rs.getString("position_name");
                    candidate_id = rs.getInt("candidate_id");
                    vote_status = rs.getInt("vote_status");
                    campaign = rs.getString("campaign");
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (Candidate != null && Candidate.equalsIgnoreCase(name)) {
            String insertQuery = "INSERT INTO votes (voter_id, candidate_id) VALUES(?, ?)";
            try {
                PreparedStatement insertPS = conn.prepareStatement(insertQuery);
                insertPS.setInt(1, currentUser);
                insertPS.setInt(2, candidate_id);
                int rows = insertPS.executeUpdate();

                if (rows > 0){
                    System.out.println("You have voted for " + name);
                    votes_candidate_fk = votes_candidate_fk + 1;
                    vote_status = vote_status + 1;
                    if (selectedPosition == 1){
                        voteButton.setDisable(true);
                    }

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
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

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
        Platform.runLater(() -> {
            chartsContainer.getChildren().clear();

            if (conn == null) {
                return;
            }

            int positionID = -1;
            String positionName = "";

            try {
                String getPosQuery = "SELECT position_id, position_name FROM positions";
                Statement posSTMT = conn.createStatement();
                ResultSet posRS = posSTMT.executeQuery(getPosQuery);

                while (posRS.next()) {
                    positionID = posRS.getInt("position_id");
                    positionName = posRS.getString("position_name");

                    String query = "SELECT CONCAT(l.first_name, ' ', l.last_name) AS c_name, c.party, COUNT(v.vote_id) AS total_votes " +
                            "FROM candidate c " +
                            "INNER JOIN login l ON c.login_id = l.login_id " +
                            "LEFT JOIN votes v ON c.candidate_id = v.candidate_id " +
                            "WHERE c.position_id = " + positionID + " " +
                            "GROUP BY c.candidate_id, l.first_name, l.last_name, c.party";

                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

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

                    CategoryAxis xAxis = new CategoryAxis();
                    NumberAxis yAxis = new NumberAxis();
                    yAxis.setTickLabelFill(Color.web("#dfc07d"));

                    StackedBarChart<String, Number> chart = new StackedBarChart<>(xAxis, yAxis);
                    chart.setHorizontalGridLinesVisible(true);
                    chart.setVerticalGridLinesVisible(true);

                    chart.lookup(".chart-plot-background").setStyle("-fx-background-color: #f4f4f4;");

                    Node lines = chart.lookup(".chart-horizontal-grid-lines");
                    if (lines != null) {
                        lines.setStyle("-fx-stroke: #e0e0e0; -fx-stroke-dash-array: 2 2;");
                    }

                    Node Vlines = chart.lookup(".chart-vertical-grid-lines");
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

                    xAxis.setTickLabelFill(Color.web("#dfc07d"));
                    yAxis.setTickLabelFill(Color.web("#dfc07d"));

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

                    Node legend = chart.lookup(".chart-legend");
                    if (legend instanceof Region) {
                        Region legReg = (Region) legend;

                        legReg.setStyle("-fx-alignment: CENTER; "
                                + "-fx-background-color: transparent; "
                                + "-fx-hgap: 30px; "
                                + "-fx-vgap: 0px; "
                                + "-fx-font-family: 'Arial Rounded MT Bold'; "
                                + "-fx-font-size: 14px; "
                                + "-fx-text-fill: #dfc07d"
                        );

                        for (Node label : legReg.lookupAll(".chart-legend-item-label")) {
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
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

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
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Voter Profile");

        // hidden close button (only exit through X on top of pop-up)
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        Node closeBtn = dialog.getDialogPane().lookupButton(ButtonType.CLOSE);
        if (closeBtn != null) {
            closeBtn.managedProperty().bind(closeBtn.visibleProperty());
            closeBtn.setVisible(false);
        }

        Label nameLabel     = new Label(voterFullName);
        Label usernameLabel = new Label(voterUsername);
        Label ssnLabel       = new Label("SSN: " + ssn);
        Label roleLabel     = new Label("Role: Registered Voter");
        Label idLabel       = new Label("Voter ID: " + voterId);
        Label statusLabel       = new Label("Vote Status: " + voteStatusText);

        Button editProfileBtn = new Button("Edit Profile");
        Button logOutBtn      = new Button("Log Out");

        final String currentName     = voterFullName;
        final String currentUsername = voterUsername;

        // edit profile button actions 
        editProfileBtn.setOnAction(e -> {
            TextInputDialog firstNameDialog = new TextInputDialog(currentName.split(" ")[0]);
            firstNameDialog.setTitle("Edit First Name");
            firstNameDialog.setHeaderText("Enter New First Name:");
            Optional<String> firstNameResult = firstNameDialog.showAndWait();

            TextInputDialog lastNameDialog = new TextInputDialog(currentName.contains(" ") ? currentName.split(" ", 2)[1] : "");
            lastNameDialog.setTitle("Edit Last Name");
            lastNameDialog.setHeaderText("Enter New Last Name:");
            Optional<String> lastNameResult = lastNameDialog.showAndWait();

            TextInputDialog usernameDialog = new TextInputDialog(currentUsername);
            usernameDialog.setTitle("Edit Username");
            usernameDialog.setHeaderText("Enter New Username:");
            Optional<String> usernameResult = usernameDialog.showAndWait();

            if (firstNameResult.isPresent() && lastNameResult.isPresent() && usernameResult.isPresent()) {
                try {
                    String updateQuery = "UPDATE login SET first_name = ?, last_name = ?, l_username = ? " +
                                        "WHERE login_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(updateQuery)) {
                        ps.setString(1, firstNameResult.get());
                        ps.setString(2, lastNameResult.get());
                        ps.setString(3, usernameResult.get());
                        ps.setInt(4, currentUser);

                        int rows = ps.executeUpdate();
                        System.out.println("Profile updated. Rows affected: " + rows);
                    }

                    loadDashboard();

                    dialog.close();
                    Platform.runLater(() -> goToProfile(actionEvent));

                } catch (Exception err) {
                    err.printStackTrace();
                }
            }
        });

        // log out button actions
        logOutBtn.setOnAction(e -> {
            dialog.close();
            Stage stage = (Stage) chartsContainer.getScene().getWindow();
            stage.close();
        });

        // UI for dialog, buttons, and text labels
        VBox profileLayout = new VBox(15);
        profileLayout.setPadding(new Insets(20));
        profileLayout.setPrefWidth(500);
        profileLayout.setPrefHeight(380);
        profileLayout.setAlignment(Pos.CENTER);

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

        VBox.setMargin(nameLabel, new Insets(0, 0, -5, 0));
        VBox.setMargin(statusLabel, new Insets(0, 0, 15, 0));
        profileLayout.getChildren().addAll(nameLabel, usernameLabel, ssnLabel, roleLabel, idLabel, statusLabel, editProfileBtn, logOutBtn);

        dialog.getDialogPane().setContent(profileLayout);
        dialog.getDialogPane().setStyle("-fx-background-color: #0E1525; -fx-font-family: 'Arial Rounded MT Bold';");
        dialog.showAndWait();
    }
}