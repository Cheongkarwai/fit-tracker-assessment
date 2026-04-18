package org.cheong.fittrackermiddleware.service;

import org.cheong.fittrackermiddleware.model.User;
import org.cheong.fittrackermiddleware.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public Mono<User> saveUser(User user) {
        return userRepository.save(user);
    }

    public Mono<Boolean> checkIfUserExists(String userId) {
        return userRepository.existsById(userId);
    }
}
