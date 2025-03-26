package io.hhplus.tdd.point;

import io.hhplus.tdd.Exception.PointErrorCode;
import io.hhplus.tdd.Exception.PointException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService{

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @Override
    public UserPoint getUserPoint(long id) {
        //포인트 조회
       UserPoint userPoint = pointRepository.selectById(id);
        if (userPoint == null) {
            throw new PointException(PointErrorCode.USER_ID_NOT_EXIST);
        }
        return userPoint;
    }

    @Override
    public List<PointHistory> getUserPointHistory(long id) {
        return pointHistoryRepository.selectAllByUserId(id);
    }

    @Override
    public UserPoint charge(long id, long amount) {
        //포인트 충전
        UserPoint userPoint = pointRepository.selectById(id);
        UserPoint charged = userPoint.charge(amount);
        UserPoint saved = pointRepository.insertOrUpdate(charged);

        //충전 내역 저장
        pointHistoryRepository.insert(
                PointHistory.createChargeHistory(saved.id(), saved.point(), saved.updateMillis())
        );
        return saved;
    }

    @Override
    public UserPoint use(long id, long amount) {
        UserPoint userPoint = pointRepository.selectById(id);

        //포인트 사용
        UserPoint used = userPoint.use(amount);
        UserPoint updated = pointRepository.insertOrUpdate(used);

        //사용 내역 저장
        pointHistoryRepository.insert(
                PointHistory.createUseHistory(updated.id(), amount, updated.updateMillis())
        );
        return updated;
    }

}
