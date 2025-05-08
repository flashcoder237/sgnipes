package com.ipes.service;

import com.ipes.model.*;
import com.ipes.model.Module;
import java.io.*;
import java.util.*;


/**
 * Service pour la gestion de la structure pédagogique
 */
public class StructurePedagogiqueService {
    
    // En l'absence de base de données, nous utilisons des collections en mémoire
    private static List<Cycle> cycles = new ArrayList<>();
    private static List<Niveau> niveaux = new ArrayList<>();
    private static List<Option> options = new ArrayList<>();
    private static List<Module> modules = new ArrayList<>();
    private static List<UniteEnseignement> unitesEnseignement = new ArrayList<>();
    private static List<ElementConstitutif> elementsConstitutifs = new ArrayList<>();
    
    private static Long cycleId = 1L;
    private static Long niveauId = 1L;
    private static Long optionId = 1L;
    private static Long moduleId = 1L;
    private static Long ueId = 1L;
    private static Long ecId = 1L;
    
    // CRUD pour Cycle
    public Cycle creerCycle(Cycle cycle) {
        cycle.setId(cycleId++);
        cycles.add(cycle);
        return cycle;
    }
    
    public Cycle getCycle(Long id) {
        return cycles.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }
    
    public List<Cycle> getAllCycles() {
        return new ArrayList<>(cycles);
    }
    
    public void updateCycle(Cycle cycle) {
        int index = cycles.indexOf(getCycle(cycle.getId()));
        if (index >= 0) {
            cycles.set(index, cycle);
        }
    }
    
    public void deleteCycle(Long id) {
        cycles.removeIf(c -> c.getId().equals(id));
    }
    
    // CRUD pour Niveau
    public Niveau creerNiveau(Niveau niveau) {
        niveau.setId(niveauId++);
        niveaux.add(niveau);
        return niveau;
    }
    
    public Niveau getNiveau(Long id) {
        return niveaux.stream().filter(n -> n.getId().equals(id)).findFirst().orElse(null);
    }
    
    public List<Niveau> getAllNiveaux() {
        return new ArrayList<>(niveaux);
    }
    
    public List<Niveau> getNiveauxByCycle(Long cycleId) {
        return niveaux.stream().filter(n -> n.getCycle().getId().equals(cycleId)).toList();
    }
    
    public void updateNiveau(Niveau niveau) {
        int index = niveaux.indexOf(getNiveau(niveau.getId()));
        if (index >= 0) {
            niveaux.set(index, niveau);
        }
    }
    
    public void deleteNiveau(Long id) {
        niveaux.removeIf(n -> n.getId().equals(id));
    }
    
    // CRUD pour Option
    public Option creerOption(Option option) {
        option.setId(optionId++);
        options.add(option);
        return option;
    }
    
    public Option getOption(Long id) {
        return options.stream().filter(o -> o.getId().equals(id)).findFirst().orElse(null);
    }
    
    public List<Option> getAllOptions() {
        return new ArrayList<>(options);
    }
    
    public List<Option> getOptionsByNiveau(Long niveauId) {
        return options.stream().filter(o -> o.getNiveau().getId().equals(niveauId)).toList();
    }
    
    public void updateOption(Option option) {
        int index = options.indexOf(getOption(option.getId()));
        if (index >= 0) {
            options.set(index, option);
        }
    }
    
    public void deleteOption(Long id) {
        options.removeIf(o -> o.getId().equals(id));
    }
    
    // CRUD pour Module
    public Module creerModule(Module module) {
        module.setId(moduleId++);
        modules.add(module);
        return module;
    }
    
    public Module getModule(Long id) {
        return modules.stream().filter(m -> m.getId().equals(id)).findFirst().orElse(null);
    }
    
    public List<Module> getAllModules() {
        return new ArrayList<>(modules);
    }
    
    public List<Module> getModulesByNiveau(Long niveauId) {
        return modules.stream().filter(m -> m.getNiveau().getId().equals(niveauId)).toList();
    }
    
