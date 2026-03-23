package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.entity.User;
import jakarta.validation.Valid;

import java.util.List;

public interface UserService {

    User saveUser(User user);

    User findUserById(Long id) throws Exception;

    User findUserByName(String name) throws Exception;

    User findUserBySurname(String surname) throws Exception;

    User findUserByNameAndSurname(String name, String surname) throws Exception;

    User updateUser(Long id, @Valid User user) throws Exception;

    void deleteUser(Long id) throws Exception;

    List<User> findAllUsers() throws Exception;
}
