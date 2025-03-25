package io.hhplus.tdd.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;

import java.util.List;


public interface PointService {

    UserPoint findUserPoint(long id);
    List<PointHistory> findPointHistory(long userId);
    UserPoint chargePoint(long id, long amount);
    UserPoint usePoint(long id, long amount);


}
