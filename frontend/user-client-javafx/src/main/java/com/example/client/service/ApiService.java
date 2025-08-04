package com.example.client.service;

import com.example.client.model.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;

public class ApiService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();
    private final String BASE_URL = "http://localhost:8080/users";

    public String login(LoginRequest request) throws Exception {
        String requestBody = mapper.writeValueAsString(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body(); // le JWT
        } else {
            throw new RuntimeException("Échec de la connexion (code " + response.statusCode() + ")");
        }
    }
    public String register(LoginRequest request, String email) throws Exception {
        // Construire le corps de la requête avec Jackson
        var root = new java.util.HashMap<String, String>();
        root.put("username", request.getUsername());
        root.put("password", request.getPassword());
        root.put("email", email);
    
        String requestBody = mapper.writeValueAsString(root);
    
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();
    
        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    
        if (response.statusCode() == 201) {
            return "Inscription réussie !";
        } else {
            throw new RuntimeException("Échec de l'inscription (code " + response.statusCode() + ")");
        }
    }
    

}
