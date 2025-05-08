package com.ipes.controller;

import com.ipes.model.*;
import com.ipes.model.Module;
import com.ipes.service.*;
import com.ipes.util.ApplicationContext;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.*;
import java.util.*;

/**
 * Contrôleur pour l'importation des notes depuis un fichier CSV
 */
public class ImportNotesController implements Refreshable {
    
    // Services
    private StructurePedagogiqueService structureService;
    private EtudiantService etudiantService;
    private DocumentService documentService;
    
    // Composants de l'interface
    @FXML private TextField fichierTextField;
    @FXML private TextArea aperçuTextArea;
    
    @FXML private ComboBox<String> anneeAcademiqueCombo;
    @FXML private ComboBox<ConfigImport> configCombo;
    @FXML private ComboBox<Cycle> cycleCombo;
    @FXML private TextField nomConfigTextField;
    @FXML private ComboBox<Niveau> niveauCombo;
    @FXML private CheckBox nouvelleCfgCheckBox;
    @FXML private ComboBox<Option> optionCombo;
    
    @FXML private ComboBox<String> colonneMatriculeCombo;
    @FXML private TableView<MappingColonneECWrapper> mappingTable;
    @FXML private TableColumn<MappingColonneECWrapper, Integer> indexColonneColumn;
    @FXML private TableColumn<MappingColonneECWrapper, String> nomColonneColumn;
    @FXML private TableColumn<MappingColonneECWrapper, ElementConstitutif> elementConstitutifColumn;
    @FXML private TableColumn<MappingColonneECWrapper, String> actionsColumn;
    
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;
    
    // Données
    private File fichierCsv;
    private List<String[]> donneesCsv = new ArrayList<>();
    private String[] enTetesCsv;
    private ObservableList<MappingColonneECWrapper> mappings = FXCollections.observableArrayList();
    
    // Propriétés
    private BooleanProperty fichierValide = new SimpleBooleanProperty(false);
    private BooleanProperty configValide = new SimpleBooleanProperty(false);
    private BooleanProperty nouvelleCfg = new SimpleBooleanProperty(false);
    
