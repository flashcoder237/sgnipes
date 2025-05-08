package com.ipes.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Configuration d'importation de notes
 */
public class ConfigImport implements Serializable {
    private Long id;
    private String nom;
    private String description;
    private Niveau niveau;
    private Option option;
    private List<MapColonneEC> mappings = new ArrayList<>();
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Niveau getNiveau() { return niveau; }
    public void setNiveau(Niveau niveau) { this.niveau = niveau; }
    
    public Option getOption() { return option; }
    public void setOption(Option option) { this.option = option; }
    
    public List<MapColonneEC> getMappings() { return mappings; }
    public void setMappings(List<MapColonneEC> mappings) { this.mappings = mappings; }
    
    public void addMapping(MapColonneEC mapping) {
        mappings.add(mapping);
        mapping.setConfig(this);
    }
}