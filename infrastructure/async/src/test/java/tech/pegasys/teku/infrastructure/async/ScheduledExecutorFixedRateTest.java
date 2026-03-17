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

  @Test
  void shouldFireEventEvery10Seconds() throws InterruptedException {
    long now = System.currentTimeMillis();
    long initialDelay = 10_000 - (now % 10_000);

    executor.scheduleAtFixedRate(
        () ->
            LOG.info(
                "Scheduled event fired at {}",
                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))),
        initialDelay,
        10_000,
        TimeUnit.MILLISECONDS);

    Thread.sleep(TimeUnit.MINUTES.toMillis(300));
  }
}