import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class VideoStreaming {

    static Semaphore bufferEmpty = new Semaphore(0);
    static Semaphore bufferFull = new Semaphore(5);

    static class VideoBuffer {

        private Queue<Integer> buffer = new LinkedList<>();
        private final int CAPACITY = 5;

        public void downloadChunk(int chunk) throws InterruptedException {

            // TODO:
            // If buffer is full, wait.
            // Add the chunk.
            // Wake waiting threads.

            bufferFull.acquire();
            bufferEmpty.release();
            buffer.add(chunk);
        }

        public int playChunk() throws InterruptedException {

            // TODO:
            // If buffer is empty, wait.
            // Remove a chunk.
            // Wake waiting threads.
            // Return removed chunk.
            bufferEmpty.acquire();
            buffer.remove();
            bufferFull.release();
            return -1;
        }
    }

    static class Downloader extends Thread {

        private VideoBuffer buffer;

        public Downloader(VideoBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public void run() {

            int chunk = 1;

            while (true) {
                try {

                    Thread.sleep(500);

                    buffer.downloadChunk(chunk++);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class Player extends Thread {

        private VideoBuffer buffer;

        public Player(VideoBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public void run() {

            while (true) {
                try {

                    Thread.sleep(1000);

                    buffer.playChunk();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {

        VideoBuffer buffer = new VideoBuffer();

        Downloader downloader = new Downloader(buffer);
        Player player = new Player(buffer);

        downloader.start();
        player.start();
    }
}