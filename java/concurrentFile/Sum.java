import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Sum {

    private static final int PART_SIZE = 1024 * 1024;

    public static int sum(FileInputStream fis) throws IOException {
        
	int byteRead;
        int sum = 0;
        
        while ((byteRead = fis.read()) != -1) {
        	sum += byteRead;
        }

        return sum;
    }

    public static long sum(String path) throws IOException, InterruptedException {
        Path filePath = Paths.get(path);
        if (Files.isRegularFile(filePath)) {
            long fileSize = Files.size(filePath);
            int numParts = (int) Math.ceil((double) fileSize / PART_SIZE);
            List<SumCalculatorThread> threads = new ArrayList<>();

            for (int i = 0; i < numParts; i++) {
                long start = i * PART_SIZE;
                long end = Math.min(start + PART_SIZE, fileSize);
                SumCalculatorThread thread = new SumCalculatorThread(filePath, start, end);
                threads.add(thread);
                thread.start();
            }

            long totalSum = 0;
            for (SumCalculatorThread thread : threads) {
                thread.join();
                totalSum += thread.getSum();
            }

            return totalSum;
        } else {
            throw new RuntimeException("Non-regular file: " + path);
        }
    }

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            System.err.println("Usage: java Sum filepath1 filepath2 filepathN");
            System.exit(1);
        }

	//many exceptions could be thrown here. we don't care
        Thread[] threads = new Thread[args.length];
        Task[] tasks = new Task[args.length];
        for (int i = 0; i < args.length; i++) {
            Task task = new Task(args[i]);
            Thread thread = new Thread(task);
            threads[i] = thread;
            tasks[i] = task;
            thread.start();
        }
        long sum = 0;
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
            sum += tasks[i].getSum();
        }
        System.out.println("total: " + sum);
    }

    public static class Task implements Runnable {

        private String path;
        private long sum;
    
        public Task(String path){
            this.path = path;
            this.sum = 0;
        }
    
        @Override
        public void run() {
            try {
                this.sum = sum(path);
            } catch (Exception e) {
                System.err.println(e);
            }
        }

        public long getSum() {
            return sum;
        }
        
    }

    private static class SumCalculatorThread extends Thread {
        private final Path filePath;
        private final long start;
        private final long end;
        private long sum = 0;

        public SumCalculatorThread(Path filePath, long start, long end) {
            this.filePath = filePath;
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r")) {
                raf.seek(start);
                long remaining = end - start;
                byte[] buffer = new byte[1024];
                int bytesRead;
                while (remaining > 0 && (bytesRead = raf.read(buffer, 0, (int)Math.min(buffer.length, remaining))) != -1) {
                    for (int i = 0; i < bytesRead; i++) {
                        sum += buffer[i];
                    }
                    remaining -= bytesRead;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public long getSum() {
            return sum;
        }
    }

}


