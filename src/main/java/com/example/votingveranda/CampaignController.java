package com.example.votingveranda;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

import javax.swing.*;

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

    @FXML
    public void makeChanges(ActionEvent event) {
        if (conn == null) {
            return;
        }

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

            try {
                String query = "UPDATE candidate SET campaign = ? WHERE candidate_id = ?";
                java.sql.PreparedStatement ps = conn.prepareStatement(query);

                ps.setString(1, updatedCampaign);
                ps.setInt(2, currentUser);

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

    @FXML
    public void goToProfile(ActionEvent actionEvent) {
    }
}
