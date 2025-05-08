package com.ipes.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;




/**
 * Représente un module d'enseignement
 */
public class Module implements Serializable {
    private Long id;
    private String code;
    private String intitule;
    private double coefficient;
    private Niveau niveau;
    private List<UniteEnseignement> unites = new ArrayList<>();
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getIntitule() { return intitule; }
    public void setIntitule(String intitule) { this.intitule = intitule; }
    
    public double getCoefficient() { return coefficient; }
    public void setCoefficient(double coefficient) { this.coefficient = coefficient; }
    
    public Niveau getNiveau() { return niveau; }
    public void setNiveau(Niveau niveau) { this.niveau = niveau; }
    
    public List<UniteEnseignement> getUnites() { return unites; }
    public void setUnites(List<UniteEnseignement> unites) { this.unites = unites; }
    
    public void addUniteEnseignement(UniteEnseignement ue) {
        unites.add(ue);
        ue.setModule(this);
    }
    
    @Override
    public String toString() {
        return intitule;
    }
}
