package io.hhplus.tdd.point;

import io.hhplus.tdd.Exception.PointErrorCode;
import io.hhplus.tdd.Exception.PointException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.useRepresentation;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PointIntegrationTest {

    @Autowired
    PointService pointService;

    @Test
    @DisplayName("포인트 조회 성공 - 기존 보유 포인트가 올바르게 조회된다")
    void findSuccess_AddsToExistingBalance(){
        //given
        long userId = 1L;
        long chargeAmount = 1000L;

        //when
        UserPoint result = pointService.charge(userId, chargeAmount);

        //then
        assertThat(result.point()).isEqualTo(1000L);
        assertThat(result.id()).isEqualTo(userId);
    }

    @Test
    @DisplayName("포인트 조회 성공 - 존재하지 않는 ID도 0 포인트로 초기화되어 반환된다.")
    void userPointReturns_DefaultWhenUserNotExists() {
        // given
        long userId = 999L;

        // when
        UserPoint result = pointService.getUserPoint(userId);

        // then
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(0L); // 기본값
    }

    @Test
    @DisplayName("포인트 충전 성공 - 기존 보유 포인트에 충전 금액이 누적된다")
    void chargeSuccess_MultipleCharge() {
        //given
        long userId = 2L;

        //when
        pointService.charge(userId, 300L);
        UserPoint result = pointService.charge(userId, 700L);

        //then
        assertThat(result.point()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("포인트 충전 실패 - 충전 금액이 0보다 작을 경우 예외 발생")
    void chargePointFail_NegativeAmount(){
        //given
        long userId = 10L;
        long chargeAmount = -1000L;

        //when //then
        PointException exception = assertThrows(PointException.class, () -> pointService.charge(userId, chargeAmount));
        assertThat(exception.getErrorCode()).isEqualTo(PointErrorCode.CHARGE_AMOUNT_LESS_THAN_ZERO);
    }

    @Test
    @DisplayName("포인트 충전 실패 - 보유 가능한 최대 포인트 초과 시 예외 발생")
    void chargePointFail_ExceedMax(){
        //given
        long userId = 11L;
        pointService.charge(userId, 1_000_000L);

        //when //then
        PointException exception = assertThrows(PointException.class, () -> pointService.charge(userId, 1L));
        assertThat(exception.getErrorCode()).isEqualTo(PointErrorCode.CHARGE_AMOUNT_GREATER_THAN_MAX);
    }

    @Test
    @DisplayName("포인트 사용 성공 - 보유 포인트에서 사용 금액만큼 차감된다")
    void useSuccess_WhenLessThanBalance(){
        //given
        long userId = 3L;
        pointService.charge(userId, 1000L);

        //when
        UserPoint result = pointService.use(userId, 400L);

        //then
        assertThat(result.point()).isEqualTo(600L);
        assertThat(result.id()).isEqualTo(userId);
    }

    @Test
    @DisplayName("포인트 사용 성공 - 잔액을 전부 사용한다.")
    void useSuccess_UseFullBalance(){
        //given
        long userId = 4L;
        pointService.charge(userId, 500L);

        //when
        UserPoint result = pointService.use(userId, 500L);

        //then
        assertThat(result.point()).isEqualTo(0L);
    }

    @Test
    @DisplayName("포인트 사용 실패 - 사용 금액이 0 이하일 경우 예외 발생")
    void usePointFail_NegativeAmount(){
        //given
        long userId = 20L;
        pointService.charge(userId, 1000L);

        //when //then
        PointException exception = assertThrows(PointException.class, () -> pointService.use(userId, -100L));
        assertThat(exception.getErrorCode()).isEqualTo(PointErrorCode.USE_AMOUNT_LESS_THAN_ZERO);
    }

    @Test
    @DisplayName("포인트 사용 실패 - 사용 금액이 잔액보다 많을 경우 예외 발생")
    void usePointFail_InsufficientBalance(){
        //given
        long userId = 21L;
        pointService.charge(userId, 100L);

        //when //then
        PointException exception = assertThrows(PointException.class, () -> pointService.use(userId, 500L));
        assertThat(exception.getErrorCode()).isEqualTo(PointErrorCode.BALANCE_LESS_THAN_USE_AMOUNT);
    }

    @Test
    @DisplayName("포인트 내역 조회 성공 - 충전과 사용 이력이 정확히 반환된다")
    void userPointHistorySuccess(){
        //given
        long userId = 4L;
        pointService.charge(userId, 1000L);
        pointService.use(userId, 300L);

        //when
        List<PointHistory> histories = pointService.getUserPointHistory(userId);

        //then
        assertThat(histories).hasSize(2);
        assertThat(histories.get(0).type()).isEqualTo(TransactionType.CHARGE);
        assertThat(histories.get(1).type()).isEqualTo(TransactionType.USE);
    }

    @Test
    @DisplayName("포인트 사용 실패 - 사용자 ID가 존재하지 않을 경우 예외 발생")
    void usePointFail_UserNotFound() {
        // given
        long userId = 9999L;

        // when & then
        PointException exception = assertThrows(PointException.class, () -> pointService.use(userId, 100L));
        assertThat(exception.getErrorCode()).isEqualTo(PointErrorCode.BALANCE_LESS_THAN_USE_AMOUNT);
    }

}

