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

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VoterController {
    private Connection conn = null;
    static int votes_candidate_fk = 0;

    private int currentUser;
    @FXML private Label voterName;
    @FXML private VBox chartsContainer;
    @FXML private Button voteButton;

    public void setCurrentUser(int loginID) {
        this.currentUser = loginID;
        loadDashboard();
        viewStandings();

        try {
            String query = "SELECT COUNT(DISTINCT position_id) FROM candidate";
            Statement check = conn.createStatement();
            ResultSet checkRS = check.executeQuery(query);
            int positionsCand = 0;
            if (checkRS.next()) {
                positionsCand = checkRS.getInt(1);
            }

            String voteQuery = "SELECT COUNT(DISTINCT c.position_id) FROM votes v " +
                                "INNER JOIN candidate c ON v.candidate_id = c.candidate_id " +
                                "WHERE v.voter_id = (SELECT voter_id FROM voter WHERE login_id = ?)";
            PreparedStatement voteSTMT = conn.prepareStatement(voteQuery);
            voteSTMT.setInt(1, currentUser);
            ResultSet voteRS = voteSTMT.executeQuery();
            int votedPositions = 0;

            if (voteRS.next()) {
                votedPositions = voteRS.getInt(1);
            }

            if (votedPositions >= positionsCand && positionsCand > 0) {
                System.out.println("DEBUG: Button was locked because voter has already voted!");
                if (voteButton != null) {
                    voteButton.setDisable(true);
                    voteButton.setOpacity(0.5);
                } else {
                    System.out.println("DEBUG: Button should be clickable. Voted positions: " + votedPositions);
                }

                String updateFlag = "UPDATE voter SET vote_status = 1 WHERE login_id = ?";
                PreparedStatement updatePS = conn.prepareStatement(updateFlag);
                updatePS.setInt(1, currentUser);
                updatePS.executeUpdate();
            } else {
                if (voteButton != null) {
                    voteButton.setDisable(false);
                    voteButton.setOpacity(1.0);
                }
            }

        } catch (SQLException er) {
            er.printStackTrace();
        }
    }

    @FXML
    // method used to initialize the candidate page
    public void initialize() {
        this.conn = DatabaseAPI.db_connection();
    }

    // Method for casting vote
    @FXML
    public void castVote(ActionEvent event) {
        System.out.println("!!! CAST VOTE BUTTON CLICK RECEIVED IN CONTROLLER !!!");
        try {
            String totalQuery = "SELECT COUNT(DISTINCT position_id) FROM candidate";
            Statement totalSTMT = conn.createStatement();
            ResultSet totalRS = totalSTMT.executeQuery(totalQuery);
            int totalPosCand = 0;
            if (totalRS.next()) {
                totalPosCand = totalRS.getInt(1);
            }

            List<Integer> positionIDs = new ArrayList<>();
            List<String> positionNames = new ArrayList<>();

            String posQuery = "SELECT position_id, position_name FROM positions ORDER BY position_id ASC";
            try (Statement posSTMT = conn.createStatement();
                    ResultSet posRS = posSTMT.executeQuery(posQuery)) {
                while (posRS.next()) {
                        positionIDs.add(posRS.getInt("position_id"));
                        positionNames.add(posRS.getString("position_name"));
                    }
            }

            int posVoted = 0;

            for (int i = 0; i < positionIDs.size(); i++) {
                int positionID = positionIDs.get(i);
                String posName = positionNames.get(i);

                String checkVote = "SELECT COUNT(*) FROM votes v " +
                                    "INNER JOIN candidate c ON v.candidate_id = c.candidate_id " +
                                    "INNER JOIN voter vo ON v.voter_id = vo.voter_id " +
                                    "WHERE vo.login_id = ? AND c.position_id = ?";

                boolean alreadyVoted = false;
                try (PreparedStatement checkVoteSTMT = conn.prepareStatement(checkVote)) {
                    checkVoteSTMT.setInt(1, currentUser);
                    checkVoteSTMT.setInt(2, positionID);
                    try (ResultSet checkVoteRS = checkVoteSTMT.executeQuery()) {
                        if (checkVoteRS.next() && checkVoteRS.getInt(1) > 0) {
                            alreadyVoted = true;
                        }
                    }
                }

                if (alreadyVoted) {
                    System.out.println("DEBUG: User already voted for " + posName);
                    posVoted++;
                    continue;
                }

                StringBuilder candList = new StringBuilder("Candidates for " + posName + ":\n\n");
                boolean candFound = false;

                String candQuery = "SELECT c.candidate_id, c.campaign, CONCAT(l.first_name, ' ', l.last_name) AS c_name, c.party " +
                                    "FROM candidate c INNER JOIN login l on c.login_id = l.login_id " +
                                    "WHERE c.position_id = ?";
                try (PreparedStatement candSTMT = conn.prepareStatement(candQuery)) {
                    candSTMT.setInt(1, positionID);
                    try (ResultSet candRS = candSTMT.executeQuery()) {
                        while (candRS.next()) {
                            candFound = true;
                            String name = candRS.getString("c_name") != null ? candRS.getString("c_name") : "Unknown Candidate";
                            String party = candRS.getString("party") != null ? candRS.getString("party") : "Independent";
                            String campaign = candRS.getString("campaign") != null ? candRS.getString("campaign") : "No campaign statement";

                            candList.append("• ")
                                    .append(name).append("\n")
                                    .append("(").append(party).append(")\n")
                                    .append("Campaign: ").append(campaign).append("\n\n");
                        }
                    }
                }

                if (!candFound) {
                    System.out.println("DEBUG: Skipping " + posName + " because it has 0 registered candidates.");
                    continue;
                }

                Alert candAlert = new Alert(Alert.AlertType.INFORMATION);
                candAlert.setTitle(posName + " Candidates");
                candAlert.setHeaderText("Candidates running for " + posName + ":");

                TextArea textArea = new TextArea(candList.toString());
                textArea.setWrapText(true);
                textArea.setEditable(false);
                candAlert.getDialogPane().setContent(textArea);
                candAlert.showAndWait();

                String selectedCand = "";
                int matchedCandID = 0;
                String matchName = "";

                while (true) {
                    TextInputDialog voteDialog = new TextInputDialog();
                    voteDialog.setTitle("Cast Your Vote");
                    voteDialog.setHeaderText("Please insert the full name of the candidate you wish to vote for as " + posName + ":");
                    Optional<String> result = voteDialog.showAndWait();

                    if (!result.isPresent()) {
                        break;
                    }

                    selectedCand = result.get().trim();

//                    if (selectedCand == null) {
//                        break;
//                    }
//
//                    selectedCand = selectedCand.trim();

                    if (selectedCand.length() > 0) {
                        String innerQuery = "SELECT c.candidate_id, CONCAT(l.first_name, ' ', l.last_name) AS c_name " +
                                        "FROM candidate c INNER JOIN login l ON c.login_id = l.login_id " +
                                        "WHERE c.position_id = ? AND CONCAT(l.first_name, ' ', l.last_name) LIKE ?";
                        try (PreparedStatement stmt = conn.prepareStatement(innerQuery)) {
                            stmt.setInt(1, positionID);
                            stmt.setString(2, "%" + selectedCand + "%");
                            try (ResultSet rs = stmt.executeQuery()) {
                                if (rs.next()) {
                                    matchedCandID = rs.getInt("candidate_id");
                                    matchName = rs.getString("c_name");
                                    break;
                                } else {
                                    Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Candidate not found. Please try again");
                                    errorAlert.showAndWait();
                                }
                            }
                        }
                    } else {
                        Alert warnAlert = new Alert(Alert.AlertType.ERROR, "You cannot leave the input blank.");
                        warnAlert.showAndWait();
                    }
                }

                if (matchedCandID > 0) {
                    String insertQuery = "INSERT INTO votes (voter_id, candidate_id) VALUES((SELECT voter_id FROM voter WHERE login_id = ?), ?)";
                    try (PreparedStatement insertPS = conn.prepareStatement(insertQuery)) {
                        insertPS.setInt(1, currentUser);
                        insertPS.setInt(2, matchedCandID);
                        int rows = insertPS.executeUpdate();

                        if (rows > 0) {
                            System.out.println("You have voted for " + matchName + " as " + posName);
                            votes_candidate_fk = votes_candidate_fk + 1;
                            posVoted++;
                        }
                    }
                }
            }

            if (posVoted >= totalPosCand && totalPosCand > 0) {
                try {
                    String updateQuery = "UPDATE voter SET vote_status = 1 WHERE login_id = ?";
                    PreparedStatement updatePS = conn.prepareStatement(updateQuery);
                    updatePS.setInt(1, currentUser);
                    updatePS.executeUpdate();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                if (voteButton != null){
                    voteButton.setDisable(true);
                    voteButton.setOpacity(0.5);
                }
            }

            viewStandings();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDashboard() {
        if (conn == null) {
            voterName.setText("Try again later");
            return;
        }

        try {
            String query = "SELECT l.first_name, l.last_name, v.vote_status FROM voter v " +
                    "INNER JOIN login l ON v.login_id = l.login_id " +
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
        if (conn == null) {
            return;
        }

        Platform.runLater(() -> chartsContainer.getChildren().clear());

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

                final Label finalTitle = sectionTitle;
                final StackedBarChart<String,Number> finalChart = chart;

                Platform.runLater(() -> {
                    chartsContainer.getChildren().addAll(finalTitle, finalChart);

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
                });
            }
        } catch (Exception error) {
            System.out.println("Error finding position: " + error.getMessage());
            error.printStackTrace();
            return;
        }
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