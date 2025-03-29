package io.hhplus.tdd.point;

import io.hhplus.tdd.Exception.PointErrorCode;
import io.hhplus.tdd.Exception.PointException;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static final long MAX_POINT = 1_000_000L;

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public UserPoint charge(long amount) {
        if (amount <= 0) {
            throw new PointException(PointErrorCode.CHARGE_AMOUNT_LESS_THAN_ZERO);
        }
        long total = this.point + amount;

        if (total > MAX_POINT) {
            throw new PointException(PointErrorCode.CHARGE_AMOUNT_GREATER_THAN_MAX);
        }
        return new UserPoint(id, total, System.currentTimeMillis());
    }

    public UserPoint use(long amount) {
        if (amount <= 0) {
            throw new PointException(PointErrorCode.USE_AMOUNT_LESS_THAN_ZERO);
        }
        if (this.point < amount) {
            throw new PointException(PointErrorCode.BALANCE_LESS_THAN_USE_AMOUNT);
        }
        long afterUse = this.point - amount;
        return new UserPoint(id, afterUse, System.currentTimeMillis());
    }
}
