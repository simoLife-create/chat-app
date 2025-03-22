package com.saadbarhrouj.server.database;

import com.saadbarhrouj.server.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserDAO {

    private static final Logger logger = LogManager.getLogger(UserDAO.class);
    private DatabaseManager databaseManager;

    public UserDAO(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public User createUser(String nom, String email, String motDePasse) throws SQLException {
        String sql = "INSERT INTO utilisateurs (nom, email, mot_de_passe) VALUES (?, ?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, nom);
            pstmt.setString(2, email);
            pstmt.setString(3, motDePasse); // Stockage du mot de passe en clair (temporaire !)

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newId = generatedKeys.getInt(1);
                        // Récupérer l'utilisateur nouvellement créé (avec la date d'inscription)
                        return getUserById(newId);
                    } else {
                        logger.warn("Impossible de récupérer l'ID de l'utilisateur après la création.");
                        return null;
                    }
                }
            } else {
                logger.warn("Impossible de créer l'utilisateur.");
                return null;
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la création de l'utilisateur.", e);
            throw e;
        }
    }


    public User validateUser(String email, String password) throws SQLException {
        String sql = "SELECT id, nom, email, date_inscription FROM utilisateurs WHERE email = ? AND mot_de_passe = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, password); // Comparaison du mot de passe en clair (temporaire !)

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setEmail(rs.getString("email"));
                user.setMotDePasse(password); // On remet le mot de passe clair pour l'instant
                user.setDateInscription(rs.getString("date_inscription")); //Ou autre type de données

                logger.info("Utilisateur validé: " + email);
                return user;
            } else {
                logger.warn("Utilisateur non validé: " + email);
                return null;
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la validation de l'utilisateur.", e);
            throw e;
        }
    }


    public User getUserById(int id) throws SQLException {
        String sql = "SELECT id, nom, email, mot_de_passe, date_inscription FROM utilisateurs WHERE id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setEmail(rs.getString("email"));
                user.setMotDePasse(rs.getString("mot_de_passe")); // temporaire
                user.setDateInscription(rs.getString("date_inscription"));

                return user;
            } else {
                logger.warn("Utilisateur non trouvé avec l'ID: " + id);
                return null;
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération de l'utilisateur avec l'ID: " + id, e);
            throw e;
        }
    }
}