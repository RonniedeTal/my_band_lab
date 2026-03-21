package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.entity.User;

public interface UserService {

    User saveUser(User user);
    User findUserById(Long id) throws Exception;

    User findUserByName(String name) throws Exception;

    User findUserBySurname(String surname) throws Exception;
}
