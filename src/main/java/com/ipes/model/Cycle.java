package com.ipes.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente un cycle d'études (Licence, Master, etc.)
 */
public class Cycle implements Serializable {
    private Long id;
    private String code;
    private String intitule;
    private int dureeAnnees;
    private List<Niveau> niveaux = new ArrayList<>();
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getIntitule() { return intitule; }
    public void setIntitule(String intitule) { this.intitule = intitule; }
    
    public int getDureeAnnees() { return dureeAnnees; }
    public void setDureeAnnees(int dureeAnnees) { this.dureeAnnees = dureeAnnees; }
    
    public List<Niveau> getNiveaux() { return niveaux; }
    public void setNiveaux(List<Niveau> niveaux) { this.niveaux = niveaux; }
    
    public void addNiveau(Niveau niveau) {
        niveaux.add(niveau);
        niveau.setCycle(this);
    }
    
    @Override
    public String toString() {
        return intitule;
    }
}
