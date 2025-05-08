package com.ipes.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;



/**
 * Représente une Unité d'Enseignement (UE)
 */
public class UniteEnseignement implements Serializable {
    private Long id;
    private String code;
    private String intitule;
    private double coefficient;
    private int credits;
    private Module module;
    private Option option;
    private List<ElementConstitutif> elements = new ArrayList<>();
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getIntitule() { return intitule; }
    public void setIntitule(String intitule) { this.intitule = intitule; }
    
    public double getCoefficient() { return coefficient; }
    public void setCoefficient(double coefficient) { this.coefficient = coefficient; }
    
    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }
    
    public Module getModule() { return module; }
    public void setModule(Module module) { this.module = module; }
    
    public Option getOption() { return option; }
    public void setOption(Option option) { this.option = option; }
    
    public List<ElementConstitutif> getElements() { return elements; }
    public void setElements(List<ElementConstitutif> elements) { this.elements = elements; }
    
    public void addElementConstitutif(ElementConstitutif element) {
        elements.add(element);
        element.setUnite(this);
    }
    
    @Override
    public String toString() {
        return code + " - " + intitule;
    }
}

