package com.ipes.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton qui gère le contexte de l'application et les services
 */
public class ApplicationContext {
    
    private static ApplicationContext instance;
    private Map<String, Object> services;
    private Map<String, Object> properties;
    
    /**
     * Constructeur privé (singleton)
     */
    private ApplicationContext() {
        services = new HashMap<>();
        properties = new HashMap<>();
    }
    
    /**
     * Retourne l'instance unique du contexte
     */
    public static ApplicationContext getInstance() {
        if (instance == null) {
            instance = new ApplicationContext();
        }
        return instance;
    }
    
    /**
     * Enregistre un service dans le contexte
     * @param name Nom du service
     * @param service Instance du service
     */
    public void registerService(String name, Object service) {
        services.put(name, service);
    }
    
    /**
     * Récupère un service par son nom
     * @param name Nom du service
     * @return Le service ou null si non trouvé
     */
    @SuppressWarnings("unchecked")
    public <T> T getService(String name) {
        return (T) services.get(name);
    }
    
    /**
     * Définit une propriété dans le contexte
     * @param name Nom de la propriété
     * @param value Valeur de la propriété
     */
    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }
    
    /**
     * Récupère une propriété par son nom
     * @param name Nom de la propriété
     * @return La valeur de la propriété ou null si non trouvée
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String name) {
        return (T) properties.get(name);
    }
    
    /**
     * Récupère une propriété avec une valeur par défaut
     * @param name Nom de la propriété
     * @param defaultValue Valeur par défaut
     * @return La valeur de la propriété ou la valeur par défaut si non trouvée
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String name, T defaultValue) {
        T value = (T) properties.get(name);
        return value != null ? value : defaultValue;
    }
}