    /**
     * Initialise le contrôleur
     */
    @FXML
    public void initialize() {
        // Récupérer les services
        structureService = ApplicationContext.getInstance().getService("structureService");
        etudiantService = ApplicationContext.getInstance().getService("etudiantService");
        documentService = ApplicationContext.getInstance().getService("documentService");
        
        // Initialiser les combobox
        initAnneeAcademiqueCombo();
        initConfigCombo();
        initCycleCombo();
        
        // Initialiser le tableau de mapping
        initMappingTable();
        
        // Configurer les listeners pour les combobox
        configCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                chargerConfig(newVal);
            } else {
                reinitialiserMapping();
            }
        });
        
        cycleCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadNiveaux(newVal);
            } else {
                niveauCombo.getItems().clear();
                niveauCombo.setDisable(true);
                optionCombo.getItems().clear();
                optionCombo.setDisable(true);
            }
        });
        
        niveauCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadOptions(newVal);
                
                // Réinitialiser le mapping si le niveau change
                reinitialiserMapping();
                
                // Charger les configurations disponibles pour ce niveau
                if (niveauCombo.getValue() != null) {
                    loadConfigs(niveauCombo.getValue(), optionCombo.getValue());
                }
            } else {
                optionCombo.getItems().clear();
                optionCombo.setDisable(true);
            }
        });
        
        optionCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            // Réinitialiser le mapping si l'option change
            reinitialiserMapping();
            
            // Charger les configurations disponibles pour cette option
            if (niveauCombo.getValue() != null) {
                loadConfigs(niveauCombo.getValue(), newVal);
            }
        });
        
        // Lier les propriétés aux contrôles
        nomConfigTextField.disableProperty().bind(nouvelleCfg.not());
    }
    
    /**
     * Initialise le combo des années académiques
     */
    private void initAnneeAcademiqueCombo() {
        // Générer les 5 dernières années académiques
        List<String> anneesAcademiques = new ArrayList<>();
        int anneeActuelle = java.time.LocalDate.now().getYear();
        
        for (int i = 0; i < 5; i++) {
            int debut = anneeActuelle - i;
            int fin = debut + 1;
            anneesAcademiques.add(debut + "-" + fin);
        }
        
        anneeAcademiqueCombo.setItems(FXCollections.observableArrayList(anneesAcademiques));
        anneeAcademiqueCombo.getSelectionModel().selectFirst();
    }
    
    /**
     * Initialise le combo des configurations d'importation
     */
    private void initConfigCombo() {
        configCombo.setItems(FXCollections.observableArrayList());
        
        // Configurer l'affichage des configurations
        configCombo.setConverter(new StringConverter<ConfigImport>() {
            @Override
            public String toString(ConfigImport config) {
                return config != null ? config.getNom() : null;
            }
            
            @Override
            public ConfigImport fromString(String string) {
                return null; // Not used
            }
        });
    }
    
    /**
     * Initialise le combo des cycles
     */
    private void initCycleCombo() {
        cycleCombo.setItems(FXCollections.observableArrayList(structureService.getAllCycles()));
        if (!cycleCombo.getItems().isEmpty()) {
            cycleCombo.getSelectionModel().selectFirst();
            loadNiveaux(cycleCombo.getValue());
        }
    }
    
    /**
     * Charge les niveaux pour un cycle
     */
    private void loadNiveaux(Cycle cycle) {
        niveauCombo.setDisable(false);
        niveauCombo.setItems(FXCollections.observableArrayList(
            structureService.getNiveauxByCycle(cycle.getId())
        ));
        
        if (!niveauCombo.getItems().isEmpty()) {
            niveauCombo.getSelectionModel().selectFirst();
            loadOptions(niveauCombo.getValue());
        } else {
            niveauCombo.setDisable(true);
            optionCombo.getItems().clear();
            optionCombo.setDisable(true);
        }
    }
    
    /**
     * Charge les options pour un niveau
     */
    private void loadOptions(Niveau niveau) {
        optionCombo.setDisable(false);
        optionCombo.setItems(FXCollections.observableArrayList(
            structureService.getOptionsByNiveau(niveau.getId())
        ));
        
        if (!optionCombo.getItems().isEmpty()) {
            optionCombo.getSelectionModel().selectFirst();
        } else {
            optionCombo.setDisable(true);
        }
    }
    
    /**
     * Charge les configurations d'importation disponibles pour un niveau et une option
     */
    private void loadConfigs(Niveau niveau, Option option) {
        Long optionId = option != null ? option.getId() : null;
        
        List<ConfigImport> configs;
        if (optionId != null) {
            configs = documentService.getConfigsImportByNiveauOption(niveau.getId(), optionId);
        } else {
            configs = documentService.getConfigsImportByNiveauOption(niveau.getId(), 0L);
        }
        
        configCombo.setItems(FXCollections.observableArrayList(configs));
        
        if (!configCombo.getItems().isEmpty()) {
            configCombo.getSelectionModel().selectFirst();
        } else {
            configCombo.getSelectionModel().clearSelection();
        }
    }
    
    /**
     * Initialise le tableau de mapping
     */
    private void initMappingTable() {
        // Configurer les colonnes
        indexColonneColumn.setCellValueFactory(new PropertyValueFactory<>("indexColonne"));
        nomColonneColumn.setCellValueFactory(new PropertyValueFactory<>("nomColonne"));
        elementConstitutifColumn.setCellValueFactory(new PropertyValueFactory<>("elementConstitutif"));
        
        // Configurer l'affichage des ECs
        elementConstitutifColumn.setCellFactory(column -> new TableCell<MappingColonneECWrapper, ElementConstitutif>() {
            private final ComboBox<ElementConstitutif> comboBox = new ComboBox<>();
            
            {
                comboBox.setConverter(new StringConverter<ElementConstitutif>() {
                    @Override
                    public String toString(ElementConstitutif ec) {
                        return ec != null ? ec.getCode() + " - " + ec.getIntitule() : null;
                    }
                    
                    @Override
                    public ElementConstitutif fromString(String string) {
                        return null; // Not used
                    }
                });
                
                comboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (isEditing()) {
                        commitEdit(newVal);
                    }
                });
            }
            
            @Override
            protected void updateItem(ElementConstitutif ec, boolean empty) {
                super.updateItem(ec, empty);
                
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Charger les ECs disponibles
                    Niveau niveau = niveauCombo.getValue();
                    Option option = optionCombo.getValue();
                    
                    if (niveau != null) {
                        List<ElementConstitutif> ecs = new ArrayList<>();
                        
                        // Ajouter les ECs des UEs du niveau
                        for (Module module : niveau.getModules()) {
                            for (UniteEnseignement ue : module.getUnites()) {
                                ecs.addAll(ue.getElements());
                            }
                        }
                        
                        // Ajouter les ECs des UEs de l'option
                        if (option != null) {
                            for (UniteEnseignement ue : option.getUnites()) {
                                ecs.addAll(ue.getElements());
                            }
                        }
                        
                        comboBox.setItems(FXCollections.observableArrayList(ecs));
                    }
                    
                    comboBox.setValue(ec);
                    setGraphic(comboBox);
                    setText(null);
                }
            }
            
            @Override
            public void startEdit() {
                super.startEdit();
                setGraphic(comboBox);
                setText(null);
            }
            
            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(comboBox.getValue() != null ? comboBox.getValue().toString() : null);
            }
            
            @Override
            public void commitEdit(ElementConstitutif ec) {
                super.commitEdit(ec);
                
                // Mettre à jour l'élément
                MappingColonneECWrapper wrapper = getTableView().getItems().get(getIndex());
                wrapper.setElementConstitutif(ec);
                
                // Vérifier si la configuration est valide
                verifierConfigValide();
            }
        });
        
        // Configurer les actions
        actionsColumn.setCellFactory(column -> new TableCell<MappingColonneECWrapper, String>() {
            private final Button supprimerButton = new Button("Supprimer");
            
            {
                supprimerButton.setOnAction(event -> {
                    MappingColonneECWrapper wrapper = getTableView().getItems().get(getIndex());
                    mappings.remove(wrapper);
                    
                    // Vérifier si la configuration est valide
                    verifierConfigValide();
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(supprimerButton);
                }
            }
        });
        
        // Définir la source de données
        mappingTable.setItems(mappings);
    }
    
    /**
     * Charge une configuration d'importation
     */
    private void chargerConfig(ConfigImport config) {
        // Mettre à jour les sélections
        if (config.getNiveau() != null) {
            Cycle cycle = config.getNiveau().getCycle();
            if (cycle != null) {
                cycleCombo.getSelectionModel().select(cycle);
                loadNiveaux(cycle);
            }
            
            niveauCombo.getSelectionModel().select(config.getNiveau());
            loadOptions(config.getNiveau());
        }
        
        if (config.getOption() != null) {
            optionCombo.getSelectionModel().select(config.getOption());
        }
        
        // Réinitialiser le mapping
        reinitialiserMapping();
        
        // Ajouter les mappings de la configuration
        for (MapColonneEC mapping : config.getMappings()) {
            MappingColonneECWrapper wrapper = new MappingColonneECWrapper();
            wrapper.setIndexColonne(mapping.getIndexColonne());
            wrapper.setNomColonne(mapping.getNomColonne());
            wrapper.setElementConstitutif(mapping.getElementConstitutif());
            
            mappings.add(wrapper);
        }
        
        // Désactiver la nouvelle configuration
        nouvelleCfg.set(false);
        
        // Vérifier si la configuration est valide
        verifierConfigValide();
    }
    
    /**
     * Sélectionne un fichier CSV à importer
     */
    @FXML
    private void handleParcourir() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
        
        fichierCsv = fileChooser.showOpenDialog(null);
        
        if (fichierCsv != null) {
            fichierTextField.setText(fichierCsv.getAbsolutePath());
            
            // Charger les en-têtes et les premières lignes
            chargerAperçuCsv();
            
            // Mettre à jour le combo des colonnes matricule
            if (enTetesCsv != null) {
                colonneMatriculeCombo.setItems(FXCollections.observableArrayList(enTetesCsv));
                
                // Essayer de détecter automatiquement la colonne matricule
                for (int i = 0; i < enTetesCsv.length; i++) {
                    String enTete = enTetesCsv[i].toLowerCase();
                    if (enTete.contains("matricule") || enTete.contains("mat") || enTete.equals("id")) {
                        colonneMatriculeCombo.getSelectionModel().select(i);
                        break;
                    }
                }
                
                fichierValide.set(true);
            }
        }
    }
    
    /**
     * Charge les en-têtes et les premières lignes du fichier CSV
     */
    private void chargerAperçuCsv() {
        donneesCsv.clear();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(fichierCsv))) {
            // Lire les en-têtes
            String ligne = reader.readLine();
            if (ligne != null) {
                enTetesCsv = ligne.split(",");
                donneesCsv.add(enTetesCsv);
                
                // Lire les 5 premières lignes pour l'aperçu
                for (int i = 0; i < 5 && (ligne = reader.readLine()) != null; i++) {
                    donneesCsv.add(ligne.split(","));
                }
                
                // Afficher l'aperçu
                afficherAperçuCsv();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de lecture",
                    "Impossible de lire le fichier CSV : " + e.getMessage());
        }
    }
    
    /**
     * Affiche l'aperçu du fichier CSV
     */
    private void afficherAperçuCsv() {
        StringBuilder sb = new StringBuilder();
        
        for (String[] ligne : donneesCsv) {
            for (int i = 0; i < ligne.length; i++) {
                sb.append(ligne[i]);
                if (i < ligne.length - 1) {
                    sb.append(" | ");
                }
            }
            sb.append("\n");
        }
        
        aperçuTextArea.setText(sb.toString());
    }
    
    /**
     * Toggle la nouvelle configuration
     */
    @FXML
    private void handleNouvelleCfg() {
        nouvelleCfg.set(nouvelleCfgCheckBox.isSelected());
        
        if (nouvelleCfg.get()) {
            configCombo.getSelectionModel().clearSelection();
        }
    }
    
    /**
     * Tente de détecter automatiquement les correspondances entre les colonnes et les ECs
     */
    @FXML
    private void handleDetection() {
        if (enTetesCsv == null || enTetesCsv.length == 0) {
            showAlert(Alert.AlertType.WARNING, "Détection", "Aucun fichier",
                    "Veuillez d'abord sélectionner un fichier CSV.");
            return;
        }
        
        // Vérifier que la colonne matricule est sélectionnée
        if (colonneMatriculeCombo.getSelectionModel().getSelectedIndex() < 0) {
            showAlert(Alert.AlertType.WARNING, "Détection", "Colonne matricule non sélectionnée",
                    "Veuillez sélectionner la colonne contenant les matricules des étudiants.");
            return;
        }
        
        // Récupérer les ECs disponibles
        List<ElementConstitutif> ecs = new ArrayList<>();
        Niveau niveau = niveauCombo.getValue();
        Option option = optionCombo.getValue();
        
        if (niveau != null) {
            // Ajouter les ECs des UEs du niveau
            for (Module module : niveau.getModules()) {
                for (UniteEnseignement ue : module.getUnites()) {
                    ecs.addAll(ue.getElements());
                }
            }
            
            // Ajouter les ECs des UEs de l'option
            if (option != null) {
                for (UniteEnseignement ue : option.getUnites()) {
                    ecs.addAll(ue.getElements());
                }
            }
        }
        
        if (ecs.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Détection", "Aucun EC disponible",
                    "Il n'y a pas d'éléments constitutifs définis pour ce niveau/option.");
            return;
        }
        
        // Vider le mapping existant
        mappings.clear();
        
        // Parcourir les en-têtes et essayer de faire correspondre avec les ECs
        for (int i = 0; i < enTetesCsv.length; i++) {
            // Ignorer la colonne matricule
            if (i == colonneMatriculeCombo.getSelectionModel().getSelectedIndex()) {
                continue;
            }
            
            String enTete = enTetesCsv[i].toLowerCase().trim();
            
            // Essayer de trouver un EC correspondant
            ElementConstitutif ecCorrespondant = null;
            
            for (ElementConstitutif ec : ecs) {
                String codeEC = ec.getCode().toLowerCase();
                String intituleEC = ec.getIntitule().toLowerCase();
                
                if (enTete.equals(codeEC) || enTete.contains(codeEC) || 
                    enTete.equals(intituleEC) || enTete.contains(intituleEC)) {
                    ecCorrespondant = ec;
                    break;
                }
            }
            
            // Si une correspondance est trouvée, ajouter au mapping
            if (ecCorrespondant != null) {
                MappingColonneECWrapper wrapper = new MappingColonneECWrapper();
                wrapper.setIndexColonne(i);
                wrapper.setNomColonne(enTetesCsv[i]);
                wrapper.setElementConstitutif(ecCorrespondant);
                
                mappings.add(wrapper);
            }
        }
        
        // Vérifier si la configuration est valide
        verifierConfigValide();
        
        // Informer l'utilisateur du résultat
        showAlert(Alert.AlertType.INFORMATION, "Détection", "Détection terminée",
                mappings.size() + " correspondances ont été détectées.");
    }
    
    /**
     * Réinitialise le mapping des colonnes
     */
    @FXML
    private void handleReinitialiserMapping() {
        reinitialiserMapping();
    }
    
    /**
     * Réinitialise le mapping des colonnes (méthode interne)
     */
    private void reinitialiserMapping() {
        mappings.clear();
        configValide.set(false);
    }
    
    /**
     * Vérifie si la configuration est valide
     */
    private void verifierConfigValide() {
        // La configuration est valide s'il y a au moins un mapping
        configValide.set(!mappings.isEmpty());
    }
    
    /**
     * Enregistre la configuration d'importation
     */
    @FXML
    private void handleEnregistrerConfig() {
        if (mappings.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Enregistrement", "Aucun mapping",
                    "Il n'y a pas de mappings à enregistrer.");
            return;
        }
        
        Niveau niveau = niveauCombo.getValue();
        Option option = optionCombo.getValue();
        
        if (niveau == null) {
            showAlert(Alert.AlertType.WARNING, "Enregistrement", "Niveau non sélectionné",
                    "Veuillez sélectionner un niveau d'études.");
            return;
        }
        
        // Vérifier si c'est une nouvelle configuration
        ConfigImport config;
        
        if (nouvelleCfg.get()) {
            // Vérifier que le nom de la configuration est renseigné
            String nomConfig = nomConfigTextField.getText().trim();
            if (nomConfig.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Enregistrement", "Nom manquant",
                        "Veuillez saisir un nom pour la nouvelle configuration.");
                return;
            }
            
            // Créer une nouvelle configuration
            config = new ConfigImport();
            config.setNom(nomConfig);
            config.setDescription("Configuration créée le " + new java.util.Date());
            config.setNiveau(niveau);
            config.setOption(option);
        } else {
            // Utiliser la configuration existante
            config = configCombo.getValue();
            
            if (config == null) {
                // Si aucune configuration n'est sélectionnée, créer une nouvelle
                config = new ConfigImport();
                config.setNom("Configuration " + new java.util.Date());
                config.setDescription("Configuration créée le " + new java.util.Date());
                config.setNiveau(niveau);
                config.setOption(option);
            }
            
            // Vider les mappings existants
            config.getMappings().clear();
        }
        
        // Ajouter les nouveaux mappings
        for (MappingColonneECWrapper wrapper : mappings) {
            MapColonneEC mapping = new MapColonneEC();
            mapping.setIndexColonne(wrapper.getIndexColonne());
            mapping.setNomColonne(wrapper.getNomColonne());
            mapping.setElementConstitutif(wrapper.getElementConstitutif());
            
            config.addMapping(mapping);
        }
        
        // Enregistrer la configuration
        if (config.getId() == null) {
            config = documentService.creerConfigImport(config);
        } else {
            documentService.updateConfigImport(config);
        }
        
        // Mettre à jour la liste des configurations
        loadConfigs(niveau, option);
        
        // Sélectionner la configuration enregistrée
        configCombo.getSelectionModel().select(config);
        
        // Désactiver la nouvelle configuration
        nouvelleCfg.set(false);
        nouvelleCfgCheckBox.setSelected(false);
        
        // Informer l'utilisateur
        showAlert(Alert.AlertType.INFORMATION, "Enregistrement", "Configuration enregistrée",
                "La configuration a été enregistrée avec succès.");
    }
    
    /**
     * Affiche un aperçu de l'importation
     */
    @FXML
    private void handleAperçuImport() {
        if (!fichierValide.get()) {
            showAlert(Alert.AlertType.WARNING, "Aperçu", "Aucun fichier",
                    "Veuillez d'abord sélectionner un fichier CSV valide.");
            return;
        }
        
        if (mappings.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aperçu", "Aucun mapping",
                    "Veuillez définir au moins un mapping entre colonnes et éléments constitutifs.");
            return;
        }
        
        // Vérifier que la colonne matricule est sélectionnée
        int indexMatricule = colonneMatriculeCombo.getSelectionModel().getSelectedIndex();
        if (indexMatricule < 0) {
            showAlert(Alert.AlertType.WARNING, "Aperçu", "Colonne matricule non sélectionnée",
                    "Veuillez sélectionner la colonne contenant les matricules des étudiants.");
            return;
        }
        
        try {
            // Lire le fichier CSV
            List<String[]> donneesCompletes = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(fichierCsv))) {
                // Lire les en-têtes
                String ligne = reader.readLine();
                if (ligne != null) {
                    donneesCompletes.add(ligne.split(","));
                    
                    // Lire les données
                    while ((ligne = reader.readLine()) != null) {
                        donneesCompletes.add(ligne.split(","));
                    }
                }
            }
            
            if (donneesCompletes.size() <= 1) {
                showAlert(Alert.AlertType.WARNING, "Aperçu", "Aucune donnée",
                        "Le fichier CSV ne contient pas de données.");
                return;
            }
            
            // Préparer l'aperçu
            StringBuilder sb = new StringBuilder();
            sb.append("Aperçu de l'importation :\n\n");
            
            // Afficher les en-têtes
            String[] enTetes = donneesCompletes.get(0);
            sb.append("Matricule | ");
            
            for (MappingColonneECWrapper mapping : mappings) {
                sb.append(mapping.getElementConstitutif().getCode()).append(" | ");
            }
            sb.append("\n");
            
            // Afficher les 10 premières lignes de données
            int lignesAffichees = Math.min(10, donneesCompletes.size() - 1);
            for (int i = 1; i <= lignesAffichees; i++) {
                String[] donnees = donneesCompletes.get(i);
                
                // Vérifier que l'index matricule est valide
                if (indexMatricule < donnees.length) {
                    sb.append(donnees[indexMatricule]).append(" | ");
                    
                    // Afficher les valeurs pour chaque mapping
                    for (MappingColonneECWrapper mapping : mappings) {
                        int indexColonne = mapping.getIndexColonne();
                        if (indexColonne < donnees.length) {
                            sb.append(donnees[indexColonne]).append(" | ");
                        } else {
                            sb.append("N/A | ");
                        }
                    }
                    sb.append("\n");
                }
            }
            
            // Afficher le nombre total de lignes
            sb.append("\nTotal : ").append(donneesCompletes.size() - 1).append(" lignes");
            
            // Afficher l'aperçu
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Aperçu de l'importation");
            alert.setHeaderText("Aperçu des données à importer");
            
            TextArea textArea = new TextArea(sb.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefWidth(600);
            textArea.setPrefHeight(400);
            
            alert.getDialogPane().setContent(textArea);
            alert.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de lecture",
                    "Impossible de lire le fichier CSV : " + e.getMessage());
        }
    }
    
    /**
     * Importe les notes depuis le fichier CSV
     */
    @FXML
    private void handleImporter() {
        if (!fichierValide.get()) {
            showAlert(Alert.AlertType.WARNING, "Importation", "Aucun fichier",
                    "Veuillez d'abord sélectionner un fichier CSV valide.");
            return;
        }
        
        if (!configValide.get()) {
            showAlert(Alert.AlertType.WARNING, "Importation", "Configuration invalide",
                    "Veuillez définir une configuration d'importation valide.");
            return;
        }
        
        // Vérifier que la colonne matricule est sélectionnée
        int indexMatricule = colonneMatriculeCombo.getSelectionModel().getSelectedIndex();
        if (indexMatricule < 0) {
            showAlert(Alert.AlertType.WARNING, "Importation", "Colonne matricule non sélectionnée",
                    "Veuillez sélectionner la colonne contenant les matricules des étudiants.");
            return;
        }
        
        // Récupérer les paramètres d'importation
        String anneeAcademique = anneeAcademiqueCombo.getValue();
        Niveau niveau = niveauCombo.getValue();
        Option option = optionCombo.getValue();
        
        if (niveau == null) {
            showAlert(Alert.AlertType.WARNING, "Importation", "Niveau non sélectionné",
                    "Veuillez sélectionner un niveau d'études.");
            return;
        }
        
        // Créer ou récupérer la configuration
        final ConfigImport configToUse; // Changed to final variable
        
        if (nouvelleCfg.get()) {
            // Créer une nouvelle configuration
            String nomConfig = nomConfigTextField.getText().trim();
            if (nomConfig.isEmpty()) {
                nomConfig = "Import " + new java.util.Date();
            }
            
            ConfigImport newConfig = new ConfigImport(); // Using a temporary variable
            newConfig.setNom(nomConfig);
            newConfig.setDescription("Configuration créée le " + new java.util.Date());
            newConfig.setNiveau(niveau);
            newConfig.setOption(option);
            
            // Ajouter les mappings
            for (MappingColonneECWrapper wrapper : mappings) {
                MapColonneEC mapping = new MapColonneEC();
                mapping.setIndexColonne(wrapper.getIndexColonne());
                mapping.setNomColonne(wrapper.getNomColonne());
                mapping.setElementConstitutif(wrapper.getElementConstitutif());
                
                newConfig.addMapping(mapping);
            }
            
            // Enregistrer la configuration
            configToUse = documentService.creerConfigImport(newConfig);
        } else {
            // Utiliser la configuration existante ou créer une temporaire
            if (configCombo.getValue() != null) {
                configToUse = configCombo.getValue();
            } else {
                ConfigImport tempConfig = new ConfigImport(); // Using a temporary variable
                tempConfig.setNom("Import temporaire");
                tempConfig.setNiveau(niveau);
                tempConfig.setOption(option);
                
                // Ajouter les mappings
                for (MappingColonneECWrapper wrapper : mappings) {
                    MapColonneEC mapping = new MapColonneEC();
                    mapping.setIndexColonne(wrapper.getIndexColonne());
                    mapping.setNomColonne(wrapper.getNomColonne());
                    mapping.setElementConstitutif(wrapper.getElementConstitutif());
                    
                    tempConfig.addMapping(mapping);
                }
                
                // Enregistrer la configuration temporaire
                configToUse = documentService.creerConfigImport(tempConfig);
            }
        }
        
        // Capture final variables for use in the task
        final String finalAnneeAcademique = anneeAcademique;
        
        // Préparer la barre de progression
        progressBar.setVisible(true);
        progressLabel.setVisible(true);
        progressBar.setProgress(0);
        
        // Créer une tâche en arrière-plan pour importer les notes
        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                return documentService.importerNotes(fichierCsv, configToUse.getId(), finalAnneeAcademique);
            }
        };
        
        // Lier la barre de progression à la tâche
        progressBar.progressProperty().bind(task.progressProperty());
        progressLabel.textProperty().bind(task.messageProperty());
        
        // Gérer la fin de la tâche
        task.setOnSucceeded(event -> {
            Integer nombreNotesImportees = task.getValue();
            progressBar.progressProperty().unbind();
            progressBar.setProgress(1);
            progressLabel.textProperty().unbind();
            progressLabel.setText("Importation terminée : " + nombreNotesImportees + " notes importées");
            
            showAlert(Alert.AlertType.INFORMATION, "Importation terminée", 
                    "Notes importées avec succès",
                    nombreNotesImportees + " notes ont été importées avec succès.");
        });
        
        task.setOnFailed(event -> {
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
            progressLabel.textProperty().unbind();
            progressLabel.setText("Erreur lors de l'importation des notes");
            
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur d'importation",
                    "Une erreur est survenue lors de l'importation des notes : " + 
                            task.getException().getMessage());
        });
        
        // Démarrer la tâche
        new Thread(task).start();
    }
    /**
     * Affiche une boîte de dialogue
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Actualise la vue
     */
    @Override
    public void refresh() {
        // Rafraîchir les données
        initCycleCombo();
        mappings.clear();
        configValide.set(false);
        fichierValide.set(false);
        nouvelleCfg.set(false);
        nouvelleCfgCheckBox.setSelected(false);
        fichierTextField.clear();
        aperçuTextArea.clear();
    }
    
    /**
     * Classe wrapper pour afficher les mappings dans le tableau
     */
    public static class MappingColonneECWrapper {
        private int indexColonne;
        private String nomColonne;
        private ElementConstitutif elementConstitutif;
        
        public int getIndexColonne() {
            return indexColonne;
        }
        
        public void setIndexColonne(int indexColonne) {
            this.indexColonne = indexColonne;
        }
        
        public String getNomColonne() {
            return nomColonne;
        }
        
        public void setNomColonne(String nomColonne) {
            this.nomColonne = nomColonne;
        }
        
        public ElementConstitutif getElementConstitutif() {
            return elementConstitutif;
        }
        
        public void setElementConstitutif(ElementConstitutif elementConstitutif) {
            this.elementConstitutif = elementConstitutif;
        }
    }
}