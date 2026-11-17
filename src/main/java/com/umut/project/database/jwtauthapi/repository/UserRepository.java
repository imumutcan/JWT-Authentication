package com.umut.project.database.jwtauthapi.repository;

import com.umut.project.database.jwtauthapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long > {
    Optional <User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByMail(String mail);
}
