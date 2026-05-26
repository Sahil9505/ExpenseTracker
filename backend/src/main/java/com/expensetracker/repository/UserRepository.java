package com.expensetracker.repository;

import com.expensetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email for login
    Optional<User> findByEmail(String email);

    // Check if an email already exists (used during registration)
    boolean existsByEmail(String email);
}
