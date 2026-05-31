import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DocumentProcessing {
    static final int NUM_RUNS = 100;
    static final int NUM_WORKERS = 40;

    static Lock lock;
    static Semaphore scannerSemaphore;
    static Semaphore workerSemaphore;

    public static void init() {
        lock = new ReentrantLock();
        scannerSemaphore = new Semaphore(1);
        workerSemaphore = new Semaphore(0);
    }

    public static void main(String[] args) {
        init();

        DocumentState state = new DocumentState(NUM_WORKERS);
        Scanner scanner = new Scanner(state);
        scanner.start();

        List<Worker> workers = new ArrayList<>();
        for (int i = 0; i < NUM_WORKERS; i++) {
            workers.add(new Worker(i, state));
        }

        for (int i = 0; i < NUM_WORKERS; i++) {
            workers.get(i).start();
        }
    }

    static class DocumentState {
        private int scannedDocuments = 0;
        private final int capacity;

        public DocumentState(int capacity) {
            this.capacity = capacity;
        }

        public int getCapacity() {
            return capacity;
        }

        public void scanPackage() {
            if (scannedDocuments != 0) {
                throw new RuntimeException("Претходниот пакет уште не е готов!");
            }
            scannedDocuments = capacity;
            System.out.println("Скенерот подготви нов пакет документи.");
        }

        public void decrementDocumentsLeft() {
            if (scannedDocuments <= 0) {
                throw new RuntimeException("Нема повеќе документи за обработка во овој пакет!");
            }
            scannedDocuments--;
        }

        public boolean isPackageEmpty() {
            return scannedDocuments == 0;
        }

        public void processDocument(int workerId) {
            System.out.println(String.format("Вработениот со ID: %d обработува документ.", workerId));
        }
    }

    static class Worker extends Thread {
        private DocumentState state;
        private int workerId;

        public Worker(int workerId, DocumentState state) {
            this.state = state;
            this.workerId = workerId;
        }

        public void execute() throws InterruptedException {
            workerSemaphore.acquire();
            state.processDocument(workerId);
            lock.lock();
            state.decrementDocumentsLeft();
            if (state.isPackageEmpty()) {
                scannerSemaphore.release();
            }
            lock.unlock();
        }

        @Override
        public void run() {
            for (int i = 0; i < DocumentProcessing.NUM_RUNS; i++) {
                try {
                    execute();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class Scanner extends Thread {
        private DocumentState state;

        public Scanner(DocumentState state) {
            this.state = state;
        }

        public void execute() throws InterruptedException {
            scannerSemaphore.acquire();
            state.scanPackage();
            workerSemaphore.release(state.getCapacity());
        }

        @Override
        public void run() {
            for (int i = 0; i < DocumentProcessing.NUM_RUNS; i++) {
                try {
                    execute();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}