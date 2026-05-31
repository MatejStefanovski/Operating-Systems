class Warehouse {
    int stock = 0;
    int capacity = 100;

    public synchronized void addProduct() {
        while (stock == capacity){
            try {
                wait();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        stock++;
        notifyAll();
    }

    public synchronized void removeProduct() {
          while (stock == 0){
              try{
                  wait();
              }
              catch(InterruptedException e){
                  e.printStackTrace();
              }
          }
          stock--;
          notifyAll();
    }

    public synchronized int getStock() {
        return stock;
    }
}

class ProducerThread extends Thread {
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

class ConsumerThread extends Thread {
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

public class ProductConsumer_V2 {
    public static void main(String[] args) throws Exception {
        Warehouse warehouse = new Warehouse();

        ProducerThread p1 = new ProducerThread(warehouse, 100000);
        ProducerThread p2 = new ProducerThread(warehouse, 100000);
        ProducerThread p3 = new ProducerThread(warehouse, 100000);

        ConsumerThread c1 = new ConsumerThread(warehouse, 100000);
        ConsumerThread c2 = new ConsumerThread(warehouse, 100000);

        p1.start();
        p2.start();
        p3.start();

        c1.start();
        c2.start();

        p1.join();
        p2.join();
        p3.join();

        c1.join();
        c2.join();

        System.out.println("Final stock = " + warehouse.getStock());
    }
}
