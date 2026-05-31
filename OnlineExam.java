import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class OnlineExam {

    static final int NUM_STUDENTS = 30;
    static final int NUM_RUNS = 100;

    static Semaphore arrived;
    static Semaphore examStarted;
    static Semaphore examSubmitted;
    static  Semaphore sessionFinished;
    static Lock lock;

    static int numArrived;
    public static void init() {
        arrived = new Semaphore(0);
        examStarted = new Semaphore(0);
        examSubmitted = new Semaphore(0);
        sessionFinished = new Semaphore(0);
        lock = new ReentrantLock();

        numArrived = 0;
    }

    public static void main(String[] args) {

        init();

        ExamState state = new ExamState(NUM_STUDENTS);

        Professor professor = new Professor(state);
        professor.start();

        List<Student> students = new ArrayList<>();

        for (int i = 0; i < NUM_STUDENTS; i++) {
            students.add(new Student(i, state));
        }

        for (Student s : students) {
            s.start();
        }
    }

    static class ExamState {

        private int arrived = 0;
        private int submitted = 0;

        private final int capacity;

        public ExamState(int capacity) {
            this.capacity = capacity;
        }

        public void studentArrives(int id) {
            arrived++;
            System.out.println("Student " + id + " arrived.");
        }

        public void startExam() {
            if (arrived != capacity) {
                throw new RuntimeException(
                        "Exam started before all students arrived!");
            }
            System.out.println("Exam started.");
        }

        public void solveExam(int id) {
            System.out.println("Student " + id + " solving.");
        }

        public void submitExam(int id) {
            submitted++;
            System.out.println("Student " + id + " submitted.");
        }

        public void gradeExams() {
            if (submitted != capacity) {
                throw new RuntimeException(
                        "Not all students submitted!");
            }

            arrived = 0;
            submitted = 0;

            System.out.println("Grading complete.");
        }
    }

    static class Student extends Thread {

        private final int id;
        private final ExamState state;

        public Student(int id, ExamState state) {
            this.id = id;
            this.state = state;
        }

        public void execute() throws InterruptedException {

            state.studentArrives(id);
            arrived.release();

            examStarted.acquire();

            state.solveExam(id);

            state.submitExam(id);

            examSubmitted.release();

            sessionFinished.acquire();
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

    static class Professor extends Thread {

        private final ExamState state;

        public Professor(ExamState state) {
            this.state = state;
        }

        public void execute() throws InterruptedException {

            arrived.acquire(30);

            state.startExam();

            examStarted.release(30);

            examSubmitted.acquire(30);

            state.gradeExams();

            sessionFinished.release(30);
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
