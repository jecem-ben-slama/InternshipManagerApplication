// UserType.java
package com.iit.internship_manager.domain.enums;

// Used only for routing registration requests.
// Kept separate from Role because Role is a security/business concept,
// UserType is purely a DTO dispatching concept.
public enum UserType {
    ADMIN_IT,
    STUDENT,
    TEACHER
}