import java.util.HashSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MessageProcessing {

    static Semaphore readyMessages = new Semaphore(0);
    static Semaphore requestMessage = new Semaphore(0);
    static Semaphore messageProvided = new Semaphore(0);
    static Semaphore processed = new Semaphore(0);

    static Lock lock = new ReentrantLock();

    static int waitingMessages = 0;

    static class Processor extends Thread {

        public void execute() throws InterruptedException {

            int processedMessages = 0;
            while (processedMessages < 50) {
                // Wait until at least 5 messages are ready
                readyMessages.acquire(5);
                System.out.println("Activate processing");
                lock.lock();
                waitingMessages -= 5;
                int available = 5;
                lock.unlock();
                while (available > 0) {
                    System.out.println("Request message");
                    requestMessage.release();
                    messageProvided.acquire();
                    System.out.println("Process message");
                    Thread.sleep(200);
                    processed.release();
                    processedMessages++;
                    lock.lock();
                    if (waitingMessages > 0) {
                        waitingMessages--;
                        available++;
                    }
                    lock.unlock();
                    available--;
                }
                System.out.println("Processing pause");
            }
        }

        @Override
        public void run() {
            try {
                execute();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class MessageSource extends Thread {

        public void execute() throws InterruptedException {
            Thread.sleep(50);
            System.out.println("Message ready");
            lock.lock();
            waitingMessages++;
            lock.unlock();
            readyMessages.release();
            requestMessage.acquire();
            System.out.println("Provide message");
            messageProvided.release();
            processed.acquire();
            System.out.println("Message delivered. Leaving.");
        }

        @Override
        public void run() {
            try {
                execute();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        HashSet<Thread> threads = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            threads.add(new MessageSource());
        }
        threads.add(new Processor());
        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join(1000);
        }
        boolean deadlock = false;
        for (Thread t : threads) {
            if (t.isAlive()) {
                t.interrupt();
                deadlock = true;
            }
        }
        if (deadlock) {
            System.err.println("Possible deadlock");
        }
    }
}
