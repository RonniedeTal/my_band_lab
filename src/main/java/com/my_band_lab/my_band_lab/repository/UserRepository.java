package com.my_band_lab.my_band_lab.repository;

import com.my_band_lab.my_band_lab.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {



    Optional<User> findByNameIgnoreCase(String name);

    Optional<User> findBySurnameIgnoreCase(String surname);

    Optional<User> findByNameIgnoreCaseAndSurnameIgnoreCase(String name, String surname);

    Optional<User> findByEmailIgnoreCase(@NotBlank(message = "Please introduce your email") @Email(message = "Please provide a valid email address") String email);
}
