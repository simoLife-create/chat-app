package com.saadbarhrouj.client.controller;

import com.saadbarhrouj.client.model.User;
import com.saadbarhrouj.client.network.TCPClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RegisterController {

    private static final Logger logger = LogManager.getLogger(RegisterController.class);

    @FXML
    private TextField nomField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private TCPClient tcpClient;  //Initialisé ailleurs (par exemple, dans ChatApp)

    public void setTcpClient(TCPClient tcpClient) {
        this.tcpClient = tcpClient;
    }

    @FXML
    void onRegisterButtonClick(ActionEvent event) {
        String nom = nomField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (nom == null || nom.isEmpty() || email == null || email.isEmpty() || password == null || password.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        logger.info("Nom: " + nom); // Ajout
        logger.info("Email: " + email); // Ajout
        logger.info("Mot de passe: " + password); // Ajout

        try {
            User user = tcpClient.register(nom, email, password);
            if (user != null) {
                logger.info("Inscription réussie pour l'utilisateur: " + email);
                showAlert("Succès", "Inscription réussie. Veuillez vous connecter.");
                switchToLoginView(event);
            } else {
                logger.warn("Echec de l'inscription pour l'utilisateur: " + email);
                showAlert("Erreur", "L'inscription a échoué.");
            }
        } catch (IOException e) {
            logger.error("Erreur lors de la communication avec le serveur.", e);
            showAlert("Erreur", "Erreur de communication avec le serveur.");
        }
    }

    @FXML
    void onLoginButtonClick(ActionEvent event) {
        switchToLoginView(event);
    }


    private void switchToLoginView(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/saadbarhrouj/client/view/LoginView.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("Application de Chat - Connexion");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            logger.error("Erreur lors du chargement de la vue de connexion.", e);
            showAlert("Erreur", "Impossible de charger la vue de connexion.");
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}