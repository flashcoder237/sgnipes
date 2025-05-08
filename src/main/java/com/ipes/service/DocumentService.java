package com.ipes.service;

import com.ipes.model.*;
import com.ipes.model.Module;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Service pour la gestion des documents et importation de données
 */
public class DocumentService {
    
    // En l'absence de base de données, nous utilisons des collections en mémoire
    private static List<ModelDocument> modeles = new ArrayList<>();
    private static List<Document> documents = new ArrayList<>();
    private static List<ConfigImport> configsImport = new ArrayList<>();
    
    private static Long modeleId = 1L;
    private static Long documentId = 1L;
    private static Long configImportId = 1L;
    
    // Référence aux autres services
    @SuppressWarnings("unused")
    private StructurePedagogiqueService structureService;
    private EtudiantService etudiantService;
    
    public DocumentService(StructurePedagogiqueService structureService, EtudiantService etudiantService) {
        this.structureService = structureService;
        this.etudiantService = etudiantService;
    }
    
    // CRUD pour Modèle de Document
    public ModelDocument creerModeleDocument(ModelDocument modele) {
        modele.setId(modeleId++);
        modeles.add(modele);
        return modele;
    }
    
    public ModelDocument getModeleDocument(Long id) {
        return modeles.stream().filter(m -> m.getId().equals(id)).findFirst().orElse(null);
    }
    
    public List<ModelDocument> getAllModelesDocument() {
        return new ArrayList<>(modeles);
    }
    
    public List<ModelDocument> getModelesByType(String type) {
        return modeles.stream().filter(m -> m.getType().equals(type)).toList();
    }
    
    public void updateModeleDocument(ModelDocument modele) {
        int index = modeles.indexOf(getModeleDocument(modele.getId()));
        if (index >= 0) {
            modeles.set(index, modele);
        }
    }
    
    public void deleteModeleDocument(Long id) {
        modeles.removeIf(m -> m.getId().equals(id));
    }
    
    // CRUD pour Document généré
    public Document creerDocument(Document document) {
        document.setId(documentId++);
        document.setNumero(genererNumeroDocument(document.getType()));
        document.setDateGeneration(LocalDate.now());
        documents.add(document);
        return document;
    }
    
    public Document getDocument(Long id) {
        return documents.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
    }
    
    public List<Document> getAllDocuments() {
        return new ArrayList<>(documents);
    }
    
    public List<Document> getDocumentsByInscription(Long inscriptionId) {
        return documents.stream().filter(d -> d.getInscription().getId().equals(inscriptionId)).toList();
    }
    
    public List<Document> getDocumentsByType(String type) {
        return documents.stream().filter(d -> d.getType().equals(type)).toList();
    }
    
    public void updateDocument(Document document) {
        int index = documents.indexOf(getDocument(document.getId()));
        if (index >= 0) {
            documents.set(index, document);
        }
    }
    
    public void deleteDocument(Long id) {
        documents.removeIf(d -> d.getId().equals(id));
    }
    
    // CRUD pour ConfigImport
    public ConfigImport creerConfigImport(ConfigImport config) {
        config.setId(configImportId++);
        configsImport.add(config);
        return config;
    }
    
    public ConfigImport getConfigImport(Long id) {
        return configsImport.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
    }
    
    public List<ConfigImport> getAllConfigsImport() {
        return new ArrayList<>(configsImport);
    }
    
    public List<ConfigImport> getConfigsImportByNiveauOption(Long niveauId, Long optionId) {
        return configsImport.stream()
            .filter(c -> c.getNiveau().getId().equals(niveauId) && 
                    (c.getOption() == null || c.getOption().getId().equals(optionId)))
            .toList();
    }
    
    public void updateConfigImport(ConfigImport config) {
        int index = configsImport.indexOf(getConfigImport(config.getId()));
        if (index >= 0) {
            configsImport.set(index, config);
        }
    }
    
    public void deleteConfigImport(Long id) {
        configsImport.removeIf(c -> c.getId().equals(id));
    }
    
