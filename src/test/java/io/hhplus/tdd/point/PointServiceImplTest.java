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
        //given
        long userId = 1L;
        UserPoint userPoint = new UserPoint(userId, 1000L, System.currentTimeMillis());

        when(pointRepository.selectById(userId)).thenReturn(userPoint);

        //when
        UserPoint result = pointService.getUserPoint(userId);

        //then
        assertThat(result).isEqualTo(userPoint);
    }

    @Test
    @DisplayName("유저 포인트 조회 실패 - 존재하지 않는 사용자")
    void getUserPointFail() {
        //given
        long userId = 2L;
        when(pointRepository.selectById(userId)).thenReturn(null);

        //when //then
        try {
            pointService.getUserPoint(userId);
            fail("예외가 발생해야 합니다.");
        } catch (PointException e) {
            assertThat(e.getMessage()).isEqualTo(PointErrorCode.USER_ID_NOT_EXIST.getMessage());
        }
    }

    @Test
    @DisplayName("포인트 충전 성공")
    void chargeSuccess() {
        //given
        long userId = 1L;
        long amount = 1000L;
        UserPoint existing = new UserPoint(userId, 500L, System.currentTimeMillis());
        UserPoint charged = existing.charge(amount);

        when(pointRepository.selectById(userId)).thenReturn(existing);
        when(pointRepository.insertOrUpdate(any())).thenReturn(charged);

        //when
        UserPoint result = pointService.charge(userId, amount);

        //then
        assertThat(result.point()).isEqualTo(1500L);
        verify(pointHistoryRepository).insert(any(PointHistory.class));
    }

    @Test
    @DisplayName("포인트 사용 성공")
    void useSuccess() {
        //given
        long userId = 1L;
        long amount = 300L;
        UserPoint existing = new UserPoint(userId, 1000L, System.currentTimeMillis());
        UserPoint used = existing.use(amount);

        when(pointRepository.selectById(userId)).thenReturn(existing);
        when(pointRepository.insertOrUpdate(any())).thenReturn(used);

        //when
        UserPoint result = pointService.use(userId, amount);

        //then
        assertThat(result.point()).isEqualTo(700L);
        verify(pointHistoryRepository).insert(any(PointHistory.class));
    }

    @Test
    @DisplayName("포인트 사용 실패 - 존재하지 않는 사용자")
    void useUserNotFoundFail() {
        //given
        when(pointRepository.selectById(anyLong())).thenReturn(null);

        //when //then
        try {
            pointService.use(1L, 100L);
            fail("예외가 발생해야 합니다.");
        } catch (PointException e) {
            assertThat(e.getMessage()).isEqualTo(PointErrorCode.USER_ID_NOT_EXIST.getMessage());
        }
    }

    @Test
    @DisplayName("포인트 이력 조회 성공")
    void getUserPointHistorySuccess() {
        //given
        long userId = 1L;
        List<PointHistory> histories = List.of(
                PointHistory.createChargeHistory(userId, 1000L, System.currentTimeMillis())
        );
        when(pointHistoryRepository.selectAllByUserId(userId)).thenReturn(histories);

        //when
        List<PointHistory> result = pointService.getUserPointHistory(userId);

        //then
        assertThat(result).isEqualTo(histories);
    }
}
