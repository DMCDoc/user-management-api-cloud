package com.example.client.controller;

import com.example.client.model.LoginRequest;
import com.example.client.service.ApiService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.util.Objects;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label resultLabel;

    private final ApiService apiService = new ApiService();

    @FXML
    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        LoginRequest request = new LoginRequest(username, password);
        try {
            String jwt = apiService.login(request);
            resultLabel.setText("Connexion réussie. Token :\n" + jwt);
        } catch (Exception e) {
            resultLabel.setText("Erreur : " + e.getMessage());
        }
    }
    @FXML
public void goToRegister(ActionEvent event) {
    try {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();

        Parent root = FXMLLoader.load(Objects.requireNonNull(
            getClass().getClassLoader().getResource("fxml/register.fxml")
        ));
        stage.setScene(new Scene(root));
        stage.show();
    } catch (Exception e) {
        resultLabel.setText("Erreur navigation : " + e.getMessage());
        e.printStackTrace();
    }
    
}


}
