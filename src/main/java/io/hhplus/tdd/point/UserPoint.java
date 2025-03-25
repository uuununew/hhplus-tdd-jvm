package io.hhplus.tdd.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public UserPoint charge(long amountToAdd){
        if(amountToAdd < 0){
            throw new IllegalArgumentException("충전 금액은 음수가 될 수 없음");
        }
        long total = this.point + amountToAdd;

        if(total > 1_000_000L){
            throw new IllegalArgumentException("포인트는 최대 1,000,000까지 충전 가능합니다.");
        }
        return new UserPoint(id(), total, System.currentTimeMillis());
    }

    public UserPoint use(long amountToUse){
        if(amountToUse <= 0){
            throw new IllegalArgumentException("사용 금액은 1 이상이어야 합니다.");
        }
        if(this.point < amountToUse){
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }
        long afterUsePoint = this.point - amountToUse;
        return new UserPoint(this.id, afterUsePoint, System.currentTimeMillis());
    }
}
