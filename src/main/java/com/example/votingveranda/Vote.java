package com.example.votingveranda;

import java.util.Scanner;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class Vote{
    private final String DB_URL = "jdbc:mysql://localhost:3306/voting_veranda";
    private final String DB_USER = "root";
    private final String DB_PASS = "root";
    private java.sql.Connection conn = null;

    private void dbConnection() {
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
    public void initialize() {
        dbConnection();
    }

    static int votes_candidate_fk = 0;
    static int candidate_id = 1;

    public static void main(String[] args) {
        Scanner Candidate = new Scanner(System.in);
        System.out.print("Please insert name of Candidate you wish to vote for: ");
        while(true) {
            if (Candidate.nextInt() == candidate_id) {
                String name = Candidate.nextLine();
                System.out.println("You have voted for " + name);
                votes_candidate_fk = votes_candidate_fk + 1;
                break;
            } else {
                System.out.println("Invalid candidate, please try again.");
            }
        }
    }
}