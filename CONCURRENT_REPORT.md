## 동시성 제어 방식에 대한 분석

### ❓동시성 제어란?
다수의 쓰레드 또는 프로세스가 하나의 공유 자원에 접근할 때 데이터의 무결성을 보장하기 위해 제어하는 기법이다.

예를 들어 여러 사용자가 동시에 같은 유저의 포인트를 충전하거나 사용할 경우 의도하지 않은 값이 저장될 수 있다. 이를 막기 위해 동시성 제어가 필요하다.

### ❗️동시성 제어의 중요성
- **데이터 정합성 보장**: 중복 충전 또는 포인트 부족 상태에서 사용 처리 등 잘못된 결과 방지
- **원자성 확보**: 여러 쓰레드가 동시에 수행되는 환경에서 연산을 하나의 단위로 안전하게 처리
- **서비스 신뢰성 향상**: 사용자 경험 개선 및 버그 방지


### 🤔  지금 프로젝트에서 발생할 수 있는 동시성 문제는 뭘까?

### 발생 가능한 문제
1. **동일 유저에게 동시에 여러 충전 요청이 들어올 경우**
2. **충전과 사용 요청이 동시에 들어올 경우**
3. **포인트 충전 중 조회나 사용 요청이 중첩되는 경우**

예를 들어 사용자 A에게 두 개의 요청이 동시에 들어와 `1000원 충전`과 `500원 사용`을 각각 수행한다고 가정했을 때 적절한 락이 없으면 둘 중 하나의 연산이 덮어씌워져 포인트가 잘못 반영될 수 있다.
이러한 시나리오에서 데이터를 안전하게 처리하지 않으면 `PointRepository`에 저장된 포인트 정보가 꼬이게 된다.

---

### 🔧 자바의 동시성 제어 방법의 종류
### ✅ synchronized
- synchronized는 자바에서 가장 기본적인 동기화 방법입니다. 한 번에 하나의 스레드만 특정 블록이나 메서드에 접근하도록 제한한다.
- 장점
    - 문법이 단순하고 사용하기 쉽다.
    - JVM 레벨에서 자동으로 락을 관리하기 때문에 비교적 안전하다.

- 단점
    - 자동 락 해체로 인해 세밀한 제어가 어렵다.
    - 락을 획득한 스레드가 작업을 마치기 전까지 다른 스레드는 대기하므로 성능 저하가 발생할 수 있다.
    - 락 획득 실패 시 타임아웃을 설정할 수 없다.

**📌 간단한 동기화 처리에는 적합하지만 세밀한 제어가 필요한 상황에서는 적합하지 않을 수 있다!**

### ✅ ReentrantLock
- ReentrantLock은 synchronized와 동일한 기능을 제공하면서 더 정교하게 락을 제어할 수 있다.
- 장점
    - lock()과 unlock()을 통해 명시적으로 락을 획득 및 해제할 수 있다.
    - tryLock()을 통해 타임아웃을 설정하거나 락 획득 실패를 처리할 수 있다.
    - 공정성 설정이 가능하여 락을 요청한 순서대로 처리할 수 있다.
- 단점
    - 락 해제를 반드시 개발자가 명시적으로 작성해야 하므로 누락 시 데드락 위험이 존재한다.
    - 구현 복잡도가 synchronized보다 높다.

**📌 정교한 동기화가 필요한 경우 유용하다! 이번 과제에서도 사용자 단위의 락 제어를 위해 사용했다.**

### ✅ ConcurrentHashMap
- 여러 스레드가 동시에 데이터를 읽고 쓸 수 있도록 설계된 Map이다. 내부적으로 세분화된 락을 사용하거나 CAS 연산을 통해 효율적인 동시성 제어를 제공한다.
- 장점
    - 대부분의 연산이 매우 빠르며 락을 사용하지 않거나 최소한으로 사용한다.
    - HashMap을 멀티스레드 환경에서 안전하게 사용할 수 있다.
- 단점
    - 트랜잭션과 같이 여러 연산을 묶어 처리해야 하는 경우에는 부적합

**📌 이번 프로젝트에서는 사용자 ID를 Key로 하고 ReentrantLock을 Value로 가지는 구조를 통해 사용자별 락을 관리하는 데 활용**

### ✅ Atomic 클래스
- Atomic 클래스는 CAS(Compare-And-Swap) 연산을 활용해 락 없이도 원자적인 연산을 수행할 수 있도록 도와준다.
- 장점
    - 락을 사용하지 않기 때문에 매우 빠르다.
    - 단일 값의 연산에 매우 적합하다.
- 단점
    - 복잡한 조건문이나 여러 단계의 로직에는 사용할 수 없다.
    - 직관적이지 않다.

**📌 단순한 카운터나 상태 플래그 처리에 적합하다. 이번 과제의 사용자별 포인트 로직에는 적합하지 않음**


### ✔️ 자바의 동시성 제어 방법 중 내가 사용한 방법
- `ReentrantLock`
- `ConcurrentHashMap`

> 기존에는 `synchronized` 키워드도 함께 고려했으나 명시적인 Lock 객체와의 중복 제어, 병렬처리 문제로 인해 **최종적으로 제거**하였습니다.

### 각 방식의 비교

