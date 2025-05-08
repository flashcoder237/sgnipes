package com.ipes.model;
import java.io.Serializable;

/**
 * Représente une note pour un élément constitutif
 */
public class Note implements Serializable {
    private Long id;
    private ElementConstitutif elementConstitutif;
    private Inscription inscription;
    private String typeEvaluation; // CC, TP, Examen, Rattrapage
    private Double valeur;
    private String observation;
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public ElementConstitutif getElementConstitutif() { return elementConstitutif; }
    public void setElementConstitutif(ElementConstitutif elementConstitutif) { this.elementConstitutif = elementConstitutif; }
    
    public Inscription getInscription() { return inscription; }
    public void setInscription(Inscription inscription) { this.inscription = inscription; }
    
    public String getTypeEvaluation() { return typeEvaluation; }
    public void setTypeEvaluation(String typeEvaluation) { this.typeEvaluation = typeEvaluation; }
    
    public Double getValeur() { return valeur; }
    public void setValeur(Double valeur) { this.valeur = valeur; }
    
    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }
}
