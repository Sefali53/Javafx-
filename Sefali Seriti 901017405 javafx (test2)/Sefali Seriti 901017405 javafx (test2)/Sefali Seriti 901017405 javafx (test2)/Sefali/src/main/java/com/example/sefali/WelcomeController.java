package com.example.sefali;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class WelcomeController {

    @FXML
    private void goToSignup(ActionEvent event) throws IOException {
        // Load the signup screen
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/sefali/signup-view.fxml"));  // Ensure correct path
        Parent root = fxmlLoader.load();
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 400, 400));
        stage.setTitle("Sign Up");
        stage.show();
    }

    @FXML
    private void goToLogin(ActionEvent event) throws IOException {
        // Load the login screen
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/sefali/login-view.fxml"));  // Ensure correct path
        Parent root = fxmlLoader.load();
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 400, 300));
        stage.setTitle("Log In");
        stage.show();
    }
}
