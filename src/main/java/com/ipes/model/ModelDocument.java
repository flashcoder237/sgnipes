package com.ipes.model;

import java.io.Serializable;

/**
 * Représente un modèle de document (relevé, attestation ou diplôme)
 */
public class ModelDocument implements Serializable {
    private Long id;
    private String nom;
    private String type; // RELEVE, ATTESTATION, DIPLOME
    private String contenu; // Template format XML ou JSON
    private byte[] logo;
    private boolean actif;
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    
    public byte[] getLogo() { return logo; }
    public void setLogo(byte[] logo) { this.logo = logo; }
    
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
}

