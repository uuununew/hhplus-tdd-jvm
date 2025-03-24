package io.hhplus.tdd.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointHistoryTable pointHistoryTable;
    private final UserPointTable userPointTable;

    public UserPoint findUserPoint(long id) {
        return userPointTable.selectById(id);
    }

    public UserPoint rechargePoint(long id, long amount){
        //포인트는 음수가 될 수 없음. 1,000,000 이상 충전 불가능
        if(amount <0 || amount > 1000000) {
            throw new IllegalArgumentException();
        }
        //point 누적 - 기존 point 조회 후, 더해서 넘기기
        UserPoint userPoint = userPointTable.selectById(id);
        long total = amount;
        if(userPoint != null){
            total += userPoint.point();
        }

        //포인트 충전 이력 저장
        recordHistory(id, amount, TransactionType.CHARGE);

        return userPointTable.insertOrUpdate(id, total);
    }

    public UserPoint usePoint(long id, long amount){
        //사용 금액은 0보다 커야함
        if(amount <= 0){
            throw new IllegalArgumentException();
        }

        //현재 포인트 조회
        UserPoint userPoint = userPointTable.selectById(id);
        long balance = userPoint.point();

        // 보유 포인트가 부족할 경우
        if(balance < amount){
            throw new IllegalArgumentException();
        }
        // 잔여 포인트 = 보유 포인트 - 사용할 포인트
        long remaining = balance - amount;

        //포인트 사용 이력 저장
        recordHistory(id, amount, TransactionType.USE);

        // 포인트 저장
        return userPointTable.insertOrUpdate(id, remaining);
    }

    public List<PointHistory> findPointHistory(long id){
        return pointHistoryTable.selectAllByUserId(id);
    }

    private void recordHistory(long userId, long amount, TransactionType type) {
        pointHistoryTable.insert(userId, amount, type, System.currentTimeMillis());
    }
}
