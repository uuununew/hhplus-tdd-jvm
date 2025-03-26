package io.hhplus.tdd.point;

import io.hhplus.tdd.Exception.PointErrorCode;
import io.hhplus.tdd.Exception.PointException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceImplTest {

    @Mock
    private PointRepository pointRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @InjectMocks
    private PointServiceImpl pointService;


    @Test
    @DisplayName("유저 포인트 조회 성공")
    void getUserPointSuccess() {
        long userId = 1L;
        UserPoint userPoint = new UserPoint(userId, 1000L, System.currentTimeMillis());

        when(pointRepository.selectById(userId)).thenReturn(Optional.of(userPoint));

        UserPoint result = pointService.getUserPoint(userId);

        assertThat(result).isEqualTo(userPoint);
    }

    @Test
    @DisplayName("포인트 충전 성공")
    void chargePointSuccess() {
        long userId = 1L;
        long amount = 1000L;
        UserPoint existing = new UserPoint(userId, 500L, System.currentTimeMillis());
        UserPoint charged = existing.charge(amount);

        when(pointRepository.selectById(userId)).thenReturn(Optional.of(existing));
        when(pointRepository.insertOrUpdate(any())).thenReturn(charged);

        UserPoint result = pointService.charge(userId, amount);

        assertThat(result.point()).isEqualTo(1500L);
        verify(pointHistoryRepository).insert(any(PointHistory.class));
    }

    @Test
    @DisplayName("포인트 충전 실패 - 음수 입력")
    void chargePointNegativeAmountFail() {
        try {
            pointService.charge(1L, -1000L);
            fail("예외가 발생해야 합니다.");
        } catch (PointException e) {
            assertEquals(PointErrorCode.CHARGE_AMOUNT_LESS_THAN_ZERO.getMessage(), e.getMessage());
        }
    }

    @Test
    @DisplayName("포인트 충전 실패 - 최대값 초과")
    void chargePointExceedMaxFail() {
        try {
            pointService.charge(1L, 1_000_001L);
            fail("예외가 발생해야 합니다.");
        } catch (PointException e) {
            assertEquals(PointErrorCode.CHARGE_AMOUNT_GREATER_THAN_MAX.getMessage(), e.getMessage());
        }
    }

    @Test
    @DisplayName("포인트 사용 성공")
    void usePointSuccess() {
        long userId = 1L;
        long amount = 300L;
        UserPoint existing = new UserPoint(userId, 1000L, System.currentTimeMillis());
        UserPoint used = existing.use(amount);

        when(pointRepository.selectById(userId)).thenReturn(Optional.of(existing));
        when(pointRepository.insertOrUpdate(any())).thenReturn(used);

        UserPoint result = pointService.use(userId, amount);

        assertThat(result.point()).isEqualTo(700L);
        verify(pointHistoryRepository).insert(any(PointHistory.class));
    }

    @Test
    @DisplayName("포인트 사용 실패 - 음수 입력")
    void usePointNegativeAmountFail() {
        try {
            pointService.use(1L, -500L);
            fail("예외가 발생해야 합니다.");
        } catch (PointException e) {
            assertThat(e.getMessage()).isEqualTo(PointErrorCode.USE_AMOUNT_LESS_THAN_ZERO.getMessage());
        }
    }

    @Test
    @DisplayName("포인트 사용 실패 - 잔액 부족")
    void usePointInsufficientBalanceFail() {
        long userId = 1L;
        UserPoint existing = new UserPoint(userId, 200L, System.currentTimeMillis());

        when(pointRepository.selectById(userId)).thenReturn(Optional.of(existing));

        try {
            pointService.use(userId, 500L);
            fail("예외가 발생해야 합니다.");
        } catch (PointException e) {
            assertThat(e.getMessage()).isEqualTo(PointErrorCode.BALANCE_LESS_THAN_USE_AMOUNT.getMessage());
        }
    }

    @Test
    @DisplayName("포인트 이력 조회 성공")
    void getUserPointHistorySuccess() {
        long userId = 1L;
        List<PointHistory> histories = List.of(
                PointHistory.createChargeHistory(userId, 1000L, System.currentTimeMillis())
        );

        when(pointHistoryRepository.selectAllByUserId(userId)).thenReturn(histories);

        List<PointHistory> result = pointService.getUserPointHistory(userId);

        assertThat(result).isEqualTo(histories);
    }
}
