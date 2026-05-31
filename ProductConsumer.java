public class ProductConsumer {
    static class Warehouse {
        int stock = 0;

        public synchronized void addProduct() {
            stock++;
        }

        public synchronized void removeProduct() {
            if (stock > 0) {
                stock--;
            }
        }

        public synchronized int getStock() {
            return stock;
        }
    }

    static class ProducerThread extends Thread {
        Warehouse warehouse;
        int repetitions;

        public ProducerThread(Warehouse warehouse, int repetitions) {
            this.warehouse = warehouse;
            this.repetitions = repetitions;
        }

        public void run() {
            for (int i = 0; i < repetitions; i++) {
                warehouse.addProduct();
            }
        }
    }

    static class ConsumerThread extends Thread {
        Warehouse warehouse;
        int repetitions;

        public ConsumerThread(Warehouse warehouse, int repetitions) {
            this.warehouse = warehouse;
            this.repetitions = repetitions;
        }

        public void run() {
            for (int i = 0; i < repetitions; i++) {
                warehouse.removeProduct();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Warehouse warehouse = new Warehouse();

        ProducerThread p1 = new ProducerThread(warehouse, 100000);
        ProducerThread p2 = new ProducerThread(warehouse, 100000);

        ConsumerThread c1 = new ConsumerThread(warehouse, 100000);
        ConsumerThread c2 = new ConsumerThread(warehouse, 100000);

        p1.start();
        p2.start();
        c1.start();
        c2.start();

        p1.join();
        p2.join();
        c1.join();
        c2.join();

        System.out.println("Final stock = " + warehouse.getStock());
    }
}
