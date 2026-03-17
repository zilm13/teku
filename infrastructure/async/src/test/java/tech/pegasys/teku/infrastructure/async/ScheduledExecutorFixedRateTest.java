/*
 * Copyright Consensys Software Inc., 2022
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package tech.pegasys.teku.infrastructure.async;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"FutureReturnValueIgnored", "JavaTimeDefaultTimeZone"})
class ScheduledExecutorFixedRateTest {

  private static final Logger LOG = LogManager.getLogger();

  private ScheduledExecutorService executor;

  @BeforeEach
  void setUp() {
    executor = Executors.newSingleThreadScheduledExecutor();
  }

  @AfterEach
  void tearDown() {
    executor.shutdownNow();
  }

  private void scheduledTask() {
    LOG.info(
        "Scheduled event fired at {}",
        LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")));
    try {
      Thread.sleep(300);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @Test
  void shouldFireEventEvery10Seconds() throws InterruptedException {
    long now = System.currentTimeMillis();
    long initialDelay = 10_000 - (now % 10_000);

    executor.scheduleAtFixedRate(
        this::scheduledTask, initialDelay, 10_000, TimeUnit.MILLISECONDS);

    Thread.sleep(TimeUnit.MINUTES.toMillis(3));
  }

  @Test
  void shouldScheduleBeforeEachTask() throws InterruptedException {
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    ExecutorService taskExecutor = Executors.newSingleThreadExecutor();

    try {
      scheduleNextSlot(scheduler, taskExecutor);
      Thread.sleep(TimeUnit.MINUTES.toMillis(3));
    } finally {
      scheduler.shutdownNow();
      taskExecutor.shutdownNow();
    }
  }

  private void scheduleNextSlot(
      final ScheduledExecutorService scheduler, final ExecutorService taskExecutor) {
    long now = System.currentTimeMillis();
    long delay = 10_000 - (now % 10_000);

    scheduler.schedule(
        () -> {
          scheduleNextSlot(scheduler, taskExecutor);
          taskExecutor.execute(this::scheduledTask);
        },
        delay,
        TimeUnit.MILLISECONDS);
  }

  @Test
  void shouldScheduleBeforeEachTaskWithDuplicateCheck() throws InterruptedException {
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    ExecutorService taskExecutor = Executors.newSingleThreadExecutor();
    AtomicLong lastScheduledSecond = new AtomicLong(-1);

    try {
      scheduleNextSlotWithDuplicateCheck(scheduler, taskExecutor, lastScheduledSecond);
      Thread.sleep(TimeUnit.MINUTES.toMillis(3));
    } finally {
      scheduler.shutdownNow();
      taskExecutor.shutdownNow();
    }
  }

  private void scheduleNextSlotWithDuplicateCheck(
      final ScheduledExecutorService scheduler,
      final ExecutorService taskExecutor,
      final AtomicLong lastScheduledSecond) {
    long now = System.currentTimeMillis();
    long delay = 10_000 - (now % 10_000);
    long targetSecond = (now + delay) / 1000;

    if (lastScheduledSecond.get() >= targetSecond) {
      delay += 10_000;
      targetSecond += 10;
      LOG.info("Duplicate slot detected, skipping to next at {}s", targetSecond);
    }
    lastScheduledSecond.set(targetSecond);

    scheduler.schedule(
        () -> {
          scheduleNextSlotWithDuplicateCheck(scheduler, taskExecutor, lastScheduledSecond);
          taskExecutor.execute(this::scheduledTask);
        },
        delay,
        TimeUnit.MILLISECONDS);
  }
}