    /**
     * Génère un numéro unique pour un document
     */
    private String genererNumeroDocument(String type) {
        // Format: TYPE-ANNEE-NUMERO (ex: REL-2025-00001)
        int annee = LocalDate.now().getYear();
        long count = documents.stream().filter(d -> d.getType().equals(type)).count() + 1;
        
        String prefix = "";
        switch (type) {
            case "RELEVE":
                prefix = "REL";
                break;
            case "ATTESTATION":
                prefix = "ATT";
                break;
            case "DIPLOME":
                prefix = "DIP";
                break;
            default:
                prefix = "DOC";
        }
        
        return String.format("%s-%d-%05d", prefix, annee, count);
    }
    
    /**
     * Génère un relevé de notes pour une inscription
     */
    public byte[] genererReleve(Inscription inscription, ModelDocument modele) {
        // Dans une implémentation réelle, nous utiliserions JasperReports ou iText
        // Ici, nous simulons une génération simple de PDF
        
        try {
            // Préparation du contenu du document
            StringBuilder sb = new StringBuilder();
            
            // Entête du document
            sb.append("RELEVÉ DE NOTES\n\n");
            sb.append("INSTITUT PRIVÉ D'ENSEIGNEMENT SUPÉRIEUR\n\n");
            
            // Informations de l'étudiant
            Etudiant etudiant = inscription.getEtudiant();
            sb.append("INFORMATIONS ÉTUDIANT\n");
            sb.append("Matricule: ").append(etudiant.getMatricule()).append("\n");
            sb.append("Nom et Prénom: ").append(etudiant.getNom()).append(" ").append(etudiant.getPrenom()).append("\n");
            sb.append("Date de naissance: ").append(etudiant.getDateNaissance()).append("\n");
            sb.append("Lieu de naissance: ").append(etudiant.getLieuNaissance()).append("\n\n");
            
            // Informations académiques
            sb.append("ANNÉE ACADÉMIQUE: ").append(inscription.getAnneeAcademique()).append("\n");
            sb.append("FILIÈRE: ").append(inscription.getNiveau().getCycle().getIntitule()).append("\n");
            sb.append("NIVEAU: ").append(inscription.getNiveau().getIntitule()).append("\n");
            if (inscription.getOption() != null) {
                sb.append("OPTION: ").append(inscription.getOption().getIntitule()).append("\n");
            }
            sb.append("\n");
            
            // Tableau des notes
            sb.append("RÉSULTATS\n");
            sb.append("CODE\tINTITULÉ\tCOEFF\tNOTE\tMENTION\n");
            
            // Liste des UEs et ECs avec notes
            Map<UniteEnseignement, List<ElementConstitutif>> ueEcs = new HashMap<>();
            
            // Regrouper les ECs par UE
            List<UniteEnseignement> ues = new ArrayList<>();
            if (inscription.getOption() != null) {
                ues.addAll(inscription.getOption().getUnites());
            }
            
            // Ajouter les UEs des modules du niveau
            for (Module module : inscription.getNiveau().getModules()) {
                for (UniteEnseignement ue : module.getUnites()) {
                    if (!ues.contains(ue)) {
                        ues.add(ue);
                    }
                }
            }
            
            // Pour chaque UE, récupérer ses ECs
            for (UniteEnseignement ue : ues) {
                ueEcs.put(ue, ue.getElements());
            }
            
            // Afficher les UEs et ECs avec leurs notes
            for (Map.Entry<UniteEnseignement, List<ElementConstitutif>> entry : ueEcs.entrySet()) {
                UniteEnseignement ue = entry.getKey();
                List<ElementConstitutif> ecs = entry.getValue();
                
                sb.append("\nUNITÉ D'ENSEIGNEMENT: ").append(ue.getIntitule()).append(" (").append(ue.getCredits()).append(" crédits)\n");
                
                // Afficher les ECs
                for (ElementConstitutif ec : ecs) {
                    Note note = etudiantService.getNoteByInscriptionAndEC(inscription.getId(), ec.getId());
                    double valeurNote = note != null ? note.getValeur() : 0;
                    String mention = getMention(valeurNote);
                    
                    sb.append(ec.getCode()).append("\t")
                      .append(ec.getIntitule()).append("\t")
                      .append(ec.getCoefficient()).append("\t")
                      .append(valeurNote).append("\t")
                      .append(mention).append("\n");
                }
                
                // Moyenne de l'UE
                double moyenneUE = inscription.calculerMoyenneUE(ue);
                String mentionUE = getMention(moyenneUE);
                sb.append("MOYENNE DE L'UE:\t\t\t").append(moyenneUE).append("\t").append(mentionUE).append("\n");
            }
            
            // Moyenne générale
            double moyenneGenerale = inscription.calculerMoyenneGenerale();
            String mentionGenerale = getMention(moyenneGenerale);
            sb.append("\nMOYENNE GÉNÉRALE:\t\t\t").append(moyenneGenerale).append("\t").append(mentionGenerale).append("\n");
            
            // Résultat final
            boolean admis = moyenneGenerale >= 10;
            sb.append("\nRÉSULTAT: ").append(admis ? "ADMIS" : "AJOURNÉ").append("\n");
            
            // Pied de page
            sb.append("\n\nFait à [VILLE], le ").append(LocalDate.now()).append("\n");
            sb.append("Le Directeur de l'établissement\n\n");
            sb.append("(Signature et cachet)\n");
            
            // Retourner le contenu sous forme de byte[]
            return sb.toString().getBytes();
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Génère une attestation de réussite pour une inscription
     */
    public byte[] genererAttestation(Inscription inscription, ModelDocument modele) {
        try {
            // Préparation du contenu du document
            StringBuilder sb = new StringBuilder();
            
            // Entête du document
            sb.append("ATTESTATION DE RÉUSSITE\n\n");
            sb.append("INSTITUT PRIVÉ D'ENSEIGNEMENT SUPÉRIEUR\n\n");
            
            // Corps de l'attestation
            sb.append("Je soussigné, [NOM DU DIRECTEUR], Directeur de l'Institut Privé d'Enseignement Supérieur,\n\n");
            
            sb.append("atteste que l'étudiant(e):\n\n");
            
            // Informations de l'étudiant
            Etudiant etudiant = inscription.getEtudiant();
            sb.append("Nom et Prénom: ").append(etudiant.getNom()).append(" ").append(etudiant.getPrenom()).append("\n");
            sb.append("Matricule: ").append(etudiant.getMatricule()).append("\n");
            sb.append("Date de naissance: ").append(etudiant.getDateNaissance()).append("\n");
            sb.append("Lieu de naissance: ").append(etudiant.getLieuNaissance()).append("\n\n");
            
            // Résultat
            double moyenneGenerale = inscription.calculerMoyenneGenerale();
            String mentionGenerale = getMention(moyenneGenerale);
            
            sb.append("a été déclaré(e) ADMIS(E) en ");
            sb.append(inscription.getNiveau().getIntitule());
            if (inscription.getOption() != null) {
                sb.append(", option ").append(inscription.getOption().getIntitule());
            }
            sb.append(", au titre de l'année académique ").append(inscription.getAnneeAcademique()).append(",\n");
            sb.append("avec une moyenne générale de ").append(moyenneGenerale).append("/20, ");
            sb.append("mention ").append(mentionGenerale).append(".\n\n");
            
            // Validité et référence
            sb.append("La présente attestation est délivrée à l'intéressé(e) pour servir et valoir ce que de droit.\n\n");
            sb.append("Référence PV: [RÉFÉRENCE PV DE DÉLIBÉRATION]\n\n");
            
            // Pied de page
            sb.append("Fait à [VILLE], le ").append(LocalDate.now()).append("\n\n");
            sb.append("Le Directeur de l'établissement\n\n");
            sb.append("(Signature et cachet)\n");
            
            // Retourner le contenu sous forme de byte[]
            return sb.toString().getBytes();
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Génère un diplôme pour une inscription
     */
    public byte[] genererDiplome(Inscription inscription, ModelDocument modele) {
        try {
            // Préparation du contenu du document
            StringBuilder sb = new StringBuilder();
            
            // Entête du document
            sb.append("DIPLÔME\n\n");
            sb.append("RÉPUBLIQUE [PAYS]\n");
            sb.append("INSTITUT PRIVÉ D'ENSEIGNEMENT SUPÉRIEUR\n\n");
            
            // Corps du diplôme
            sb.append("Vu les textes en vigueur;\n");
            sb.append("Vu les résultats de l'intéressé(e);\n");
            sb.append("Vu le procès-verbal de délibération du jury;\n\n");
            
            sb.append("Il est conféré à:\n\n");
            
            // Informations de l'étudiant
            Etudiant etudiant = inscription.getEtudiant();
            sb.append("Monsieur/Madame ").append(etudiant.getNom()).append(" ").append(etudiant.getPrenom()).append("\n");
            sb.append("Né(e) le ").append(etudiant.getDateNaissance()).append(" à ").append(etudiant.getLieuNaissance()).append("\n");
            sb.append("De nationalité ").append(etudiant.getNationalite()).append("\n\n");
            
            // Intitulé du diplôme
            sb.append("Le diplôme de ");
            sb.append(inscription.getNiveau().getCycle().getIntitule()).append(" en ");
            sb.append(inscription.getNiveau().getIntitule());
            if (inscription.getOption() != null) {
                sb.append(", option ").append(inscription.getOption().getIntitule());
            }
            sb.append("\n\n");
            
            // Mention
            double moyenneGenerale = inscription.calculerMoyenneGenerale();
            String mentionGenerale = getMention(moyenneGenerale);
            sb.append("Avec la mention: ").append(mentionGenerale).append("\n\n");
            
            // Numéro du diplôme
            String numeroDiplome = genererNumeroDocument("DIPLOME");
            sb.append("Diplôme N°: ").append(numeroDiplome).append("\n");
            sb.append("Délivré le: ").append(LocalDate.now()).append("\n\n");
            
            // Signatures
            sb.append("Le titulaire\t\tLe Directeur\t\tLe Ministre\n\n");
            sb.append("_________\t\t_________\t\t_________\n");
            
            // Retourner le contenu sous forme de byte[]
            return sb.toString().getBytes();
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Détermine la mention en fonction de la note
     */
    private String getMention(double note) {
        if (note >= 16) return "TRÈS BIEN";
        if (note >= 14) return "BIEN";
        if (note >= 12) return "ASSEZ BIEN";
        if (note >= 10) return "PASSABLE";
        return "INSUFFISANT";
    }
    
    /**
     * Importe les notes depuis un fichier CSV en utilisant une configuration d'importation
     * @param fichierCsv Le fichier CSV contenant les notes
     * @param configId L'ID de la configuration d'importation à utiliser
     * @param anneeAcademique L'année académique pour les inscriptions
     * @return Le nombre de notes importées
     */
    public int importerNotes(File fichierCsv, Long configId, String anneeAcademique) {
        try {
            // Récupérer la configuration d'importation
            ConfigImport config = getConfigImport(configId);
            if (config == null) {
                return 0;
            }
            
            // Lire le fichier CSV
            BufferedReader reader = new BufferedReader(new FileReader(fichierCsv));
            String ligne;
            
            // Lire l'en-tête pour trouver les index des colonnes
            String entete = reader.readLine();
            if (entete == null) {
                reader.close();
                return 0;
            }
            
            String[] colonnes = entete.split(",");
            
            // Colonnes obligatoires
            int indexMatricule = -1;
            
            // Trouver l'index de la colonne matricule
            for (int i = 0; i < colonnes.length; i++) {
                String colonne = colonnes[i].trim();
                if (colonne.equalsIgnoreCase("matricule") || colonne.equalsIgnoreCase("mat")) {
                    indexMatricule = i;
                    break;
                }
            }
            
            if (indexMatricule == -1) {
                reader.close();
                throw new IllegalArgumentException("Le fichier ne contient pas de colonne matricule");
            }
            
            // Préparer les mappings entre colonnes et ECs
            Map<Integer, ElementConstitutif> mappings = new HashMap<>();
            for (MapColonneEC mapping : config.getMappings()) {
                // Si le mapping utilise le nom de colonne, trouver son index
                if (mapping.getNomColonne() != null && !mapping.getNomColonne().isEmpty()) {
                    for (int i = 0; i < colonnes.length; i++) {
                        if (colonnes[i].trim().equalsIgnoreCase(mapping.getNomColonne())) {
                            mappings.put(i, mapping.getElementConstitutif());
                            break;
                        }
                    }
                } else {
                    // Sinon utiliser l'index de colonne directement
                    mappings.put(mapping.getIndexColonne(), mapping.getElementConstitutif());
                }
            }
            
            if (mappings.isEmpty()) {
                reader.close();
                throw new IllegalArgumentException("Aucun mapping valide n'a été trouvé");
            }
            
            // Lire et traiter les données
            int nombreNotesImportees = 0;
            
            while ((ligne = reader.readLine()) != null) {
                String[] donnees = ligne.split(",");
                if (donnees.length <= indexMatricule) continue;
                
                String matricule = donnees[indexMatricule].trim();
                
                // Chercher l'étudiant par matricule
                Etudiant etudiant = etudiantService.getEtudiantByMatricule(matricule);
                if (etudiant == null) continue;
                
                // Trouver l'inscription correspondante
                Inscription inscription = null;
                for (Inscription insc : etudiantService.getInscriptionsByEtudiant(etudiant.getId())) {
                    if (insc.getAnneeAcademique().equals(anneeAcademique) &&
                        insc.getNiveau().getId().equals(config.getNiveau().getId()) &&
                        (config.getOption() == null || insc.getOption().getId().equals(config.getOption().getId()))) {
                        inscription = insc;
                        break;
                    }
                }
                
                // Créer l'inscription si elle n'existe pas
                if (inscription == null) {
                    inscription = new Inscription();
                    inscription.setEtudiant(etudiant);
                    inscription.setNiveau(config.getNiveau());
                    inscription.setOption(config.getOption());
                    inscription.setAnneeAcademique(anneeAcademique);
                    inscription.setDateInscription(LocalDate.now());
                    inscription.setStatut("Régulier");
                    
                    inscription = etudiantService.creerInscription(inscription);
                }
                
                // Créer ou mettre à jour les notes
                for (Map.Entry<Integer, ElementConstitutif> entry : mappings.entrySet()) {
                    int index = entry.getKey();
                    ElementConstitutif ec = entry.getValue();
                    
                    if (index >= donnees.length) continue;
                    
                    String valeurStr = donnees[index].trim();
                    if (valeurStr.isEmpty()) continue;
                    
                    try {
                        double valeur = Double.parseDouble(valeurStr);
                        
                        // Vérifier si une note existe déjà
                        Note note = etudiantService.getNoteByInscriptionAndEC(inscription.getId(), ec.getId());
                        
                        if (note == null) {
                            // Créer une nouvelle note
                            note = new Note();
                            note.setInscription(inscription);
                            note.setElementConstitutif(ec);
                            note.setTypeEvaluation("Examen");
                            note.setValeur(valeur);
                            
                            etudiantService.creerNote(note);
                        } else {
                            // Mettre à jour la note existante
                            note.setValeur(valeur);
                            etudiantService.updateNote(note);
                        }
                        
                        nombreNotesImportees++;
                    } catch (NumberFormatException e) {
                        // Ignorer les valeurs non numériques
                    }
                }
            }
            
            reader.close();
            return nombreNotesImportees;
            
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    // Méthodes pour sauvegarder et charger les données
    public void sauvegarderDonnees(String fichier) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fichier))) {
            oos.writeObject(modeles);
            oos.writeObject(documents);
            oos.writeObject(configsImport);
            oos.writeLong(modeleId);
            oos.writeLong(documentId);
            oos.writeLong(configImportId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
    public void chargerDonnees(String fichier) {
        File file = new File(fichier);
        if (!file.exists()) return;
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fichier))) {
            modeles = (List<ModelDocument>) ois.readObject();
            documents = (List<Document>) ois.readObject();
            configsImport = (List<ConfigImport>) ois.readObject();
            modeleId = ois.readLong();
            documentId = ois.readLong();
            configImportId = ois.readLong();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}