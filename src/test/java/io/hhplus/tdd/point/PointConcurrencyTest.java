package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PointConcurrencyTest {

    @Autowired
    private PointService pointService;

    //10명의 사용자가 동시에 요청을 보내는 상황을 위한 스레드 풀
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    @Test
    @DisplayName("동시 충전 - 여러 사용자가 동시에 동일 유저에게 충전을 요청한다")
    void concurrentChargeToSameUser() throws InterruptedException{
        //given
        long userId = 1L;
        int threadCount = 10; //동시에 실행할 스레드 수
        long amount = 100L;

        CountDownLatch latch = new CountDownLatch(threadCount);

        //when
        //10개의 쓰레드가 동시에 유저에게 100포인트씩 충전
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    pointService.charge(userId, amount);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        //then
        UserPoint result = pointService.getUserPoint(userId);
        assertThat(result.point()).isEqualTo(amount * threadCount);
    }

    @Test
    @DisplayName("동시 요청 - 동일 유저에게 동시에 충전과 사용 요청(충전 먼저)")
    void concurrentChargeAndUse_chargeFirst() throws InterruptedException{
        //given
        long userId = 2L;
        pointService.charge(userId, 1000L);

        CountDownLatch latch = new CountDownLatch(2);

        //when
        //충전요청
        executor.submit(() -> {
            try {
                pointService.charge(userId, 1000L);
            } finally {
                latch.countDown();
            }
        });

        //사용 요청
        executor.submit(() -> {
            try {
                pointService.use(userId, 700L);
            } finally {
                latch.countDown();
            }
        });

        latch.await();

        //then
        //최종 포인트 : 1000 + 1000 - 700 = 1300
        UserPoint result = pointService.getUserPoint(userId);
        assertThat(result.point()).isEqualTo(1300L);
    }

    @Test
    @DisplayName("동시 요청 - 동일 유저에게 동시에 충전과 사용 요청 (사용 먼저)")
    void mixedConcurrentChargeAndUse_useFirst() throws InterruptedException {
        //given
        long userId = 3L;
        pointService.charge(userId, 1000L);

        CountDownLatch latch = new CountDownLatch(2);

        //when
        // 사용 요청
        executor.submit(() -> {
            try {
                pointService.use(userId, 700L);
            } finally {
                latch.countDown();
            }
        });

        //충전 요청
        executor.submit(() -> {
            try {
                pointService.charge(userId, 1000L);
            } finally {
                latch.countDown();
            }
        });

        latch.await();

        //then
        // 최종 포인트 : 1000 - 700 + 1000 = 1300
        UserPoint result = pointService.getUserPoint(userId);
        assertThat(result.point()).isEqualTo(1300L);
    }

}
