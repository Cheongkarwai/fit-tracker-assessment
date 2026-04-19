package org.cheong.fittrackermiddleware.service;

import lombok.extern.slf4j.Slf4j;
import org.cheong.fittrackermiddleware.adapter.AuthorizedFitTrackerAdapter;
import org.cheong.fittrackermiddleware.dto.UserResponse;
import org.cheong.fittrackermiddleware.model.User;
import org.cheong.fittrackermiddleware.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    private final AuthorizedFitTrackerAdapter authorizedFitTrackerAdapter;

    public UserService(UserRepository userRepository,
                       AuthorizedFitTrackerAdapter authorizedFitTrackerAdapter){
        this.userRepository = userRepository;
        this.authorizedFitTrackerAdapter = authorizedFitTrackerAdapter;
    }

    public Mono<User> saveUser(User user) {
        user.setNew(true);
        return userRepository.save(user);
    }

    public Mono<Boolean> checkIfUserExists(String userId) {
        return userRepository.existsById(userId);
    }

    public Mono<UserResponse> getFitTrackerUser(String userId) {
        return authorizedFitTrackerAdapter.getUser(userId)
                .doOnNext(userResponse -> log.info("Get user from fit tracker={}", userResponse));
    }
}
