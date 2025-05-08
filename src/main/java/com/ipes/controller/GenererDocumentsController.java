package com.ipes.controller;

import com.ipes.model.*;
import com.ipes.service.*;
import com.ipes.util.ApplicationContext;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Contrôleur pour la génération des documents académiques
 */
@SuppressWarnings("unused")
public class GenererDocumentsController implements Refreshable {
    
    // Services
    private StructurePedagogiqueService structureService;
    private EtudiantService etudiantService;
    private DocumentService documentService;
    
    // Composants de l'interface
    @FXML private ComboBox<String> anneeAcademiqueCombo;
    @FXML private ComboBox<String> typeDocumentCombo;
    @FXML private ComboBox<Cycle> cycleCombo;
    @FXML private ComboBox<ModelDocument> modeleDocumentCombo;
    @FXML private ComboBox<Niveau> niveauCombo;
    @FXML private ComboBox<Option> optionCombo;
    
    @FXML private TextField filtreTextField;
    @FXML private CheckBox selectionnerToutCheckBox;
    @FXML private Label nbSelectionLabel;
    
    @FXML private TableView<EtudiantInscriptionWrapper> etudiantsTable;
    @FXML private TableColumn<EtudiantInscriptionWrapper, Boolean> selectionColumn;
    @FXML private TableColumn<EtudiantInscriptionWrapper, String> matriculeColumn;
    @FXML private TableColumn<EtudiantInscriptionWrapper, String> nomColumn;
    @FXML private TableColumn<EtudiantInscriptionWrapper, String> prenomColumn;
    @FXML private TableColumn<EtudiantInscriptionWrapper, LocalDate> dateNaissanceColumn;
    @FXML private TableColumn<EtudiantInscriptionWrapper, Double> moyenneColumn;
    @FXML private TableColumn<EtudiantInscriptionWrapper, String> statutColumn;
    
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;
    
    // Données
    private ObservableList<EtudiantInscriptionWrapper> etudiantsData = FXCollections.observableArrayList();
    private FilteredList<EtudiantInscriptionWrapper> etudiantsFiltered;
    
    // Propriétés
    private IntegerProperty nbSelectionnes = new SimpleIntegerProperty(0);
    
