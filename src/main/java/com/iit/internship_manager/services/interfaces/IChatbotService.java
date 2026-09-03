package com.iit.internship_manager.services.interfaces;

public interface IChatbotService {
    boolean isConfigured();

    String ask(String userRole, String userName, String question);

    boolean requiresExplicitConfirmation(String question);
}