    public void updateModule(Module module) {
        int index = modules.indexOf(getModule(module.getId()));
        if (index >= 0) {
            modules.set(index, module);
        }
    }
    
    public void deleteModule(Long id) {
        modules.removeIf(m -> m.getId().equals(id));
    }
    
    // CRUD pour Unité d'Enseignement
    public UniteEnseignement creerUniteEnseignement(UniteEnseignement ue) {
        ue.setId(ueId++);
        unitesEnseignement.add(ue);
        return ue;
    }
    
    public UniteEnseignement getUniteEnseignement(Long id) {
        return unitesEnseignement.stream().filter(ue -> ue.getId().equals(id)).findFirst().orElse(null);
    }
    
    public List<UniteEnseignement> getAllUnitesEnseignement() {
        return new ArrayList<>(unitesEnseignement);
    }
    
    public List<UniteEnseignement> getUEsByModule(Long moduleId) {
        return unitesEnseignement.stream().filter(ue -> ue.getModule() != null && ue.getModule().getId().equals(moduleId)).toList();
    }
    
    public List<UniteEnseignement> getUEsByOption(Long optionId) {
        return unitesEnseignement.stream().filter(ue -> ue.getOption() != null && ue.getOption().getId().equals(optionId)).toList();
    }
    
    public void updateUniteEnseignement(UniteEnseignement ue) {
        int index = unitesEnseignement.indexOf(getUniteEnseignement(ue.getId()));
        if (index >= 0) {
            unitesEnseignement.set(index, ue);
        }
    }
    
    public void deleteUniteEnseignement(Long id) {
        unitesEnseignement.removeIf(ue -> ue.getId().equals(id));
    }
    
    // CRUD pour Élément Constitutif
    public ElementConstitutif creerElementConstitutif(ElementConstitutif ec) {
        ec.setId(ecId++);
        elementsConstitutifs.add(ec);
        return ec;
    }
    
    public ElementConstitutif getElementConstitutif(Long id) {
        return elementsConstitutifs.stream().filter(ec -> ec.getId().equals(id)).findFirst().orElse(null);
    }
    
    public List<ElementConstitutif> getAllElementsConstitutifs() {
        return new ArrayList<>(elementsConstitutifs);
    }
    
    public List<ElementConstitutif> getECsByUE(Long ueId) {
        return elementsConstitutifs.stream().filter(ec -> ec.getUnite().getId().equals(ueId)).toList();
    }
    
    public void updateElementConstitutif(ElementConstitutif ec) {
        int index = elementsConstitutifs.indexOf(getElementConstitutif(ec.getId()));
        if (index >= 0) {
            elementsConstitutifs.set(index, ec);
        }
    }
    
    public void deleteElementConstitutif(Long id) {
        elementsConstitutifs.removeIf(ec -> ec.getId().equals(id));
    }
    
    // Méthodes pour sauvegarder et charger les données
    public void sauvegarderDonnees(String fichier) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fichier))) {
            oos.writeObject(cycles);
            oos.writeObject(niveaux);
            oos.writeObject(options);
            oos.writeObject(modules);
            oos.writeObject(unitesEnseignement);
            oos.writeObject(elementsConstitutifs);
            oos.writeLong(cycleId);
            oos.writeLong(niveauId);
            oos.writeLong(optionId);
            oos.writeLong(moduleId);
            oos.writeLong(ueId);
            oos.writeLong(ecId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
    public void chargerDonnees(String fichier) {
        File file = new File(fichier);
        if (!file.exists()) return;
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fichier))) {
            cycles = (List<Cycle>) ois.readObject();
            niveaux = (List<Niveau>) ois.readObject();
            options = (List<Option>) ois.readObject();
            modules = (List<Module>) ois.readObject();
            unitesEnseignement = (List<UniteEnseignement>) ois.readObject();
            elementsConstitutifs = (List<ElementConstitutif>) ois.readObject();
            cycleId = ois.readLong();
            niveauId = ois.readLong();
            optionId = ois.readLong();
            moduleId = ois.readLong();
            ueId = ois.readLong();
            ecId = ois.readLong();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}