package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService{
    @Autowired
    private UserRepository userRepository;

    //saveUser
    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
