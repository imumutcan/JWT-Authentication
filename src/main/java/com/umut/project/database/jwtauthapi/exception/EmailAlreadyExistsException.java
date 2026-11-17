package com.umut.project.database.jwtauthapi.exception;

public class EmailAlreadyExistsException extends ResourceAlreadyExistsException{
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
