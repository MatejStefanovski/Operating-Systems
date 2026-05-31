import java.util.HashSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class VolleyballTournament {
    static class Player extends Thread {

        static Lock lock = new ReentrantLock();

        static Semaphore inHall = new Semaphore(12);
        static Semaphore DressingRoom = new Semaphore(4);

        static Semaphore startGame = new Semaphore(0);
        static Semaphore finishGame = new Semaphore(0);

        static int countDressed = 0;
        static int count = 0;

        public void execute() throws InterruptedException {
            inHall.acquire();

            // at most 12 players should print this in parallel
            System.out.println("Player inside.");
            // at most 4 players may enter in the dressing room in parallel

            DressingRoom.acquire();
            System.out.println("In dressing room.");
            Thread.sleep(10);// this represent the dressing time
            // after all players are ready, they should start with the game together
            DressingRoom.release();

            lock.lock();
            countDressed++;
            if (countDressed == 12){
                startGame.release(12);
            }
            lock.unlock();

            startGame.acquire();

            System.out.println("Game started.");
            Thread.sleep(100);// this represent the game duration

            System.out.println("Player done.");

            lock.lock();
            count++;
            if (count == 12){
                System.out.println("Game finished.");
                countDressed = 0;
                count = 0;
                finishGame.release(12);
            }
            lock.unlock();

            finishGame.acquire();
            // only one player should print the next line, representing that the game has finished

            inHall.release();
        }

        @Override
        public void run(){
            try
            {
                execute();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        HashSet<Player> threads = new HashSet<>();
        for (int i = 0; i < 60; i++) {
            Player p = new Player();
            threads.add(p);
        }
        // run all threads in background
        for (Player p : threads) {
            p.start();
        }
        // after all of them are started, wait each of them to finish for maximum 2_000 ms
        for (Player p : threads) {
            p.join(2_000);
        }
        boolean flag = false;
        for  (Player p : threads) {
            if (p.isAlive()) {
                p.interrupt();
                flag = true;
            }
        }
        // for each thread, terminate it if it is not finished
        if (flag){
            System.out.println("Possible deadlock!");
        }
        System.out.println("Tournament finished.");
    }
}
