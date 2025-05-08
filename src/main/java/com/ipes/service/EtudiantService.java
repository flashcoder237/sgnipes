package com.ipes.service;

import com.ipes.model.*;
import java.io.*;
import java.util.*;

/**
 * Service pour la gestion des étudiants et des notes
 */
public class EtudiantService {
    
    // En l'absence de base de données, nous utilisons des collections en mémoire
    private static List<Etudiant> etudiants = new ArrayList<>();
    private static List<Inscription> inscriptions = new ArrayList<>();
    private static List<Note> notes = new ArrayList<>();
    
    private static Long etudiantId = 1L;
    private static Long inscriptionId = 1L;
    private static Long noteId = 1L;
    
    // CRUD pour Etudiant
    public Etudiant creerEtudiant(Etudiant etudiant) {
        etudiant.setId(etudiantId++);
        etudiants.add(etudiant);
        return etudiant;
    }
    
    public Etudiant getEtudiant(Long id) {
        return etudiants.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
    }
    
    public Etudiant getEtudiantByMatricule(String matricule) {
        return etudiants.stream().filter(e -> e.getMatricule().equals(matricule)).findFirst().orElse(null);
    }
    
    public List<Etudiant> getAllEtudiants() {
        return new ArrayList<>(etudiants);
    }
    
    public void updateEtudiant(Etudiant etudiant) {
        int index = etudiants.indexOf(getEtudiant(etudiant.getId()));
        if (index >= 0) {
            etudiants.set(index, etudiant);
        }
    }
    
    public void deleteEtudiant(Long id) {
        etudiants.removeIf(e -> e.getId().equals(id));
    }
    
    // CRUD pour Inscription
    public Inscription creerInscription(Inscription inscription) {
        inscription.setId(inscriptionId++);
        inscriptions.add(inscription);
        return inscription;
    }
    
    public Inscription getInscription(Long id) {
        return inscriptions.stream().filter(i -> i.getId().equals(id)).findFirst().orElse(null);
    }
    
    public List<Inscription> getAllInscriptions() {
        return new ArrayList<>(inscriptions);
    }
    
    public List<Inscription> getInscriptionsByEtudiant(Long etudiantId) {
        return inscriptions.stream().filter(i -> i.getEtudiant().getId().equals(etudiantId)).toList();
    }
    
    public List<Inscription> getInscriptionsByNiveauOption(Long niveauId, Long optionId, String anneeAcademique) {
        return inscriptions.stream()
            .filter(i -> i.getNiveau().getId().equals(niveauId) &&
                    i.getOption().getId().equals(optionId) &&
                    i.getAnneeAcademique().equals(anneeAcademique))
            .toList();
    }
    
    public void updateInscription(Inscription inscription) {
        int index = inscriptions.indexOf(getInscription(inscription.getId()));
        if (index >= 0) {
            inscriptions.set(index, inscription);
        }
    }
    
    public void deleteInscription(Long id) {
        inscriptions.removeIf(i -> i.getId().equals(id));
    }
    
    // CRUD pour Note
    public Note creerNote(Note note) {
        note.setId(noteId++);
        notes.add(note);
        return note;
    }
    
    public Note getNote(Long id) {
        return notes.stream().filter(n -> n.getId().equals(id)).findFirst().orElse(null);
    }
    
    public List<Note> getAllNotes() {
        return new ArrayList<>(notes);
    }
    
    public List<Note> getNotesByInscription(Long inscriptionId) {
        return notes.stream().filter(n -> n.getInscription().getId().equals(inscriptionId)).toList();
    }
    
    public Note getNoteByInscriptionAndEC(Long inscriptionId, Long ecId) {
        return notes.stream()
            .filter(n -> n.getInscription().getId().equals(inscriptionId) && 
                    n.getElementConstitutif().getId().equals(ecId))
            .findFirst()
            .orElse(null);
    }
    
    public void updateNote(Note note) {
        int index = notes.indexOf(getNote(note.getId()));
        if (index >= 0) {
            notes.set(index, note);
        }
    }
    
    public void deleteNote(Long id) {
        notes.removeIf(n -> n.getId().equals(id));
    }
    
    /**
     * Calcule les statistiques de réussite pour un niveau/option
     */
    public Map<String, Object> calculerStatistiques(Long niveauId, Long optionId, String anneeAcademique) {
        List<Inscription> inscriptionsFiltered = getInscriptionsByNiveauOption(niveauId, optionId, anneeAcademique);
        
        int nbEtudiants = inscriptionsFiltered.size();
        int nbReussite = 0;
        double moyenneGenerale = 0;
        
        for (Inscription inscription : inscriptionsFiltered) {
            double moyenne = inscription.calculerMoyenneGenerale();
            moyenneGenerale += moyenne;
            
            if (moyenne >= 10) {
                nbReussite++;
            }
        }
        
        if (nbEtudiants > 0) {
            moyenneGenerale /= nbEtudiants;
        }
        
        double tauxReussite = nbEtudiants > 0 ? (double) nbReussite / nbEtudiants * 100 : 0;
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("nbEtudiants", nbEtudiants);
        stats.put("nbReussite", nbReussite);
        stats.put("moyenneGenerale", moyenneGenerale);
        stats.put("tauxReussite", tauxReussite);
        
        return stats;
    }
    
    /**
     * Calcule les statistiques pour une UE spécifique
     */
    public Map<String, Object> calculerStatistiquesUE(UniteEnseignement ue, Long niveauId, Long optionId, String anneeAcademique) {
        List<Inscription> inscriptionsFiltered = getInscriptionsByNiveauOption(niveauId, optionId, anneeAcademique);
        
        int nbEtudiants = inscriptionsFiltered.size();
        int nbReussite = 0;
        double moyenneUE = 0;
        
        for (Inscription inscription : inscriptionsFiltered) {
            double moyenne = inscription.calculerMoyenneUE(ue);
            moyenneUE += moyenne;
            
            if (moyenne >= 10) {
                nbReussite++;
            }
        }
        
        if (nbEtudiants > 0) {
            moyenneUE /= nbEtudiants;
        }
        
        double tauxReussite = nbEtudiants > 0 ? (double) nbReussite / nbEtudiants * 100 : 0;
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("nbEtudiants", nbEtudiants);
        stats.put("nbReussite", nbReussite);
        stats.put("moyenneUE", moyenneUE);
        stats.put("tauxReussite", tauxReussite);
        
        return stats;
    }
    
    // Méthodes pour sauvegarder et charger les données
    public void sauvegarderDonnees(String fichier) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fichier))) {
            oos.writeObject(etudiants);
            oos.writeObject(inscriptions);
            oos.writeObject(notes);
            oos.writeLong(etudiantId);
            oos.writeLong(inscriptionId);
            oos.writeLong(noteId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
    public void chargerDonnees(String fichier) {
        File file = new File(fichier);
        if (!file.exists()) return;
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fichier))) {
            etudiants = (List<Etudiant>) ois.readObject();
            inscriptions = (List<Inscription>) ois.readObject();
            notes = (List<Note>) ois.readObject();
            etudiantId = ois.readLong();
            inscriptionId = ois.readLong();
            noteId = ois.readLong();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}