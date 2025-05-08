package com.ipes;

import com.ipes.service.*;
import com.ipes.controller.*;
import com.ipes.util.ApplicationContext;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Application principale du Système de Gestion Académique IPES
 */
public class App extends Application {

    private static Scene scene;
    private static Stage mainStage;
    
    @Override
    public void start(Stage stage) throws IOException {
        // Initialiser les services et le contexte de l'application
        initializeServices();
        
        // Définir la fenêtre principale
        mainStage = stage;
        scene = new Scene(loadFXML("main"), 1200, 800);
        
        // Configurer la fenêtre
        stage.setTitle("Système de Gestion Académique IPES");
        stage.setScene(scene);
        
        // Charger l'icône de l'application
        try {
            stage.getIcons().add(new Image(App.class.getResourceAsStream("/com/ipes/images/icon.png")));
        } catch (Exception e) {
            System.err.println("Icône non trouvée: " + e.getMessage());
        }
        
        // Afficher la fenêtre
        stage.show();
    }
    
    /**
     * Initialise les services et le contexte de l'application
     */
    private void initializeServices() {
        // Créer les instances des services
        StructurePedagogiqueService structureService = new StructurePedagogiqueService();
        EtudiantService etudiantService = new EtudiantService();
        DocumentService documentService = new DocumentService(structureService, etudiantService);
        
        // Charger les données sauvegardées
        structureService.chargerDonnees("data/structure.dat");
        etudiantService.chargerDonnees("data/etudiants.dat");
        documentService.chargerDonnees("data/documents.dat");
        
        // Enregistrer les services dans le contexte de l'application
        ApplicationContext.getInstance().registerService("structureService", structureService);
        ApplicationContext.getInstance().registerService("etudiantService", etudiantService);
        ApplicationContext.getInstance().registerService("documentService", documentService);
    }
    
    /**
     * Méthode appelée lors de la fermeture de l'application
     */
    @Override
    public void stop() {
        // Sauvegarder les données avant de quitter
        StructurePedagogiqueService structureService = ApplicationContext.getInstance().getService("structureService");
        EtudiantService etudiantService = ApplicationContext.getInstance().getService("etudiantService");
        DocumentService documentService = ApplicationContext.getInstance().getService("documentService");
        
        // Créer le répertoire data si nécessaire
        new java.io.File("data").mkdirs();
        
        // Sauvegarder les données
        structureService.sauvegarderDonnees("data/structure.dat");
        etudiantService.sauvegarderDonnees("data/etudiants.dat");
        documentService.sauvegarderDonnees("data/documents.dat");
    }

    /**
     * Change la vue principale
     */
    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    /**
     * Charge un fichier FXML et retourne son nœud racine
     */
    public static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/ipes/view/" + fxml + ".fxml"));
        
        // Charger les ressources linguistiques
        ResourceBundle bundle = ResourceBundle.getBundle("com.ipes.bundles.Language", Locale.getDefault());
        fxmlLoader.setResources(bundle);
        
        return fxmlLoader.load();
    }
    
    /**
     * Retourne la fenêtre principale
     */
    public static Stage getMainStage() {
        return mainStage;
    }

    /**
     * Point d'entrée principal
     */
    public static void main(String[] args) {
        launch();
    }
}