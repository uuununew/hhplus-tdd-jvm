package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointServiceImplTest {

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @InjectMocks
    private PointServiceImpl pointService;


    @Test
    @DisplayName("포인트 충전 - 음수 금액 입력 시 예외")
    public void chargePointFail_AmountIsNegative(){
        //given
        long userId = 1L;
        long chargeAmount = -1000L;

        //when //then
        assertThatThrownBy(() -> pointService.chargePoint(userId, chargeAmount))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("포인트 충전 -  1,000,000 초과 시 예외")
    public void chargePointFail_AmountExceedsLimit(){
        //given
        long userId  = 1L;
        long chargeAmount = 1_000_001L;

        //when //then
        assertThatThrownBy(() -> pointService.chargePoint(userId, chargeAmount))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("포인트 충전 - 기존 보유 포인트에 누적되어 저장")
    public void chargePointSuccess_AddExistingPoint(){
        //given
        long userId = 1L;
        long existingPoint = 500L;
        long chargeAmount = 1000L;

        UserPoint existing = new UserPoint(userId, existingPoint, System.currentTimeMillis());
        when(userPointTable.selectById(userId)).thenReturn(existing);

        //기대값
        UserPoint expected = new UserPoint(userId, 1500L, System.currentTimeMillis());
        when(userPointTable.insertOrUpdate(userId, 1500L)).thenReturn(expected);

        //when
        UserPoint result = pointService.chargePoint(userId, chargeAmount);

        //then
        assertThat(result.point()).isEqualTo(1500L);

        verify(userPointTable).insertOrUpdate(userId, 1500L);
        verify(pointHistoryTable).insert(
                eq(userId), eq(chargeAmount), eq(TransactionType.CHARGE), anyLong()
        );
    }

    @Test
    @DisplayName("포인트 사용 - 0이하 금액 사용 시 예외")
    public void usePointFail_AmountLessOrEqualZero(){
        long userId = 1L;
        long wrongAmount = 0L;

        //when //then
        assertThatThrownBy(() -> pointService.usePoint(userId, wrongAmount))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("포인트 사용 - 보유 포인트 초과 사용 시 예외")
    public void usePointFail_Over(){
        //given
        long userId = 1L;
        long existingPoint = 1000L;
        long useAmount = 1500L;

        UserPoint existing = new UserPoint(userId, existingPoint, System.currentTimeMillis());
        when(userPointTable.selectById(userId)).thenReturn(existing);

        //when //then
        assertThatThrownBy(() -> pointService.usePoint(userId, useAmount))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("포인트 사용 - 정상동작")
    public void usePointSuccess(){
        //given
        long userId = 1L;
        long existingPoint = 2000L;
        long useAmount = 500L;

        UserPoint existing = new UserPoint(userId, existingPoint, System.currentTimeMillis());
        when(userPointTable.selectById(userId)).thenReturn(existing);

        //기대값
        UserPoint expected = new UserPoint(userId, 1500L, System.currentTimeMillis());
        when(userPointTable.insertOrUpdate(userId, 1500L)).thenReturn(expected);

        //when
        UserPoint result = pointService.usePoint(userId, useAmount);

        //then
        assertThat(result.point()).isEqualTo(1500L);

        verify(userPointTable).insertOrUpdate(userId, 1500L);
        verify(pointHistoryTable).insert(
                eq(userId), eq(useAmount), eq(TransactionType.USE), anyLong());
    }

}