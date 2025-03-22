package com.saadbarhrouj.server;

import com.google.gson.Gson;
import com.saadbarhrouj.server.database.UserDAO;
import com.saadbarhrouj.server.model.User;
import com.saadbarhrouj.shared.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TCPServer {

    private static final Logger logger = LogManager.getLogger(TCPServer.class);
    private ServerSocket serverSocket;
    private UserDAO userDAO; // Pour interagir avec la base de données des utilisateurs

    public TCPServer(int port, UserDAO userDAO) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.userDAO = userDAO;
        logger.info("Serveur TCP démarré sur le port: " + port);

    }

    public void start() {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Nouvelle connexion TCP acceptée depuis: " + clientSocket.getInetAddress());
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            logger.error("Erreur lors de l'acceptation de la connexion TCP.", e);
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.error("Erreur lors de la fermeture du socket serveur.", e);
            }
        }
    }

    private class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private Gson gson;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            this.gson = new Gson();
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
                    logger.info("Connexion TCP avec " + clientSocket.getInetAddress() + " fermée.");
                } catch (IOException e) {
                    logger.error("Erreur lors de la fermeture des ressources client.", e);
                }
            }
        }

        private void processInput(String input) throws IOException {
            if (input.startsWith(Protocol.REGISTER)) {
                String[] parts = input.split(" ");
                if (parts.length == 5) {
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
            }
            else if (input.startsWith(Protocol.LOGIN)) {
                String[] parts = input.split(" ");
                if (parts.length == 4) {
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
            } else if (input.startsWith("SEND_MESSAGE")) {
                // Traitement de l'envoi de messages
            } else {
                out.println(Protocol.UNKNOWN_COMMAND);
                logger.warn("Commande inconnue reçue: " + input);
            }
        }
    }
}