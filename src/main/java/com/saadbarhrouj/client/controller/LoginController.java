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

public class LoginController {

    private static final Logger logger = LogManager.getLogger(LoginController.class);

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private TCPClient tcpClient; // Initialisé ailleurs (par exemple, dans ChatApp)

    public void setTcpClient(TCPClient tcpClient) {
        this.tcpClient = tcpClient;
    }


    @FXML
    void onLoginButtonClick(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        try {
            User user = tcpClient.login(email, password);
            if (user != null) {
                logger.info("Connexion réussie pour l'utilisateur: " + email);
                switchToChatView(event, user);
            } else {
                logger.warn("Echec de la connexion pour l'utilisateur: " + email);
                showAlert("Erreur", "Email ou mot de passe incorrect.");
            }
        } catch (IOException e) {
            logger.error("Erreur lors de la communication avec le serveur.", e);
            showAlert("Erreur", "Erreur de communication avec le serveur.");
        }
    }

    @FXML
    void onRegisterButtonClick(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/saadbarhrouj/client/view/RegisterView.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("Application de Chat - Inscription");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            logger.error("Erreur lors du chargement de la vue d'inscription.", e);
            showAlert("Erreur", "Impossible de charger la vue d'inscription.");
        }
    }

    private void switchToChatView(ActionEvent event, User user) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/saadbarhrouj/client/view/ChatView.fxml"));
            Parent root = fxmlLoader.load();

            // Assurez-vous que ChatViewController a une méthode setUser
            ChatViewController chatViewController = fxmlLoader.getController();
            chatViewController.setUser(user);
            chatViewController.setTcpClient(tcpClient);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("Application de Chat - Chat");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            logger.error("Erreur lors du chargement de la vue de chat.", e);
            showAlert("Erreur", "Impossible de charger la vue de chat.");
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