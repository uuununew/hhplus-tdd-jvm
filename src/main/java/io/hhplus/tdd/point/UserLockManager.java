package io.hhplus.tdd.point;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class UserLockManager {
    // 유저 ID별로 Lock을 보관하는 맵
    private final ConcurrentHashMap<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    /**
     * 특정 유저 ID에 대한 Lock을 반환합니다.
     * 해당 유저에 대한 Lock이 존재하지 않으면 새로 생성해서 저장하고 반환합니다.
     *
     * @param userId 사용자 ID
     * @return 해당 사용자 ID에 대한 전용 ReentrantLock
     */
    public ReentrantLock getLock(long userId) {
        return lockMap.computeIfAbsent(userId, key -> new ReentrantLock());
    }
}
