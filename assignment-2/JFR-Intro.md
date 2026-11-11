# JDK Flight Recorder (JFR): A Short Introduction

COM-3820 Parallel Programming · HW #2: Trade Server

JDK Flight Recorder (JFR) is a profiler built into the JDK. While your program runs, the JVM records timestamped events (a thread blocked on a lock, a method sample, a garbage collection) into a `.jfr` file that you analyze afterward. It comes with Java 21; there is nothing to install.

Part of your grade is your brokerage's throughput. When your load test reports a number lower than you hoped, JFR tells you why: which lock threads wait on, or where the CPU time goes. Using it is optional; nothing you record is submitted or graded.

## 1. Making a recording

Add one option to the `java` command that runs your load test. This one skips the first 10 seconds (JIT warm-up) and records the next 20:

```
java -XX:StartFlightRecording:filename=run.jfr,settings=profile,locking-threshold=1ms,delay=10s,duration=20s \
     -cp target/trade-server-1.0.jar <your load test's main class>
```

- `settings=profile`: more detail than the default settings, at somewhat higher overhead.
- `locking-threshold=1ms`: records lock waits of 1 ms or longer. The built-in threshold is 10 to 20 ms, which hides most contention; `0ms` records every contended wait. Waits shorter than the threshold are never recorded.
- `delay` and `duration`: record only the steady part of the run.

Then check what was recorded:

```
jfr summary run.jfr
```

## 2. Which lock are threads waiting on?

JFR records lock waits as two different events:

- **`jdk.JavaMonitorEnter`**: a thread blocked entering a `synchronized` block or method.
- **`jdk.ThreadPark`**: a thread parked by a `java.util.concurrent` lock (`ReentrantLock`, `ReentrantReadWriteLock`, `StampedLock`, conditions). Its `parkedClass` field says what it parked on.

If you look at the wrong one, your contention seems to vanish.

**`synchronized`.** The built-in view groups waits by call site:

```
$ jfr view --width 140 contention-by-site run.jfr

StackTrace                                                         Count    Avg.    Max.
------------------------------------------------------------------ ------ ------- -------
edu.yu.parallel.Impl.BrokerageImpl.getClientPortfolio(String)      11,523 6.76 ms 33.0 ms
edu.yu.parallel.Impl.BrokerageImpl.buyShares(String, String, int)  39,227 6.76 ms 32.8 ms
edu.yu.parallel.Impl.BrokerageImpl.sellShares(String, String, int) 33,568 6.78 ms 32.5 ms
...
```

Here every operation waits on the same monitor.

**`java.util.concurrent` locks.** The `contention-by-*` views count only `synchronized` waits, so they show nothing here. This short program groups `ThreadPark` events by what was parked on and the first frame in your own code. Save it as `ParkSummary.java` and run `java ParkSummary.java run.jfr`.

```java
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import jdk.jfr.consumer.*;

/**
 * Groups jdk.ThreadPark events by the class parked on and the first
 * stack frame in your own code. Usage: java ParkSummary.java recording.jfr
 */
public class ParkSummary {
    record Stats(long count, Duration total, Duration max) {
        Stats add(Duration d) {
            return new Stats(count + 1, total.plus(d), d.compareTo(max) > 0 ? d : max);
        }
    }

    public static void main(String[] args) throws Exception {
        Map<String, Stats> bySite = new HashMap<>();
        for (RecordedEvent e : RecordingFile.readAllEvents(Path.of(args[0]))) {
            if (!e.getEventType().getName().equals("jdk.ThreadPark")) continue;
            RecordedClass parked = e.getClass("parkedClass");
            String key = (parked == null ? "?" : parked.getName()) + "  @ " + firstUserFrame(e.getStackTrace());
            bySite.merge(key, new Stats(1, e.getDuration(), e.getDuration()),
                    (a, b) -> a.add(b.total()));
        }
        System.out.printf("%7s %10s %10s  %s%n", "Count", "Total ms", "Max ms", "Parked on @ site");
        bySite.entrySet().stream()
              .sorted((a, b) -> b.getValue().total().compareTo(a.getValue().total()))
              .forEach(en -> System.out.printf("%7d %10.1f %10.1f  %s%n",
                      en.getValue().count(),
                      en.getValue().total().toNanos() / 1e6,
                      en.getValue().max().toNanos() / 1e6,
                      en.getKey()));
    }

    static String firstUserFrame(RecordedStackTrace st) {
        if (st == null) return "?";
        for (RecordedFrame f : st.getFrames()) {
            String cls = f.getMethod().getType().getName();
            if (!cls.startsWith("java.") && !cls.startsWith("jdk.") && !cls.startsWith("sun.")) {
                return cls + "." + f.getMethod().getName() + " line " + f.getLineNumber();
            }
        }
        return "(JDK only) " + st.getFrames().get(0).getMethod().getName();
    }
}
```

