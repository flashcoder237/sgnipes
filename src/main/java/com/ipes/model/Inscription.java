package com.ipes.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Représente une inscription d'un étudiant pour une année académique
 */
public class Inscription implements Serializable {
    private Long id;
    private String anneeAcademique;
    private LocalDate dateInscription;
    private Etudiant etudiant;
    private Niveau niveau;
    private Option option;
    private String statut; // Régulier, Redoublant, etc.
    private List<Note> notes = new ArrayList<>();
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getAnneeAcademique() { return anneeAcademique; }
    public void setAnneeAcademique(String anneeAcademique) { this.anneeAcademique = anneeAcademique; }
    
    public LocalDate getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDate dateInscription) { this.dateInscription = dateInscription; }
    
    public Etudiant getEtudiant() { return etudiant; }
    public void setEtudiant(Etudiant etudiant) { this.etudiant = etudiant; }
    
    public Niveau getNiveau() { return niveau; }
    public void setNiveau(Niveau niveau) { this.niveau = niveau; }
    
    public Option getOption() { return option; }
    public void setOption(Option option) { this.option = option; }
    
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    
    public List<Note> getNotes() { return notes; }
    public void setNotes(List<Note> notes) { this.notes = notes; }
    
    public void addNote(Note note) {
        notes.add(note);
        note.setInscription(this);
    }
    
    /**
     * Calcule la moyenne générale de l'étudiant
     */
    public double calculerMoyenneGenerale() {
        double sommeCoeffs = 0;
        double sommeNotes = 0;
        
        Map<ElementConstitutif, Double> notesParEC = new HashMap<>();
        for (Note note : notes) {
            notesParEC.put(note.getElementConstitutif(), note.getValeur());
        }
        
        for (Map.Entry<ElementConstitutif, Double> entry : notesParEC.entrySet()) {
            ElementConstitutif ec = entry.getKey();
            Double note = entry.getValue();
            
            sommeCoeffs += ec.getCoefficient();
            sommeNotes += note * ec.getCoefficient();
        }
        
        return sommeCoeffs > 0 ? sommeNotes / sommeCoeffs : 0;
    }
    
    /**
     * Calcule la moyenne pour une UE spécifique
     */
    public double calculerMoyenneUE(UniteEnseignement ue) {
        double sommeCoeffs = 0;
        double sommeNotes = 0;
        
        Map<ElementConstitutif, Double> notesParEC = new HashMap<>();
        for (Note note : notes) {
            if (note.getElementConstitutif().getUnite().equals(ue)) {
                notesParEC.put(note.getElementConstitutif(), note.getValeur());
            }
        }
        
        for (Map.Entry<ElementConstitutif, Double> entry : notesParEC.entrySet()) {
            ElementConstitutif ec = entry.getKey();
            Double note = entry.getValue();
            
            sommeCoeffs += ec.getCoefficient();
            sommeNotes += note * ec.getCoefficient();
        }
        
        return sommeCoeffs > 0 ? sommeNotes / sommeCoeffs : 0;
    }
}
