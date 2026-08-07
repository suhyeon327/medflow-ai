package com.medflow.user.service;

import com.medflow.common.exception.ErrorCode;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @InjectMocks UserService userService;

    @Test
    void 관리자가_사용자_단건을_조회한다() {
        // given
        User user = User.create("patient@test.com", "password", UserRole.PATIENT);
        ReflectionTestUtils.setField(user, "id", 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        var response = userService.getUser(1L);

        // then
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("patient@test.com");
        assertThat(response.role()).isEqualTo(UserRole.PATIENT);
        assertThat(response.status()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void 존재하지_않는_사용자_조회는_실패한다() {
        // given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUser(999L))
                .isInstanceOf(UserNotFoundException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void 역할과_상태를_함께_적용해_사용자를_조회한다() {
        // given
        PageRequest pageable = PageRequest.of(0, 20);
        User user = User.create("patient@test.com", "password", UserRole.PATIENT);
        when(userRepository.findAllByRoleAndStatus(
                UserRole.PATIENT, UserStatus.ACTIVE, pageable
        )).thenReturn(new PageImpl<>(List.of(user), pageable, 1));

        // when
        var response = userService.getUsers(UserRole.PATIENT, UserStatus.ACTIVE, pageable);

        // then
        assertThat(response.content()).hasSize(1);
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(20);
        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.totalPages()).isEqualTo(1);
        verify(userRepository).findAllByRoleAndStatus(UserRole.PATIENT, UserStatus.ACTIVE, pageable);
    }

    @Test
    void 필터가_없으면_전체_사용자를_페이징_조회한다() {
        // given
        PageRequest pageable = PageRequest.of(0, 20);
        when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        // when
        var response = userService.getUsers(null, null, pageable);

        // then
        assertThat(response.content()).isEmpty();
        verify(userRepository).findAll(pageable);
    }

    @Test
    void 역할로_사용자를_필터링한다() {
        // given
        PageRequest pageable = PageRequest.of(0, 20);
        when(userRepository.findAllByRole(UserRole.DOCTOR, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        // when
        userService.getUsers(UserRole.DOCTOR, null, pageable);

        // then
        verify(userRepository).findAllByRole(UserRole.DOCTOR, pageable);
    }

    @Test
    void 상태로_사용자를_필터링한다() {
        // given
        PageRequest pageable = PageRequest.of(0, 20);
        when(userRepository.findAllByStatus(UserStatus.WITHDRAWN, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        // when
        userService.getUsers(null, UserStatus.WITHDRAWN, pageable);

        // then
        verify(userRepository).findAllByStatus(UserStatus.WITHDRAWN, pageable);
    }

    @Test
    void 두_번째_페이지의_페이징_정보를_반환한다() {
        // given
        PageRequest pageable = PageRequest.of(1, 2);
        List<User> users = List.of(
                User.create("user3@test.com", "password", UserRole.PATIENT),
                User.create("user4@test.com", "password", UserRole.DOCTOR)
        );
        when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(users, pageable, 5));

        // when
        var response = userService.getUsers(null, null, pageable);

        // then
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.totalElements()).isEqualTo(5);
        assertThat(response.totalPages()).isEqualTo(3);
    }
}
