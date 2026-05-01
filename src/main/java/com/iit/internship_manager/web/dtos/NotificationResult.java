package com.iit.internship_manager.web.dtos;

public record NotificationResult(boolean sent, String reason) {

        public static NotificationResult ok() {
                return new NotificationResult(true, null);
        }

        public static NotificationResult failed(String reason) {
                return new NotificationResult(false, reason);
        }
}