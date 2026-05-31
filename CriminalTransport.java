import java.util.HashSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CriminalTransport {

    static Semaphore policeQueue = new Semaphore(0);
    static Semaphore criminalQueue = new Semaphore(0);

    static Semaphore barrier = new Semaphore(0);
    static Semaphore arrived = new Semaphore(0);

    static Lock lock = new ReentrantLock();

    static int waitingPolice = 0;
    static int waitingCriminals = 0;
    static int passengersInCar = 0;

    static class Policeman extends Thread {

        public void execute() throws InterruptedException {

            boolean captain = false;

            lock.lock();

            waitingPolice++;

            if (waitingPolice >= 4) {
                policeQueue.release(4);
                waitingPolice -= 4;
                captain = true;
            }
            else if (waitingPolice >= 2 && waitingCriminals >= 2) {
                policeQueue.release(2);
                criminalQueue.release(2);

                waitingPolice -= 2;
                waitingCriminals -= 2;

                captain = true;
            }

            lock.unlock();

            policeQueue.acquire();

            System.out.println("Policeman enters in the car");

            lock.lock();

            passengersInCar++;

            if (passengersInCar == 4) {
                barrier.release(4);
            }

            lock.unlock();

            barrier.acquire();

            if (captain) {
                System.out.println("Start driving.");
                Thread.sleep(100);
                System.out.println("Arrived.");

                arrived.release(4);

                lock.lock();
                passengersInCar = 0;
                lock.unlock();
            }

            arrived.acquire();

            System.out.println("Policeman exits from the car");
        }

        @Override
        public void run() {
            try {
                execute();
            } catch (InterruptedException e) {
            }
        }
    }

    static class Criminal extends Thread {

        public void execute() throws InterruptedException {

            boolean captain = false;

            lock.lock();

            waitingCriminals++;

            if (waitingCriminals >= 4) {
                criminalQueue.release(4);
                waitingCriminals -= 4;
                captain = true;
            }
            else if (waitingCriminals >= 2 && waitingPolice >= 2) {
                criminalQueue.release(2);
                policeQueue.release(2);

                waitingCriminals -= 2;
                waitingPolice -= 2;

                captain = true;
            }

            lock.unlock();

            criminalQueue.acquire();

            System.out.println("Criminal enters in the car");

            lock.lock();

            passengersInCar++;

            if (passengersInCar == 4) {
                barrier.release(4);
            }

            lock.unlock();

            barrier.acquire();

            if (captain) {
                System.out.println("Start driving.");
                Thread.sleep(100);
                System.out.println("Arrived.");

                arrived.release(4);

                lock.lock();
                passengersInCar = 0;
                lock.unlock();
            }

            arrived.acquire();

            System.out.println("Criminal exits from the car");
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

        for (int i = 0; i < 60; i++) {
            threads.add(new Policeman());
            threads.add(new Criminal());
        }

        for (Thread t : threads) {
            t.start();
        }

        boolean terminated = false;

        for (Thread t : threads) {
            t.join(1000);

            if (t.isAlive()) {
                terminated = true;
                t.interrupt();
            }
        }

        if (terminated) {
            System.out.println("Terminated transport");
        } else {
            System.out.println("Finished transport");
        }
    }
}
