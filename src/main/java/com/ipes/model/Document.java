package com.ipes.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Représente un document généré
 */
public class Document implements Serializable {
    private Long id;
    private String type; // RELEVE, ATTESTATION, DIPLOME
    private String numero;
    private LocalDate dateGeneration;
    private Inscription inscription;
    private byte[] contenu; // Document PDF généré
    private String auteur;
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    
    public LocalDate getDateGeneration() { return dateGeneration; }
    public void setDateGeneration(LocalDate dateGeneration) { this.dateGeneration = dateGeneration; }
    
    public Inscription getInscription() { return inscription; }
    public void setInscription(Inscription inscription) { this.inscription = inscription; }
    
    public byte[] getContenu() { return contenu; }
    public void setContenu(byte[] contenu) { this.contenu = contenu; }
    
    public String getAuteur() { return auteur; }
    public void setAuteur(String auteur) { this.auteur = auteur; }
}


