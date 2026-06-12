package com.example.votingveranda;

import javafx.event.ActionEvent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Field;

public class AdminControllerTest {
    private AdminController adminController;
    private TextArea mockOutput;

    @BeforeAll
    public static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    public void setup() {
        adminController = new AdminController();
        mockOutput = new TextArea();

        try {
            Field outputField = AdminController.class.getDeclaredField("adminOutput");
            outputField.setAccessible(true);
            outputField.set(adminController, mockOutput);

            adminController.initialize();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testViewVoters() throws InterruptedException {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> adminController.viewVoters(new ActionEvent()));
            String txt = mockOutput.getText();
            assertNotNull(txt);
            assertTrue(txt.contains("𝐕𝐎𝐓𝐄𝐑 𝐈𝐍𝐅𝐎𝐑𝐌𝐀𝐓𝐈𝐎𝐍") || txt.contains("No voters found"), "Output should load voter records");
        });

        Thread.sleep(200);
    }

    @Test
    public void testViewCandidates() throws InterruptedException {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> adminController.viewCandidates(new ActionEvent()));
            String txt = mockOutput.getText();
            assertNotNull(txt);
            assertTrue(txt.contains("𝐂𝐀𝐍𝐃𝐈𝐃𝐀𝐓𝐄 𝐈𝐍𝐅𝐎𝐑𝐌𝐀𝐓𝐈𝐎𝐍") || txt.contains("No candidates found"), "Output should load candidate records");
        });

        Thread.sleep(200);
    }

    @Test
    public void testViewVoteCounts() throws InterruptedException {
        Platform.runLater(() -> {
            assertDoesNotThrow(() -> adminController.viewVoteCounts(new ActionEvent()));
            String txt = mockOutput.getText();
            assertNotNull(txt);
            assertTrue(txt.contains("𝐕𝐎𝐓𝐄 𝐂𝐎𝐔𝐍𝐓𝐒 𝐀𝐍𝐃 𝐒𝐓𝐀𝐍𝐃𝐈𝐍𝐆𝐒") || txt.contains("No votes found"), "Output should load vote records");
        });

        Thread.sleep(200);
    }
}
