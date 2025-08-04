package com.example.client.controller;

import com.example.client.model.LoginRequest;
import com.example.client.service.ApiService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.URL;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField;
    @FXML private Label resultLabel;

    private final ApiService apiService = new ApiService();

    @FXML
    public void handleRegister(ActionEvent event) { // Ajout du paramètre ActionEvent
        String username = usernameField.getText();
        String password = passwordField.getText();
        String email = emailField.getText();

        LoginRequest request = new LoginRequest(username, password);
        try {
            String result = apiService.register(request, email);
            resultLabel.setText(result);
            resultLabel.setStyle("-fx-text-fill: green;");
            
            // Naviguer vers le login après 2 secondes
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(() -> {
                        goToLogin(event); // Utiliser le paramètre event pour la navigation
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();;
                }
            }).start();
            
        } catch (Exception e) {
            resultLabel.setText("Erreur : " + e.getMessage());
            resultLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
public void goToLogin(ActionEvent event) {
    try {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();

        // Diagnostic : vérifie si le fichier est bien trouvé
        URL fxml = getClass().getResource("/fxml/login.fxml");
        System.out.println("URL chargée : " + fxml);
        
        if (fxml == null) {
            resultLabel.setText("Erreur navigation : Fichier login.fxml introuvable");
            return;
        }

        Parent root = FXMLLoader.load(fxml);
        stage.setScene(new Scene(root));
    } catch (Exception e) {
        resultLabel.setText("Erreur navigation : " + e.getMessage());
        e.printStackTrace();
    }
}
}