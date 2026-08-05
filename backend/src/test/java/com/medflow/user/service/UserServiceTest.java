package com.medflow.user.service;

import com.medflow.common.exception.UserNotFoundException;
import com.medflow.user.entity.User;
import com.medflow.user.entity.UserRole;
import com.medflow.user.entity.UserStatus;
import com.medflow.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getUsers_withRoleAndStatus_returnsFilteredPage() {
        PageRequest pageable = PageRequest.of(0, 20);
        User user = mock(User.class);

        when(userRepository.findAllByRoleAndStatus(
                UserRole.PATIENT,
                UserStatus.ACTIVE,
                pageable
        )).thenReturn(new PageImpl<>(List.of(user), pageable, 1));

        var response = userService.getUsers(
                UserRole.PATIENT,
                UserStatus.ACTIVE,
                pageable
        );

        assertThat(response.content()).hasSize(1);
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(20);
        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.totalPages()).isEqualTo(1);
    }

    @Test
    void getUsers_withoutFilters_returnsAllUsersPage() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(userRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        assertThat(userService.getUsers(null, null, pageable).content()).isEmpty();
        verify(userRepository).findAll(pageable);
    }

    @Test
    void getUsers_withRole_returnsRoleFilteredPage() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(userRepository.findAllByRole(UserRole.DOCTOR, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        userService.getUsers(UserRole.DOCTOR, null, pageable);

        verify(userRepository).findAllByRole(UserRole.DOCTOR, pageable);
    }

    @Test
    void getUsers_withStatus_returnsStatusFilteredPage() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(userRepository.findAllByStatus(UserStatus.WITHDRAWN, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        userService.getUsers(null, UserStatus.WITHDRAWN, pageable);

        verify(userRepository).findAllByStatus(UserStatus.WITHDRAWN, pageable);
    }

    @Test
    void getUser_whenUserDoesNotExist_throwsException() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(userId))
                .isInstanceOf(UserNotFoundException.class);
    }
}
