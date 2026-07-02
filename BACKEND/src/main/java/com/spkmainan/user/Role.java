package com.spkmainan.user;

/**
 * User roles. Stored as a string in the database and exposed as an
 * authority named {@code ROLE_<NAME>} to Spring Security.
 */
public enum Role {
    ADMIN,
    USER
}
