package com.iit.internship_manager.services;

import com.iit.internship_manager.services.interfaces.IChatbotService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class OpenAiChatbotService implements IChatbotService {

    private static final String[] SENSITIVE_ACTION_KEYWORDS = {
            "creer", "ajouter", "modifier", "mettre a jour", "supprimer",
            "planifier", "programmer", "confirmer", "accepter", "refuser",
            "annuler", "desactiver", "reactiver", "envoyer", "affecter",
            "generer", "uploader", "televerser"
    };

    @Override
    public boolean isConfigured() {
        return true;
    }

    @Override
    public String ask(String userRole, String userName, String question) {
        String normalizedQuestion = normalize(question);
        String normalizedRole = normalize(userRole);
        String displayName = StringUtils.hasText(userName) ? userName.trim() : "utilisateur";

        if (!StringUtils.hasText(question)) {
            return "Veuillez saisir une question. Je peux vous guider sur les sujets, candidatures, affectations, rendez-vous, taches, fichiers, profils et utilisateurs.";
        }

        if (containsAny(normalizedQuestion, "bonjour", "salut", "hello", "bonsoir")) {
            return "Bonjour " + displayName + ". Je suis l assistant local IIT StageManager. Je peux vous guider de maniere fiable sur l utilisation de la plateforme, sans dependre d un service externe.";
        }

        if (containsAny(normalizedQuestion, "qui es tu", "qui etes vous", "que peux tu faire", "aide", "help")) {
            return buildCapabilitiesReply(normalizedRole);
        }

        if (containsAny(normalizedQuestion, "dashboard", "tableau de bord", "agenda", "alerte")) {
            return """
                    Le dashboard est organise en trois zones :
                    1. a gauche, l agenda mensuel cliquable pour reperer les rendez-vous ;
                    2. au centre, la liste des rendez-vous de la semaine en cours ;
                    3. a droite, les alertes pour les rendez-vous prevus dans moins de 24 heures.
                    Vous pouvez changer de mois et cliquer sur un jour pour voir le detail des rendez-vous associes.
                    """;
        }

        if (containsAny(normalizedQuestion, "profil", "photo", "mot de passe", "password")) {
            return """
                    Dans Mon profil, vous pouvez :
                    - changer votre photo de profil ;
                    - modifier votre nom et votre prenom ;
                    - mettre a jour votre mot de passe et sa confirmation.
                    Les autres informations principales restent en lecture seule selon votre role.
                    """;
        }

        if (containsAny(normalizedQuestion, "motivation", "motiver", "demotive", "demotivation")) {
            return """
                    Si vous manquez de motivation :
                    - fixez un objectif simple pour aujourd hui ;
                    - commencez par une tache courte de 15 a 20 minutes ;
                    - identifiez le vrai blocage ;
                    - faites un point avec votre encadrant si le blocage dure.
                    Vous pouvez aussi revoir vos taches, vos rendez-vous et vos messages pour repartir sur des priorites claires.
                    """;
        }

        if (containsAny(normalizedQuestion, "organisation", "organiser", "planning personnel", "gerer mon temps", "productivite")) {
            return """
                    Pour mieux organiser votre travail :
                    - consultez vos taches dans Rendez-vous & Todo ;
                    - classez-les par priorite ;
                    - gardez 2 ou 3 objectifs maximum par jour ;
                    - prevoyez un moment pour preparer vos echanges avec l encadrant.
                    Une routine simple et reguliere est souvent plus efficace qu un grand planning difficile a tenir.
                    """;
        }

        if (containsAny(normalizedQuestion, "stress", "stresse", "pression", "bloque", "blocage", "probleme personnel")) {
            return """
                    Si vous etes bloque ou sous pression :
                    - separez le probleme technique, organisationnel ou relationnel ;
                    - notez ce que vous avez deja essaye ;
                    - preparez une question precise ;
                    - demandez un rendez-vous si besoin.
                    Si le sujet depasse le cadre du projet, contactez aussi l administration ou le responsable concerne.
                    """;
        }

        if (containsAny(normalizedQuestion, "ecrire a mon encadrant", "message a mon encadrant", "email a mon encadrant", "contacter mon encadrant")) {
            return """
                    Pour ecrire a votre encadrant :
                    - annoncez clairement le sujet ;
                    - expliquez le contexte en une phrase ;
                    - posez une demande precise ;
                    - proposez vos disponibilites si vous voulez un rendez-vous.
                    Exemple :
                    Bonjour, je souhaite faire un point sur l avancement du projet et valider la prochaine etape. Etes-vous disponible cette semaine ?
                    """;
        }

        if (containsAny(normalizedQuestion, "preparer un rendez", "avant un rendez", "preparer la reunion", "preparer un meeting")) {
            return """
                    Avant un rendez-vous, preparez :
                    - l etat d avancement reel ;
                    - ce qui est termine ;
                    - ce qui bloque ;
                    - 2 ou 3 questions precises.
                    Vous pouvez aussi verifier les fichiers partages et les messages recents pour arriver avec un contexte complet.
                    """;
        }

        if (containsAny(normalizedQuestion, "choisir un sujet", "quel sujet", "quel projet", "comment choisir mon sujet")) {
            return """
                    Pour choisir un bon sujet :
                    - verifiez qu il correspond a votre niveau ;
                    - regardez les technologies demandees ;
                    - privilegiez un objectif clair ;
                    - assurez-vous que le cadre de travail avec l encadrant vous convient.
                    Commencez par Tous les sujets puis comparez les projets disponibles.
                    """;
        }

        if (containsAny(normalizedQuestion, "rapport", "soutenance", "presentation", "rediger")) {
            return """
                    Pour preparer votre rapport ou votre soutenance :
                    - gardez une trace reguliere de votre travail ;
                    - notez les decisions importantes ;
                    - conservez les versions utiles de vos fichiers ;
                    - structurez ensuite votre contenu en : contexte, objectifs, realisation, resultats et limites.
                    """;
        }

        if (containsAny(normalizedQuestion, "rendez", "meeting", "agenda")) {
            return buildMeetingReply(normalizedRole);
        }

        if (containsAny(normalizedQuestion, "tache", "todo", "task")) {
            return """
                    Le suivi des taches fonctionne ainsi :
                    - l encadrant cree les taches ;
                    - l etudiant met a jour leur statut ;
                    - les etats utilises sont En attente, En cours et Termine.
                    Vous pouvez consulter ces taches depuis Affectations > Rendez-vous & Todo.
                    """;
        }

        if (containsAny(normalizedQuestion, "fichier", "document", "upload", "telecharger")) {
            return """
                    Dans l espace Fichiers d une affectation :
                    - l etudiant et l encadrant peuvent echanger des documents ;
                    - le nom du deposant est affiche clairement ;
                    - vous pouvez telecharger un document depuis la bibliotheque des fichiers ;
                    - la suppression depend des droits definis pour l affectation.
                    """;
        }

        if (containsAny(normalizedQuestion, "chat", "message", "messagerie")) {
            return """
                    La messagerie est liee a une candidature precise :
                    - l historique charge les messages existants ;
                    - les nouveaux messages arrivent en direct ;
                    - l etudiant et l encadrant echangent depuis Details & Chat.
                    Ouvrez la candidature concernee puis utilisez l espace Discussion.
                    """;
        }

        if (containsAny(normalizedQuestion, "sujet", "projet", "postuler", "candidature", "personnel")) {
            return buildProjectReply(normalizedRole);
        }

        if (containsAny(normalizedQuestion, "charge de travail", "workload", "groupe", "encadrant")) {
            return """
                    La page Charge de travail affiche maintenant les details complets des groupes :
                    - nom du groupe ;
                    - sujet du stage ;
                    - description complete ;
                    - encadrant ;
                    - annee et statut ;
                    - membres du groupe.
                    """;
        }

        if (containsAny(normalizedQuestion, "utilisateur", "admin", "etudiants sans stage", "etudiants avec stage")) {
            return buildAdministrationReply(normalizedRole);
        }

        return buildFallbackReply(normalizedRole);
    }

    @Override
    public boolean requiresExplicitConfirmation(String question) {
        String normalized = normalize(question);
        if (!StringUtils.hasText(normalized)) {
            return false;
        }

        for (String keyword : SENSITIVE_ACTION_KEYWORDS) {
            if (normalized.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String buildCapabilitiesReply(String normalizedRole) {
        if (containsAny(normalizedRole, "etudiant", "student")) {
            return """
                    Je peux vous aider pour :
                    - trouver un sujet disponible ;
                    - postuler ou proposer un projet personnel ;
                    - suivre vos candidatures ;
                    - consulter vos rendez-vous, taches et fichiers ;
                    - utiliser la messagerie avec votre encadrant.
                    """;
        }

        if (containsAny(normalizedRole, "enseignant", "teacher", "encadrant")) {
            return """
                    Je peux vous aider pour :
                    - traiter les candidatures ;
                    - examiner les details d un projet avant acceptation ;
                    - suivre vos groupes, rendez-vous, taches et fichiers ;
                    - consulter votre charge de travail ;
                    - utiliser la messagerie avec les etudiants.
                    """;
        }

        return """
                Je peux vous aider pour :
                - gerer les utilisateurs ;
                - filtrer les comptes actifs ou desactives ;
                - suivre les affectations et les charges de travail ;
                - consulter les etudiants avec ou sans stage ;
                - guider l usage general de la plateforme.
                """;
    }

    private String buildMeetingReply(String normalizedRole) {
        if (containsAny(normalizedRole, "etudiant", "student")) {
            return """
                    Pour les rendez-vous :
                    - vous pouvez consulter les rendez-vous de votre affectation ;
                    - selon la regle en place, vous pouvez proposer ou repondre aux rendez-vous ;
                    - le dashboard affiche l agenda mensuel, la semaine courante et les alertes 24h.
                    Vous pouvez ouvrir Affectations > Rendez-vous & Todo pour le detail.
                    """;
        }

        return """
                Pour gerer les rendez-vous :
                - ouvrez une affectation ;
                - allez dans Rendez-vous & Todo ;
                - verifiez la disponibilite, la date, le lieu et le statut ;
                - utilisez le dashboard pour voir les rendez-vous de la semaine et les alertes 24h.
                """;
    }

    private String buildProjectReply(String normalizedRole) {
        if (containsAny(normalizedRole, "etudiant", "student")) {
            return """
                    Pour un etudiant :
                    - Dans Tous les sujets, utilisez la recherche et postulez aux projets disponibles ;
                    - Pour un projet personnel, ouvrez Candidatures puis cliquez sur Projet personnel ;
                    - Choisissez l encadrant ;
                    - Selectionnez vos collegues dans la limite du groupe definie ;
                    - Envoyez la demande pour qu elle apparaisse ensuite chez l encadrant.
                    """;
        }

        if (containsAny(normalizedRole, "enseignant", "teacher", "encadrant")) {
            return """
                    Pour un enseignant :
                    - consultez les candidatures recues ;
                    - ouvrez Examiner la demande ou Details & Chat pour voir le contenu complet ;
                    - verifiez le groupe, le sujet, la description et les echanges ;
                    - acceptez ou refusez ensuite la demande.
                    """;
        }

        return """
                Les sujets et projets peuvent etre consultes et filtres par type, statut et role.
                Les candidatures permettent de relier un groupe a un sujet et d assurer le suivi avant affectation.
                """;
    }

    private String buildAdministrationReply(String normalizedRole) {
        if (containsAny(normalizedRole, "enseignant", "teacher", "encadrant")) {
            return """
                    Si vous etes responsable, la page Etudiants est organisee en deux tableaux :
                    - etudiants sans stage ;
                    - etudiants avec stage.
                    La navigation correspondante est disponible dans la barre laterale gauche.
                    """;
        }

        return """
                Cote administration :
                - la liste des utilisateurs peut etre filtree par role et par statut ;
                - vous pouvez distinguer les comptes actifs et desactives ;
                - la gestion des charges de travail et des affectations est centralisee dans les pages dediees.
                """;
    }

    private String buildFallbackReply(String normalizedRole) {
        List<String> topics = new ArrayList<>();
        topics.add("dashboard et agenda");
        topics.add("sujets et candidatures");
        topics.add("rendez-vous et taches");
        topics.add("fichiers et messagerie");
        topics.add("profil et utilisateurs");

        if (containsAny(normalizedRole, "enseignant", "teacher", "encadrant")) {
            topics.add("charge de travail");
        }

        return "Je peux vous aider sur : " + String.join(", ", topics)
                + ". Posez une question plus precise, par exemple : \"Comment postuler a un sujet ?\" ou \"Comment voir les rendez-vous de cette semaine ?\"";
    }

    private boolean containsAny(String text, String... keywords) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        for (String keyword : keywords) {
            if (text.contains(normalize(keyword))) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value
                .trim()
                .toLowerCase(Locale.ROOT)
                .replace('é', 'e')
                .replace('è', 'e')
                .replace('ê', 'e')
                .replace('ë', 'e')
                .replace('à', 'a')
                .replace('â', 'a')
                .replace('ù', 'u')
                .replace('û', 'u')
                .replace('ü', 'u')
                .replace('î', 'i')
                .replace('ï', 'i')
                .replace('ô', 'o')
                .replace('ö', 'o')
                .replace('ç', 'c')
                .replace('\'', ' ');
    }
}
