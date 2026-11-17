package com.umut.project.database.jwtauthapi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username cannot be null!")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Password cannot be null!")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Email cannot be null!")
    @Email(message = "Please enter a valid email address!")
    @Column(nullable = false, unique = true)
    private String mail;

    @NotBlank(message = "Role cannot be null!")
    @Column(nullable = false)
    private String role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
