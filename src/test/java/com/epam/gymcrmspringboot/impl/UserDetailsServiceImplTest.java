package com.epam.gymcrmspringboot.impl;

import com.epam.gymcrmspringboot.model.TraineeEntity;
import com.epam.gymcrmspringboot.model.TrainerEntity;
import com.epam.gymcrmspringboot.model.UserEntity;
import com.epam.gymcrmspringboot.repository.UserRepository;
import com.epam.gymcrmspringboot.service.LoginAttemptService;
import com.epam.gymcrmspringboot.service.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl Tests")
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("Should return locked exception when username is temporarily blocked")
    void shouldThrowLockedExceptionWhenBlocked() {
        when(loginAttemptService.isBlocked("John.Doe")).thenReturn(true);

        LockedException exception = assertThrows(LockedException.class,
                () -> userDetailsService.loadUserByUsername("John.Doe"));

        assertEquals("Account temporarily locked due to multiple failed login attempts. Try again later.", exception.getMessage());
        verify(loginAttemptService).isBlocked("John.Doe");
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Should load active user with non-locked account flags")
    void shouldLoadUserDetailsWhenNotBlocked() {
        UserEntity user = UserEntity.builder()
                .username("John.Doe")
                .password("encodedPass")
                .isActive(true)
                .build();
        user.setTrainer(new TrainerEntity());

        when(loginAttemptService.isBlocked("John.Doe")).thenReturn(false);
        when(userRepository.findActiveUserWithRoleLinks("John.Doe")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("John.Doe");

        assertEquals("John.Doe", details.getUsername());
        assertEquals("encodedPass", details.getPassword());
        assertTrue(details.isEnabled());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.getAuthorities().stream().anyMatch(authority -> "ROLE_TRAINER".equals(authority.getAuthority())));
        verify(loginAttemptService).isBlocked("John.Doe");
        verify(userRepository).findActiveUserWithRoleLinks("John.Doe");
    }

    @Test
    @DisplayName("Should allow trainee authority as well")
    void shouldLoadTraineeAuthority() {
        UserEntity user = UserEntity.builder()
                .username("Jane.Doe")
                .password("encodedPass")
                .isActive(true)
                .build();
        user.setTrainee(new TraineeEntity());

        when(loginAttemptService.isBlocked("Jane.Doe")).thenReturn(false);
        when(userRepository.findActiveUserWithRoleLinks("Jane.Doe")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("Jane.Doe");

        assertTrue(details.getAuthorities().stream().anyMatch(authority -> "ROLE_TRAINEE".equals(authority.getAuthority())));
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when active user does not exist")
    void shouldThrowWhenActiveUserMissing() {
        when(loginAttemptService.isBlocked("missing.user")).thenReturn(false);
        when(userRepository.findActiveUserWithRoleLinks("missing.user")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("missing.user"));
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user has no mapped role")
    void shouldThrowWhenUserHasNoRoleMapping() {
        UserEntity user = UserEntity.builder()
                .username("norole.user")
                .password("encodedPass")
                .isActive(true)
                .build();

        when(loginAttemptService.isBlocked("norole.user")).thenReturn(false);
        when(userRepository.findActiveUserWithRoleLinks("norole.user")).thenReturn(Optional.of(user));

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("norole.user"));
    }
}


