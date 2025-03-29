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
import java.util.concurrent.locks.ReentrantLock;

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

    @Mock
    private UserLockManager userLockManager;

    @InjectMocks
    private PointServiceImpl pointService;


    @Test
    @DisplayName("유저 포인트 조회 성공 - 보유 포인트가 올바르게 조회된다.")
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
    @DisplayName("포인트 충전 성공 - 기존 보유 포인트에 충전 금액이 누적된다.")
    void chargeSuccess() {
        //given
        long userId = 1L;
        long amount = 1000L;
        UserPoint existing = new UserPoint(userId, 500L, System.currentTimeMillis());
        UserPoint charged = existing.charge(amount);

        //동시성 제어를 위한 Mock Lock 설정
        ReentrantLock mockLock = new ReentrantLock();
        when(userLockManager.getLock(userId)).thenReturn(mockLock);

        when(pointRepository.selectById(userId)).thenReturn(existing);
        when(pointRepository.insertOrUpdate(any())).thenReturn(charged);

        //when
        UserPoint result = pointService.charge(userId, amount);

        //then
        assertThat(result.point()).isEqualTo(1500L);
        verify(pointHistoryRepository).insert(any(PointHistory.class));
    }

    @Test
    @DisplayName("포인트 사용 성공 - 보유 포인트에서 사용 금액만큼 차감된다")
    void useSuccess() {
        //given
        long userId = 1L;
        long amount = 300L;
        UserPoint existing = new UserPoint(userId, 1000L, System.currentTimeMillis());
        UserPoint used = existing.use(amount);

        //동시성 제어를 위한 Mock Lock 설정
        ReentrantLock mockLock = new ReentrantLock();
        when(userLockManager.getLock(userId)).thenReturn(mockLock);


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
        long userId = 1L;
        long amount = 100L;

        when(pointRepository.selectById(anyLong())).thenReturn(null);

        //동시성 제어를 위한 Mock Lock 설정
        ReentrantLock mockLock = new ReentrantLock();
        when(userLockManager.getLock(userId)).thenReturn(mockLock);

        //when //then
        try {
            pointService.use(1L, 100L);
            fail("예외가 발생해야 합니다.");
        } catch (PointException e) {
            assertThat(e.getMessage()).isEqualTo(PointErrorCode.USER_ID_NOT_EXIST.getMessage());
        }
    }

    @Test
    @DisplayName("포인트 이력 조회 성공 - 충전과 사용 이력이 정확히 반환된다")
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
