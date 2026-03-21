package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