    /**
     * Initialise le contrôleur
     */
    @FXML
    public void initialize() {
        // Récupérer les services
        structureService = ApplicationContext.getInstance().getService("structureService");
        etudiantService = ApplicationContext.getInstance().getService("etudiantService");
        documentService = ApplicationContext.getInstance().getService("documentService");
        
        // Initialiser les combos
        initTypeDocumentCombo();
        initAnneeAcademiqueCombo();
        initCycleCombo();
        
        // Initialiser les colonnes du tableau
        initTableColumns();
        
        // Initialiser le filtre
        etudiantsFiltered = new FilteredList<>(etudiantsData, p -> true);
        etudiantsTable.setItems(etudiantsFiltered);
        
        // Configurer le filtrage
        filtreTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            etudiantsFiltered.setPredicate(etudiant -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                
                if (etudiant.getMatricule().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (etudiant.getNom().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (etudiant.getPrenom().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });
        
        // Mettre à jour le compteur de sélection
        nbSelectionLabel.textProperty().bind(nbSelectionnes.asString());
        
        // Configurer les listeners pour les combobox
        niveauCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadOptions(newVal);
            } else {
                optionCombo.getItems().clear();
                optionCombo.setDisable(true);
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
        
        typeDocumentCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadModeles(newVal);
            } else {
                modeleDocumentCombo.getItems().clear();
                modeleDocumentCombo.setDisable(true);
            }
        });
    }
    
    /**
     * Initialise le combo des types de documents
     */
    private void initTypeDocumentCombo() {
        List<String> typesDocument = Arrays.asList("RELEVE", "ATTESTATION", "DIPLOME");
        typeDocumentCombo.setItems(FXCollections.observableArrayList(typesDocument));
        typeDocumentCombo.getSelectionModel().selectFirst();
    }
    
    /**
     * Initialise le combo des années académiques
     */
    private void initAnneeAcademiqueCombo() {
        // Générer les 5 dernières années académiques
        List<String> anneesAcademiques = new ArrayList<>();
        int anneeActuelle = LocalDate.now().getYear();
        
        for (int i = 0; i < 5; i++) {
            int debut = anneeActuelle - i;
            int fin = debut + 1;
            anneesAcademiques.add(debut + "-" + fin);
        }
        
        anneeAcademiqueCombo.setItems(FXCollections.observableArrayList(anneesAcademiques));
        anneeAcademiqueCombo.getSelectionModel().selectFirst();
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
        List<Option> options = structureService.getOptionsByNiveau(niveau.getId());
        
        // Ajouter une option "Toutes les options"
        Option optionTout = new Option();
        optionTout.setId(0L);
        optionTout.setCode("TOUTES");
        optionTout.setIntitule("Toutes les options");
        
        List<Option> optionsAvecTout = new ArrayList<>();
        optionsAvecTout.add(optionTout);
        optionsAvecTout.addAll(options);
        
        optionCombo.setItems(FXCollections.observableArrayList(optionsAvecTout));
        
        if (!optionCombo.getItems().isEmpty()) {
            optionCombo.getSelectionModel().selectFirst();
        } else {
            optionCombo.setDisable(true);
        }
    }
    
    /**
     * Charge les modèles de documents pour un type
     */
    private void loadModeles(String type) {
        modeleDocumentCombo.setDisable(false);
        modeleDocumentCombo.setItems(FXCollections.observableArrayList(
            documentService.getModelesByType(type)
        ));
        
        if (!modeleDocumentCombo.getItems().isEmpty()) {
            modeleDocumentCombo.getSelectionModel().selectFirst();
        } else {
            // Créer un modèle par défaut si aucun n'existe
            ModelDocument modeleDefaut = new ModelDocument();
            modeleDefaut.setId(0L);
            modeleDefaut.setNom("Modèle par défaut");
            modeleDefaut.setType(type);
            modeleDefaut.setActif(true);
            
            modeleDocumentCombo.setItems(FXCollections.observableArrayList(modeleDefaut));
            modeleDocumentCombo.getSelectionModel().selectFirst();
        }
    }
    
    /**
     * Initialise les colonnes du tableau des étudiants
     */
    private void initTableColumns() {
        // Colonne de sélection
        selectionColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectionColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectionColumn));
        selectionColumn.setEditable(true);
        
        // Colonnes d'informations
        matriculeColumn.setCellValueFactory(new PropertyValueFactory<>("matricule"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        dateNaissanceColumn.setCellValueFactory(new PropertyValueFactory<>("dateNaissance"));
        moyenneColumn.setCellValueFactory(new PropertyValueFactory<>("moyenne"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        
        // Formater la colonne de date
        dateNaissanceColumn.setCellFactory(column -> new TableCell<EtudiantInscriptionWrapper, LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        });
        
        // Formater la colonne de moyenne
        moyenneColumn.setCellFactory(column -> new TableCell<EtudiantInscriptionWrapper, Double>() {
            @Override
            protected void updateItem(Double moyenne, boolean empty) {
                super.updateItem(moyenne, empty);
                if (empty || moyenne == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", moyenne));
                }
            }
        });
        
        // Rendre la table éditable
        etudiantsTable.setEditable(true);
    }
    
    /**
     * Recherche les étudiants selon les critères sélectionnés
     */
    @FXML
    private void handleRechercher() {
        // Récupérer les critères de recherche
        String anneeAcademique = anneeAcademiqueCombo.getValue();
        Niveau niveau = niveauCombo.getValue();
        Option option = optionCombo.getValue();
        
        if (niveau == null) {
            showAlert(Alert.AlertType.WARNING, "Recherche", "Paramètres incomplets",
                    "Veuillez sélectionner un niveau d'études.");
            return;
        }
        
        // Récupérer les inscriptions
        List<Inscription> inscriptions;
        if (option != null && option.getId() != 0) {
            // Option spécifique
            inscriptions = etudiantService.getInscriptionsByNiveauOption(
                    niveau.getId(), option.getId(), anneeAcademique);
        } else {
            // Toutes les options
            inscriptions = new ArrayList<>();
            for (Option opt : structureService.getOptionsByNiveau(niveau.getId())) {
                inscriptions.addAll(etudiantService.getInscriptionsByNiveauOption(
                        niveau.getId(), opt.getId(), anneeAcademique));
            }
        }
        
        // Convertir les inscriptions en wrappers pour l'affichage
        etudiantsData.clear();
        for (Inscription inscription : inscriptions) {
            EtudiantInscriptionWrapper wrapper = new EtudiantInscriptionWrapper(inscription);
            wrapper.selectedProperty().addListener((obs, oldVal, newVal) -> {
                // Mettre à jour le compteur de sélections
                nbSelectionnes.set(nbSelectionnes.get() + (newVal ? 1 : -1));
            });
            etudiantsData.add(wrapper);
        }
        
        // Mettre à jour le tableau
        etudiantsFiltered.setPredicate(p -> true);
        etudiantsTable.refresh();
        
        // Mettre à jour le compteur
        nbSelectionnes.set(0);
    }
    
    /**
     * Réinitialise les filtres de recherche
     */
    @FXML
    private void handleReinitialiser() {
        // Réinitialiser les combos
        if (!anneeAcademiqueCombo.getItems().isEmpty()) {
            anneeAcademiqueCombo.getSelectionModel().selectFirst();
        }
        
        if (!typeDocumentCombo.getItems().isEmpty()) {
            typeDocumentCombo.getSelectionModel().selectFirst();
        }
        
        if (!cycleCombo.getItems().isEmpty()) {
            cycleCombo.getSelectionModel().selectFirst();
        }
        
        // Vider le filtre et le tableau
        filtreTextField.clear();
        etudiantsData.clear();
        nbSelectionnes.set(0);
    }
    
    /**
     * Sélectionne ou désélectionne tous les étudiants
     */
    @FXML
    private void handleSelectionnerTout() {
        boolean selectionner = selectionnerToutCheckBox.isSelected();
        
        for (EtudiantInscriptionWrapper wrapper : etudiantsFiltered) {
            wrapper.setSelected(selectionner);
        }
        
        // Mettre à jour le compteur
        nbSelectionnes.set(selectionner ? etudiantsFiltered.size() : 0);
    }
    
    /**
     * Génère un aperçu d'un document pour l'étudiant sélectionné
     */
    @FXML
    private void handleApercu() {
        // Vérifier qu'au moins un étudiant est sélectionné
        EtudiantInscriptionWrapper selected = etudiantsFiltered.stream()
                .filter(EtudiantInscriptionWrapper::isSelected)
                .findFirst()
                .orElse(null);
        
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Aperçu", "Aucun étudiant sélectionné",
                    "Veuillez sélectionner au moins un étudiant pour générer un aperçu.");
            return;
        }
        
        // Récupérer le type de document et le modèle
        String typeDocument = typeDocumentCombo.getValue();
        ModelDocument modele = modeleDocumentCombo.getValue();
        
        // Générer l'aperçu
        try {
            byte[] contenu = null;
            Inscription inscription = selected.getInscription();
            
            switch (typeDocument) {
                case "RELEVE":
                    contenu = documentService.genererReleve(inscription, modele);
                    break;
                case "ATTESTATION":
                    contenu = documentService.genererAttestation(inscription, modele);
                    break;
                case "DIPLOME":
                    contenu = documentService.genererDiplome(inscription, modele);
                    break;
            }
            
            if (contenu != null) {
                // Sauvegarder le document temporaire
                File tempFile = File.createTempFile("apercu_", ".txt");
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    fos.write(contenu);
                }
                
                // Ouvrir le document
                java.awt.Desktop.getDesktop().open(tempFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de génération",
                    "Une erreur est survenue lors de la génération de l'aperçu : " + e.getMessage());
        }
    }
    
    /**
     * Génère les documents pour tous les étudiants sélectionnés
     */
    @FXML
    private void handleGenerer() {
        // Vérifier qu'au moins un étudiant est sélectionné
        List<EtudiantInscriptionWrapper> selected = etudiantsFiltered.stream()
                .filter(EtudiantInscriptionWrapper::isSelected)
                .toList();
        
        if (selected.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Génération", "Aucun étudiant sélectionné",
                    "Veuillez sélectionner au moins un étudiant pour générer les documents.");
            return;
        }
        
        // Sélectionner le répertoire de destination
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Sélectionner le répertoire de destination");
        File outputDir = dirChooser.showDialog(null);
        
        if (outputDir == null) {
            return;
        }
        
        // Récupérer le type de document et le modèle
        String typeDocument = typeDocumentCombo.getValue();
        ModelDocument modele = modeleDocumentCombo.getValue();
        
        // Préparer la barre de progression
        progressBar.setVisible(true);
        progressLabel.setVisible(true);
        progressBar.setProgress(0);
        
        // Créer une tâche en arrière-plan pour générer les documents
        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                int total = selected.size();
                int success = 0;
                
                for (int i = 0; i < total; i++) {
                    EtudiantInscriptionWrapper wrapper = selected.get(i);
                    Inscription inscription = wrapper.getInscription();
                    
                    updateProgress(i, total);
                    updateMessage("Génération de document pour " + wrapper.getNom() + " " + wrapper.getPrenom());
                    
                    try {
                        byte[] contenu = null;
                        
                        // Générer le document selon le type
                        switch (typeDocument) {
                            case "RELEVE":
                                contenu = documentService.genererReleve(inscription, modele);
                                break;
                            case "ATTESTATION":
                                contenu = documentService.genererAttestation(inscription, modele);
                                break;
                            case "DIPLOME":
                                contenu = documentService.genererDiplome(inscription, modele);
                                break;
                        }
                        
                        if (contenu != null) {
                            // Créer le document dans la base de données
                            Document document = new Document();
                            document.setType(typeDocument);
                            document.setInscription(inscription);
                            document.setAuteur("Utilisateur");
                            document.setContenu(contenu);
                            
                            Document savedDocument = documentService.creerDocument(document);
                            
                            // Sauvegarder le fichier
                            String extension = ".txt"; // Dans une impl réelle, ce serait .pdf
                            String nomFichier = typeDocument + "_" + wrapper.getMatricule() + "_" + savedDocument.getNumero() + extension;
                            File outputFile = new File(outputDir, nomFichier);
                            
                            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                                fos.write(contenu);
                                success++;
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur lors de la génération du document pour " + 
                                wrapper.getNom() + " " + wrapper.getPrenom() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                
                updateProgress(total, total);
                return success;
            }
        };
        
        // Lier la barre de progression à la tâche
        progressBar.progressProperty().bind(task.progressProperty());
        progressLabel.textProperty().bind(task.messageProperty());
        
        // Gérer la fin de la tâche
        task.setOnSucceeded(event -> {
            Integer success = task.getValue();
            progressBar.progressProperty().unbind();
            progressBar.setProgress(1);
            progressLabel.textProperty().unbind();
            progressLabel.setText("Génération terminée : " + success + " document(s) généré(s) sur " + selected.size());
            
            showAlert(Alert.AlertType.INFORMATION, "Génération terminée", 
                    "Documents générés avec succès",
                    success + " document(s) sur " + selected.size() + " ont été générés avec succès.");
        });
        
        task.setOnFailed(event -> {
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
            progressLabel.textProperty().unbind();
            progressLabel.setText("Erreur lors de la génération des documents");
            
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de génération",
                    "Une erreur est survenue lors de la génération des documents : " + 
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
        etudiantsData.clear();
        nbSelectionnes.set(0);
    }
    
    /**
     * Classe wrapper pour afficher les étudiants et inscriptions dans le tableau
     */
    public static class EtudiantInscriptionWrapper {
        private final Inscription inscription;
        private final BooleanProperty selected = new SimpleBooleanProperty(false);
        
        public EtudiantInscriptionWrapper(Inscription inscription) {
            this.inscription = inscription;
        }
        
        public Inscription getInscription() {
            return inscription;
        }
        
        public String getMatricule() {
            return inscription.getEtudiant().getMatricule();
        }
        
        public String getNom() {
            return inscription.getEtudiant().getNom();
        }
        
        public String getPrenom() {
            return inscription.getEtudiant().getPrenom();
        }
        
        public LocalDate getDateNaissance() {
            return inscription.getEtudiant().getDateNaissance();
        }
        
        public double getMoyenne() {
            return inscription.calculerMoyenneGenerale();
        }
        
        public String getStatut() {
            double moyenne = getMoyenne();
            return moyenne >= 10 ? "Admis" : "Ajourné";
        }
        
        public boolean isSelected() {
            return selected.get();
        }
        
        public void setSelected(boolean selected) {
            this.selected.set(selected);
        }
        
        public BooleanProperty selectedProperty() {
            return selected;
        }
    }
}