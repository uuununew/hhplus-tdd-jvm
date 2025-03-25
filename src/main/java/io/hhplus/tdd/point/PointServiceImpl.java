package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService{
    private final PointHistoryTable pointHistoryTable;
    private final UserPointTable userPointTable;

    @Override
    public UserPoint findUserPoint(long id) {
        return userPointTable.selectById(id);
    }

    @Override
    public UserPoint chargePoint(long id, long amount){
        //현재 유저 포인트 조회
        UserPoint current = userPointTable.selectById(id);

        //포인트 충전
        UserPoint charge = current.charge(amount);

        //포인트 충전 이력 저장
        recordHistory(id, amount, TransactionType.CHARGE);

        return userPointTable.insertOrUpdate(id, charge.point());
    }

    @Override
    public UserPoint usePoint(long id, long amount){
       //현재 포인트 조회
        UserPoint current = userPointTable.selectById(id);

        //포인트 사용(차감)
        UserPoint use = current.use(amount);

        //포인트 사용 이력 저장
        recordHistory(id, amount, TransactionType.USE);

        // 포인트 저장
        return userPointTable.insertOrUpdate(id, use.point());
    }

    @Override
    public List<PointHistory> findPointHistory(long id){
        return pointHistoryTable.selectAllByUserId(id);
    }


    private void recordHistory(long userId, long amount, TransactionType type) {
        pointHistoryTable.insert(userId, amount, type, System.currentTimeMillis());
    }

}
