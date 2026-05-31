import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Cinema {

    static final int NUM_VIEWERS = 20;
    static final int NUM_RUNS = 100;

    static Lock lock;
    static Semaphore isFull;
    static Semaphore Start;
    static Semaphore Leave;
    static Semaphore Yes;
    static Semaphore AllLeft;

    static int NumViewers;
    static int ViewersLeftCimena;

    public static void init() {
        isFull = new Semaphore(NUM_VIEWERS);
        Start = new Semaphore(0);
        Leave = new Semaphore(0);
        Yes = new Semaphore(0);
        AllLeft = new Semaphore(0);
        NumViewers = 0;
        ViewersLeftCimena = 0;
        lock = new ReentrantLock();
    }

    public static void main(String[] args) {

        init();

        CinemaState state = new CinemaState(NUM_VIEWERS);

        Manager manager = new Manager(state);
        manager.start();

        List<Viewer> viewers = new ArrayList<>();

        for (int i = 0; i < NUM_VIEWERS; i++) {
            viewers.add(new Viewer(i, state));
        }

        for (Viewer v : viewers) {
            v.start();
        }
    }

    static class CinemaState {

        private int viewersInside = 0;
        private int viewersLeft = 0;

        private final int capacity;

        public CinemaState(int capacity) {
            this.capacity = capacity;
        }

        public void viewerEnters(int id) {
            viewersInside++;
            System.out.println("Viewer " + id + " entered.");
        }

        public void startScreening() {
            if (viewersInside != capacity) {
                throw new RuntimeException(
                        "Screening started before all viewers entered!");
            }
            System.out.println("Screening started.");
        }

        public void watchMovie(int id) {
            System.out.println("Viewer " + id + " watching.");
        }

        public void viewerLeaves() {
            viewersLeft++;
        }

        public boolean allViewersLeft() {
            return viewersLeft == capacity;
        }

        public void prepareNextScreening() {
            if (viewersLeft != capacity) {
                throw new RuntimeException(
                        "Not all viewers have left!");
            }

            viewersInside = 0;
            viewersLeft = 0;

            System.out.println("Preparing next screening.");
        }
    }

    static class Viewer extends Thread {

        private final int id;
        private final CinemaState state;

        public Viewer(int id, CinemaState state) {
            this.id = id;
            this.state = state;
        }

        public void execute() throws InterruptedException {

            state.viewerEnters(id);
            lock.lock();
            NumViewers++;
            if (NumViewers == 20){
                Yes.release();
                NumViewers = 0;
            }
            lock.unlock();

            Start.acquire();

            state.watchMovie(id);

            state.viewerLeaves();

            Leave.release();
            lock.lock();
            ViewersLeftCimena++;
            if (ViewersLeftCimena == 20){
                AllLeft.release();
                ViewersLeftCimena = 0;
            }
            lock.unlock();
        }

        @Override
        public void run() {
            for (int i = 0; i < NUM_RUNS; i++) {
                try {
                    execute();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class Manager extends Thread {

        private final CinemaState state;

        public Manager(CinemaState state) {
            this.state = state;
        }

        public void execute() throws InterruptedException {
            isFull.release(20);
            Yes.acquire();

            state.startScreening();

            Start.release(NUM_VIEWERS);

            Leave.acquire(NUM_VIEWERS);

            AllLeft.acquire();
            state.prepareNextScreening();
        }

        @Override
        public void run() {
            for (int i = 0; i < NUM_RUNS; i++) {
                try {
                    execute();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
