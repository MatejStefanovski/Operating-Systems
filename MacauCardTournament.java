import java.util.HashSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MacauCardTournament {

    static Semaphore greenSeats = new Semaphore(2);
    static Semaphore redSeats = new Semaphore(2);
    static Lock lock = new ReentrantLock();
    static Semaphore startGame = new Semaphore(0);
    static Semaphore finishGame = new Semaphore(0);

    static int playersHere;
    static int playersFinish;

    static class GreenPlayer extends Thread  {

        public void execute() throws InterruptedException {
            System.out.println("Green player ready");
            Thread.sleep(50);

            greenSeats.acquire();

            System.out.println("Green player here");
            lock.lock();
            playersHere++;
            if (playersHere == 4){
                startGame.release(4);
            }
            lock.unlock();

            startGame.acquire();
            for (int num = 1; num <=3; num++) {
                System.out.println("Game "+ num +" started");
                Thread.sleep(200);
                System.out.println("Green player finished game "+ num);
                lock.lock();
                playersFinish++;
                if (playersFinish == 4){
                    playersFinish=0;
                    System.out.println("Game "+ num +" finished");
                    finishGame.release(4);
                }
                lock.unlock();
                finishGame.acquire();
            }
            lock.lock();
            playersHere--;
            if (playersHere == 0){
                System.out.println("Match finished");
                greenSeats.release(2);
                redSeats.release(2);
            }
            lock.unlock();
        }

        @Override
        public void run(){
            try {
                execute();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
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
            playersHere++;
            if (playersHere == 4){
                startGame.release(4);
            }
            lock.unlock();
            startGame.acquire();
            for (int num = 1; num <=3; num++) {
                System.out.println("Game "+ num +" started");
                Thread.sleep(200);
                System.out.println("Red player finished game "+ num);
                lock.lock();
                playersFinish++;
                if (playersFinish == 4){
                    playersFinish=0;
                    System.out.println("Game "+ num +" finished");
                    finishGame.release(4);
                }
                lock.unlock();
                finishGame.acquire();
            }
            lock.lock();
            playersHere--;
            if (playersHere == 0) {
                System.out.println("Match finished");
                greenSeats.release(2);
                redSeats.release(2);
            }
            lock.unlock();
        }

        @Override
        public void run(){
            try {
                execute();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    public static void main(String[] args) throws InterruptedException {
        HashSet<Thread> threads = new HashSet<Thread>();
        for (int i = 0; i < 30; i++) {
            RedPlayer red = new RedPlayer();
            threads.add(red);
            GreenPlayer green = new GreenPlayer();
            threads.add(green);
        }
        // start 30 red and 30 green players in background
        for(Thread thread : threads){
            thread.start();
        }
        // after all of them are started, wait each of them to finish for 1_000 ms
        for(Thread thread : threads){
            thread.join(1_000);
        }
        // after the waiting for each of the players is done, check the one that are not finished and terminate them
        for(Thread thread : threads){
            if (thread.isAlive()){
                thread.interrupt();
                System.out.println("Possible Deadlock");
            }
        }
        System.err.println("Possible deadlock");
    }

}
