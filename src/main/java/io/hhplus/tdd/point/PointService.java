package io.hhplus.tdd.point;

import io.hhplus.tdd.Exception.PointException;
import io.hhplus.tdd.Exception.PointErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


public interface PointService {
    UserPoint getUserPoint(long id);
    List<PointHistory> getUserPointHistory(long id);
    UserPoint charge(long id, long amount);
    UserPoint use(long id, long amount);
}
