package com.example.votingveranda;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

public class CampaignController {
    // information to connect to database using mySQL workbench
    private final String DB_URL = "jdbc:mysql://localhost:3306/voting_veranda";
    private final String DB_USER = "root";
    private final String DB_PASS = "root";
    private java.sql.Connection conn = null;

    // hard-coded user to display on candidate page
    private int currentUser = 1;

    // fx:ids from .fxml file
    @FXML private Label campaignText;
    @FXML private Label candidateName;
    @FXML private AreaChart candidateStandings;
//    @FXML private JButton editCampaign;

    // database connection method
    private void db_connection() {
        // if "Connecting" and "Success" show up in terminal, database has connected
        System.out.println("Connecting");
        try {
            conn = java.sql.DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            if (conn != null) {
                System.out.println("Success");
            }
        } catch (java.sql.SQLException e) {
            System.out.println("Fail");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    // method used to initialize the candidate page
    public void initialize() {
        db_connection();
        loadCampaign(currentUser);
        viewStandings();
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
                            "WHERE candidate_id = " + candidateID;
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

    // method to view standings of all candidates
    private void viewStandings() {
        candidateStandings.getData().clear();

        if (candidateStandings.getXAxis() instanceof CategoryAxis) {
            ((CategoryAxis) candidateStandings.getXAxis()).setGapStartAndEnd(false);
        }

        XYChart.Series<String, Number> demSeries = new XYChart.Series<>();
        demSeries.setName("Democratic Party");
        demSeries.getData().add(new XYChart.Data<>("May 15",300));
        demSeries.getData().add(new XYChart.Data<>("May 20",230));
        demSeries.getData().add(new XYChart.Data<>("May 25",428));
        demSeries.getData().add(new XYChart.Data<>("May 30",133));
        demSeries.getData().add(new XYChart.Data<>("June 5",510));
        demSeries.getData().add(new XYChart.Data<>("June 10",291));

        XYChart.Series<String, Number> repSeries = new XYChart.Series<>();
        repSeries.setName("Republican Party");
        repSeries.getData().add(new XYChart.Data<>("May 15",529));
        repSeries.getData().add(new XYChart.Data<>("May 20",102));
        repSeries.getData().add(new XYChart.Data<>("May 25",342));
        repSeries.getData().add(new XYChart.Data<>("May 30",343));
        repSeries.getData().add(new XYChart.Data<>("June 5",104));
        repSeries.getData().add(new XYChart.Data<>("June 10",221));

        XYChart.Series<String, Number> greenSeries = new XYChart.Series<>();
        greenSeries.setName("Green Party");
        greenSeries.getData().add(new XYChart.Data<>("May 15",100));
        greenSeries.getData().add(new XYChart.Data<>("May 20",250));
        greenSeries.getData().add(new XYChart.Data<>("May 25",242));
        greenSeries.getData().add(new XYChart.Data<>("May 30",133));
        greenSeries.getData().add(new XYChart.Data<>("June 5",120));
        greenSeries.getData().add(new XYChart.Data<>("June 10",191));

        candidateStandings.getData().addAll(demSeries, repSeries, greenSeries);

        candidateStandings.setPrefHeight(Region.USE_COMPUTED_SIZE);
        candidateStandings.setPrefWidth(Region.USE_COMPUTED_SIZE);
        candidateStandings.setMinHeight(275);

        candidateStandings.setStyle(
                "-fx-background-color: transparent; "
                + "-fx-font-size: 13px; "
                + "-fx-text-fill: #dfc07d; "
                + "-fx-text-background-color: #dfc07d;"
                + "CHART_COLOR_1: #1a73e8; "
                + "CHART_COLOR_2: #ea4335; "
                + "CHART_COLOR_3: #34a853;"
        );

        // color coordinated UI: blue for democrat, red for republican, green for green party
        demSeries.getNode().lookup(".chart-series-area-line").setStyle("-fx-stroke: #1a73e8;");
        demSeries.getNode().lookup(".chart-series-area-fill").setStyle("-fx-fill: rgba(26, 115, 232, 0.15);");

        repSeries.getNode().lookup(".chart-series-area-line").setStyle("-fx-stroke: #ea4335;");
        repSeries.getNode().lookup(".chart-series-area-fill").setStyle("-fx-fill: rgba(234, 67, 53, 0.15);");

        greenSeries.getNode().lookup(".chart-series-area-line").setStyle("-fx-stroke: #34a853;");
        greenSeries.getNode().lookup(".chart-series-area-fill").setStyle("-fx-fill: rgba(52, 168, 83, 0.15);");

        for (XYChart.Data<String, Number> data : demSeries.getData()) {
            if (data.getNode() != null) data.getNode().lookup(".chart-area-symbol").setStyle("-fx-background-color: #1a73e8, white;");
        }

        for (XYChart.Data<String, Number> data : repSeries.getData()) {
            if (data.getNode() != null) data.getNode().lookup(".chart-area-symbol").setStyle("-fx-background-color: #ea4335, white;");
        }

        for (XYChart.Data<String, Number> data : greenSeries.getData()) {
            if (data.getNode() != null) data.getNode().lookup(".chart-area-symbol").setStyle("-fx-background-color: #34a853, white;");
        }

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
                String query = "UPDATE candidate SET campaign = ? WHERE candidate_id = " + currentUser;
                java.sql.PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, updatedCampaign);

                int updatedRows = ps.executeUpdate();
                if (updatedRows > 0) {
                    campaignText.setText(updatedCampaign);
                    System.out.println("successful update");
                }
            } catch (Exception e) {
                System.out.println("sql error" + e.getMessage());
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
                            "WHERE c.candidate_id = " + currentUser;
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

        final String currentParty = party;
        final String currentPosition = position;

        // edit button actions
        editProfileBtn.setOnAction(e -> {
            javafx.scene.control.TextInputDialog partyDialog = new javafx.scene.control.TextInputDialog(currentParty);
            partyDialog.setTitle("Edit Party");
            partyDialog.setHeaderText("Enter New Party: ");
            java.util.Optional<String> partyResult = partyDialog.showAndWait();

            javafx.scene.control.TextInputDialog positionDialog = new javafx.scene.control.TextInputDialog(currentPosition);
            positionDialog.setTitle("Edit Position");
            positionDialog.setHeaderText("Enter New Position: ");
            java.util.Optional<String> positionResult = positionDialog.showAndWait();

            if (partyResult.isPresent() && positionResult.isPresent()) {
                try {
                    String insertQ2 = "INSERT IGNORE INTO positions (position_name) VALUES (?)";
                    java.sql.PreparedStatement ps2 = conn.prepareStatement(insertQ2);
                    ps2.setString(1, positionResult.get());
                    ps2.executeUpdate();

                    String query1 = "UPDATE candidate SET party = ? WHERE candidate_id = " + currentUser;
                    java.sql.PreparedStatement profilePS1 = conn.prepareStatement(query1);
                    profilePS1.setString(1, partyResult.get());
                    int rows1 = profilePS1.executeUpdate();

                    String query2 = "UPDATE candidate SET position_id = (SELECT position_id FROM positions WHERE LOWER(position_name) = LOWER(?) LIMIT 1) WHERE candidate_id = " + currentUser;
                    java.sql.PreparedStatement profilePS2 = conn.prepareStatement(query2);
                    profilePS2.setString(1, positionResult.get());
                    int rows2 = profilePS2.executeUpdate();

                    System.out.println("SQL Execute Rows Updated -> Party rows: " + rows1 + ", Position rows: " + rows2);
                    System.out.println("Profile Updated Successfully!");
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
