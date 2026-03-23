package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{
    @Autowired
    private UserRepository userRepository;

    //saveUser
    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User findUserById(Long id) throws Exception {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new Exception("User not found");
        }
        return user.get();
    }

    @Override
    public User findUserByName(String name) throws Exception{
        Optional<User> user = userRepository.findByNameIgnoreCase(name);
        if (user.isEmpty()) {
            throw new Exception("User not found");
        }
        return user.get();
    }

    @Override
    public User findUserBySurname(String surname) throws Exception {
        Optional<User> user = userRepository.findBySurnameIgnoreCase(surname);
        if (user.isEmpty()) {
            throw new Exception("User not found");
        }
        return user.get();
    }

    @Override
    public User findUserByNameAndSurname(String name, String surname) throws Exception {
        Optional<User> user = userRepository.findByNameIgnoreCaseAndSurnameIgnoreCase(name, surname);
        if (user.isEmpty()) {
            throw new Exception("User not found with name: " + name + " and surname: " + surname);
        }
        return user.get();
    }

    @Override
    public User updateUser(Long id, User userDetails) throws Exception {
        // Buscar el usuario existente
        User existingUser = findUserById(id);

        // Actualizar solo los campos que vienen en la petición
        if (userDetails.getName() != null && !userDetails.getName().isEmpty()) {
            existingUser.setName(userDetails.getName());
        }

        if (userDetails.getSurname() != null && !userDetails.getSurname().isEmpty()) {
            existingUser.setSurname(userDetails.getSurname());
        }

        if (userDetails.getEmail() != null && !userDetails.getEmail().isEmpty()) {
            // Verificar si el nuevo email ya está en uso por otro usuario
            Optional<User> userWithEmail = userRepository.findByEmailIgnoreCase(userDetails.getEmail());
            if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(id)) {
                throw new Exception("Email already in use: " + userDetails.getEmail());
            }
            existingUser.setEmail(userDetails.getEmail());
        }

        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            existingUser.setPassword(userDetails.getPassword());
        }

        if (userDetails.getProfileImageUrl() != null) {
            existingUser.setProfileImageUrl(userDetails.getProfileImageUrl());
        }

        // El updatedAt se actualizará automáticamente con @PreUpdate
        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(Long id) throws Exception {
        User user = findUserById(id);
        userRepository.delete(user);
    }
    @Override
    public List<User> findAllUsers() throws Exception {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new Exception("No users found");
        }
        return users;
    }

}
