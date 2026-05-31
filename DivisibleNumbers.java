import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DivisibleNumbers {

    static long totalSum = 0;
    static int totalCount = 0;

    static final NumberGenerator random = new NumberGenerator();

    private static final int ARRAY_LENGTH = 10000;
    private static final int NUM_THREADS = 10;

    // TODO: Define synchronization elements and initialize

    static Lock lock = new ReentrantLock();
    static Semaphore semaphore = new Semaphore(0);
    // DO NOT CHANGE
    public static int[] getSubArray(int[] array, int start, int end) {
        return Arrays.copyOfRange(array, start, end);
    }

    public static void main(String[] args) throws InterruptedException {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter x: ");
        int x = sc.nextInt();

        int[] arr = ArrayGenerator.generate(ARRAY_LENGTH);

        StatisticsThread[] threads = new StatisticsThread[NUM_THREADS];
        int chunk = ARRAY_LENGTH / NUM_THREADS;
        for (int i = 0; i < NUM_THREADS; i++) {
            int start = i * chunk;
            int end = start + chunk;
            if (i==NUM_THREADS-1) {
                end = arr.length;
            }
            int[] subarr = getSubArray(arr, start, end);
            StatisticsThread thread = new StatisticsThread(subarr, x);
            threads[i] = thread;
        }
        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i].start();
        }

        for (int i = 0; i < NUM_THREADS; i++) {
            semaphore.acquire();
        }
        // TODO: Make the StatisticsThread class a thread and start 10 instances
        // Each instance should take a subarray from the original array with equal length



        // all threads done



        // DO NOT CHANGE

        System.out.println("Total sum: " + totalSum);
        System.out.println("Total count: " + totalCount);

    }

    // TODO: Make the StatisticsThread class a thread
    static class StatisticsThread extends Thread {

        private int[] arr;
        private int x;

        public StatisticsThread(int[] arr, int x) {
            this.arr = arr;
            this.x = x;
        }

        // TODO: Implement the run method

        @Override
        public void run() {
            int localCounter = 0;
            int delivi = 0;
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] % x == 0) {
                    localCounter++;
                    delivi+=arr[i];
                }
            }
            lock.lock();
            totalCount += localCounter;
            totalSum += delivi;
            lock.unlock();
            semaphore.release();
        }
    }


    //************ DO NOT CHANGE ************//

    static class NumberGenerator {

        static final Random random = new Random();
        static final int RANDOM_BOUND = 1000;

        public int nextInt() {
            return random.nextInt(RANDOM_BOUND);
        }
    }

    static class ArrayGenerator {

        static int[] generate(int length) {

            int[] array = new int[length];

            for (int i = 0; i < length; i++) {

                array[i] = DivisibleNumbers.random.nextInt();
            }

            return array;
        }
    }
}