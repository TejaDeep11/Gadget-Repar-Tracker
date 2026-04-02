package com.pes.gadgetrepair.service.impl;

import com.pes.gadgetrepair.model.User;
import com.pes.gadgetrepair.repository.UserRepository;
import com.pes.gadgetrepair.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Optional;

/*
Design Principles Used:
1. Single Responsibility Principle
2. Dependency Inversion Principle
3. Constructor-based Dependency Injection

Role:
Handles authentication and user management logic.
*/
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User authenticate(String email,String password) {

        Optional<User> userOpt = userRepository.findByEmail(email);

        if(userOpt.isPresent()){

            User user = userOpt.get();

            if(user.getPasswordHash().equals(password)){
                return user;
            }

        }

        return null;
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}