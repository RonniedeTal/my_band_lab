package com.my_band_lab.my_band_lab.controller;


import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.repository.UserRepository;
import com.my_band_lab.my_band_lab.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/saveUser")
    public User saveUser(@Valid @RequestBody User user) {return userService.saveUser(user);}

    @GetMapping("/user/id/{id}")
    User findUserById(@PathVariable Long id) throws Exception{
        return userService.findUserById(id);
    }

    @GetMapping("/user/name/{name}")
    User findUserByName(@PathVariable String name) throws Exception{
        return userService.findUserByName(name);
    }

    @GetMapping("/user/surname/{surname}")
    User findUserBySurname(@PathVariable String surname) throws Exception{
        return userService.findUserBySurname(surname);
    }

}
