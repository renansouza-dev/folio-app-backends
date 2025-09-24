package com.renansouza.folio.security.ratelimiter.storage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class RateLimiterEntryTest {

  private RateLimiterStorage storage;

  @BeforeEach
  void setUp() {
    storage = new InMemoryRateLimiterStorage();
  }

  @Nested
  class CoreOperations {

    @Test
    void get_shouldReturnEmptyWhenKeyIsNull() {
      Optional<Long> result = storage.get(null);
      assertTrue(result.isEmpty());
    }

    @Test
    void get_shouldReturnEmptyWhenKeyDoesNotExist() {
      Optional<Long> result = storage.get("non-existent-key");
      assertTrue(result.isEmpty());
    }

    @Test
    void get_shouldReturnValueWhenKeyExists() {
      // Arrange
      String key = "test-key";
      Long value = 42L;
      Duration ttl = Duration.ofMinutes(10);

      storage.set(key, value, ttl);

      // Act
      Optional<Long> result = storage.get(key);

      // Assert
      assertTrue(result.isPresent());
      assertEquals(value, result.get());
    }

    @Test
    void get_shouldReturnEmptyAndRemoveExpiredEntry() {
      // Arrange
      String key = "expired-key";
      Long value = 100L;
      Duration ttl = Duration.ofMillis(1);

      storage.set(key, value, ttl);

      // Wait for expiration
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      // Act
      Optional<Long> result = storage.get(key);

      // Assert
      assertTrue(result.isEmpty());
      assertFalse(storage.exists(key));
    }

    @Test
    void set_shouldThrowExceptionWhenParametersAreNull() {
      assertThrows(IllegalArgumentException.class,
          () -> storage.set(null, 1L, Duration.ofMinutes(1)));
      assertThrows(IllegalArgumentException.class,
          () -> storage.set("key", null, Duration.ofMinutes(1)));
      assertThrows(IllegalArgumentException.class, () -> storage.set("key", 1L, null));
    }

    @Test
    void set_shouldStoreValueWithTTL() {
      // Arrange
      String key = "test-key";
      Long value = 123L;
      Duration ttl = Duration.ofHours(1);

      // Act
      storage.set(key, value, ttl);

      // Assert
      Optional<Long> result = storage.get(key);
      assertTrue(result.isPresent());
      assertEquals(value, result.get());
      assertTrue(storage.exists(key));
    }

    @Test
    void increment_shouldThrowExceptionWhenParametersAreNull() {
      assertThrows(IllegalArgumentException.class,
          () -> storage.increment(null, 1L, Duration.ofMinutes(1)));
      assertThrows(IllegalArgumentException.class,
          () -> storage.increment("key", null, Duration.ofMinutes(1)));
      assertThrows(IllegalArgumentException.class, () -> storage.increment("key", 1L, null));
    }

    @Test
    void increment_shouldCreateNewEntryWhenKeyDoesNotExist() {
      // Arrange
      String key = "new-key";
      Long increment = 5L;
      Duration ttl = Duration.ofMinutes(10);

      // Act
      Long result = storage.increment(key, increment, ttl);

      // Assert
      assertEquals(increment, result);
      Optional<Long> storedValue = storage.get(key);
      assertTrue(storedValue.isPresent());
      assertEquals(increment, storedValue.get());
    }

    @Test
    void increment_shouldCreateNewEntryWhenExistingEntryIsExpired() {
      // Arrange
      String key = "expired-key";
      Long initialValue = 10L;
      Long increment = 5L;
      Duration shortTtl = Duration.ofMillis(1);
      Duration newTtl = Duration.ofMinutes(10);

      storage.set(key, initialValue, shortTtl);

      // Wait for expiration
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      // Act
      Long result = storage.increment(key, increment, newTtl);

      // Assert
      assertEquals(increment, result);
      Optional<Long> storedValue = storage.get(key);
      assertTrue(storedValue.isPresent());
      assertEquals(increment, storedValue.get());
    }

    @Test
    void increment_shouldIncrementExistingValidEntry() {
      // Arrange
      String key = "valid-key";
      Long initialValue = 10L;
      Long increment = 3L;
      Duration ttl = Duration.ofHours(1);

      storage.set(key, initialValue, ttl);

      // Act
      Long result = storage.increment(key, increment, ttl);

      // Assert
      assertEquals(13L, result);
      Optional<Long> storedValue = storage.get(key);
      assertTrue(storedValue.isPresent());
      assertEquals(13L, storedValue.get());
    }

    @Test
    void delete_shouldReturnFalseWhenKeyIsNull() {
      boolean result = storage.delete(null);
      assertFalse(result);
    }

    @Test
    void delete_shouldReturnFalseWhenKeyDoesNotExist() {
      boolean result = storage.delete("non-existent-key");
      assertFalse(result);
    }

    @Test
    void delete_shouldReturnTrueAndRemoveExistingKey() {
      // Arrange
      String key = "test-key";
      storage.set(key, 42L, Duration.ofMinutes(10));

      // Act
      boolean result = storage.delete(key);

      // Assert
      assertTrue(result);
      assertFalse(storage.exists(key));
      assertTrue(storage.get(key).isEmpty());
    }

    @Test
    void exists_shouldReturnFalseWhenKeyIsNull() {
      boolean result = storage.exists(null);
      assertFalse(result);
    }

    @Test
    void exists_shouldReturnFalseWhenKeyDoesNotExist() {
      boolean result = storage.exists("non-existent-key");
      assertFalse(result);
    }

    @Test
    void exists_shouldReturnTrueWhenKeyExists() {
      // Arrange
      String key = "test-key";
      storage.set(key, 42L, Duration.ofMinutes(10));

      // Act & Assert
      assertTrue(storage.exists(key));
    }

    @Test
    void exists_shouldReturnFalseAndRemoveExpiredEntry() {
      // Arrange
      String key = "expired-key";
      storage.set(key, 42L, Duration.ofMillis(1));

      // Wait for expiration
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      // Act & Assert
      assertFalse(storage.exists(key));
      assertTrue(storage.get(key).isEmpty());
    }

    @Test
    void expire_shouldReturnFalseWhenParametersAreNull() {
      assertFalse(storage.expire(null, Duration.ofMinutes(1)));
      assertFalse(storage.expire("key", null));
    }

    @Test
    void expire_shouldReturnFalseWhenKeyDoesNotExist() {
      boolean result = storage.expire("non-existent-key", Duration.ofMinutes(1));
      assertFalse(result);
    }

    @Test
    void expire_shouldReturnFalseWhenEntryIsExpired() {
      // Arrange
      String key = "expired-key";
      storage.set(key, 42L, Duration.ofMillis(1));

      // Wait for expiration
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      // Act
      boolean result = storage.expire(key, Duration.ofMinutes(1));

      // Assert
      assertFalse(result);
    }

    @Test
    void expire_shouldReturnTrueAndUpdateExpirationWhenEntryIsValid() {
      // Arrange
      String key = "valid-key";
      storage.set(key, 42L, Duration.ofMinutes(1));

      // Act
      boolean result = storage.expire(key, Duration.ofHours(1));

      // Assert
      assertTrue(result);
      assertTrue(storage.exists(key));
      Optional<Long> value = storage.get(key);
      assertTrue(value.isPresent());
      assertEquals(42L, value.get());
    }
  }

  @Nested
  class ConcurrencyOperations {

    @Test
    @Timeout(10)
    void concurrentIncrement_shouldBeThreadSafe() throws InterruptedException {
      // Arrange
      String key = "concurrent-key";
      int threadCount = 100;
      int incrementsPerThread = 10;
      Long incrementValue = 1L;
      Duration ttl = Duration.ofMinutes(10);

      AtomicInteger successCount;
      try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(threadCount);
        successCount = new AtomicInteger(0);

        // Act
        for (int i = 0; i < threadCount; i++) {
          executor.submit(() -> {
            try {
              startLatch.await(); // Wait for all threads to be ready

              for (int j = 0; j < incrementsPerThread; j++) {
                storage.increment(key, incrementValue, ttl);
              }
              successCount.incrementAndGet();
            } catch (Exception e) {
              // ignore
            } finally {
              completeLatch.countDown();
            }
          });
        }

        startLatch.countDown(); // Start all threads
        completeLatch.await(); // Wait for all threads to complete
        executor.shutdown();
      }

      // Assert
      assertEquals(threadCount, successCount.get());
      Optional<Long> finalValue = storage.get(key);
      assertTrue(finalValue.isPresent());
      assertEquals(threadCount * incrementsPerThread, finalValue.get().longValue());
    }

    @Test
    @Timeout(10)
    void concurrentReadWrite_shouldBeThreadSafe() throws InterruptedException {
      // Arrange
      String keyPrefix = "concurrent-rw-";
      int writerThreads = 50;
      int readerThreads = 50;
      int operationsPerThread = 20;
      Duration ttl = Duration.ofMinutes(10);

      AtomicInteger writeSuccessCount;
      AtomicInteger readSuccessCount;
      try (ExecutorService executor = Executors.newFixedThreadPool(writerThreads + readerThreads)) {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(writerThreads + readerThreads);
        writeSuccessCount = new AtomicInteger(0);
        readSuccessCount = new AtomicInteger(0);

        // Start writer threads
        for (int i = 0; i < writerThreads; i++) {
          final int threadId = i;
          executor.submit(() -> {
            try {
              startLatch.await();

              for (int j = 0; j < operationsPerThread; j++) {
                String key = keyPrefix + threadId + "-" + j;
                storage.set(key, (long) (threadId * 1000 + j), ttl);
                storage.increment(key, 1L, ttl);
              }
              writeSuccessCount.incrementAndGet();
            } catch (Exception e) {
              // ignore
            } finally {
              completeLatch.countDown();
            }
          });
        }

        // Start reader threads
        for (int i = 0; i < readerThreads; i++) {
          final int threadId = i;
          executor.submit(() -> {
            try {
              startLatch.await();

              for (int j = 0; j < operationsPerThread; j++) {
                String key = keyPrefix + (threadId % writerThreads) + "-" + j;
                storage.get(key);
                storage.exists(key);
              }
              readSuccessCount.incrementAndGet();
            } catch (Exception e) {
              // ignore
            } finally {
              completeLatch.countDown();
            }
          });
        }

        startLatch.countDown();
        completeLatch.await();
        executor.shutdown();
      }

      // Assert
      assertEquals(writerThreads, writeSuccessCount.get());
      assertEquals(readerThreads, readSuccessCount.get());
    }

    @Test
    @Timeout(10)
    void concurrentExpiration_shouldHandleRaceConditions() throws InterruptedException {
      // Arrange
      String key = "expiration-race-key";
      int threadCount = 50;
      Duration shortTtl = Duration.ofMillis(50);
      Duration longTtl = Duration.ofMinutes(10);

      AtomicInteger operationCount;
      try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(threadCount);
        operationCount = new AtomicInteger(0);

        // Set initial value
        storage.set(key, 100L, shortTtl);

        // Start threads that will operate on potentially expired entries
        for (int i = 0; i < threadCount; i++) {
          executor.submit(() -> {
            try {
              startLatch.await();

              // Mix of operations that should handle expired entries gracefully
              storage.get(key);
              storage.increment(key, 1L, longTtl);
              storage.exists(key);
              storage.expire(key, longTtl);

              operationCount.incrementAndGet();
            } catch (Exception e) {
              // ignore
            } finally {
              completeLatch.countDown();
            }
          });
        }

        startLatch.countDown();
        completeLatch.await();
        executor.shutdown();
      }

      // Assert - no exceptions should have been thrown
      assertEquals(threadCount, operationCount.get());
    }
  }

  @Nested
  class PerformanceOperations {

    @Test
    @Timeout(5)
    void performance_massiveSequentialOperations() {
      // Arrange
      int operationCount = 100000;
      String keyPrefix = "perf-seq-";
      Duration ttl = Duration.ofMinutes(10);

      // Act & Measure
      long startTime = System.currentTimeMillis();

      for (int i = 0; i < operationCount; i++) {
        String key = keyPrefix + (i % 1000); // Reuse some keys
        storage.set(key, (long) i, ttl);
        storage.get(key);
        storage.increment(key, 1L, ttl);
      }

      long endTime = System.currentTimeMillis();
      long duration = endTime - startTime;

      // Assert - Should complete within reasonable time
      assertTrue(duration < 5000, "Operations took too long: " + duration + "ms");
      System.out.println("Sequential operations took: " + duration + "ms");
    }

    @Test
    @Timeout(10)
    void performance_massiveConcurrentOperations() throws InterruptedException {
      // Arrange
      int threadCount = 20;
      int operationsPerThread = 5000;
      String keyPrefix = "perf-concurrent-";
      Duration ttl = Duration.ofMinutes(10);

      long startTime;
      try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(threadCount);

        startTime = System.currentTimeMillis();

        // Act
        for (int i = 0; i < threadCount; i++) {
          final int threadId = i;
          executor.submit(() -> {
            try {
              startLatch.await();

              for (int j = 0; j < operationsPerThread; j++) {
                String key = keyPrefix + (threadId * operationsPerThread + j) % 1000;
                storage.set(key, (long) j, ttl);
                storage.get(key);
                storage.increment(key, 1L, ttl);
              }
            } catch (Exception e) {
              // ignore
            } finally {
              completeLatch.countDown();
            }
          });
        }

        startLatch.countDown();
        completeLatch.await();
        executor.shutdown();
      }

      long endTime = System.currentTimeMillis();
      long duration = endTime - startTime;

      // Assert
      assertTrue(duration < 10000, "Concurrent operations took too long: " + duration + "ms");
      System.out.println("Concurrent operations took: " + duration + "ms");
    }

    @Test
    @Timeout(5)
    void performance_memoryUsage() {
      // Arrange
      int entryCount = 50000;
      String keyPrefix = "memory-test-";
      Duration ttl = Duration.ofMinutes(10);

      // Measure memory before
      System.gc();
      Runtime runtime = Runtime.getRuntime();
      long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

      // Act - Create many entries
      for (int i = 0; i < entryCount; i++) {
        storage.set(keyPrefix + i, (long) i, ttl);
      }

      // Measure memory after
      System.gc();
      long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
      long memoryUsed = memoryAfter - memoryBefore;

      // Assert - Memory usage should be reasonable (less than 100MB for 50k entries)
      assertTrue(memoryUsed < 100_000_000, "Memory usage too high: " + memoryUsed + " bytes");
      System.out.println("Memory used for " + entryCount + " entries: " + memoryUsed + " bytes");

      // Verify all entries exist
      assertEquals(entryCount, IntStream.range(0, entryCount)
          .mapToObj(i -> keyPrefix + i)
          .mapToInt(key -> storage.exists(key) ? 1 : 0)
          .sum());
    }
  }

  @Nested
  class EdgeCasesOperations {

    @Test
    void increment_shouldHandleOverflow() {
      // Arrange
      String key = "overflow-key";
      Duration ttl = Duration.ofMinutes(10);

      storage.set(key, Long.MAX_VALUE - 1, ttl);

      // Act & Assert - Should handle overflow gracefully
      assertDoesNotThrow(() -> {
        Long result = storage.increment(key, 10L, ttl);
        // The result will overflow but should not throw an exception
        assertNotNull(result);
      });
    }

    @Test
    void set_shouldHandleZeroAndNegativeValues() {
      // Arrange
      String zeroKey = "zero-key";
      String negativeKey = "negative-key";
      Duration ttl = Duration.ofMinutes(10);

      // Act
      storage.set(zeroKey, 0L, ttl);
      storage.set(negativeKey, -100L, ttl);

      // Assert
      Optional<Long> zeroValue = storage.get(zeroKey);
      Optional<Long> negativeValue = storage.get(negativeKey);

      assertTrue(zeroValue.isPresent());
      assertEquals(0L, zeroValue.get());

      assertTrue(negativeValue.isPresent());
      assertEquals(-100L, negativeValue.get());
    }

    @Test
    void set_shouldHandleVeryShortTTL() {
      // Arrange
      String key = "short-ttl-key";
      Duration veryShortTtl = Duration.ofNanos(1);

      // Act
      storage.set(key, 42L, veryShortTtl);

      // Assert - Entry might already be expired
      // This tests that the system handles very short TTLs gracefully
      Optional<Long> value = storage.get(key);
      // Either the value exists (if retrieved quickly enough) or it's already expired
      assertTrue(value.isEmpty() || value.equals(Optional.of(42L)));
    }

    @RepeatedTest(5)
    void stressTest_rapidOperations() {
      // This test runs multiple times to catch intermittent concurrency issues
      String key = "stress-key";
      Duration ttl = Duration.ofMinutes(1);

      // Rapid sequence of different operations
      for (int i = 0; i < 1000; i++) {
        storage.set(key, (long) i, ttl);
        storage.get(key);
        storage.increment(key, 1L, ttl);
        storage.exists(key);
        if (i % 100 == 0) {
          storage.expire(key, ttl);
        }
      }

      // Should complete without exceptions
      assertTrue(storage.exists(key));
    }
  }
}