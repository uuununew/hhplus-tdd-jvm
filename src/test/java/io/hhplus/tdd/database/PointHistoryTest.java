package io.hhplus.tdd.database;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PointHistoryTest {

    PointHistoryTable pointHistoryTable = new PointHistoryTable();

    @Test
    @DisplayName("이력 저장 후 조회")
    void insertAndSelectByUserId() {
        //given
        long userId = 1L;

        //when
        pointHistoryTable.insert(userId, 1000L, TransactionType.CHARGE, System.currentTimeMillis());
        pointHistoryTable.insert(userId, 500L, TransactionType.USE, System.currentTimeMillis());

        List<PointHistory> result = pointHistoryTable.selectAllByUserId(userId);

        //then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).type()).isEqualTo(TransactionType.CHARGE);
        assertThat(result.get(1).type()).isEqualTo(TransactionType.USE);
    }
}