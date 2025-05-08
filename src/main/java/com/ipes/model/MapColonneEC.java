package com.ipes.model;

import java.io.Serializable;

/**
 * Représente un mapping entre une colonne de fichier et un EC
 */
public class MapColonneEC implements Serializable {
    private Long id;
    private String nomColonne;
    private int indexColonne;
    private ElementConstitutif elementConstitutif;
    private ConfigImport config;
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNomColonne() { return nomColonne; }
    public void setNomColonne(String nomColonne) { this.nomColonne = nomColonne; }
    
    public int getIndexColonne() { return indexColonne; }
    public void setIndexColonne(int indexColonne) { this.indexColonne = indexColonne; }
    
    public ElementConstitutif getElementConstitutif() { return elementConstitutif; }
    public void setElementConstitutif(ElementConstitutif elementConstitutif) { this.elementConstitutif = elementConstitutif; }
    
    public ConfigImport getConfig() { return config; }
    public void setConfig(ConfigImport config) { this.config = config; }
}