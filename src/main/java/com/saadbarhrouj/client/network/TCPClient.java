package com.saadbarhrouj.client.network;

import com.google.gson.Gson;
import com.saadbarhrouj.client.model.User;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import com.saadbarhrouj.shared.Protocol; // Import de la classe Protocol
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TCPClient {

    private static final Logger logger = LogManager.getLogger(TCPClient.class);

    private String serverAddress;
    private int serverPort;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Gson gson;

    public TCPClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.gson = new Gson();
    }

    public void startConnection() throws IOException {
        try {
            clientSocket = new Socket(serverAddress, serverPort);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            logger.info("Connecté au serveur TCP : " + serverAddress + ":" + serverPort);
        } catch (IOException e) {
            logger.error("Erreur lors de la connexion au serveur TCP.", e);
            throw e;
        }
    }


    public User login(String email, String password) throws IOException {
        // Préparer la requête de connexion
        String request = Protocol.LOGIN + " " + email + " " + password;
        out.println(request);  //Envoyer au serveur
        String response = in.readLine(); // Lire la réponse du serveur

        if (response != null && response.startsWith(Protocol.LOGIN_SUCCESS)) {
            // Si la réponse indique une connexion réussie, désérialiser l'objet utilisateur
            String userData = response.substring(Protocol.LOGIN_SUCCESS.length()).trim();
            return gson.fromJson(userData, User.class);
        } else {
            // Si la réponse indique un échec de connexion
            logger.warn("Echec de la connexion: " + response);
            return null;
        }
    }

    public User register(String nom, String email, String password) throws IOException {
        // Préparer la requête d'inscription
        String request = Protocol.REGISTER + " " + nom + " " + email + " " + password;
        out.println(request);
        String response = in.readLine();

        if (response != null && response.startsWith(Protocol.REGISTER_SUCCESS)) {
            String userData = response.substring(Protocol.REGISTER_SUCCESS.length()).trim();
            return gson.fromJson(userData, User.class);
        } else {
            logger.warn("Echec de l'inscription: " + response);
            return null;
        }
    }



    // Méthodes pour envoyer et recevoir des messages, gérer les appels, envoyer des fichiers, etc.

    public void sendMessage(String message) throws IOException {
        out.println(message);
    }

    public String receiveMessage() throws IOException {
        return in.readLine();
    }

    public void stopConnection() throws IOException {
        if (in != null) in.close();
        if (out != null) out.close();
        if (clientSocket != null) clientSocket.close();
        logger.info("Déconnexion du serveur TCP.");

    }
}