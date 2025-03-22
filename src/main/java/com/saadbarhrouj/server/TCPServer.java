package com.saadbarhrouj.server;

import com.google.gson.Gson;
import com.saadbarhrouj.server.database.UserDAO;
import com.saadbarhrouj.server.model.User;
import com.saadbarhrouj.shared.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TCPServer {

    private static final Logger logger = LogManager.getLogger(TCPServer.class);
    private ServerSocket serverSocket;
    private UserDAO userDAO; // Pour interagir avec la base de données des utilisateurs
    private DatagramSocket udpSocket;  // Déplacer ici
    private int udpPort = 12346;

    public TCPServer(int port, UserDAO userDAO) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.userDAO = userDAO;
        logger.info("Serveur TCP démarré sur le port: " + port);

        try {
            udpSocket = new DatagramSocket(udpPort);
            logger.info("Serveur UDP démarré sur le port: " + udpPort);
            startUdpListener();  // Démarrer l'écoute UDP dans le constructeur
        } catch (SocketException e) {
            logger.error("Erreur lors de la création du socket UDP.", e);
            throw e;  // Important de relancer l'exception pour signaler l'échec
        }

    }

    public void start() {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Nouvelle connexion TCP acceptée depuis: " + clientSocket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandler.start();
            }
        } catch (IOException e) {
            logger.error("Erreur lors de l'acceptation de la connexion TCP.", e);
        } finally {
            try {
                serverSocket.close();
                if (udpSocket != null) {
                    udpSocket.close();
                }
            } catch (IOException e) {
                logger.error("Erreur lors de la fermeture du socket serveur.", e);
            }
        }
    }

    private void startUdpListener() {
        new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                while (true) {
                    udpSocket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());
                    InetAddress clientAddress = packet.getAddress();
                    int clientPort = packet.getPort();

                    logger.info("Message UDP reçu de " + clientAddress + ":" + clientPort + ": " + message);

                    // Envoyer un ACK au client UDP
                    String ackMessage = "ACK";
                    byte[] ackBuffer = ackMessage.getBytes();
                    DatagramPacket ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length, clientAddress, clientPort);
                    udpSocket.send(ackPacket);

                    // Envoyer le message à tous les clients TCP connectés
                    for (ClientHandler client : ClientHandler.clients) {  //Utilise la liste statique
                        client.out.println("MESSAGE " + message);  // Envoyer via TCP
                    }
                }

            } catch (IOException e) {
                logger.error("Erreur lors de la réception du message UDP.", e);
            }
        }).start();
    }

    private class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private Gson gson;

        //Ajouter la liste statique des clients
        public static  java.util.List<ClientHandler> clients = new java.util.ArrayList<>();


        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            this.gson = new Gson();
            clients.add(this); // Ajouter le client à la liste
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    logger.info("Reçu du client: " + inputLine);
                    processInput(inputLine);
                }

            } catch (IOException e) {
                logger.error("Erreur lors de la communication avec le client.", e);
            } finally {
                try {
                    in.close();
                    out.close();
                    clientSocket.close();
                    clients.remove(this);  // Supprimer le client de la liste
                    logger.info("Connexion TCP avec " + clientSocket.getInetAddress() + " fermée.");
                } catch (IOException e) {
                    logger.error("Erreur lors de la fermeture des ressources client.", e);
                }
            }
        }

        private void processInput(String input) throws IOException {
            logger.info("Requete reçue: " + input);
            if (input.startsWith(Protocol.REGISTER)) {
                String[] parts = input.split(" ");
                if (parts.length == 4) { // Condition corrigée
                    String nom = parts[1];
                    String email = parts[2];
                    String password = parts[3];

                    try {
                        User user = userDAO.createUser(nom, email, password);
                        if (user != null) {
                            String userJson = gson.toJson(user);
                            out.println(Protocol.REGISTER_SUCCESS + " " + userJson);
                            logger.info("Inscription réussie pour l'utilisateur: " + email);
                        } else {
                            out.println(Protocol.REGISTER_FAILED);
                            logger.warn("Echec de l'inscription pour l'utilisateur: " + email);
                        }
                    } catch (SQLException e) {
                        logger.error("Erreur lors de l'accès à la base de données.", e);
                        out.println(Protocol.REGISTER_FAILED);
                    }
                } else {
                    out.println(Protocol.INVALID_REQUEST);
                    logger.warn("Requête d'inscription invalide reçue.");
                }
            } else if (input.startsWith(Protocol.LOGIN)) {
                String[] parts = input.split(" ");
                if (parts.length == 3) {
                    String email = parts[1];
                    String password = parts[2];

                    try {
                        User user = userDAO.validateUser(email, password);
                        if (user != null) {
                            // Conversion de l'objet utilisateur en JSON
                            String userJson = gson.toJson(user);

                            // Envoi de la réponse de succès au client, incluant les données de l'utilisateur
                            out.println(Protocol.LOGIN_SUCCESS + " " + userJson);
                            logger.info("Connexion réussie pour l'utilisateur: " + email);
                        } else {
                            out.println(Protocol.LOGIN_FAILED);
                            logger.warn("Echec de la connexion pour l'utilisateur: " + email);
                        }
                    } catch (SQLException e) {
                        logger.error("Erreur lors de l'accès à la base de données.", e);
                        out.println(Protocol.LOGIN_FAILED);
                    }
                } else {
                    out.println(Protocol.INVALID_REQUEST);
                    logger.warn("Requête de connexion invalide reçue.");
                }
            } else if (input.startsWith(Protocol.SEND_MESSAGE)) {
                logger.info("Traitement de SEND_MESSAGE " + input);
                String message = input.substring(Protocol.SEND_MESSAGE.length()).trim();
                logger.info("retransmission du message :" + message);
                // Envoyer le message à tous les clients TCP connectés
                for (ClientHandler client : ClientHandler.clients) {
                    client.out.println("MESSAGE " + message);
                }
            }
            else {
                out.println(Protocol.UNKNOWN_COMMAND);
                logger.warn("Commande inconnue reçue: " + input);
            }
        }
    }
}