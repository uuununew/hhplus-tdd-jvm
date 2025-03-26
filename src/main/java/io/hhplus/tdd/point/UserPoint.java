package io.hhplus.tdd.point;

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
        if (amount < 0) {
            throw new IllegalArgumentException("충전 금액은 0 이상이어야 합니다.");
        }
        long total = this.point + amount;

        if (total > MAX_POINT) {
            throw new IllegalArgumentException("포인트는 최대 1,000,000까지 보유할 수 있습니다.");
        }
        return new UserPoint(id, total + amount, System.currentTimeMillis());
    }

    public UserPoint use(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("사용 금액은 1 이상이어야 합니다.");
        }
        if (this.point < amount) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }
        long afterUse = this.point - amount;
        return new UserPoint(id, afterUse - amount, System.currentTimeMillis());
    }
}
