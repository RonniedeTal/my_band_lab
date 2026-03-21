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

    @GetMapping("/user/fullname")
    User findUserByFullName(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            @RequestParam(required = false) String fullName) throws Exception {

        // Si viene fullName, lo dividimos
        if (fullName != null && !fullName.isEmpty()) {
            String[] parts = fullName.split(" ");
            if (parts.length >= 2) {
                name = parts[0];
                surname = parts[1];
            }
        }

        // Validar que tenemos ambos campos
        if (name == null || surname == null || name.isEmpty() || surname.isEmpty()) {
            throw new Exception("Please provide name and surname");
        }

        return userService.findUserByNameAndSurname(name, surname);
    }
}
