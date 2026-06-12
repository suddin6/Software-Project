package com.example.votingveranda;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

public class CandidateController {
    private java.sql.Connection conn = null;

    // hard-coded user to display on candidate page
    private int currentUser;

    // fx:ids from .fxml file
    @FXML private Label campaignText;
    @FXML private Label candidateName;
    @FXML private StackedBarChart<String, Number> candidateStandings;

    public void setCurrentUser(int loginID) {
        this.currentUser = loginID;

        loadCampaign(this.currentUser);
        viewStandings();
    }

    @FXML
    // method used to initialize the candidate page
    public void initialize() {
        this.conn = DatabaseAPI.db_connection();
    }

    // display the campaign on the candidate page
    private void loadCampaign(int candidateID) {
        if (conn == null) {
            campaignText.setText("Not shown yet");
            candidateName.setText("Try again later");
            return;
        }

        // using an SQL query to display the campaign and candidate name
        try {
            String query = "SELECT l.first_name, l.last_name, c.campaign FROM candidate c " +
                            "INNER JOIN login l ON c.login_id = l.login_id " +
                            "WHERE l.login_id = " + candidateID;
            java.sql.Statement stmt = conn.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                campaignText.setText(rs.getString("campaign"));
                candidateName.setText(rs.getString("first_name") + " " + rs.getString("last_name"));
            } else {
                campaignText.setText("Campaign Not Loading");
                candidateName.setText("Unknown Name");
            }

        } catch (Exception e) {
            System.out.println("SQL error: " + e.getMessage());
            e.printStackTrace();
            campaignText.setText("Displaying campaign for " + candidateID);
            candidateName.setText("Name Not Found");
        }
    }

    // method to view standings of candidates relative to position
    private void viewStandings() {
        if (conn == null) {
            return;
        }

        int positionID = -1;
        String positionName = "";

        try {
            String getPosQuery = "SELECT c.position_id, p.position_name FROM candidate c " +
                                    "INNER JOIN positions p ON c.position_id = p.position_id " +
                                    "WHERE c.login_id = " + currentUser;
            java.sql.Statement posSTMT = conn.createStatement();
            java.sql.ResultSet posRS = posSTMT.executeQuery(getPosQuery);
            if (posRS.next()) {
                positionID = posRS.getInt("position_id");
                positionName = posRS.getString("position_name");
            }
        } catch (Exception error) {
            System.out.println("Error finding position: " + error.getMessage());
            error.printStackTrace();
            return;
        }

        if (positionID == -1) {
            return;
        }

        try {
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

            while (rs.next()) {
                String name = rs.getString("c_name");
                String party= rs.getString("party");
                int votes = rs.getInt("total_votes");

                if ("Democrat".equalsIgnoreCase(party)) {
                    demSeries.getData().add(new XYChart.Data<>(name, votes));
                } else if ("Republican".equalsIgnoreCase(party)) {
                    repSeries.getData().add(new XYChart.Data<>(name, votes));
                } else if ("Green Party".equalsIgnoreCase(party)) {
                    greenSeries.getData().add(new XYChart.Data<>(name, votes));
                }
            }

            javafx.application.Platform.runLater(() -> {
                candidateStandings.getData().clear();

                if (demSeries.getData().size() > 0) {
                    candidateStandings.getData().add(demSeries);
                }

                if (repSeries.getData().size() > 0) {
                    candidateStandings.getData().add(repSeries);
                }

                if (greenSeries.getData().size() > 0) {
                    candidateStandings.getData().add(greenSeries);
                }

                candidateStandings.setPrefHeight(Region.USE_COMPUTED_SIZE);
                candidateStandings.setPrefWidth(Region.USE_COMPUTED_SIZE);
                candidateStandings.setMinHeight(275);

                candidateStandings.setAnimated(false);
                candidateStandings.setCategoryGap(0);

                candidateStandings.setStyle(
                        "-fx-background-color: transparent; "
                                + "-fx-font-size: 13px; "
                                + "-fx-text-fill: #dfc07d; "
                                + "-fx-text-background-color: #dfc07d; "
                                + "CHART_COLOR_1: #1a73e8; "
                                + "CHART_COLOR_2: #ea4335; "
                                + "CHART_COLOR_3: #34a853;"
                                + "-fx-bar-width: 35px"
                );

                for (int i = 0; i < candidateStandings.getData().size(); i++) {
                    XYChart.Series<String, Number> series = candidateStandings.getData().get(i);
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

                // UI for the legend of Standings chart
                javafx.scene.Node legend = candidateStandings.lookup(".chart-legend");
                if (legend instanceof javafx.scene.layout.Region) {
                    javafx.scene.layout.Region legReg = (javafx.scene.layout.Region) legend;
                    legReg.setPrefWidth(Region.USE_COMPUTED_SIZE);
                    legReg.setMinWidth(600);

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
                candidateStandings.getXAxis().setTickLabelFill(javafx.scene.paint.Color.web("#dfc07d"));
                candidateStandings.getYAxis().setTickLabelFill(javafx.scene.paint.Color.web("#dfc07d"));
            });
        } catch (Exception e) {
            System.out.println("Error for standings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // method used for campaign changes
    @FXML
    public void makeChanges(ActionEvent event) {
        if (conn == null) {
            return;
        }

        // Dialog pop-up window that allows user to edit their text
        javafx.scene.control.Dialog<String> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Update Campaign");
        dialog.setHeaderText("Update your campaign statement below:");

        javafx.scene.control.ButtonType saveButtonType = new javafx.scene.control.ButtonType("Save", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, javafx.scene.control.ButtonType.CANCEL);

        javafx.scene.control.TextArea ta = new javafx.scene.control.TextArea(campaignText.getText());
        ta.setWrapText(true);
        ta.setPrefHeight(400);
        ta.setPrefWidth(600);

        dialog.getDialogPane().setStyle("-fx-background-color: #f4f4f4; -fx-font-family: 'Arial Rounded MT Bold'");
        ta.setStyle("-fx-control-inner-background: #ffffff; -fx-font-size: 14px");

        dialog.getDialogPane().setContent(ta);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return ta.getText();
            }
            return null;
        });

        java.util.Optional<String> finish = dialog.showAndWait();

        if (finish.isPresent()) {
            String updatedCampaign = finish.get();

            // update the screen and the database
            try {
                String query = "UPDATE candidate SET campaign = ? WHERE login_id = " + currentUser;
                java.sql.PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, updatedCampaign);

                int updatedRows = ps.executeUpdate();
                if (updatedRows > 0) {
                    campaignText.setText(updatedCampaign);
                }
            } catch (Exception e) {
                System.out.println("SQL Error" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // method to view candidate profile
    @FXML
    public void goToProfile(ActionEvent actionEvent) {
        String candName = "Unknown";
        String candUsername = "Unknown";
        String party = "Unknown";
        String position = "Unknown";

        try {
            String query = "SELECT l.first_name, l.last_name, l.l_username, c.party, p.position_name FROM candidate c " +
                            "INNER JOIN login l ON c.login_id = l.login_id " +
                            "LEFT JOIN positions p ON c.position_id = p.position_id " +
                            "WHERE l.login_id = " + currentUser;
            java.sql.PreparedStatement ps = conn.prepareStatement(query);
            java.sql.ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                candName = rs.getString("first_name") + " " + rs.getString("last_name");
                candUsername = rs.getString("l_username");
                String dbParty = rs.getString("party");
                party = (dbParty != null) ? dbParty : "None";
                String dbPosition = rs.getString("position_name");
                position = (dbParty != null) ? dbPosition : "None";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // profile dialog pop-up window
        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Your Profile");

        // hidden close button (only exit functionality through X on top of pop-up)
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
        javafx.scene.Node closeBtn = dialog.getDialogPane().lookupButton(javafx.scene.control.ButtonType.CLOSE);
        if (closeBtn != null) {
            closeBtn.managedProperty().bind(closeBtn.visibleProperty());
            closeBtn.setVisible(false);
        }

        Label nameLabel = new Label(candName);
        Label usernameLabel = new Label(candUsername);
        Label partyLabel = new Label(party);
        Label positionLabel = new Label(position);

        Button editProfileBtn = new Button("Edit Profile");
        Button logOutBtn = new Button("Log Out");

        final String currentName = candName;
        final String currentUsername = candUsername;
        final String currentParty = party;
        final String currentPosition = position;

        // edit button actions
        editProfileBtn.setOnAction(e -> {
            javafx.scene.control.TextInputDialog nameFDialog = new javafx.scene.control.TextInputDialog(currentName.split(" ")[0]);
            nameFDialog.setTitle("Edit First Name");
            nameFDialog.setHeaderText("Enter New First Name: ");
            java.util.Optional<String> nameFResult = nameFDialog.showAndWait();

            javafx.scene.control.TextInputDialog nameLDialog = new javafx.scene.control.TextInputDialog(currentName.contains(" ") ? currentName.split(" ", 2)[1] : "");
            nameLDialog.setTitle("Edit Last Name");
            nameLDialog.setHeaderText("Enter New Last Name: ");
            java.util.Optional<String> nameLResult = nameLDialog.showAndWait();

            javafx.scene.control.TextInputDialog usernameDialog = new javafx.scene.control.TextInputDialog(currentUsername);
            usernameDialog.setTitle("Edit Username");
            usernameDialog.setHeaderText("Enter New Username: ");
            java.util.Optional<String> usernameResult = usernameDialog.showAndWait();

            javafx.scene.control.TextInputDialog partyDialog = new javafx.scene.control.TextInputDialog(currentParty);
            partyDialog.setTitle("Edit Party");
            partyDialog.setHeaderText("Enter New Party: ");
            java.util.Optional<String> partyResult = partyDialog.showAndWait();

            javafx.scene.control.TextInputDialog positionDialog = new javafx.scene.control.TextInputDialog(currentPosition);
            positionDialog.setTitle("Edit Position");
            positionDialog.setHeaderText("Enter New Position: ");
            java.util.Optional<String> positionResult = positionDialog.showAndWait();

            if (nameFResult.isPresent() && nameLResult.isPresent() && usernameResult.isPresent() && partyResult.isPresent() && positionResult.isPresent()) {
                try {
                    String insertQ2 = "INSERT IGNORE INTO positions (position_name) VALUES (?)";
                    try (java.sql.PreparedStatement ps2 = conn.prepareStatement(insertQ2)) {
                        ps2.setString(1, positionResult.get());
                        ps2.executeUpdate();
                    }

                    String updateLoginQuery = "UPDATE login SET first_name = ?, last_name = ?, l_username = ? WHERE login_id = ?";
                    try (java.sql.PreparedStatement loginPS = conn.prepareStatement(updateLoginQuery)) {
                        loginPS.setString(1, nameFResult.get());
                        loginPS.setString(2, nameLResult.get());
                        loginPS.setString(3, usernameResult.get());
                        loginPS.setInt(4, currentUser);
                        loginPS.executeUpdate();
                    }

                    String candidateQuery = "UPDATE candidate SET party = ?, " +
                                            "position_id = (SELECT position_id FROM positions WHERE LOWER(position_name) = LOWER(?) LIMIT 1) " +
                                            "WHERE login_id = ?";
                    try (java.sql.PreparedStatement candPS = conn.prepareStatement(candidateQuery)) {
                        candPS.setString(1, partyResult.get());
                        candPS.setString(2, positionResult.get());
                        candPS.setInt(3, currentUser);
                        candPS.executeUpdate();
                    }

                    dialog.close();

                    loadCampaign(currentUser);

                    javafx.application.Platform.runLater(() -> goToProfile(actionEvent));

                } catch (Exception err) {
                    err.printStackTrace();
                }
            }
        });

        // log out button actions
        logOutBtn.setOnAction(e -> {
            dialog.close();
            javafx.stage.Stage stage = (javafx.stage.Stage) candidateStandings.getScene().getWindow();
            stage.close();
        });

        // UI for dialog, buttons, and text labels
        javafx.scene.layout.VBox profileLayout = new javafx.scene.layout.VBox(15);
        profileLayout.setPadding(new javafx.geometry.Insets(20));
        profileLayout.setPrefWidth(500);
        profileLayout.setPrefHeight(350);
        profileLayout.setAlignment(javafx.geometry.Pos.CENTER);

        nameLabel.setStyle("-fx-text-fill: #549892; -fx-font-size: 26px;");
        usernameLabel.setStyle("-fx-text-fill: #dfc07d; -fx-font-size: 14px;");
        partyLabel.setStyle("-fx-text-fill: #dfc07d; -fx-font-size: 18px;");
        positionLabel.setStyle("-fx-text-fill: #dfc07d; -fx-font-size: 18px;");

        editProfileBtn.setStyle("-fx-background-color: #dfc07d; -fx-font-size: 18px; -fx-text-fill: #0E1525; -fx-font-weight: bold; -fx-min-width: 180px; -fx-min-height: 35px; -fx-cursor: hand;");
        logOutBtn.setStyle("-fx-background-color: #ea4335; -fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 180px; -fx-min-height: 35px; -fx-cursor: hand;");

        javafx.scene.layout.VBox.setMargin(nameLabel, new javafx.geometry.Insets(0, 0, -5, 0));
        javafx.scene.layout.VBox.setMargin(positionLabel, new javafx.geometry.Insets(0, 0, 15, 0));
        profileLayout.getChildren().addAll(nameLabel, usernameLabel, partyLabel, positionLabel, editProfileBtn, logOutBtn);

        dialog.getDialogPane().setContent(profileLayout);
        dialog.getDialogPane().setStyle("-fx-background-color: #0E1525; -fx-font-family: 'Arial Rounded MT Bold';");
        dialog.showAndWait();
    }
}