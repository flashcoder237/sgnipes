package com.ipes.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;



/**
 * Représente une option ou spécialité
 */
public class Option implements Serializable {
    private Long id;
    private String code;
    private String intitule;
    private Niveau niveau;
    private List<UniteEnseignement> unites = new ArrayList<>();
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getIntitule() { return intitule; }
    public void setIntitule(String intitule) { this.intitule = intitule; }
    
    public Niveau getNiveau() { return niveau; }
    public void setNiveau(Niveau niveau) { this.niveau = niveau; }
    
    public List<UniteEnseignement> getUnites() { return unites; }
    public void setUnites(List<UniteEnseignement> unites) { this.unites = unites; }
    
    public void addUniteEnseignement(UniteEnseignement ue) {
        unites.add(ue);
        ue.setOption(this);
    }
    
    @Override
    public String toString() {
        return intitule;
    }
}
