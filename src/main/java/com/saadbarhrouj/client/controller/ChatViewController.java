package com.saadbarhrouj.client.controller;

import com.saadbarhrouj.client.model.User;
import com.saadbarhrouj.client.network.TCPClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChatViewController {

    private static final Logger logger = LogManager.getLogger(ChatViewController.class);

    private User user;
    private TCPClient tcpClient;

    @FXML
    private TextArea chatArea;

    @FXML
    private TextField messageField;

    private DatagramSocket udpSocket;
    private InetAddress serverAddress;
    private int serverPort = 12346; // Port UDP du serveur
    private Timer timer; // Pour la retransmission

    public void setUser(User user) {
        this.user = user;
        logger.info("ChatViewController: Utilisateur défini : " + user.getEmail());
        // Vous pouvez afficher le nom de l'utilisateur dans l'interface ici
    }

    public void setTcpClient(TCPClient tcpClient) {
        this.tcpClient = tcpClient;
        startTcpListener(); // Déplacer l'appel ici
    }

    @FXML
    void initialize() {
        // Initialisation du controller
        logger.info("ChatViewController initialisé.");

        try {
            udpSocket = new DatagramSocket();
            serverAddress = InetAddress.getByName("localhost"); // Remplacez par l'adresse du serveur
            logger.info("Socket UDP Créé");

            // Démarrer un thread pour écouter les ACKs UDP
            startUdpListener();


        } catch (SocketException | UnknownHostException e) {
            logger.error("Erreur lors de la création du socket UDP.", e);
            showAlert("Erreur", "Erreur lors de la création du socket UDP.");
        }
    }

    @FXML
    private void onSendButtonClick(ActionEvent event) {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            try {
                logger.info("Envoi du message UDP: " + message); // Ajout

                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, serverPort);
                udpSocket.send(packet);

                logger.info("Message UDP envoyé."); // Ajout
                messageField.clear();
                startResendTimer(message); // Démarrer la retransmission
            } catch (IOException ex) {
                logger.error("Erreur lors de l'envoi du message UDP.", ex);
                showAlert("Erreur", "Erreur lors de l'envoi du message UDP.");
            }
        }
    }

    private void startResendTimer(String message) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Retransmettre le message
                try {
                    byte[] buffer = message.getBytes();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, serverPort);
                    udpSocket.send(packet);
                    logger.warn("Message retransmis : " + message);
                    startResendTimer(message); // Redémarrer le timer
                } catch (IOException ex) {
                    logger.error("Erreur lors de la retransmission du message UDP.", ex);
                    showAlert("Erreur", "Erreur lors de la retransmission du message UDP.");
                    timer.cancel();
                }
            }
        }, 1000); // Délai de 1 seconde
    }

    private void startUdpListener() {
        new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                while (true) {
                    udpSocket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());

                    if ("ACK".equals(message.trim())) {
                        logger.info("ACK reçu du serveur.");
                        timer.cancel(); // Annuler le timer si ACK reçu
                    }

                }
            } catch (IOException e) {
                logger.error("Erreur lors de la réception de l'ACK UDP.", e);
            }
        }).start();
    }

    private void startTcpListener() {
        new Thread(() -> {
            try {
                String message;
                while ((message = tcpClient.receiveMessage()) != null) {
                    String finalMessage = message;
                    Platform.runLater(() -> chatArea.appendText(finalMessage + "\n")); // Mise à jour de l'UI
                }
            } catch (IOException e) {
                logger.error("Erreur lors de la réception du message TCP.", e);
            }
        }).start();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}