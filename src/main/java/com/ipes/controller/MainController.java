package com.ipes.controller;

import com.ipes.App;
import com.ipes.util.ApplicationContext;
import com.ipes.service.*;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Contrôleur principal de l'application
 */
@SuppressWarnings("unused")
public class MainController {
    
    @FXML
    private StackPane contentPane;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Label dateLabel;
    
    /**
     * Initialise le contrôleur
     */
    @FXML
    public void initialize() {
        // Mettre à jour la date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        dateLabel.setText(LocalDate.now().format(formatter));
    }
    
    /**
     * Charge une vue dans le panneau de contenu
     */
    private void loadView(String fxml) {
        try {
            Parent view = App.loadFXML(fxml);
            contentPane.getChildren().setAll(view);
            updateStatus("Vue chargée: " + fxml);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement", "Impossible de charger la vue: " + fxml, e.getMessage());
            updateStatus("Erreur: Impossible de charger la vue " + fxml);
        }
    }
    
    /**
     * Met à jour le message de la barre d'état
     */
    public void updateStatus(String message) {
        statusLabel.setText(message);
    }
    
    /**
     * Affiche une boîte de dialogue d'erreur
     */
    private void showError(String title, String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Affiche une boîte de dialogue d'information
     */
    private void showInfo(String title, String header, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Gestionnaires d'événements pour les éléments de menu et la barre d'outils
     */
    @FXML
    private void handleHome() {
        // Afficher l'écran d'accueil (vider le contentPane)
        contentPane.getChildren().clear();
        updateStatus("Écran d'accueil");
    }
    
    @FXML
    private void handleStructure() {
        loadView("structure");
    }
    
    @FXML
    private void handleEtudiants() {
        loadView("etudiants");
    }
    
    @FXML
    private void handleNotes() {
        loadView("notes");
    }
    
    @FXML
    private void handleDocuments() {
        loadView("documents");
    }
    
    @FXML
    private void handleImportNotes() {
        loadView("importNotes");
    }
    
    @FXML
    private void handleGenererDocuments() {
        loadView("genererDocuments");
    }
    
    @FXML
    private void handleExportData() {
        // Sauvegarder les données
        StructurePedagogiqueService structureService = ApplicationContext.getInstance().getService("structureService");
        EtudiantService etudiantService = ApplicationContext.getInstance().getService("etudiantService");
        DocumentService documentService = ApplicationContext.getInstance().getService("documentService");
        
        try {
            structureService.sauvegarderDonnees("data/structure.dat");
            etudiantService.sauvegarderDonnees("data/etudiants.dat");
            documentService.sauvegarderDonnees("data/documents.dat");
            
            showInfo("Exportation réussie", "Données exportées avec succès", 
                    "Les données ont été sauvegardées dans le répertoire 'data'.");
            updateStatus("Données exportées avec succès");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur d'exportation", "Impossible d'exporter les données", e.getMessage());
            updateStatus("Erreur: Impossible d'exporter les données");
        }
    }
    
    @FXML
    private void handleImportData() {
        // Charger les données sauvegardées
        StructurePedagogiqueService structureService = ApplicationContext.getInstance().getService("structureService");
        EtudiantService etudiantService = ApplicationContext.getInstance().getService("etudiantService");
        DocumentService documentService = ApplicationContext.getInstance().getService("documentService");
        
        try {
            structureService.chargerDonnees("data/structure.dat");
            etudiantService.chargerDonnees("data/etudiants.dat");
            documentService.chargerDonnees("data/documents.dat");
            
            showInfo("Importation réussie", "Données importées avec succès", 
                    "Les données ont été chargées depuis le répertoire 'data'.");
            updateStatus("Données importées avec succès");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur d'importation", "Impossible d'importer les données", e.getMessage());
            updateStatus("Erreur: Impossible d'importer les données");
        }
    }
    
    @FXML
    private void handlePreferences() {
        loadView("preferences");
    }
    
    @FXML
    private void handleRefresh() {
        // Recharger la vue actuelle
        Parent currentView = contentPane.getChildren().size() > 0 ? (Parent) contentPane.getChildren().get(0) : null;
        if (currentView != null) {
            // Obtenir le contrôleur associé et appeler sa méthode refresh si elle existe
            Object controller = currentView.getUserData();
            if (controller != null && controller instanceof Refreshable) {
                ((Refreshable) controller).refresh();
                updateStatus("Vue actualisée");
            }
        }
    }
    
    @FXML
    private void handleAbout() {
        showInfo("À propos", "Système de Gestion Académique IPES", 
                "Version 1.0\n" +
                "© 2025 - Tous droits réservés\n\n" +
                "Ce logiciel permet la gestion des notes et la génération de documents académiques " +
                "pour les Instituts Privés d'Enseignement Supérieur.");
    }
    
    @FXML
    private void handleHelp() {
        loadView("aide");
    }
    
    @FXML
    private void handleExit() {
        // Sauvegarder les données avant de quitter
        handleExportData();
        
        // Fermer l'application
        App.getMainStage().close();
    }
}

/**
 * Interface pour les contrôleurs qui supportent l'actualisation
 */
interface Refreshable {
    void refresh();
}