| 방식 | 장점 | 단점 |
|------|------|------|
| synchronized | 사용법이 간단하고 자바 내장 기능 | 세밀한 제어가 어렵고, 모니터 객체에 종속됨 |
| ReentrantLock | lock 획득, 해제 타이밍을 명시적으로 제어 가능 | 코드가 길고 명시적인 해제가 필요 |
| ConcurrentHashMap | 병렬 환경에서 안전한 Map | 자원별 Lock 관리 구조를 직접 설계해야 함 |

---
### ✏️ 왜 ReentrantLock + ConcurrentHashMap을 사용했는가?
동일 유저에 대한 Lock을 세밀하게 제어하기 위해 `ConcurrentHashMap<Long, ReentrantLock>` 구조를 활용해 사용자 단위의 Lock을 관리했습니다.

**👤 사용자 단위 락 관리 방식**
- 유저 ID를 Key로, 해당 유저 전용 ReentrantLock을 Value로 가지는 구조
- 이 구조를 통해 동일 유저에 대한 요청만 동기화
- 서로 다른 유저에 대한 요청은 동시에 처리되므로 성능 저하를 방지할 수 있음

**ConcurrentHashMap과 ReentrantLock을 함께 사용하면 유저별로 락이 분리되어 충돌을 줄이고 높은 동시 처리 성능을 확보할 수 있습니다.**

```java
@Component
public class UserLockManager {
    private final ConcurrentHashMap<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public ReentrantLock getLock(long userId) {
        return lockMap.computeIfAbsent(userId, id -> new ReentrantLock());
    }
}
```

---
### 적용해보기

**리팩토링 전 (synchronized + ReentrantLock 중복 사용)**
```java
@Override
public UserPoint charge(long id, long amount) {
    // 1. 해당 유저 ReentrantLock을 가져옴
    ReentrantLock lock = userLockManager.getLock(id);

    // 2. lock 객체를 모니터로 사용하는 synchronized 블록
    synchronized (lock) {
        // 3. 명시적으로 Lock을 획득
        lock.lock();
        try {
            //포인트 충전
            UserPoint userPoint = pointRepository.selectById(id);
            UserPoint charged = userPoint.charge(amount);
            UserPoint saved = pointRepository.insertOrUpdate(charged);

            //충전 내역 저장
            pointHistoryRepository.insert(
                PointHistory.createChargeHistory(saved.id(), saved.point(), saved.updateMillis())
            );
            return saved;
        } finally {
            //4. lock 해제
            lock.unlock();
        }
    }
}
```
**⚠️ synchronized(lock)과 lock.lock()을 동시에 사용하는 것은 불필요한 중복 제어입니다.
ReentrantLock 하나만으로도 충분히 임계 구역을 보호할 수 있으므로 이 방식은 오히려 락 경합 증가 및 코드 가독성 저하를 유발할 수 있습니다.**

**리팩토링 후**
```java
@Override
public UserPoint charge(long id, long amount) {
    // 1. 해당 유저 ReentrantLock을 가져옴
    ReentrantLock lock = userLockManager.getLock(id);
    // 2. Lock을 획득
    lock.lock();
    try {
        //포인트 충전
        UserPoint userPoint = pointRepository.selectById(id);
        UserPoint charged = userPoint.charge(amount);
        UserPoint saved = pointRepository.insertOrUpdate(charged);

        //충전 내역 저장
        pointHistoryRepository.insert(
                PointHistory.createChargeHistory(saved.id(), saved.point(), saved.updateMillis())
        );
        return saved;
    } finally {
        //3. lock 해제
        lock.unlock();
    }
}
```
**🪄 ReentrantLock만으로 동시성 제어가 명확하게 가능해졌고 코드도 간결해졌습니다.
유저 단위 락은 ConcurrentHashMap<Long, ReentrantLock> 구조를 통해 안전하게 관리됩니다.**

### ❌ synchronized는 왜 제거했는가?

처음에는 `synchronized(lock)`과 `lock.lock()`을 같이 사용했으나 여러 문제가 발생할 수 있어서 제거하였습니다.

- ReentrantLock은 자체적으로 락 기능을 제공해 동기화가 필요한 임계 구역을 안전하게 보호할 수 있습니다. 따라서 여기에 synchronized를 추가로 사용하는 것은 중복된 제어 방식이며 불필요합니다.
- 특히 synchronized는 하나의 공통된 객체를 기준으로 전체 블록을 동기화하기 때문에 같은 객체를 사용하는 여러 쓰레드가 동시에 접근할 경우 전부 직렬로 처리되게 됩니다.
- 이로 인해 특정 유저에 대한 요청뿐 아니라 다른 유저의 요청까지도 불필요하게 대기하게 되는 문제가 발생할 수 있습니다.
- 이런 구조는 유저별로 락을 세분화해서 관리하는 목적과는 맞지 않고 동시에 처리할 수 있는 작업까지 차단하게 되어 전체 성능이 저하될 수 있습니다.

따라서 `ReentrantLock`과 사용자별 락 저장을 위한  `ConcurrentHashMap`을 함께 사용하는 방식으로 리팩토링하였습니다.


---

### 결론
- 정밀한 락 제어가 필요한 프로젝트 상황에서는 `ReentrantLock`이 유리하다.
- 유저 단위로 락을 관리해야 하므로 `ConcurrentHashMap`이 함께 사용되었다.
- `synchronized`는 중복 제어로 인해 최종적으로 제거하였다.
- 단순한 synchronized보다 세밀한 ReentrantLock + ConcurrentHashMap 조합을 활용해
  동일 사용자 단위의 동시성 문제를 안전하고 효율적으로 해결할 수 있었다.
