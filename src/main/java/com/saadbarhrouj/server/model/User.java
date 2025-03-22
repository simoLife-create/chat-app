package com.saadbarhrouj.server.model; // ou com.saadbarhrouj.server.model

public class User {
    private int id;
    private String nom;
    private String email;
    private String motDePasse; // Pour l'instant, on stocke le mot de passe en clair (à changer plus tard !)
    private String dateInscription; // ou autre type de données

    public User() {
        // Constructeur par défaut (requis pour Gson)
    }

    public User(int id, String nom, String email, String motDePasse, String dateInscription) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.dateInscription = dateInscription;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(String dateInscription) {
        this.dateInscription = dateInscription;
    }
}