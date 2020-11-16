package Utils;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * <p/>
 * Producer-consumer test program for BoundedBuffer
 *
 * @author Brian Goetz and Tim Peierls
 * modified by Dr. Jun Yuan for CS371 FALL 2020
 */
public class UnboundBufferTest {
    private static ExecutorService pool;
    private CyclicBarrier barrier;
    private UnboundBuffer<Integer> bb;
    private int nTrials, nPairs;
    private AtomicInteger putSum = new AtomicInteger(0);
    private AtomicInteger takeSum = new AtomicInteger(0);

    @Test
    public void test1_1p1c() {
        pool = Executors.newCachedThreadPool();
        prepTest(10, 1, 100000);
        test(); // sample parameters
        pool.shutdown();
    }

    @Test
    public void test2_2p2c() {
        pool = Executors.newCachedThreadPool();
        prepTest(10, 2, 100000);
        test(); // sample parameters
        pool.shutdown();
    }

    @Test
    public void test3_10pairs() {
        pool = Executors.newCachedThreadPool();
        prepTest(10, 10, 100000);
        test(); // sample parameters
        pool.shutdown();
    }

    private void prepTest(int capacity, int npairs, int ntrials) {
        this.bb = new UnboundBuffer<Integer>(capacity);
        this.nTrials = ntrials;
        this.nPairs = npairs;
        this.barrier = new CyclicBarrier(npairs * 2 + 1);
    }

    void test() {
        try {
            for (int i = 0; i < nPairs; i++) {
                pool.execute(new Producer());
                pool.execute(new Consumer());
            }
            barrier.await(); // wait for all threads to be ready
            barrier.await(); // wait for all threads to finish
            assertEquals(putSum.get(), takeSum.get());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static int xorShift(int y) {
        y ^= (y << 6);
        y ^= (y >>> 21);
        y ^= (y << 7);
        return y;
    }

    class Producer implements Runnable {
        public void run() {
            try {
                int seed = (this.hashCode() ^ (int) System.nanoTime());
                int sum = 0;
                barrier.await();
                for (int i = nTrials; i > 0; --i) {
                    bb.deposit(seed);
                    sum += seed;
                    seed = xorShift(seed);
                }
                putSum.getAndAdd(sum);
                barrier.await();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    class Consumer implements Runnable {
        public void run() {
            try {
                barrier.await();
                int sum = 0;
                for (int i = nTrials; i > 0; --i) {
                    sum += bb.fetch();
                }
                takeSum.getAndAdd(sum);
                barrier.await();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}