Sample output:

```
  Count   Total ms     Max ms  Parked on @ site
  45951   113115.4       29.3  java.util.concurrent.locks.ReentrantLock$NonfairSync  @ edu.yu.parallel.Impl.BrokerageImpl.buyShares line 163
  39536    97939.5       29.4  java.util.concurrent.locks.ReentrantLock$NonfairSync  @ edu.yu.parallel.Impl.BrokerageImpl.sellShares line 192
```

Over 20 seconds of recording, threads spent 113 seconds in total parked at line 163. Compare total wait time with recording length times thread count (here 20 s × 40 threads = 800 s) to see how much of the run is spent waiting.

To see full stacks for one event type: `jfr print --events jdk.ThreadPark --stack-depth 8 run.jfr`.

## 3. Where does the CPU time go?

Once threads stop waiting, the limit is the work itself: hashing, copying, allocation. Two views show it:

```
jfr view --width 140 hot-methods run.jfr
jfr view --width 140 allocation-by-class run.jfr
```

`hot-methods` lists the methods that appear most in CPU samples; `allocation-by-class` lists what your code allocates most, which drives garbage collection. Your load test's own methods show up too (picking random clients, for example); they cost time in the measurement, not in your brokerage. A JDK method near the top, such as `HashMap.getNode` or `HashMap$TreeNode.find`, usually points to a data structure your code uses on every operation; look for the caller in your own code.

## 4. Pitfalls

- **Wrong event type.** `synchronized` → `JavaMonitorEnter`; `java.util.concurrent` locks → `ThreadPark`. If you use both, check both.
- **Threshold.** Waits shorter than `locking-threshold` are not recorded. A lock taken millions of times for a few microseconds each can matter more than a few long waits.
- **Idle threads.** A thread that parks with only JDK frames in its stack (for example on `AbstractQueuedSynchronizer$ConditionObject`) is usually waiting for work, not for your lock. `ParkSummary` shows these as `(JDK only)`.
- **JFR's own threads.** Entries under `jdk.jfr.internal` are the recorder itself.
- **Overhead.** Never take your throughput number from a recorded run. Record separately, and measure without JFR.

## 5. A workable loop

1. Run your load test without JFR and note the throughput.
2. Record a run as in section 1.
3. Find the largest cost: total wait time at one site, or a method near the top of `hot-methods`.
4. Change one thing.
5. Run `mvn test` and your own correctness checks. A faster brokerage that loses a share earns nothing for speed.
6. Measure again without JFR, and keep the change only if throughput went up.

Run at 40 threads, as I do when grading; contention that is invisible at 4 threads can dominate at 40.

## 6. References

- [JDK Flight Recorder tutorial series (dev.java)](https://dev.java/learn/jvm/jfr): short and current, from the Java team.
- [New JFR `view` command (Erik Gahlin)](https://egahlin.github.io/2023/05/30/views.html): the views added in Java 21.
- [JFR event reference for Java 21 (SAP)](https://sap.github.io/jfrevents/21.html): every event type and its fields.
