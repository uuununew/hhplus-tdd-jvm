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
        Optional<UserPoint> optional = pointRepository.selectById(id);
        if (optional.isEmpty()) {
            throw new PointException(PointErrorCode.USER_ID_NOT_EXIST);
        }
        return optional.get();
    }

    @Override
    public List<PointHistory> getUserPointHistory(long id) {
        return pointHistoryRepository.selectAllByUserId(id);
    }

    @Override
    public UserPoint charge(long id, long amount) {
        UserPoint userPoint = pointRepository.selectById(id).orElse(UserPoint.empty(id));
        UserPoint charged = userPoint.charge(amount);
        UserPoint saved = pointRepository.insertOrUpdate(charged);

        pointHistoryRepository.insert(
                PointHistory.createChargeHistory(saved.id(), saved.point(), saved.updateMillis())
        );
        return saved;
    }

    @Override
    public UserPoint use(long id, long amount) {

        Optional<UserPoint> optional = pointRepository.selectById(id);
        if (optional.isEmpty()) {
            throw new PointException(PointErrorCode.USER_ID_NOT_EXIST);
        }

        UserPoint used = optional.get().use(amount);
        UserPoint updated = pointRepository.insertOrUpdate(used);

        pointHistoryRepository.insert(
                PointHistory.createUseHistory(updated.id(), amount, updated.updateMillis())
        );
        return updated;
    }

}
