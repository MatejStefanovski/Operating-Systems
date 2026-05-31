import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class EvenSum {

    static long totalSum = 0;

    static final NumberGenerator random = new NumberGenerator();

    private static final int ARRAY_LENGTH = 10000;
    private static final int NUM_THREADS = 10;

    // TODO: Define synchronization elements and initialize
    static Lock lock = new ReentrantLock();

    // DO NOT CHANGE
    public static int[] getSubArray(int[] array, int start, int end) {
        return Arrays.copyOfRange(array, start, end);
    }

    public static void main(String[] args) throws InterruptedException {

        int[] arr = ArrayGenerator.generate(ARRAY_LENGTH);

        // TODO: Make the SumThread class a thread and start 10 instances
        SumThread[] threads = new SumThread[NUM_THREADS];
        int chunk = ARRAY_LENGTH / NUM_THREADS;
        for (int i = 0; i < NUM_THREADS; i++) {
            int start = i * chunk;
            int end = (i + 1) * chunk;
            int[] subarray = getSubArray(arr, start, end);
            threads[i] = new SumThread(subarray);
            threads[i].start();
        }

        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i].join();
        }
        // Each instance should take a subarray from the original array with equal length



        // all threads done



        // DO NOT CHANGE

        System.out.println("The total sum of even numbers is: " + totalSum);

    }

    // TODO: Make the SumThread class a thread
    static class SumThread extends Thread {

        private int[] arr;

        public SumThread(int[] arr) {
            this.arr = arr;
        }

        public void run() {

            long localSum = 0;

            for (int num : this.arr) {

                if (num % 2 == 0) {
                    localSum += num;
                }
            }

            // TODO: Add localSum to totalSum using lock
            lock.lock();
            totalSum += localSum;
            lock.unlock();
        }
    }


    //************ DO NOT CHANGE ************//

    static class NumberGenerator {

        static final Random random = new Random();
        static final int RANDOM_BOUND = 100;

        public int nextInt() {
            return random.nextInt(RANDOM_BOUND);
        }
    }

    static class ArrayGenerator {

        static int[] generate(int length) {

            int[] array = new int[length];

            for (int i = 0; i < length; i++) {

                array[i] = EvenSum.random.nextInt();
            }

            return array;
        }
    }
}
