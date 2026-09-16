package com.medflow.security.principal;

import com.medflow.user.entity.User;
import com.medflow.user.entity.UserRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserPrincipalTest {

    @Test
    void 활성_사용자는_로그인_인증이_허용된다() {
        // given
        User user = User.create("active@example.com", "password", UserRole.PATIENT);

        // when
        UserPrincipal principal = UserPrincipal.from(user);

        // then
        assertThat(principal.isEnabled()).isTrue();
    }

    @Test
    void 탈퇴_사용자는_로그인_인증이_거부된다() {
        // given
        User user = User.create("withdrawn@example.com", "password", UserRole.PATIENT);
        user.withdraw();

        // when
        UserPrincipal principal = UserPrincipal.from(user);

        // then
        assertThat(principal.isEnabled()).isFalse();
    }
}
