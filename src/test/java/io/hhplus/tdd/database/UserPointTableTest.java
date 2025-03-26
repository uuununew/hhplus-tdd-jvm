package io.hhplus.tdd.database;

import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class UserPointTableTest {

    UserPointTable userPointTable = new UserPointTable();

    @Test
    @DisplayName("포인트 삽입/업데이트 후 조회")
    void insertOrUpdateAndSelect() {
        //given
        long userId = 1L;

        //when
        // insert
        userPointTable.insertOrUpdate(userId, 1000L);

        //then
        // select
        UserPoint result = userPointTable.selectById(userId);
        assertThat(result.point()).isEqualTo(1000L);
    }

}