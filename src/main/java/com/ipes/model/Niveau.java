package com.ipes.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Représente un niveau d'études (L1, L2, L3, etc.)
 */
public class Niveau implements Serializable {
    private Long id;
    private String code;
    private String intitule;
    private int annee; // 1, 2, 3, etc.
    private Cycle cycle;
    private List<Option> options = new ArrayList<>();
    private List<Module> modules = new ArrayList<>();
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getIntitule() { return intitule; }
    public void setIntitule(String intitule) { this.intitule = intitule; }
    
    public int getAnnee() { return annee; }
    public void setAnnee(int annee) { this.annee = annee; }
    
    public Cycle getCycle() { return cycle; }
    public void setCycle(Cycle cycle) { this.cycle = cycle; }
    
    public List<Option> getOptions() { return options; }
    public void setOptions(List<Option> options) { this.options = options; }
    
    public List<Module> getModules() { return modules; }
    public void setModules(List<Module> modules) { this.modules = modules; }
    
    public void addOption(Option option) {
        options.add(option);
        option.setNiveau(this);
    }
    
    public void addModule(Module module) {
        modules.add(module);
        module.setNiveau(this);
    }
    
    @Override
    public String toString() {
        return intitule;
    }
}
