import java.util.Scanner;

public class SkalarenProizvod {
    public static void main(String[] args) throws InterruptedException {
        int n = 1_000_000;
        double[] a = new  double[n];
        double[] b = new  double[n];
        for (int i = 0; i < n; i++) {
            a[i] = i;
            b[i] = i + 1;
        }
        Scanner sc = new Scanner(System.in);
        int m = sc.nextInt();
        ParallelDotProduct[] threads = new ParallelDotProduct[m];
        int chunksize=n/m;
        for (int i = 0; i < m; i++) {
            int start = i*chunksize;
            int end;
            if (i==m-1){
                end = n;
            }
            else{
                end = start+chunksize;
            }
            threads[i]=new ParallelDotProduct(a, b, start, end);
            threads[i].start();
        }
        double result=0;
        for (int i = 0; i < m; i++) {
            threads[i].join();
            result+=threads[i].getPartialSum();
        }
        System.out.println(result);
    }

    public static class ParallelDotProduct extends Thread {
        private double[] a;
        private double[] b;
        private int start;
        private int end;
        private double partialSum=0;

        public ParallelDotProduct(double[] a, double[] b, int start, int end) {
            this.a = a;
            this.b = b;
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            for (int i = start; i < end; i++) {
                partialSum +=  a[i] * b[i];
            }
        }

        public double getPartialSum() {
            return partialSum;
        }
    }
}
