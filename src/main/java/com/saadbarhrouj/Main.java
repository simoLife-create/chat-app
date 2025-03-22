package com.saadbarhrouj;

import com.saadbarhrouj.server.database.DatabaseManager;
import com.saadbarhrouj.server.database.UserDAO;
import com.saadbarhrouj.server.model.User;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/mon_app_chat?serverTimezone=UTC";
        String user = "root"; // Utiliser l'utilisateur root
        String password = "";   // Pas de mot de passe

        DatabaseManager databaseManager = new DatabaseManager(url, user, password);
        UserDAO userDAO = new UserDAO(databaseManager);

        try (Connection connection = databaseManager.getConnection()) {
            logger.info("Connexion à la base de données réussie !");

            // Test de la création d'un utilisateur
            User newUser = userDAO.createUser("TestUser", "test@example.com", "password123");
            if (newUser != null) {
                logger.info("Utilisateur créé avec succès : " + newUser.getId() + ", " + newUser.getNom());

                // Test de la validation de l'utilisateur
                User validatedUser = userDAO.validateUser("test@example.com", "password123");
                if (validatedUser != null) {
                    logger.info("Utilisateur validé avec succès : " + validatedUser.getId() + ", " + validatedUser.getNom());
                } else {
                    logger.warn("Utilisateur non validé.");
                }
            } else {
                logger.warn("Impossible de créer l'utilisateur.");
            }

        } catch (SQLException e) {
            logger.error("Erreur lors de la connexion à la base de données : " + e.getMessage(), e);
        }
    }
}