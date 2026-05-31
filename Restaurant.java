import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Ingredient {
    private final String name;
    private final String unit;
    private int quantity;
    private boolean outOfStock;

    public Ingredient(String name, String unit) {
        this.name = name;
        this.unit = unit;
        quantity = 0;
        outOfStock = true;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean getOutOfStock() {
        return outOfStock;
    }

    public void setOutOfStock(boolean outOfStock) {
        this.outOfStock = outOfStock;
    }

    public void supply(int quantity) {
        this.quantity += quantity;
    }

    public void consume(int quantity) {
        this.quantity -= quantity;
    }
}

class Supplier extends Thread {
    private final String name;
    private final List<Ingredient> fridge;
    private final Lock fridgeLock;
    private volatile boolean isSupplying;

    public Supplier(String name, List<Ingredient> fridge, Lock frigdeLock) {
        this.name = name;
        this.fridge = fridge;
        this.fridgeLock = frigdeLock;
        isSupplying = true;
    }

    public void finish() {
        isSupplying = false;
    }

    @Override
    public void run() {
        while (isSupplying) {
            for (Ingredient ingredient : fridge) {
                fridgeLock.lock();
                if (ingredient.getQuantity() < 1000 || ingredient.getOutOfStock()) {
                    Random random = new Random();
                    int quantity = (random.nextInt(10) + 1) * 1000;
                    ingredient.supply(quantity);
                    System.out.printf("%s supplies %d %s of %s.\n", name, quantity, ingredient.getUnit(), ingredient.getName());
                }
                fridgeLock.unlock();
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }

        System.out.printf("%s finished supplying.\n", name);
    }
}

class Cook extends Thread {
    private final String name;
    private final List<Map<String, Integer>> recipes;
    private final Semaphore recipesSemaphore;
    private final List<Ingredient> fridge;
    private final Lock fridgeLock;
    private volatile boolean isCooking;

    public Cook(String name, List<Ingredient> fridge, Lock fridgeLock) {
        this.name = name;
        recipes = new ArrayList<>();
        recipesSemaphore = new Semaphore(1);
        this.fridge = fridge;
        this.fridgeLock = fridgeLock;
        isCooking = true;
    }

    public void submit(Map<String, Integer> recipe) throws InterruptedException {
        System.out.printf("%s received a new recipe.\n", name);
        recipesSemaphore.acquire();
        recipes.add(recipe);
        recipesSemaphore.release();
        System.out.printf("%s added the new recipe.\n", name);
    }

    public void finish() {
        isCooking = false;
    }

    @Override
    public void run() {
        System.out.printf("%s starts cooking...\n", name);

        while (isCooking || !recipes.isEmpty()) {
            try {
                sleep(100);
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }

            try {
                recipesSemaphore.acquire();
                if (!recipes.isEmpty()) {
                    Map<String, Integer> recipe = recipes.getFirst();
                    recipesSemaphore.release();
                    System.out.printf("%s starts to cook a recipe.\n", name);

                    System.out.printf("%s is cooking a recipe.\n", name);
                    Set<String> names = recipe.keySet();
                    for (String name : names) {
                        for (Ingredient ingredient : fridge) {
                            if (ingredient.getName().equals(name)) {
                                fridgeLock.lock();
                                int quantity = recipe.get(name);
                                System.out.printf("%s requests %d %s of %s.\n", this.name, quantity,
                                        ingredient.getUnit(), ingredient.getName());

                                while (ingredient.getQuantity() < quantity) {
                                    ingredient.setOutOfStock(true);
                                    fridgeLock.unlock();
                                    System.out.printf("%s waits %s to be supplied.\n", this.name, ingredient.getName());
                                    Thread.sleep(1000);
                                    fridgeLock.lock();
                                }

                                ingredient.setOutOfStock(false);
                                ingredient.consume(quantity);
                                fridgeLock.unlock();

                                System.out.printf("%s used %d of %s.\n", this.name, quantity, ingredient.getName());
                            }
                        }
                    }

                    recipesSemaphore.acquire();
                    recipes.removeFirst();
                    recipesSemaphore.release();
                    System.out.printf("%s cooked a recipe.\n", name);
                } else {
                    recipesSemaphore.release();
                }
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }

        System.out.printf("%s finished.\n", name);
    }
}

public class Restaurant {
    public static void main(String[] args) throws InterruptedException {
        Ingredient milk = new Ingredient("milk", "ml");
        Ingredient flour = new Ingredient("flour", "g");
        Ingredient cacao = new Ingredient("cacao", "g");

        List<Ingredient> fridge = new ArrayList<>();
        Lock fridgeLock = new ReentrantLock();

        fridge.add(milk);
        fridge.add(flour);
        fridge.add(cacao);

        Cook cook1 = new Cook("Marko", fridge, fridgeLock);
        Cook cook2 = new Cook("Peter", fridge, fridgeLock);

        Supplier supplier = new Supplier("John", fridge, fridgeLock);

        cook1.start();
        cook2.start();

        supplier.start();

        HashMap<String, Integer> recipe1 = new HashMap<String, Integer>();
        recipe1.put("flour", 400);
        recipe1.put("cacao", 600);
        recipe1.put("milk", 300);

        HashMap<String, Integer> recipe2 = new HashMap<String, Integer>();
        recipe2.put("flour", 1400);
        recipe2.put("cacao", 1600);
        recipe2.put("milk", 1300);

        HashMap<String, Integer> recipe3 = new HashMap<String, Integer>();
        recipe3.put("flour", 500);
        recipe3.put("cacao", 700);
        recipe3.put("milk", 700);

        HashMap<String, Integer> recipe4 = new HashMap<String, Integer>();
        recipe4.put("flour", 2500);
        recipe4.put("cacao", 5700);
        recipe4.put("milk", 3700);

        cook1.submit(recipe1);
        cook2.submit(recipe2);
        cook1.submit(recipe3);
        cook2.submit(recipe4);

        cook1.finish();
        cook2.finish();

        cook1.join();
        cook2.join();

        supplier.finish();

        supplier.join();
    }
}