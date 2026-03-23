package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class UserGraphQLController {

    @Autowired
    private UserService userService;

    // Query: Obtener usuario por ID
    @QueryMapping
    public User userById(@Argument Long id) throws Exception {
        return userService.findUserById(id);
    }

    // Query: Obtener usuario por nombre
    @QueryMapping
    public User userByName(@Argument String name) throws Exception {
        return userService.findUserByName(name);
    }

    // Query: Obtener usuario por apellido
    @QueryMapping
    public User userBySurname(@Argument String surname) throws Exception {
        return userService.findUserBySurname(surname);
    }

    // Query: Obtener usuario por nombre y apellido
    @QueryMapping
    public User userByNameAndSurname(@Argument String name, @Argument String surname) throws Exception {
        return userService.findUserByNameAndSurname(name, surname);
    }

    // Query: Obtener todos los usuarios
    @QueryMapping
    public List<User> users() throws Exception {
        return userService.findAllUsers();
    }

    // Mutation: Crear usuario
    @MutationMapping
    public User createUser(
            @Argument String name,
            @Argument String surname,
            @Argument String email,
            @Argument String password,
            @Argument String profileImageUrl) throws Exception {

        User user = User.builder()
                .name(name)
                .surname(surname)
                .email(email)
                .password(password)
                .profileImageUrl(profileImageUrl)
                .build();

        return userService.saveUser(user);
    }

    // Mutation: Actualizar usuario
    @MutationMapping
    public User updateUser(
            @Argument Long id,
            @Argument String name,
            @Argument String surname,
            @Argument String email,
            @Argument String password,
            @Argument String profileImageUrl) throws Exception {

        User userDetails = User.builder()
                .name(name)
                .surname(surname)
                .email(email)
                .password(password)
                .profileImageUrl(profileImageUrl)
                .build();

        return userService.updateUser(id, userDetails);
    }

    // Mutation: Eliminar usuario
    @MutationMapping
    public Boolean deleteUser(@Argument Long id) throws Exception {
        userService.deleteUser(id);
        return true;
    }
}