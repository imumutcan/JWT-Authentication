package com.umut.project.database.jwtauthapi.exception;

public class UserAlreadyExistsException extends ResourceAlreadyExistsException{
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
