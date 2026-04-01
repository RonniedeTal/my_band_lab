package com.my_band_lab.my_band_lab.repository;

import com.my_band_lab.my_band_lab.entity.Role;
import com.my_band_lab.my_band_lab.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional  // Cada test se ejecuta en una transacción que se revierte al final
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    // Usamos emails únicos para cada test para evitar conflictos
    private static long counter = 0;

    private User createUniqueUser(String baseEmail, String name, String surname, Role role) {
        counter++;
        return User.builder()
                .name(name)
                .surname(surname)
                .email(baseEmail + counter + "@test.com")
                .password("encodedPassword")
                .role(role)
                .build();
    }

    // ==================== TESTS: save ====================

    @Test
    @DisplayName("✅ Debe guardar un nuevo usuario")
    void save_ShouldPersistUser() {
        User newUser = createUniqueUser("save", "Nuevo", "Usuario", Role.USER);

        User saved = userRepository.save(newUser);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo(newUser.getEmail());
        assertThat(saved.getName()).isEqualTo("Nuevo");
    }

    // ==================== TESTS: findByEmailIgnoreCase ====================

    @Test
    @DisplayName("✅ Debe encontrar usuario por email (case-insensitive)")
    void findByEmailIgnoreCase_ShouldReturnUser_WhenEmailExists() {
        User user = createUniqueUser("email", "EmailTest", "User", Role.USER);
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmailIgnoreCase(user.getEmail().toUpperCase());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(user.getEmail());
        assertThat(found.get().getName()).isEqualTo("EmailTest");
    }

    @Test
    @DisplayName("❌ No debe encontrar usuario cuando email no existe")
    void findByEmailIgnoreCase_ShouldReturnEmpty_WhenEmailDoesNotExist() {
        Optional<User> found = userRepository.findByEmailIgnoreCase("nonexistent_" + System.currentTimeMillis() + "@example.com");

        assertThat(found).isEmpty();
    }

    // ==================== TESTS: findByNameIgnoreCase ====================

    @Test
    @DisplayName("✅ Debe encontrar usuario por nombre (case-insensitive)")
    void findByNameIgnoreCase_ShouldReturnUser_WhenNameExists() {
        User user = createUniqueUser("name", "NombreTest", "Apellido", Role.USER);
        userRepository.save(user);

        Optional<User> found = userRepository.findByNameIgnoreCase("NOMBRETEST");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("NombreTest");
    }

    @Test
    @DisplayName("❌ No debe encontrar usuario cuando nombre no existe")
    void findByNameIgnoreCase_ShouldReturnEmpty_WhenNameDoesNotExist() {
        Optional<User> found = userRepository.findByNameIgnoreCase("NoExiste" + System.currentTimeMillis());

        assertThat(found).isEmpty();
    }

    // ==================== TESTS: findBySurnameIgnoreCase ====================

    @Test
    @DisplayName("✅ Debe encontrar usuario por apellido (case-insensitive)")
    void findBySurnameIgnoreCase_ShouldReturnUser_WhenSurnameExists() {
        User user = createUniqueUser("surname", "SurnameTest", "ApellidoTest", Role.USER);
        userRepository.save(user);

        Optional<User> found = userRepository.findBySurnameIgnoreCase("APELLIDOTEST");

        assertThat(found).isPresent();
        assertThat(found.get().getSurname()).isEqualTo("ApellidoTest");
    }

    // ==================== TESTS: findByNameIgnoreCaseAndSurnameIgnoreCase ====================

    @Test
    @DisplayName("✅ Debe encontrar usuario por nombre y apellido")
    void findByNameIgnoreCaseAndSurnameIgnoreCase_ShouldReturnUser_WhenBothMatch() {
        User user = createUniqueUser("full", "FullName", "FullSurname", Role.USER);
        userRepository.save(user);

        Optional<User> found = userRepository.findByNameIgnoreCaseAndSurnameIgnoreCase("FULLNAME", "FULLSURNAME");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("FullName");
        assertThat(found.get().getSurname()).isEqualTo("FullSurname");
    }

    @Test
    @DisplayName("❌ No debe encontrar usuario cuando nombre no coincide")
    void findByNameIgnoreCaseAndSurnameIgnoreCase_ShouldReturnEmpty_WhenNameDoesNotMatch() {
        User user = createUniqueUser("mismatch", "CorrectName", "CorrectSurname", Role.USER);
        userRepository.save(user);

        Optional<User> found = userRepository.findByNameIgnoreCaseAndSurnameIgnoreCase("WrongName", "CorrectSurname");

        assertThat(found).isEmpty();
    }

    // ==================== TESTS: findByRole ====================

    @Test
    @DisplayName("✅ Debe encontrar todos los usuarios con rol USER")
    void findByRole_ShouldReturnUsers_WhenRoleUser() {
        User user1 = createUniqueUser("user1", "UserOne", "UserOneSurname", Role.USER);
        User user2 = createUniqueUser("user2", "UserTwo", "UserTwoSurname", Role.USER);
        userRepository.save(user1);
        userRepository.save(user2);

        List<User> users = userRepository.findByRole(Role.USER);

        assertThat(users).isNotEmpty();
        assertThat(users.stream().filter(u -> u.getRole() == Role.USER).count()).isEqualTo(users.size());
    }

    @Test
    @DisplayName("✅ Debe encontrar todos los usuarios con rol ARTIST")
    void findByRole_ShouldReturnUsers_WhenRoleArtist() {
        User artist = createUniqueUser("artist", "ArtistName", "ArtistSurname", Role.ARTIST);
        userRepository.save(artist);

        List<User> users = userRepository.findByRole(Role.ARTIST);

        assertThat(users).isNotEmpty();
        assertThat(users.stream().anyMatch(u -> u.getRole() == Role.ARTIST)).isTrue();
    }

    @Test
    @DisplayName("✅ Debe encontrar todos los usuarios con rol ADMIN")
    void findByRole_ShouldReturnUsers_WhenRoleAdmin() {
        User admin = createUniqueUser("admin", "AdminName", "AdminSurname", Role.ADMIN);
        userRepository.save(admin);

        List<User> users = userRepository.findByRole(Role.ADMIN);

        assertThat(users).isNotEmpty();
        assertThat(users.stream().anyMatch(u -> u.getRole() == Role.ADMIN)).isTrue();
    }

    // ==================== TESTS: findAll ====================

    @Test
    @DisplayName("✅ Debe encontrar todos los usuarios")
    void findAll_ShouldReturnAllUsers() {
        User user1 = createUniqueUser("findAll1", "FindAll1", "FindAll1", Role.USER);
        User user2 = createUniqueUser("findAll2", "FindAll2", "FindAll2", Role.USER);
        userRepository.save(user1);
        userRepository.save(user2);

        List<User> users = userRepository.findAll();

        assertThat(users).hasSizeGreaterThanOrEqualTo(2);
    }

    // ==================== TESTS: findById ====================

    @Test
    @DisplayName("✅ Debe encontrar usuario por ID")
    void findById_ShouldReturnUser_WhenIdExists() {
        User user = createUniqueUser("findById", "FindById", "FindById", Role.USER);
        User saved = userRepository.save(user);

        Optional<User> found = userRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("❌ No debe encontrar usuario cuando ID no existe")
    void findById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        Optional<User> found = userRepository.findById(999999L);

        assertThat(found).isEmpty();
    }

    // ==================== TESTS: delete ====================

    @Test
    @DisplayName("✅ Debe eliminar un usuario")
    void delete_ShouldRemoveUser() {
        User user = createUniqueUser("delete", "Delete", "Delete", Role.USER);
        User saved = userRepository.save(user);
        Long userId = saved.getId();

        userRepository.delete(saved);

        Optional<User> found = userRepository.findById(userId);
        assertThat(found).isEmpty();
    }
}