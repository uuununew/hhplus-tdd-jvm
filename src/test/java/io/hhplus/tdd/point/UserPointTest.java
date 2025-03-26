package io.hhplus.tdd.point;

import io.hhplus.tdd.Exception.PointErrorCode;
import io.hhplus.tdd.Exception.PointException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

class UserPointTest {

    @Test
    @DisplayName("포인트 충전 - 정상 동작")
    void chargeSuccess() {
        //given
        UserPoint userPoint = new UserPoint(1L, 1000L, System.currentTimeMillis());

        //when
        UserPoint charged = userPoint.charge(500L);

        //then
        assertThat(charged.point()).isEqualTo(1500L);
        assertThat(charged.id()).isEqualTo(userPoint.id());
    }

    @Test
    @DisplayName("포인트 충전 실패 - 최대값 초과")
    void chargeExceedMaxFail() {
        //given
        UserPoint userPoint = new UserPoint(1L, 999_999L, System.currentTimeMillis());

        //when
        PointException exception = assertThrows(PointException.class, () -> {
            userPoint.charge(2L);
        });

        //then
        assertThat(exception.getErrorCode()).isEqualTo(PointErrorCode.CHARGE_AMOUNT_GREATER_THAN_MAX);
    }

    @Test
    @DisplayName("포인트 사용 - 정상 동작")
    void useSuccess() {
        //given
        UserPoint userPoint = new UserPoint(1L, 1000L, System.currentTimeMillis());

        //when
        UserPoint used = userPoint.use(300L);

        //then
        assertThat(used.point()).isEqualTo(700L);
        assertThat(used.id()).isEqualTo(userPoint.id());
    }

    @Test
    @DisplayName("포인트 사용 실패 - 음수 금액")
    void useNegativeAmountFail() {
        //given
        UserPoint userPoint = new UserPoint(1L, 1000L, System.currentTimeMillis());

        //when
        PointException exception = assertThrows(PointException.class, () -> {
            userPoint.use(-100L);
        });

        //then
        assertThat(exception.getErrorCode()).isEqualTo(PointErrorCode.USE_AMOUNT_LESS_THAN_ZERO);
    }

    @Test
    @DisplayName("포인트 사용 실패 - 잔액 부족")
    void useOverBalanceFail() {
        //given
        UserPoint userPoint = new UserPoint(1L, 100L, System.currentTimeMillis());

        //when
        PointException exception = assertThrows(PointException.class, () -> {
            userPoint.use(200L);
        });

        //then
        assertThat(exception.getErrorCode()).isEqualTo(PointErrorCode.BALANCE_LESS_THAN_USE_AMOUNT);
    }

}