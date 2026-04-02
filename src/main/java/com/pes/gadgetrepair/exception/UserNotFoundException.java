package com.pes.gadgetrepair.exception;

/*
 Custom Exception

 Purpose:
 Thrown when a user cannot be found in the system.

 Example Usage:
 userRepository.findById(id)
*/

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(Long userId) {
        super("User not found with ID: " + userId);
    }

    public UserNotFoundException(String field, String value) {
        super("User not found with " + field + ": " + value);
    }
}