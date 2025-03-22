package com.saadbarhrouj.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DatabaseManager {

    private static final Logger logger = LogManager.getLogger(DatabaseManager.class);
    private String url;
    private String user;
    private String password;

    public DatabaseManager(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Charger le driver JDBC MySQL
            Connection connection = DriverManager.getConnection(url, user, password);
            logger.debug("Connexion à la base de données réussie.");
            return connection;
        } catch (ClassNotFoundException e) {
            logger.error("Driver JDBC non trouvé.", e);
            throw new SQLException("Driver JDBC non trouvé.", e);
        } catch (SQLException e) {
            logger.error("Erreur lors de la connexion à la base de données.", e);
            throw e;
        }
    }
}