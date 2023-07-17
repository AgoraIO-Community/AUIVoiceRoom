package io.agora.uikit.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.metrics.AutoConfigureMetrics;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@AutoConfigureMetrics
public class RedisUtilTest {
    @Autowired
    private RedisUtil redisUtil;

    private String key = "test";
    private String keyHash = "hash_test";
    private String keyLock = "lock_test";
    private String keyZset = "zset_test";
    private String val = "test";
    private long expire = 60;

    @Test
    void testLock() throws Exception {
        redisUtil.lock(keyLock);
        assertTrue(redisUtil.unlock(keyLock));
    }

    @Test
    void testLockConcurrency() throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        int count = 5;
        final CountDownLatch countDownLatch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            log.info("testLockConcurrency, i:{}", i);
            executorService.execute(() -> {
                redisUtil.lock(keyLock);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("testLockConcurrency, time:{}", DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                redisUtil.unlock(keyLock);
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
    }

    @Test
    void testTryLock() throws Exception {
        assertTrue(redisUtil.tryLock(keyLock));
        assertTrue(redisUtil.unlock(keyLock));
    }

    @Test
    void testTryLockConcurrency() throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        int count = 5;
        final CountDownLatch countDownLatch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            log.info("testTryLockConcurrency, i:{}", i);
            executorService.execute(() -> {
                redisUtil.tryLock(keyLock);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("testTryLockConcurrency, time:{}", DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                redisUtil.unlock(keyLock);
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
    }

    @Test
    void testTryLockConcurrencyWaitSecond() throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        int count = 5;
        final CountDownLatch countDownLatch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            log.info("testTryLockConcurrencyWaitSecond, i:{}", i);
            executorService.execute(() -> {
                redisUtil.tryLock(keyLock, 10);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("testTryLockConcurrencyWaitSecond, time:{}",
                        DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                redisUtil.unlock(keyLock);
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
    }

    @Test
    void testUnlock() throws Exception {
        redisUtil.lock(keyLock);
        assertTrue(redisUtil.unlock(keyLock));
        assertTrue(redisUtil.tryLock(keyLock));
        assertTrue(redisUtil.unlock(keyLock));
        assertTrue(redisUtil.tryLock(keyLock, 5));
        assertTrue(redisUtil.unlock(keyLock));
    }

    @Test
    void testDelete() {
        redisUtil.set(key, val, expire);
        assertTrue(redisUtil.delete(key));
        assertFalse(redisUtil.delete(key));
        assertEquals(null, redisUtil.get(key));
    }

    @Test
    void testGet() {
        redisUtil.set(key, val, expire);
        assertTrue(redisUtil.hasKey(key));
        assertEquals(val, redisUtil.get(key));
    }

    @Test
    void testHashGet() {
        assertNull(redisUtil.hashGet(keyHash, "key1"));
        redisUtil.hashPut(keyHash, "key1", "val1");
        redisUtil.hashPut(keyHash, "key2", "val2");
        assertNotNull(redisUtil.hashGet(keyHash, "key1"));
        assertEquals(2, redisUtil.hashSize(keyHash));
        assertEquals("{key1=val1, key2=val2}", redisUtil.hashEntries(keyHash).toString());
        assertEquals("[val1, val2]", redisUtil.hashValues(keyHash).toString());
        assertEquals(1, redisUtil.hashDelete(keyHash, "key1"));
        assertEquals(1, redisUtil.hashSize(keyHash));
        assertTrue(redisUtil.delete(keyHash));
    }

    @Test
    void testSet() {
        redisUtil.set(key, val, expire);
    }

    @Test
    void testZsetAdd() {
        assertTrue(redisUtil.zsetAdd(keyZset, val, 2));
        assertTrue(redisUtil.zsetAdd(keyZset, "val2", 4));
        assertTrue(redisUtil.zsetAdd(keyZset, "val3", 1));
        assertTrue(redisUtil.zsetAdd(keyZset, "val4", 3));
        assertEquals(1, redisUtil.zsetRemove(keyZset, val));
        assertEquals(3, redisUtil.zsetSize(keyZset));

        Set<Object> resultRange = redisUtil.zsetRange(keyZset, 0, -1);
        assertEquals("[val3, val4, val2]", resultRange.toString());
        log.info("testZsetAdd, resultRange:{}", resultRange);

        Set<Object> resultRangeReverse = redisUtil.zsetRangeReverse(keyZset, 0, -1);
        assertEquals("[val2, val4, val3]", resultRangeReverse.toString());
        log.info("testZsetAdd, resultRangeReverse:{}", resultRangeReverse);

        Set<Object> resultRangeByScore = redisUtil.zsetRangeByScore(keyZset, 1, 2);
        assertEquals("[val3]", resultRangeByScore.toString());
        log.info("testZsetAdd, resultRangeByScore:{}", resultRangeByScore);

        assertTrue(redisUtil.delete(keyZset));
    }

    @Test
    void testZsetPopMax() {
        assertTrue(redisUtil.zsetAdd(keyZset, "val1", 2));
        assertTrue(redisUtil.zsetAdd(keyZset, "val2", 1));
        assertTrue(redisUtil.zsetAdd(keyZset, "val3", 3));
        assertEquals("val3", redisUtil.zsetPopMax(keyZset).getValue());
        assertEquals("val1", redisUtil.zsetPopMax(keyZset).getValue());
        assertEquals("val2", redisUtil.zsetPopMax(keyZset).getValue());
        assertEquals(null, redisUtil.zsetPopMax(keyZset));
        assertFalse(redisUtil.delete(keyZset));
    }

    @Test
    void testZsetRemove() {
        assertTrue(redisUtil.zsetAdd(keyZset, val, 2));
        assertEquals(1, redisUtil.zsetSize(keyZset));
        assertEquals(1, redisUtil.zsetRemove(keyZset, val));
        assertEquals(0, redisUtil.zsetSize(keyZset));
        assertEquals(0, redisUtil.zsetRemove(keyZset, val));
        assertFalse(redisUtil.delete(keyZset));
    }
}
