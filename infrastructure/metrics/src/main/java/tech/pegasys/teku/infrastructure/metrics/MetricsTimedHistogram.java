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

package tech.pegasys.teku.infrastructure.metrics;

import com.google.errorprone.annotations.MustBeClosed;
import io.prometheus.client.Histogram;
import java.io.Closeable;
import java.io.IOException;

public class MetricsTimedHistogram {
  final Histogram delegate;

  public MetricsTimedHistogram(final String name, final String help) {
    // TODO: MetricsSystem param
    // TODO: register in MetricsSystem
    this.delegate = Histogram.build(name, help).create();
  }

  @MustBeClosed
  public HistogramTimer startTimer() {
    return new HistogramTimer(delegate.startTimer());
  }

  public static class HistogramTimer implements Closeable {
    final Histogram.Timer delegate;

    public HistogramTimer(Histogram.Timer delegate) {
      this.delegate = delegate;
    }

    @Override
    public void close() throws IOException {
      delegate.close();
    }
  }
}
