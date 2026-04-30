package com.iit.internship_manager.domain.exceptions;

import com.iit.internship_manager.domain.enums.ErrorCode;

public class AccountDeactivatedException extends DomainException {

    public AccountDeactivatedException() {
        super(
                ErrorCode.ACCOUNT_DEACTIVATED,
                "Votre compte est désactivé. Veuillez contacter l'administration.");
    }
}