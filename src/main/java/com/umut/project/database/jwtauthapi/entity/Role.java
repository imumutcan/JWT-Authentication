package com.umut.project.database.jwtauthapi.entity;

public enum Role {
    ADMIN("ADMIN"),
    USER("USER");


    private Role(String value)
    {
        this.value = value;
    }

    private String value;


    public String getValue()
    {
        return value;
    }
}
