module com.example.votingveranda {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;

    opens com.example.votingveranda to javafx.fxml;
    exports com.example.votingveranda;
}