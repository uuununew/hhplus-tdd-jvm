package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserPointTest {

    @Test
    @DisplayName("포인트 충전 - 정상 동작")
    void chargeSuccess() {
        UserPoint userPoint = new UserPoint(1L, 1000L, System.currentTimeMillis());
        UserPoint charged = userPoint.charge(500L);

        assertThat(charged.point()).isEqualTo(1500L);
        assertThat(charged.id()).isEqualTo(userPoint.id());
    }

    @Test
    @DisplayName("포인트 충전 실패 - 최대값 초과")
    void chargeExceedMaxFail() {
        UserPoint userPoint = new UserPoint(1L, 999_999L, System.currentTimeMillis());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userPoint.charge(2L);
        });

        assertThat(exception.getMessage()).contains("최대");
    }

    @Test
    @DisplayName("포인트 사용 - 정상 동작")
    void useSuccess() {
        UserPoint userPoint = new UserPoint(1L, 1000L, System.currentTimeMillis());
        UserPoint used = userPoint.use(300L);

        assertThat(used.point()).isEqualTo(700L);
        assertThat(used.id()).isEqualTo(userPoint.id());
    }

    @Test
    @DisplayName("포인트 사용 실패 - 음수 금액")
    void useNegativeAmountFail() {
        UserPoint userPoint = new UserPoint(1L, 1000L, System.currentTimeMillis());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userPoint.use(-100L);
        });

        assertThat(exception.getMessage()).contains("1 이상");
    }

    @Test
    @DisplayName("포인트 사용 실패 - 잔액 부족")
    void useOverBalanceFail() {
        UserPoint userPoint = new UserPoint(1L, 100L, System.currentTimeMillis());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userPoint.use(200L);
        });

        assertThat(exception.getMessage()).contains("부족");
    }

}