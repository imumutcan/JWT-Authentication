package com.umut.project.database.jwtauthapi.exception;

import org.springframework.http.HttpStatus;

public class ResourceAlreadyExistsException extends BaseException{
    public ResourceAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
