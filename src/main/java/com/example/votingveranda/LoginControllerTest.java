package com.example.votingveranda;

import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Field;

public class LoginControllerTest {
    private LoginController testController;
    private String lastSwitch = "";
    private String lastAlertTitle = "";

    @BeforeAll
    public static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    public void setUp() {
        lastSwitch = "";
        lastAlertTitle = "";

        testController = new LoginController();

        try {
            Field userField = LoginController.class.getDeclaredField("usernameField");
            userField.setAccessible(true);
            userField.set(testController, new TextField());

            Field passField = LoginController.class.getDeclaredField("passwordField");
            passField.setAccessible(true);
            passField.set(testController, new PasswordField());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testInvalidCred() throws InterruptedException {
        setPrivateFieldValue("usernameField", "non-existent user");
        setPrivateFieldValue("passwordField", "wrong-password");

        Platform.runLater(() -> {
           assertDoesNotThrow(() -> testController.handleLogin());
           assertEquals(0, testController.getCurrentUser());
        });
        Thread.sleep(200);
    }

    @Test
    public void testVoterLogin() throws InterruptedException {
        setPrivateFieldValue("usernameField", "suddin6");
        setPrivateFieldValue("passwordField", "randomPass@1");

        Platform.runLater(() -> {
            assertDoesNotThrow(() -> testController.handleLogin());
            assertTrue(testController.getCurrentUser() > 0, "Voter ID should be loaded from DB");
        });
        Thread.sleep(200);
    }

    @Test
    public void testAdminLogin() throws InterruptedException {
        setPrivateFieldValue("usernameField", "charfan");
        setPrivateFieldValue("passwordField", "randomPass@10");

        Platform.runLater(() -> {
            assertDoesNotThrow(() -> testController.handleLogin());
            assertTrue(testController.getCurrentUser() > 0, "Admin ID should be loaded from DB");
        });
        Thread.sleep(200);
    }

    private void setPrivateFieldValue(String fieldName, String value) {
        try {
            Field field = LoginController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object fieldInst = field.get(testController);
            if (fieldInst instanceof TextField) {
                ((TextField) fieldInst).setText(value);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}