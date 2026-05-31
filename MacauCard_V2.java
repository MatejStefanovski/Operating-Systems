import java.util.HashSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MacauCard_V2 {

    static Semaphore greenSeats = new Semaphore(2);
    static Semaphore redSeats = new Semaphore(2);

    static Semaphore startMatch = new Semaphore(0);
    static Semaphore finishGame = new Semaphore(0);

    static Lock lock = new ReentrantLock();

    static int seated = 0;
    static int finishedGamePlayers = 0;
    static int finishedMatchPlayers = 0;

    static class GreenPlayer extends Thread {

        public void execute() throws InterruptedException {
            System.out.println("Green player ready");
            Thread.sleep(50);

            greenSeats.acquire();
            System.out.println("Green player here");

            lock.lock();
            seated++;
            if (seated == 4)
                startMatch.release(4);
            lock.unlock();

            startMatch.acquire();
            for (int num = 1; num <= 3; num++) {
                System.out.println("Game " + num + " started");
                Thread.sleep(200);
                System.out.println("Green player finished game " + num);

                lock.lock();
                finishedGamePlayers++;
                if (finishedGamePlayers == 4) {
                    System.out.println("Game " + num + " finished");
                    finishedGamePlayers = 0;
                    finishGame.release(4);
                }
                lock.unlock();

                finishGame.acquire();
            }

            lock.lock();
            finishedMatchPlayers++;
            if (finishedMatchPlayers == 4) {
                System.out.println("Match finished");
                finishedMatchPlayers = 0;
                seated = 0;
                greenSeats.release(2);
                redSeats.release(2);
            }
            lock.unlock();
        }

        @Override
        public void run() {
            try {
                execute();
            } catch (InterruptedException e) {
            }
        }
    }

    static class RedPlayer extends Thread {

        public void execute() throws InterruptedException {
            System.out.println("Red player ready");
            Thread.sleep(50);

            redSeats.acquire();
            System.out.println("Red player here");

            lock.lock();
            seated++;
            if (seated == 4)
                startMatch.release(4);
            lock.unlock();

            startMatch.acquire();

            for (int num = 1; num <= 3; num++) {
                System.out.println("Game " + num + " started");
                Thread.sleep(200);
                System.out.println("Red player finished game " + num);

                lock.lock();
                finishedGamePlayers++;
                if (finishedGamePlayers == 4) {
                    System.out.println("Game " + num + " finished");
                    finishedGamePlayers = 0;
                    finishGame.release(4);
                }
                lock.unlock();

                finishGame.acquire();
            }

            lock.lock();
            finishedMatchPlayers++;
            if (finishedMatchPlayers == 4) {
                System.out.println("Match finished");
                finishedMatchPlayers = 0;
                seated = 0;
                greenSeats.release(2);
                redSeats.release(2);
            }
            lock.unlock();
        }

        @Override
        public void run() {
            try {
                execute();
            } catch (InterruptedException e) {
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {

        HashSet<Thread> threads = new HashSet<>();

        for (int i = 0; i < 30; i++) {
            threads.add(new GreenPlayer());
            threads.add(new RedPlayer());
        }

        for (Thread t : threads)
            t.start();

        for (Thread t : threads)
            t.join(1000);

        boolean deadlock = false;

        for (Thread t : threads) {
            if (t.isAlive()) {
                deadlock = true;
                t.interrupt();
            }
        }

        if (deadlock)
            System.out.println("Possible deadlock");
    }
}