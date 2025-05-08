package com.ipes.model;

import java.io.Serializable;


/**
 * Représente un Élément Constitutif (EC) - une matière
 */
public class ElementConstitutif implements Serializable {
    private Long id;
    private String code;
    private String intitule;
    private double coefficient;
    private int volumeHoraire;
    private UniteEnseignement unite;
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getIntitule() { return intitule; }
    public void setIntitule(String intitule) { this.intitule = intitule; }
    
    public double getCoefficient() { return coefficient; }
    public void setCoefficient(double coefficient) { this.coefficient = coefficient; }
    
    public int getVolumeHoraire() { return volumeHoraire; }
    public void setVolumeHoraire(int volumeHoraire) { this.volumeHoraire = volumeHoraire; }
    
    public UniteEnseignement getUnite() { return unite; }
    public void setUnite(UniteEnseignement unite) { this.unite = unite; }
    
    @Override
    public String toString() {
        return code + " - " + intitule;
    }
}