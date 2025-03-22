package com.saadbarhrouj.client;

import com.saadbarhrouj.client.controller.LoginController;
import com.saadbarhrouj.client.network.TCPClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChatApp extends Application {

    private static final Logger logger = LogManager.getLogger(ChatApp.class);
    private TCPClient tcpClient;

    @Override
    public void start(Stage stage) throws IOException {
        try {
            //Initialisation du client TCP
            tcpClient = new TCPClient("localhost", 12345); // Remplacez par l'adresse et le port de votre serveur
            tcpClient.startConnection();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/saadbarhrouj/client/view/LoginView.fxml"));
            Parent root = fxmlLoader.load();

            // Récupérer le contrôleur et passer le client TCP
            LoginController loginController = fxmlLoader.getController();
            loginController.setTcpClient(tcpClient);

            Scene scene = new Scene(root);
            stage.setTitle("Application de Chat - Connexion");
            stage.setScene(scene);
            stage.show();
            logger.info("Application lancée avec succès.");

        } catch (IOException e) {
            logger.error("Erreur lors du chargement de l'interface graphique.", e);
            throw e; // Re-lancer l'exception pour signaler l'échec du démarrage
        }
    }

    @Override
    public void stop() throws Exception {
        // Fermer la connexion TCP lors de la fermeture de l'application
        if (tcpClient != null) {
            tcpClient.stopConnection();
        }
        super.stop();
    }


    public static void main(String[] args) {
        launch(args);
    }
}