import java.util.HashSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MessageProcessing_V2 {

    static Lock lock = new ReentrantLock();
    static Semaphore messageHere = new Semaphore(0);
    static Semaphore activateProcess = new Semaphore(0);
    static Semaphore requestMessage =  new Semaphore(0);
    static Semaphore provideMessage = new Semaphore(0);
    static Semaphore done = new Semaphore(0);

    static int numMessages = 0;

    static class Processor extends Thread {


        public void execute() throws InterruptedException {
            int processedMessages=0;

            while(processedMessages < 50) {
                // wait fotr 5 ready messages in order to activate the processing
                messageHere.acquire(5);
                activateProcess.release(5);

                System.out.println("Activate processing");
                for (int i=0; i<5; i++){
                    requestMessage.release();

                    System.out.println("Request message");
                    // when the messasge is provided, process it

                    provideMessage.acquire();

                    System.out.println("Process message");
                    Thread.sleep(200);

                    done.release();
                    processedMessages++;
                    // if there are no more ready messages, pause the processing
                }
                System.out.println("Processing pause");
            }
        }

        @Override
        public void run() {
            try {
                execute();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    static class MessageSource extends Thread {

        public void execute() throws InterruptedException {
            Thread.sleep(50);
            messageHere.release();
            System.out.println("Message ready");
            activateProcess.acquire();
            requestMessage.acquire();
            // wait until the processor requests the message
            System.out.println("Provide message");
            provideMessage.release();
            // wait until the processor is done with the processing of the message
            done.acquire();
            System.out.println("Message delivered. Leaving.");
        }

        @Override
        public void run() {
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
        for (int i = 0; i < 50; i++) {
            MessageSource ms = new MessageSource();
            threads.add(ms);
        }
        threads.add(new Processor());
        // start all threads in background
        for (Thread t : threads) {
            t.start();
        }
        // after all of them are started, wait each of them to finish for 1_000 ms
        for (Thread t : threads) {
            t.join(1_000);
        }
        boolean flag = false;
        for (Thread t : threads) {
            if (t.isAlive()) {
                flag = true;
                t.interrupt();
            }
        }
        // after the waiting for each of the players is done, check the one that are not finished and terminate them
        if (flag) {
            System.err.println("Possible deadlock");
        }
    }

}
