package com.saadbarhrouj.server;

import com.saadbarhrouj.server.database.DatabaseManager;
import com.saadbarhrouj.server.database.UserDAO;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerMain {

    private static final Logger logger = LogManager.getLogger(ServerMain.class);

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/mon_app_chat?serverTimezone=UTC";
        String user = "root";
        String password = "";

        DatabaseManager databaseManager = new DatabaseManager(url, user, password);
        UserDAO userDAO = new UserDAO(databaseManager);

        int port = 12345; // Port pour le serveur TCP

        try {
            TCPServer server = new TCPServer(port, userDAO);
            server.start();
        } catch (IOException e) {
            logger.error("Erreur lors du démarrage du serveur TCP.", e);
        }
    }
}