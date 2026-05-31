public class SequentialVectorDifference {
    public static void main(String[] args) throws InterruptedException {
        int n = 1_000_000;

        double[] a = new double[n];
        double[] b = new double[n];

        for (int i = 0; i < n; i++) {
            a[i] = i;
            b[i] = i + 5;
        }

        double result = 0;
        int m = 4;

        int chunk = n/m;
        ParallelVectorDifference[] threads = new ParallelVectorDifference[m];
        for (int i = 0; i < m; i++) {
            int start = i*chunk;
            int end = (i+1)*chunk;
            threads[i] = new ParallelVectorDifference(a, b, start, end);
            threads[i].start();
        }

        for (int i = 0; i < m; i++) {
            threads[i].join();
            result+=threads[i].getRez();
        }

        System.out.println("Sum of differences = " + result);
    }

    static class ParallelVectorDifference extends Thread {
        double[] a;
        double[] b;
        double rez;
        int start;
        int end;
        public ParallelVectorDifference(double[] a, double[] b, int start, int end) {
            this.a = a;
            this.b = b;
            this.start = start;
            this.end = end;
            rez = 0;
        }
        @Override
        public void run() {
            for (int i = start; i < end; i++) {
                rez += Math.abs(a[i] - b[i]);
            }
        }
        public double getRez() {
            return rez;
        }
    }
}