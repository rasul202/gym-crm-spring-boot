package com.epam.gymcrmspringboot.service.impl;

import com.epam.gymcrmspringboot.model.TraineeEntity;
import com.epam.gymcrmspringboot.model.TrainerEntity;
import com.epam.gymcrmspringboot.model.UserEntity;
import com.epam.gymcrmspringboot.repository.UserRepository;
import com.epam.gymcrmspringboot.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    @NonNull
    private final UserRepository userRepository;
    @NonNull
    private final LoginAttemptService loginAttemptService;

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        if (loginAttemptService.isBlocked(username)) {
            throw new LockedException("Account temporarily locked due to multiple failed login attempts. Try again later.");
        }

        UserEntity user = userRepository.findActiveUserWithRoleLinks(username)
                .orElseThrow(() -> new UsernameNotFoundException("Active user not found: " + username));

        List<GrantedAuthority> authorities = resolveAuthorities(user);

        return new User(
                user.getUsername(),
                user.getPassword(),
                Boolean.TRUE.equals(user.getIsActive()),
                true,
                true,
                true,
                authorities);
    }


    private List<GrantedAuthority> resolveAuthorities(UserEntity user) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        TrainerEntity trainer = user.getTrainer();
        if (trainer != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_TRAINER"));
        }

        TraineeEntity trainee = user.getTrainee();
        if (trainee != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_TRAINEE"));
        }

        if (authorities.isEmpty()) {
            throw new UsernameNotFoundException("No role mapping found for active user: " + user.getUsername());
        }

        return authorities;
    }


}


